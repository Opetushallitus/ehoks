(ns oph.ehoks.db.db-operations.oppija
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(defn- share-from-sql [v]
  (db-ops/from-sql v {:removals [:id]}))

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

(defn insert-oppija! [oppija]
  (db-ops/insert-one! :oppijat (db-ops/to-sql oppija)))

(defn insert-shared-module! [values]
  (let [vals
        (assoc values
               :to-module-uuid
               (java.util.UUID/fromString (:to-module-uuid values))
               :shared-module-uuid
               (java.util.UUID/fromString (:shared-module-uuid values)))]
    (db-ops/insert-one! :shared_modules (db-ops/to-sql vals))))

(defn select-shared-module [uuid]
  (let [share-id (java.util.UUID/fromString uuid)]
    (db-ops/query
      [queries/select-shared-module-by-uuid share-id]
      {:row-fn share-from-sql})))

(defn select-shared-module-links [uuid]
  (let [module-id (java.util.UUID/fromString uuid)]
    (db-ops/query
      [queries/select-shared-module-links-by-module-uuid module-id]
      {:row-fn share-from-sql})))

(defn delete-shared-module! [uuid]
  (db-ops/delete!
    :shared_modules
    ["uuid = ?" (java.util.UUID/fromString uuid)]))
