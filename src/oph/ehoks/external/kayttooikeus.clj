(ns oph.ehoks.external.kayttooikeus
  (:require [oph.ehoks.external.cache :as cache]
            [oph.ehoks.external.cas :as cas]
            [oph.ehoks.external.oph-url :as u]))

(defn get-user-details [^String username]
  (first
    (:body
      (cache/with-cache!
        {:method :get
         :authenticate? true
         :service (u/get-url "kayttooikeus-service-url")
         :url (u/get-url "kayttooikeus-service.kayttaja")
         :options {:as :json
                   :query-params {"username" username}}}))))

(defn get-service-ticket-user [ticket service]
  (let [validation-data (cas/validate-ticket service ticket)]
    (when (:success? validation-data)
      (get-user-details (:user validation-data)))))

(defn get-ticket-user [ticket]
  (get-service-ticket-user ticket (u/get-url "ehoks-virkailija-backend-url")))
