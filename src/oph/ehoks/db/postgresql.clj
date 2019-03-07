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

(defn select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id [id]
  (jdbc/query
    {:connection-uri (:database-url config)}
    [queries/select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/olemassa-oleva-ammatillinen-tutkinnon-osa-from-sql}))

(defn insert-olemassa-oleva-ammatillinen-tutkinnon-osa! [m]
  (jdbc/insert!
    {:connection-uri (:database-url config)}
    :hoksit
    (h/olemassa-oleva-ammatillinen-tutkinnon-osa-to-sql m)))
