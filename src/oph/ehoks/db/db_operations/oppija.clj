(ns oph.ehoks.db.db-operations.oppija
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(defn select-oppija-by-oid
  "Hakee tietokannasta oppijan OID:n perusteella."
  [oppija-oid]
  (first
    (db-ops/query
      [queries/select-oppijat-by-oid oppija-oid]
      {:row-fn db-ops/from-sql})))

(defn select-oppija-with-opiskeluoikeus-oid-by-oid
  "Hakee yksittäisen oppijan oppija-oidin perusteella.
  Palauttaa mukana opiskeluoikeus-oidin."
  [oppija-oid]
  (first
    (db-ops/query
      [queries/select-oppija-with-opiskeluoikeus-oid-by-oid oppija-oid]
      {:row-fn db-ops/from-sql})))

(defn update-oppija!
  "Päivittää tietokannassa olevan oppijan."
  [oid oppija]
  (db-ops/update!
    :oppijat
    (db-ops/to-sql oppija)
    ["oid = ?" oid]))

(defn insert-oppija!
  "Lisää oppijan tietokantaan."
  [oppija]
  (db-ops/insert-one! :oppijat (db-ops/to-sql oppija)))
