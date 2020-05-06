(ns oph.ehoks.db.db-operations.oppija
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops])
  (:import [java.time LocalDate]
           [java.util UUID]))

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

(defn- validate-share-dates [values]
  (cond
    (.isBefore (:voimassaolo-loppu values) (LocalDate/now))
    (throw
      (Exception. "Shared link end date cannot be in the past"))
    (.isBefore (:voimassaolo-loppu values) (:voimassaolo-alku values))
    (throw
      (Exception. "Shared link end date cannot be before the start date"))))

(defn insert-shared-module! [values]
  (let [vals (assoc values
                    :to-module-uuid
                    (UUID/fromString (:to-module-uuid values))
                    :shared-module-uuid
                    (UUID/fromString (:shared-module-uuid values)))]
    (validate-share-dates values)
    (db-ops/insert-one! :shared_modules (db-ops/to-sql vals))))

(defn select-shared-module [uuid]
  (let [share-id (UUID/fromString uuid)]
    (db-ops/query
      [queries/select-shared-module-by-uuid share-id]
      {:row-fn share-from-sql})))

(defn select-shared-module-links [uuid]
  (let [module-id (UUID/fromString uuid)]
    (db-ops/query
      [queries/select-shared-module-links-by-module-uuid module-id]
      {:row-fn share-from-sql})))

(defn delete-shared-module! [uuid]
  (db-ops/delete!
    :shared_modules
    ["share_id = ?" (UUID/fromString uuid)]))
