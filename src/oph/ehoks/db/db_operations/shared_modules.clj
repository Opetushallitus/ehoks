(ns oph.ehoks.db.db-operations.shared-modules
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops])
  (:import (java.util UUID)
           (java.time LocalDate)))

(defn- share-from-sql
  "Muuttaa tietokannasta haetun sharen sen mukaiseksi, mitä odotetaan
  palvelussa."
  [v]
  (db-ops/from-sql v {:removals [:id]}))

(defn tutkinnonosa-from-sql
  "Muuttaa tietokannasta haetun tutkinnon osan sen mukaiseksi, mitä odotetaan
  palvelussa."
  [m]
  (db-ops/from-sql m {:removals [:id :hoks_id]}))

(defn- validate-share-dates
  "Varmistaa, että sharen päivämäärän ovat hyväksyttäviä."
  [{:keys [voimassaolo-alku voimassaolo-loppu]}]
  (cond
    (.isBefore ^LocalDate voimassaolo-loppu (LocalDate/now))
    (throw (ex-info "Shared link end date cannot be in the past"
                    {:type              :shared-link-validation-error
                     :voimassaolo-loppu voimassaolo-loppu}))
    (.isBefore ^LocalDate voimassaolo-loppu voimassaolo-alku)
    (throw (ex-info "Shared link end date cannot be before the start date"
                    {:type              :shared-link-validation-error
                     :voimassaolo-alku  voimassaolo-alku
                     :voimassaolo-loppu voimassaolo-loppu}))))

(defn insert-shared-module!
  "Tallentaa shared modulen tietokantaan."
  [values]
  (let [vals (assoc values
                    :tutkinnonosa-module-uuid
                    (UUID/fromString (:tutkinnonosa-module-uuid values))
                    :shared-module-uuid
                    (UUID/fromString (:shared-module-uuid values)))]
    (validate-share-dates values)
    (db-ops/insert-one! :shared_modules (db-ops/to-sql vals))))

(defn select-shared-link
  "Hakee shared linkin tietokannasta UUID:n perusteella."
  [uuid]
  (let [share-id (UUID/fromString uuid)]
    (first
      (db-ops/query
        [queries/select-shared-link-by-uuid share-id]
        {:row-fn share-from-sql}))))

(defn select-shared-module-links
  "Hakee shared module linkit module ID:n perusteella."
  [uuid]
  (let [module-id (UUID/fromString uuid)]
    (db-ops/query
      [queries/select-shared-module-links-by-module-uuid module-id]
      {:row-fn share-from-sql})))

(defn delete-shared-module!
  "Poistaa shared modulen ID:n perusteella."
  [uuid]
  (db-ops/delete!
    :shared_modules
    ["share_id = ?" (UUID/fromString uuid)]))

(defn select-oppija-opiskeluoikeus-for-shared-link
  "Hakee oppijan ja opiskeluoikeuden tiedot shared linkin ID:n perusteella."
  [uuid]
  (let [link-id (UUID/fromString uuid)]
    (first
      (db-ops/query
        [queries/select-oppija-opiskeluoikeus-for-shared-link link-id]
        {:row-fn share-from-sql}))))

(defn select-hankittavat-paikalliset-tutkinnon-osat-by-module-id
  "Hakee hankittavat paikalliset tutkinnon osat module ID:n perusteella."
  [uuid]
  (first
    (db-ops/query
      [queries/select-hankittavat-paikalliset-tutkinnon-osat-by-module-id uuid]
      {:row-fn tutkinnonosa-from-sql})))

(defn select-hankittavat-ammat-tutkinnon-osat-by-module-id
  "Hakee hankittavat ammatilliset tutkinnon osat module ID:n perusteella."
  [uuid]
  (first
    (db-ops/query
      [queries/select-hankittavat-ammat-tutkinnon-osat-by-module-id uuid]
      {:row-fn tutkinnonosa-from-sql})))

(defn select-hankittavat-yhteiset-tutkinnon-osat-by-module-id
  "Hakee hankittavat yhteiset tutkinnon osat module ID:n perusteella."
  [uuid]
  (first
    (db-ops/query
      [queries/select-hankittavat-yhteiset-tutkinnon-osat-by-module-id uuid]
      {:row-fn tutkinnonosa-from-sql})))
