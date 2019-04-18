(ns oph.ehoks.external.kayttooikeus
  (:require [oph.ehoks.external.cache :as cache]
            [oph.ehoks.external.cas :as cas]
            [oph.ehoks.external.oph-url :as u]
            [oph.ehoks.config :refer [config]]))

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

(defn get-ticket-user [ticket]
  (let [validation-data (cas/validate-ticket (:backend-url config) ticket)]
    (when (:success? validation-data)
      (get-user-details (:user validation-data)))))
