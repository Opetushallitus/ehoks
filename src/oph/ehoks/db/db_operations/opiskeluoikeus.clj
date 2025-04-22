(ns oph.ehoks.db.db-operations.opiskeluoikeus
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(defn select-opiskeluoikeudet-without-tutkinto
  "Hakee tietokannasta opiskeluoikeudet, joissa ei ole tutkintoa."
  []
  (db-ops/query
    [queries/select-hoks-opiskeluoikeudet-without-tutkinto]))

(defn select-opiskeluoikeudet-without-tutkinto-count
  "Hakee tietokannasta määrän opiskeluoikeuksista, joissa ei ole tutkintoa."
  []
  (db-ops/query
    [queries/select-hoks-opiskeluoikeudet-without-tutkinto-count]))

(defn select-opiskeluoikeudet-by-oppija-oid
  "Hakee tietokannasta opiskeluoikeudet oppijan OID:n perusteella."
  [oppija-oid]
  (db-ops/query
    [queries/select-opiskeluoikeudet-by-oppija-oid oppija-oid]
    {:row-fn db-ops/from-sql}))

(defn select-opiskeluoikeus-by-oid
  "Hakee tietokannasta opiskeluoikeuden OID:n perusteella."
  [oid & keep-columns]
  (first
    (db-ops/query
      [queries/select-opiskeluoikeudet-by-oid oid]
      {:row-fn #(db-ops/from-sql % {} keep-columns)})))

(defn select-hankintakoulutus-oids-by-master-oid
  "Hakee tietokannasta lista opiskeluoikeus OID:istä
  hankintakoulutus-opiskeluoikeus-oid:n perusteella."
  [oid]
  (db-ops/query
    [queries/select-hankintakoulutus-oids-by-master-oid oid]))

(defn insert-opiskeluoikeus!
  "Lisää yhden opiskeluoikeuden tietokantaan."
  [opiskeluoikeus]
  (db-ops/insert-one! :opiskeluoikeudet (db-ops/to-sql opiskeluoikeus)))

(defn update-opiskeluoikeus!
  "Päivittää yhden tietokannassa olevan opiskeluoikeuden."
  [oid opiskeluoikeus]
  (db-ops/update!
    :opiskeluoikeudet
    (db-ops/to-sql opiskeluoikeus)
    ["oid = ?" oid]))

(defn update-opiskeluoikeus-by-oppija-oid!
  "Päivittää yhden tietokannassa olevan opiskeluoikeuden oppija-oidin
  perusteella."
  [oppija-oid opiskeluoikeus]
  (db-ops/update!
    :opiskeluoikeudet
    (db-ops/to-sql opiskeluoikeus)
    ["oppija_oid = ?" oppija-oid]))

(defn select-count-opiskeluoikeudet-by-koulutustoimija
  "Hakee tietokannasta määrän opiskeluoikeuksista, joilla on annettu
  koulutustoimija."
  [oid]
  (db-ops/query
    [queries/select-count-by-koulutustoimija oid]))

(defn select-opiskeluoikeus-delete-confirm-info
  "Hakee HOKSiin liittyviä tietoja poistamisen varmistusdialogia varten"
  [koulutustoimija-oid]
  (->
    (select-count-opiskeluoikeudet-by-koulutustoimija koulutustoimija-oid)
    (first)
    (:count)))

(defn delete-opiskeluoikeus-from-index!
  "Poistaa opiskeluoikeuden OID:n perusteella."
  [oid]
  (db-ops/delete! :opiskeluoikeudet
                  ["oid = ?" oid]))

(defn delete-from-index-by-koulutustoimija!
  "Poistaa opiskeluoikeuden koulutustoimijan OID:n perusteella."
  [koulutustoimija-oid]
  (db-ops/delete! :opiskeluoikeudet
                  ["koulutustoimija_oid = ?" koulutustoimija-oid]))
