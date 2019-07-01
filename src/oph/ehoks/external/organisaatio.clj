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
           :url (u/get-url "organisaatio-service.get-organisaatio" oid)
           :options {:as :json
                     :query-params {:oid oid}
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
                 :query-params {:oids oids}
                 :content-type :json}})))
