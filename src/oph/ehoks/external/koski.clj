(ns oph.ehoks.external.koski
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.connection :as c]
            [ring.util.http-status :as status]
            [clojure.data.json :as json]))

(defn filter-oppija [values]
  (update values :henkilö select-keys
          [:oid :hetu :syntymäaika :etunimet :kutsumanimi :sukunimi]))

(defn get-student-info [oid]
  (c/with-api-headers
    {:method :get
     :service (:koski-url config)
     :path (format "api/oppija/%s" oid)
     :options {:basic-auth [(:cas-username config) (:cas-password config)]
               :as :json}}))

(defn get-opiskeluoikeus-info [oid]
  (try
    (:body
     (c/with-api-headers
       {:method :get
        :service (:koski-url config)
        :path (format "api/opiskeluoikeus/%s" oid)
        :options {:basic-auth [(:cas-username config) (:cas-password config)]
                  :as :json}}))
    (catch clojure.lang.ExceptionInfo e
      (let [e-data (ex-data e)
            body (if (some? (:body e-data))
                   (json/read-str (:body e-data) :key-fn keyword)
                   {})]
        (when-not (and (= (:status e-data) status/not-found)
                       (= (get-in body [0 :key])
                          "notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia"))
          (throw e))))))

(defn get-opiskeluoikeus-oppilaitos-oid [opiskeluoikeus-oid]
  (get-in
    (get-opiskeluoikeus-info opiskeluoikeus-oid)
    [:oppilaitos :oid]))
