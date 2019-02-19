(ns oph.ehoks.external.kayttooikeus
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.cache :as cache]))

(defn get-palvelukayttajat []
  (cache/with-cache!
    {:method :get
     :authenticate? true
     :service (:kayttoikeus-service-url config)
     :path "palvelukayttaja"
     :options {:as :json}}))

(defn get-user-details [^String username]
  (when (some? username)
    (some
      #(when (= (:kayttajatunnus %) username) %)
      (:body (get-palvelukayttajat)))))
