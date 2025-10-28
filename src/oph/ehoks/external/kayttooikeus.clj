(ns oph.ehoks.external.kayttooikeus
  (:require [oph.ehoks.external.cas :as cas]
            [oph.ehoks.external.oph-url :as u]
            [oph.ehoks.user :as user]
            [clojure.tools.logging :as log]))

(defn- filter-non-ehoks-privileges
  "Filter out all non-EHOKS privileges"
  [org]
  (update org :kayttooikeudet
          (fn [privileges]
            (filter
              #(= (:palvelu %) "EHOKS")
              privileges))))

(defn- remove-orgs-without-privileges
  "Remove organisations whose list of privileges is empty"
  [user]
  (update user :organisaatiot
          (fn [orgs]
            (filter
              #(not-empty (:kayttooikeudet %))
              (map filter-non-ehoks-privileges orgs)))))

(defn username->user-details!
  "Get user details of given username"
  [^String username]
  (-> {:method :get
       :authenticate? true
       :service (u/get-url "kayttooikeus-service-url")
       :url (u/get-url "kayttooikeus-service.kayttaja")
       :options {:as :json, :query-params {"username" username}}}
      (cas/with-service-ticket)
      (get-in [:body 0])
      (remove-orgs-without-privileges)))

(defn service-ticket->user-details!
  "Get username of CAS ticket at given service"
  ([ticket] (service-ticket->user-details!
              (u/get-url "ehoks-virkailija-backend-url")
              ticket))
  ([service ticket]
    (let [validation-data (cas/validate-ticket service ticket)]
      (if (:success? validation-data)
        (let [user-details (username->user-details! (:user validation-data))]
          (merge user-details (user/get-auth-info user-details)))
        (log/warnf "Service ticket validation failed: %s" validation-data)))))
