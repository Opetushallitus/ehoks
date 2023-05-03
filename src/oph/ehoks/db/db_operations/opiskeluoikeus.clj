(ns oph.ehoks.db.db-operations.opiskeluoikeus
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [clojure.string :as cs]))

(defn get-like
  "Luo LIKE-ekspression SQL:ää varten."
  [v]
  (format "%%%s%%" (or v "")))

(def translated-oppija-columns
  "Käännettyjen sarakkeiden pohjat."
  {:tutkinto "tutkinto_nimi->>" :osaamisala "osaamisala_nimi->>"})

(def default-locale "fi")

(defn- get-locale
  "Hakee nykyisen localen."
  [params]
  (str "'" (get params :locale default-locale) "'"))

(defn- get-translated-oppija-column
  "Hakee sarakkeen nimen, joka sisältää myös localen nimen."
  [column params]
  (str (get translated-oppija-columns column) (get-locale params)))

(defn- get-translated-column-filter
  "Luo osan SQL-queryn WHERE-lauseesta, jolla tiedot suodatetaan."
  [column params]
  (str
    "AND oo."
    (get-translated-oppija-column column params)
    " ILIKE '"
    (get-like (column params))
    "'"))

(defn set-oppijat-count-query
  "Luo oppijoiden määrä -queryn."
  [params]
  (-> queries/select-oppilaitos-oppijat-search-count
      (cs/replace ":tutkinto-filter"
                  (if (:tutkinto params)
                    (get-translated-column-filter :tutkinto params)
                    ""))
      (cs/replace ":osaamisala-filter"
                  (if (:osaamisala params)
                    (get-translated-column-filter :osaamisala params)
                    ""))))

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
  [oid]
  (first
    (db-ops/query
      [queries/select-opiskeluoikeudet-by-oid oid]
      {:row-fn db-ops/from-sql})))

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
