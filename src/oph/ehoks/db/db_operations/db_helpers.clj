(ns oph.ehoks.db.db-operations.db-helpers
  (:require [oph.ehoks.config :refer [config]]))

(defn get-db-connection []
  {:dbtype (:db-type config)
   :dbname (:db-name config)
   :host (:db-server config)
   :port (:db-port config)
   :user (:db-username config)
   :password (:db-password config)})
