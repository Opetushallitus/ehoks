(ns oph.ehoks.external.organisaatio
  (:require [oph.ehoks.external.cache :as cache]
            [oph.ehoks.external.oph-url :as u]
            [clojure.data.json :as json]
            [clj-http.client :refer [success?]]
            [clojure.tools.logging :as log]))

(defn get-organisaatio [oid]
  (let [resp
        (cache/with-cache!
          {:method :get
           :service (u/get-url "organisaatio-service-url")
           :url (u/get-url "organisaatio-service-url" oid)
           :options {:as :json
                     :throw-exceptions false}})]
    (if (success? resp)
      (:body resp)
      (log/warn "Error getting organization " oid ", " resp))))

(defn find-organisaatiot [oids]
  (:body
    (cache/with-cache!
      {:method :post
       :service (u/get-url "organisaatio-service-url")
       :url (u/get-url "organisaatio-service.find-organisaatiot")
       :options {:as :json
                 :body (json/write-str oids)
                 :content-type :json}})))
