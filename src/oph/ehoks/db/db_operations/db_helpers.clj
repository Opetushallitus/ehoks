(ns oph.ehoks.db.db-operations.db-helpers
  (:require [clojure.set :refer [rename-keys]]
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

(defn- insert-empty! [t]
  (jdbc/execute!
    (get-db-connection)
    (format
      "INSERT INTO %s DEFAULT VALUES" (name t))))

(defn- insert!
  ([t v]
    (if (seq v)
      (jdbc/insert! (get-db-connection) t v)
      (insert-empty! t)))
  ([t v db-conn]
    (if (seq v)
      (jdbc/insert! db-conn t v)
      (insert-empty! t))))

(defn insert-one!
  ([t v]
    (first (insert! t v)))
  ([t v db-conn]
    (first (insert! t v db-conn))))

(defn insert-multi!
  ([t v]
    (jdbc/insert-multi! (get-db-connection) t v))
  ([t v db-conn]
    (jdbc/insert-multi! db-conn t v)))

(defn update!
  ([table values where-clause]
    (jdbc/update! (get-db-connection) table values where-clause))
  ([table values where-clause db-conn]
    (jdbc/update! db-conn table values where-clause)))

(defn shallow-delete!
  ([table where-clause]
    (update! table {:deleted_at (java.util.Date.)} where-clause))
  ([table where-clause db-conn]
    (update! table {:deleted_at (java.util.Date.)} where-clause db-conn)))

(defn delete!
  ([table where-clause]
    (jdbc/delete! (get-db-connection) table where-clause))
  ([table where-clause db-conn]
    (jdbc/delete! db-conn table where-clause)))

(defn query
  ([queries opts]
    (jdbc/query (get-db-connection) queries opts))
  ([queries]
    (query queries {}))
  ([queries arg & opts]
    (query queries (apply hash-map arg opts))))

(defn convert-keys [f m]
  (rename-keys
    m
    (reduce
      (fn [c n]
        (assoc c n (f n)))
      {}
      (keys m))))

(defn remove-db-columns [m & others]
  (apply
    dissoc m
    :created_at
    :updated_at
    :deleted_at
    others))

(defn to-underscore-keys [m]
  (convert-keys #(keyword (.replace (name %) \- \_)) m))

(defn to-dash-keys [m]
  (convert-keys #(keyword (.replace (name %) \_ \-)) m))

(defn replace-in [h sk tks]
  (if (some? (get h sk))
    (dissoc (assoc-in h tks (get h sk)) sk)
    h))

(defn replace-from [h sks tk]
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

(defn replace-with-in [m kss kst]
  (if (coll? kss)
    (replace-from m kss kst)
    (replace-in m kss kst)))

(defn remove-nils [m]
  (apply dissoc m (filter #(nil? (get m %)) (keys m))))

(defn convert-sql
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
  ([m operations]
    (-> (convert-sql m operations)
        remove-nils
        remove-db-columns
        to-dash-keys))
  ([m] (from-sql m {})))

(defn to-sql
  ([m operations]
    (to-underscore-keys (convert-sql m operations)))
  ([m] (to-sql m {})))
