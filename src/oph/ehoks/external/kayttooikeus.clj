(ns oph.ehoks.external.kayttooikeus
  (:require [oph.ehoks.external.cas :as cas]
            [oph.ehoks.external.oph-url :as u]
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

(defn- fetch-user-privileges
  "Get user privileges from CAS endpoint"
  [{:as data}]
  (cas/with-service-ticket data))

(defn get-user-details
  "Get user details of given username"
  [^String username]
  (remove-orgs-without-privileges
    (first
      (:body
        (fetch-user-privileges
          {:method :get
           :authenticate? true
           :service (u/get-url "kayttooikeus-service-url")
           :url (u/get-url "kayttooikeus-service.kayttaja")
           :options {:as :json
                     :query-params {"username" username}}})))))

(defn get-service-ticket-user
  "Get username of CAS ticket at given service"
  [ticket service]
  (let [validation-data (cas/validate-ticket service ticket)]
    (if (:success? validation-data)
      (get-user-details (:user validation-data))
      (do
        (log/warnf "Service ticket validation failed: %s" validation-data)
        nil))))

(defn get-ticket-user
  "Get user of CAS ticket in eHOKS service"
  [ticket]
  (get-service-ticket-user ticket (u/get-url "ehoks-virkailija-backend-url")))
