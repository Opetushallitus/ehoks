(ns oph.ehoks.db.db-operations.opiskeluoikeus
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(defn select-opiskeluoikeudet-without-tutkinto []
  (db-ops/query
    [queries/select-hoks-opiskeluoikeudet-without-tutkinto]))

(defn select-opiskeluoikeudet-without-tutkinto-count []
  (db-ops/query
    [queries/select-hoks-opiskeluoikeudet-without-tutkinto-count]))

(defn select-opiskeluoikeudet-by-oppija-oid [oppija-oid]
  (db-ops/query
    [queries/select-opiskeluoikeudet-by-oppija-oid oppija-oid]
    {:row-fn db-ops/from-sql}))

(defn select-opiskeluoikeus-by-oid [oid]
  (first
    (db-ops/query
      [queries/select-opiskeluoikeudet-by-oid oid]
      {:row-fn db-ops/from-sql})))

(defn insert-opiskeluoikeus [opiskeluoikeus]
  (db-ops/insert-one! :opiskeluoikeudet (db-ops/to-sql opiskeluoikeus)))

(defn update-opiskeluoikeus! [oid opiskeluoikeus]
  (db-ops/update!
    :opiskeluoikeudet
    (db-ops/to-sql opiskeluoikeus)
    ["oid = ?" oid]))
