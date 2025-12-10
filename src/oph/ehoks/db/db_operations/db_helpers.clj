(ns oph.ehoks.db.db-operations.db-helpers
  (:require [clojure.data.json :as json]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :refer [difference]]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.config :refer [config]])
  (:import (org.postgresql.util PGobject)))

(extend-protocol jdbc/ISQLValue
  java.time.LocalDate
  (sql-value [value] (java.sql.Date/valueOf value))
  java.util.Date
  (sql-value [value] (java.sql.Timestamp. (.getTime value)))
  java.time.Instant
  (sql-value [value] (java.sql.Timestamp/from value))
  clojure.lang.IPersistentMap
  (sql-value [value]
    (doto (PGobject.)
      (.setType "json")
      (.setValue (json/write-str value)))))

(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Date
  (result-set-read-column [o _ _]
    (.toLocalDate o))
  PGobject
  (result-set-read-column [pgobj _ _]
    (let [type (.getType pgobj)
          value (.getValue pgobj)]
      (if (or (= type "json") (= type "jsonb"))
        (json/read-str value :key-fn keyword)
        value))))

(defn get-db-connection
  "Get PostgreSQL DB connection settings from config values"
  []
  {:dbtype (:db-type config)
   :dbname (:db-name config)
   :host (:db-server config)
   :port (:db-port config)
   :user (:db-username config)
   :password (:db-password config)})

(defn- insert-empty!
  "Insert an empty row into the given table."
  [t]
  (jdbc/execute!
    (get-db-connection)
    (format
      "INSERT INTO %s DEFAULT VALUES" (name t))))

(defn- insert!
  "Handle database insertion."
  ([t v]
    (if (seq v)
      (jdbc/insert! (get-db-connection) t v)
      (insert-empty! t)))
  ([t v db-conn]
    (if (seq v)
      (jdbc/insert! db-conn t v)
      (insert-empty! t))))

(defn insert-one!
  "Insert one row into the database."
  ([t v]
    (first (insert! t v)))
  ([t v db-conn]
    (first (insert! t v db-conn))))

(defn insert-multi!
  "Insert multiple rows into the database."
  ([t v]
    (jdbc/insert-multi! (get-db-connection) t v))
  ([t v db-conn]
    (jdbc/insert-multi! db-conn t v)))

(defn update!
  "Update a row or rows in database."
  ([table values where-clause]
    (jdbc/update! (get-db-connection) table values where-clause))
  ([table values where-clause db-conn]
    (jdbc/update! db-conn table values where-clause)))

(defn soft-delete!
  "Set deleted_at field to given/current date and time, marking row as deleted.
  Possibly triggers a cascading effect on related database rows
  (check cascading_soft_delete database migration)."
  ([table where-clause db-conn]
    (update! table {:deleted_at (java.util.Date.)} where-clause db-conn))
  ([table where-clause]
    (soft-delete! table where-clause (get-db-connection))))

(defn soft-delete-marking-updated!
  "Set deleted_at & updated_at field to given date and time, marking row as
  deleted."
  [table where-clause db-conn timestamp]
  (update! table {:deleted_at timestamp
                  :updated_at timestamp} where-clause db-conn))

(defn delete!
  "Actually delete row from database."
  ([table where-clause]
    (jdbc/delete! (get-db-connection) table where-clause))
  ([table where-clause db-conn]
    (jdbc/delete! db-conn table where-clause)))

(defn query
  "Execute DB query."
  ([queries opts]
    (jdbc/query (get-db-connection) queries opts))
  ([queries]
    (query queries {}))
  ([queries arg & opts]
    (query queries (apply hash-map arg opts))))

(defn remove-db-columns
  "Remove keys corresponding to columns used for internal purposes, keeping
  columns listed in keep-columns."
  [m keep-columns]
  (let [remove-columns (difference #{:created_at
                                     :updated_at
                                     :deleted_at
                                     :palaute_handled_at}
                                   keep-columns)]
    (apply
      dissoc m remove-columns)))

(defn convert-sql
  "Handle removals and replacements in maps."
  [m {removals :removals replaces :replaces
      :or {removals [] replaces {}}, :as operations}]
  (as-> m x
    (reduce
      (fn [c [kss kst]]
        (utils/replace-in c kss kst))
      x
      replaces)
    (apply dissoc x removals)))

(defn from-sql
  "Convert maps returned by database functions to format expected elsewhere."
  ([m operations keep-columns]
    (-> (convert-sql m operations)
        utils/remove-nils
        (remove-db-columns keep-columns)
        utils/to-dash-keys))
  ([m operations] (from-sql m operations nil))
  ([m] (from-sql m {} nil)))

(defn to-sql
  "Convert maps used elsewhere to those expected by database functions."
  ([m operations]
    (utils/to-underscore-keys (convert-sql m operations)))
  ([m] (to-sql m {})))
