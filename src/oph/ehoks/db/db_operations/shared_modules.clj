(ns oph.ehoks.db.db-operations.shared-modules
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops])
  (:import (java.util UUID)
           (java.time LocalDate)))

(defn- share-from-sql [v]
  (db-ops/from-sql v {:removals [:id]}))

(defn tutkinnonosa-from-sql [m]
  (db-ops/from-sql m {:removals [:id :hoks_id]}))

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
                    :tutkinnonosa-module-uuid
                    (UUID/fromString (:tutkinnonosa-module-uuid values))
                    :shared-module-uuid
                    (UUID/fromString (:shared-module-uuid values)))]
    (validate-share-dates values)
    (db-ops/insert-one! :shared_modules (db-ops/to-sql vals))))

(defn select-shared-link [uuid]
  (let [share-id (UUID/fromString uuid)]
    (first
      (db-ops/query
        [queries/select-shared-link-by-uuid share-id]
        {:row-fn share-from-sql}))))

(defn select-shared-module-links [uuid]
  (let [module-id (UUID/fromString uuid)]
    (db-ops/query
      [queries/select-shared-module-links-by-module-uuid module-id]
      {:row-fn share-from-sql})))

(defn delete-shared-module! [uuid]
  (db-ops/delete!
    :shared_modules
    ["share_id = ?" (UUID/fromString uuid)]))

(defn select-oppija-opiskeluoikeus-for-shared-link [uuid]
  (let [link-id (UUID/fromString uuid)]
    (first
      (db-ops/query
        [queries/select-oppija-opiskeluoikeus-for-shared-link link-id]
        {:row-fn share-from-sql}))))

(defn select-hankittavat-paikalliset-tutkinnon-osat-by-module-id [uuid]
  (first
    (db-ops/query
      [queries/select-hankittavat-paikalliset-tutkinnon-osat-by-module-id uuid]
      {:row-fn tutkinnonosa-from-sql})))

(defn select-hankittavat-ammat-tutkinnon-osat-by-module-id [uuid]
  (first
    (db-ops/query
      [queries/select-hankittavat-ammat-tutkinnon-osat-by-module-id uuid]
      {:row-fn tutkinnonosa-from-sql})))

(defn select-hankittavat-yhteiset-tutkinnon-osat-by-module-id [uuid]
  (first
    (db-ops/query
      [queries/select-hankittavat-yhteiset-tutkinnon-osat-by-module-id uuid]
      {:row-fn tutkinnonosa-from-sql})))

(defn select-osaamisen-osoittamiset-by-module-id [uuid]
  (first
    (db-ops/query [queries/select-osaamisen-osoittamiset-by-module-id uuid]
                  {:row-fn share-from-sql})))

(defn select-osaamisen-hankkimistavat-by-module-id [uuid]
  (first
    (db-ops/query [queries/select-osaamisen-hankkimistavat-by-module-id uuid]
                  {:row-fn share-from-sql})))

(defn select-nayttoymparisto-by-osaamisen-osoittaminen-id [id]
  (first
    (db-ops/query
      [queries/select-nayttoymparistot-by-id id]
      {:row-fn share-from-sql})))
