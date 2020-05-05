(ns oph.ehoks.db.db-operations.oppija
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [clojure.tools.logging :as log])
  (:import [org.postgresql.util PSQLException]))

(defn- share-from-sql [v]
  (db-ops/from-sql v {:removals [:hoks_id]}))

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

(def psql-duplicate-error
  (str "ERROR: duplicate key value violates unique constraint "
       "\"tutkinnon_osa_shares_pkey\""))

(defn- try-insert-shared-module! [values]
  (try
    (db-ops/insert-one! :shared_modules (db-ops/to-sql values))
    (catch PSQLException e
      (if-not (.startsWith (.getMessage e) psql-duplicate-error)
        (throw e)
        (do (log/warnf
              "Duplicate uuid %s in tutkinnon osa shares. Will retry."
              (:uuid values))
            nil)))))

(defn insert-shared-module! [values]
  (loop [uuid (java.util.UUID/randomUUID)]
    (if-let [result (try-insert-shared-module! (assoc values :id uuid))]
      result
      (recur (java.util.UUID/randomUUID)))))

(defn select-shared-module [uuid]
  (db-ops/query
    [queries/select-shared-module-by-uuid uuid]))

(defn select-shared-module-links [uuid]
  (db-ops/query
    [queries/select-shared-module-links-by-module-uuid uuid]))

(defn delete-shared-module! [uuid]
  (db-ops/delete!
    :shared_modules
    ["uuid = ?" (java.util.UUID/fromString uuid)]))
