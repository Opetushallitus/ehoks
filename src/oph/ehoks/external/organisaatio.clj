(ns oph.ehoks.external.organisaatio
  (:require [clojure.data.json :as json]
            [oph.ehoks.external.cache :as cache]
            [oph.ehoks.external.oph-url :as u]
            [ring.util.http-response :as response]
            [ring.util.http-status :as status])
  (:import [clojure.lang ExceptionInfo]))

(defn get-organisation!
  "Get info about organisation with `oid` from Organisaatiopalvelu. Returns
  `nil` if organisation is not found from Organisaatiopalvelu. Throws an
  exception in case of excetional status codes."
  [oid]
  (try
    (:body (cache/with-cache!
             {:method :get
              :service (u/get-url "organisaatio-service-url")
              :url (u/get-url "organisaatio-service.get-organisaatio" oid)
              :options {:as :json}}))
    (catch ExceptionInfo e
      (when-not (= (:status (ex-data e)) status/not-found)
        (throw (ex-info (str "Error while fetching organisation from "
                             "Organisaatiopalvelu")
                        {:type             ::organisation-fetching-error
                         :organisation-oid oid}
                        e))))))
;
(defn get-existing-organisation!
  "Like `get-organisaatio!` but expects an organisation with `oid` to be found
  from Organisaatiopalvelu, and thus, throws an exception if no organisation
  is found."
  [oid]
  (if-let [organisaatio (get-organisation! oid)]
    organisaatio
    (throw (ex-info
             (format "Organisation `%s` not found from Organisaatiopalvelu" oid)
             {:type             ::organisation-not-found
              :organisation-oid oid}))))

(defn not-found-handler
  [^ExceptionInfo e data _]
  {:pre [(= (:type data) ::organisation-not-found)]}
  (response/bad-request (ex-message e)))

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
