(ns oph.ehoks.external.koski
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.connection :as c]
            [clojure.set :refer [rename-keys]]))

(defn filter-oppija [values]
  (update values :henkilö select-keys
          [:oid :hetu :syntymäaika :etunimet :kutsumanimi :sukunimi]))

(defn get-student-info [oid]
  (c/with-api-headers
    :get
    (format "%s/api/oppija/%s" (:koski-url config) oid)
    {:basic-auth [(:cas-username config) (:cas-password config)]
     :as :json}))
