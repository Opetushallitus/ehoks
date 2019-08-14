(ns oph.ehoks.db.db-operations.db-helpers
  (:require [clojure.java.jdbc :as jdbc]
            [oph.ehoks.config :refer [config]]))

(defn get-db-connection []
  {:dbtype (:db-type config)
   :dbname (:db-name config)
   :host (:db-server config)
   :port (:db-port config)
   :user (:db-username config)
   :password (:db-password config)})

(defn insert-empty! [t]
  (jdbc/execute!
    (get-db-connection)
    (format
      "INSERT INTO %s DEFAULT VALUES" (name t))))

(defn insert! [t v]
  (if (seq v)
    (jdbc/insert! (get-db-connection) t v)
    (insert-empty! t)))
