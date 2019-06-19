(ns oph.ehoks.external.organisaatio
  (:require [oph.ehoks.external.cache :as cache]
            [oph.ehoks.external.oph-url :as u]))

(defn get-organisaatio [oid]
  (:body
    (cache/with-cache!
      {:method :get
       :service (u/get-url "organisaatio-service-url")
       :url (u/get-url "organisaatio-service-url" oid)
       :options {:as :json}})))
