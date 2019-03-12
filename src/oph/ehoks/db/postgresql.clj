(ns oph.ehoks.db.postgresql
  (:require [clojure.java.jdbc :as jdbc]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.db.hoks :as h]
            [clj-time.coerce :as c]
            [oph.ehoks.db.queries :as queries]))

(extend-protocol jdbc/ISQLValue
  java.util.Date
  (sql-value [value] (c/to-sql-date value)))

(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Date
  (result-set-read-column [o _ _]
    (.toLocalDate o)))

(defn select-hoks-by-oppija-oid [oid]
  (jdbc/query
    {:connection-uri (:database-url config)}
    [queries/select-hoks-by-oppija-oid oid]
    {:row-fn h/hoks-from-sql}))

(defn select-hoks-by-id [id]
  (jdbc/query
    {:connection-uri (:database-url config)}
    [queries/select-hoks-by-id id]
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

(defn select-puuttuvat-paikalliset-tutkinnon-osat-by-hoks-id [id]
  (jdbc/query
    {:connection-uri (:database-url config)}
    [queries/select-puuttuvat-paikalliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/puuttuva-paikallinen-tutkinnon-osa-from-sql}))

(defn insert-puuttuvat-paikalliset-tutkinnon-osat! [c]
  (jdbc/insert-multi!
    {:connection-uri (:database-url config)}
    :puuttuvat_paikalliset_tutkinnon_osat
    (map h/puuttuva-paikallinen-tutkinnon-osa-to-sql c)))

(defn select-hankitun-osaamisen-naytot-by-ppto-id
  "Puuttuvan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [id]
  (jdbc/query
    {:connection-uri (:database-url config)}
    [queries/select-hankitun-osaamisen-naytot-by-ppto-id id]
    {:row-fn h/hankitun-osaamisen-naytto-from-sql}))

(defn insert-ppto-hankitun-osaamisen-naytot!
  "Puuttuvan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [ppto c]
  (let [h-col (jdbc/insert-multi!
                {:connection-uri (:database-url config)}
                :hankitun_osaamisen_naytot
                (map h/hankitun-osaamisen-naytto-to-sql c))]
    (jdbc/insert-multi!
      {:connection-uri (:database-url config)}
      :puuttuvan_paikallisen_tutkinnon_osan_hankitun_osaamisen_naytto
      (map #(hash-map
              :puuttuva_paikallinen_tutkinnon_osa_id (:id ppto)
              :hankitun_osaamisen_naytto_id (:id %))
           h-col))
    h-col))

(defn insert-nayttoymparisto! [m]
  (jdbc/insert!
    {:connection-uri (:database-url config)}
    :nayttoymparistot
    (h/nayttoymparisto-to-sql m)))

(defn select-olemassa-olevat-paikalliset-tutkinnon-osat-by-hoks-id [id]
  (jdbc/query
    {:connection-uri (:database-url config)}
    [queries/select-olemassa-olevat-paikalliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/olemassa-oleva-paikallinen-tutkinnon-osa-from-sql}))

(defn select-olemassa-olevat-yhteiset-tutkinnon-osat-by-hoks-id [id]
  (jdbc/query
    {:connection-uri (:database-url config)}
    [queries/select-olemassa-olevat-yhteiset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/olemassa-oleva-yhteinen-tutkinnon-osa-from-sql}))
