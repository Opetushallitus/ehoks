(ns oph.ehoks.db.postgresql
  (:require [clojure.java.jdbc :as jdbc]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.db.hoks :as h]
            [clj-time.coerce :as c]
            [oph.ehoks.db.queries :as queries]))

(extend-protocol jdbc/ISQLValue
  java.util.Date
  (sql-value [value] (c/to-sql-date value)))

(defn clear! []
  (jdbc/delete! {:connection-uri (:database-url config)} :hoksit []))

(defn select-hoks-by-oppija-oid [oid]
  (jdbc/query
    {:connection-uri (:database-url config)}
    [queries/select-hoks-by-oppija-oid oid]
    {:row-fn h/hoks-from-sql}))

(defn insert-hoks! [hoks]
  (jdbc/insert!
    {:connection-uri (:database-url config)}
    :hoksit
    (h/hoks-to-sql hoks)))
