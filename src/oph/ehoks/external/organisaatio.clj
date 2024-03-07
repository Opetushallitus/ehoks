(ns oph.ehoks.external.organisaatio
  (:require [oph.ehoks.external.cache :as cache]
            [oph.ehoks.external.oph-url :as u]
            [clojure.data.json :as json]
            [clj-http.client :refer [success?]]
            [clojure.tools.logging :as log]))

(defn get-organisaatio!
  "Get organisaatio info; log at WARN level on failure"
  [oid]
  (let [resp
        (cache/with-cache!
          {:method :get
           :service (u/get-url "organisaatio-service-url")
           :url (u/get-url "organisaatio-service.get-organisaatio" oid)
           :options {:as :json
                     :throw-exceptions false}})]
    (if (success? resp)
      (:body resp)
      (log/warn "Error getting organization " oid ", " resp))))

(defn get-organisaatio-info
  "Get organisaatio info without handling exceptions"
  [oid]
  (get
    (cache/with-cache!
      {:method :get
       :service (u/get-url "organisaatio-service-url")
       :url (u/get-url "organisaatio-service.get-organisaatio" oid)
       :options {:as :json}})
    :body))

(defn try-to-get-organisaatiot-from-cache!
  "Find organisaatiot by OIDs (with caching)"
  [oids]
  (cache/with-cache!
    {:method :post
     :service (u/get-url "organisaatio-service-url")
     :url (u/get-url "organisaatio-service.find-organisaatiot")
     :options {:as :json
               :body (json/write-str oids)
               :query-params {:oids oids}
               :content-type :json}}))

(defn find-organisaatiot
  "Find organisaatiot"
  [oids]
  (:body (try-to-get-organisaatiot-from-cache! oids)))
