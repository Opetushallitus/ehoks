(ns oph.ehoks.external.organisaatio
  (:require [oph.ehoks.external.cache :as cache]
            [oph.ehoks.external.oph-url :as u]
            [clojure.data.json :as json]
            [clj-http.client :refer [success?]]
            [clojure.tools.logging :as log]
            [oph.ehoks.external.connection :as c]
            [oph.ehoks.external.cas :as cas]))

(defn get-organisaatio [oid]
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

;(defn try-to-get-organisaatiot-from-cache! [oids]
;  (cache/with-cache!
;    {:method :post
;     :service (u/get-url "organisaatio-service-url")
;     :url (u/get-url "organisaatio-service.find-organisaatiot")
;     :options {:as :json
;               :body (json/write-str oids)
;               :query-params {:oids oids}
;               :content-type :json}}))

(defn try-to-get-organisaatiot-from-cache! [oids]
  (let [service (u/get-url "organisaatio-service-url")
        url (u/get-url "organisaatio-service.find-organisaatiot")
        req-options {:as :json
                     :body (json/write-str oids)
                     :content-type :json}
        req {:method :post
             :service service
             :url url
             :options req-options}]
    (let [response (c/with-api-headers req)]
      (assoc
        response
        :body
        (reduce (fn [body obj]
                  (into
                    body
                    [(select-keys obj [:oid :nimi])]))
                ()
                (:body response))))))

(defn find-organisaatiot [oids]
  (:body (try-to-get-organisaatiot-from-cache! oids)))
