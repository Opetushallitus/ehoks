(ns oph.ehoks.external.organisaatio
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [oph.ehoks.external.cache :as cache]
            [oph.ehoks.external.oph-url :as u]
            [ring.util.http-status :as status])
  (:import [clojure.lang ExceptionInfo]))

(defn get-organisaatio!
  "Get info about organisation with `oid` from Organisaatiopalvelu. Returns
  `nil` if organisation is not found from Organisaatiopalvelu. Throws an
  exception in case of excetional status codes."
  [oid]
  (try
    (->> {:method :get
          :service (u/get-url "organisaatio-service-url")
          :url (u/get-url "organisaatio-service.get-organisaatio" oid)
          :options {:as :json}}
         (cache/with-cache!)
         :body
         (when oid))
    (catch ExceptionInfo e
      (log/warn e "Error while fetching" oid "from organisaatiopalvelu")
      (when-not (= (:status (ex-data e)) status/not-found)
        (throw (ex-info (format (str "Error while fetching organisation `%s` "
                                     "from Organisaatiopalvelu.")
                                oid)
                        {:type             ::organisation-fetching-error
                         :organisation-oid oid}
                        e))))))

(defn get-existing-organisaatio!
  "Like `get-organisaatio!` but expects an organisation with `oid` to be found
  from Organisaatiopalvelu, and thus, throws an exception if no organisation
  is found."
  [oid]
  (or
    (get-organisaatio! oid)
    (throw (ex-info
             (format "Organisation `%s` not found from Organisaatiopalvelu" oid)
             {:type             ::organisation-not-found
              :organisation-oid oid}))))

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
