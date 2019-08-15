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
      (if (= type "json")
        (json/read-str value :key-fn keyword)
        value))))

(defn get-db-connection []
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

(defn- insert! [t v]
  (if (seq v)
    (jdbc/insert! (get-db-connection) t v)
    (insert-empty! t)))

(defn insert-one! [t v] (first (insert! t v)))

(defn insert-multi! [t v]
  (jdbc/insert-multi! (get-db-connection) t v))

(defn update!
  ([table values where-clause]
    (jdbc/update! (get-db-connection) table values where-clause))
  ([table values where-clause db]
    (jdbc/update! db table values where-clause)))

(defn shallow-delete!
  ([table where-clause]
    (update! table {:deleted_at (java.util.Date.)} where-clause))
  ([table where-clause db-conn]
    (update! table {:deleted_at (java.util.Date.)} where-clause db-conn)))

(defn delete!
  [table where-clause]
  (jdbc/delete! (get-db-connection) table where-clause))

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
