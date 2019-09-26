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

(defn- try-insert-tutkinnon-osa-share! [values]
  (try
    (db-ops/insert-one! :tutkinnon_osa_shares (db-ops/to-sql values))
    (catch PSQLException e
      (if-not (.startsWith (.getMessage e) psql-duplicate-error)
        (throw e)
        (do (log/warnf
              "Duplicate uuid %s in tutkinnon osa shares. Will retry."
              (:uuid values))
            nil)))))

(defn insert-tutkinnon-osa-share! [values]
  (loop [uuid (java.util.UUID/randomUUID)]
    (if-let [result (try-insert-tutkinnon-osa-share! (assoc values :uuid uuid))]
      result
      (recur (java.util.UUID/randomUUID)))))

(defn select-hoks-tutkinnon-osa-shares [hoks-id koodi-uri]
  (db-ops/query
    [queries/select-hoks-tutkinnon-osa-shares hoks-id koodi-uri]
    {:row-fn share-from-sql}))

(defn delete-tutkinnon-osa-share! [uuid hoks-id]
  (db-ops/delete!
    :tutkinnon_osa_shares
    ["uuid = ? AND hoks_id = ?" (java.util.UUID/fromString uuid) hoks-id]))