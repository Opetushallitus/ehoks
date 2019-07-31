(ns oph.ehoks.external.kayttooikeus
  (:require [oph.ehoks.external.cache :as cache]
            [oph.ehoks.external.cas :as cas]
            [oph.ehoks.external.oph-url :as u]
            [clojure.tools.logging :as log]))

(defn- filter-non-ehoks-privileges [org]
  (update org :kayttooikeudet
          (fn [privileges]
            (filter
              #(= (:palvelu %) "EHOKS")
              privileges))))

(defn- remove-orgs-without-privileges [user]
  (update user :organisaatiot
          (fn [orgs]
            (filter
              #(not-empty (:kayttooikeudet %))
              (map filter-non-ehoks-privileges orgs)))))

(defn get-user-details [^String username]
  (remove-orgs-without-privileges
    (first
      (:body
        (cache/with-cache!
          {:method :get
           :authenticate? true
           :service (u/get-url "kayttooikeus-service-url")
           :url (u/get-url "kayttooikeus-service.kayttaja")
           :options {:as :json
                     :query-params {"username" username}}})))))

(defn get-service-ticket-user [ticket service]
  (let [validation-data (cas/validate-ticket service ticket)]
    (if (:success? validation-data)
      (get-user-details (:user validation-data))
      (do
        (log/warnf "Service ticket validation failed: %s" validation-data)
        nil))))

(defn get-ticket-user [ticket]
  (get-service-ticket-user ticket (u/get-url "ehoks-virkailija-backend-url")))
