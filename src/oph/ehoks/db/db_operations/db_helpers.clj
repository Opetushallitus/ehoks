(ns oph.ehoks.db.db-operations.db-helpers
  (:require [clojure.set :refer [difference rename-keys]]
            [clojure.java.jdbc :as jdbc]
            [oph.ehoks.config :refer [config]]
            [clojure.data.json :as json]
            [clj-time.coerce :as c])
  (:import (org.postgresql.util PGobject)))

(extend-protocol jdbc/ISQLValue
  java.time.LocalDate
  (sql-value [value] (java.sql.Date/valueOf value))
  java.util.Date
  (sql-value [value] (c/to-sql-time value))
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

(defn shallow-delete!
  "Set deleted_at field to current date and time, marking row as deleted."
  ([table where-clause]
    (let [now (java.util.Date.)]
      (update! table {:deleted_at now
                      :updated_at now} where-clause)))
  ([table where-clause db-conn]
    (update! table {:deleted_at (java.util.Date.)} where-clause db-conn)))

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

(defn map-keys
  "Apply a function to all keys in a map."
  [f m]
  (zipmap (map f (keys m)) (vals m)))

(defn remove-db-columns
  "Remove keys corresponding to columns used for internal purposes, keeping
  columns listed in keep-columns."
  [m keep-columns]
  (let [remove-columns (difference #{:created_at :updated_at :deleted_at}
                                   keep-columns)]
    (apply
      dissoc m remove-columns)))

(defn to-underscore-keys
  "Convert dashes in keys to underscores."
  [m]
  (map-keys #(keyword (.replace (name %) \- \_)) m))

(defn to-dash-keys
  "Convert underscores in keys to dashes."
  [m]
  (map-keys #(keyword (.replace (name %) \_ \-)) m))

(defn replace-in
  "Associate the value associated with sk with the new key or sequence of nested
  keys tks in h, and then dissociate sk."
  [h sk tks]
  (if (some? (get h sk))
    (dissoc (assoc-in h tks (get h sk)) sk)
    h))

(defn replace-from
  "Functions similarly to replace-in, but can accept a sequence of nested keys
  as the source and expects a keyword as the destination."
  [h sks tk]
  (cond
    (get-in h sks)
    (if (= (count (get-in h (drop-last sks))) 1)
      (apply
        dissoc
        (assoc h tk (get-in h sks))
        (drop-last sks))
      (update-in
        (assoc h tk (get-in h sks))
        (drop-last sks)
        dissoc
        (last sks)))
    (empty? (get-in h (drop-last sks)))
    (apply dissoc h (drop-last sks))
    :else h))

(defn replace-with-in
  "Handles replacing one (possibly nested) key with another in a map."
  [m kss kst]
  (if (coll? kss)
    (replace-from m kss kst)
    (replace-in m kss kst)))

(defn remove-nils
  "Remove all keys mapped to value nil."
  [m]
  (apply dissoc m (filter #(nil? (get m %)) (keys m))))

(defn convert-sql
  "Handle removals and replacements in maps."
  [m {removals :removals replaces :replaces
      :or {removals [] replaces {}}, :as operations}]
  (as-> m x
    (reduce
      (fn [c [kss kst]]
        (replace-with-in c kss kst))
      x
      replaces)
    (apply dissoc x removals)))

(defn from-sql
  "Convert maps returned by database functions to format expected elsewhere."
  ([m operations keep-columns]
    (-> (convert-sql m operations)
        remove-nils
        (remove-db-columns keep-columns)
        to-dash-keys))
  ([m operations] (from-sql m operations nil))
  ([m] (from-sql m {} nil)))

(defn to-sql
  "Convert maps used elsewhere to those expected by database functions."
  ([m operations]
    (to-underscore-keys (convert-sql m operations)))
  ([m] (to-sql m {})))
