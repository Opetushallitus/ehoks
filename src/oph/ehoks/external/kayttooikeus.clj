(ns oph.ehoks.external.kayttooikeus
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.cache :as cache]
            [oph.ehoks.external.cas :as cas]))

(defn get-palvelukayttajat []
  (cache/with-cache!
    {:method :get
     :authenticate? true
     :service (:kayttooikeus-service-url config)
     :path "palvelukayttaja"
     :options {:as :json}}))

(defn get-user-details [^String username]
  (first
    (:body
     (cache/with-cache!
       {:method :get
        :authenticate? true
        :service (:kayttooikeus-service-url config)
        :path "kayttooikeus/kayttaja"
        :options {:as :json
                  :query-params {"username" username}}}))))

(defn get-ticket-user [ticket]
  (let [validation-data (cas/validate-ticket (:backend-url config) ticket)]
    (when (:success? validation-data)
      (get-user-details (:user validation-data)))))
