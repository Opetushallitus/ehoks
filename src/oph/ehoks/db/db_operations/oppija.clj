(ns oph.ehoks.db.db-operations.oppija
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(defn select-oppija-by-oid [oppija-oid]
  (first
    (db-ops/query
      [queries/select-oppijat-by-oid oppija-oid]
      {:row-fn db-ops/from-sql})))

(defn update-oppija! [oid oppija]
  (db-ops/update!
    :oppijat
    (db-ops/to-sql oppija)
    ["oid = ?" oid]))

(defn insert-oppija [oppija]
  (db-ops/insert-one! :oppijat (db-ops/to-sql oppija)))
