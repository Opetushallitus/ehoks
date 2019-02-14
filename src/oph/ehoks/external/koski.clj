(ns oph.ehoks.external.koski
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.connection :as c]))

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
  (c/with-api-headers
    {:method :get
     :service (:koski-url config)
     :path (format "api/opiskeluoikeus/%s" oid)
     :options {:basic-auth [(:cas-username config) (:cas-password config)]
               :as :json}}))

(defn get-opiskeluoikeus-oppilaitos-oid [opiskeluoikeus-oid]
  (get-in
    (get-opiskeluoikeus-info opiskeluoikeus-oid)
    [:body :oppilaitos :oid]))
