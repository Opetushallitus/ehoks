(ns oph.ehoks.external.organisaatio
  (:require [oph.ehoks.external.cache :as cache]
            [oph.ehoks.external.oph-url :as u]
            [clojure.data.json :as json]))

(defn get-organisaatio [oid]
  (:body
    (cache/with-cache!
      {:method :get
       :service (u/get-url "organisaatio-service-url")
       :url (u/get-url "organisaatio-service.get-organisaatio" oid)
       :options {:as :json}})))

(defn find-organisaatiot [oids]
  (:body
    (cache/with-cache!
      {:method :post
       :service (u/get-url "organisaatio-service-url")
       :url (u/get-url "organisaatio-service.find-organisaatiot")
       :options {:as :json
                 :body (json/write-str oids)
                 :content-type :json}})))