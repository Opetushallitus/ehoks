(ns oph.ehoks.oppijaindex
  (:require [clojure.string :as cs]
            [oph.ehoks.db.postgresql :as db]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [clojure.tools.logging :as log]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.hoks :refer [from-sql]])
  (:import java.time.LocalDate))

(defn- get-like [v]
  (format "%%%s%%" (or v "")))

(def oppija-columns
  {:nimi "nimi" :tutkinto "tutkinto" :osaamisala "osaamisala"})

(defn- set-query [q params]
  (-> queries/select-oppilaitos-oppijat
      (cs/replace
        ":column" (get oppija-columns (:order-by-column params) "nimi"))
      (cs/replace ":desc" (if (:desc params) "DESC" "ASC"))))

(defn search [params]
  (db/query
    [(set-query queries/select-oppilaitos-oppijat params)
     (:oppilaitos-oid params)
     (:koulutustoimija-oid params)
     (get-like (:nimi params))
     (get-like (:tutkinto params))
     (get-like (:osaamisala params))
     (:item-count params)
     (:offset params)]
    {:row-fn from-sql}))

(defn get-count [params]
  (:count
    (first
      (db/query
        [queries/select-oppilaitos-oppijat-search-count
         (:oppilaitos-oid params)
         (:koulutustoimija-oid params)
         (get-like (:nimi params))
         (get-like (:tutkinto params))
         (get-like (:osaamisala params))]))))

(defn get-oppijat-without-index []
  (db/select-hoks-oppijat-without-index))

(defn get-oppijat-without-index-count []
  (:count (first (db/select-hoks-oppijat-without-index-count))))

(defn get-opiskeluoikeudet-without-index []
  (db/select-hoks-opiskeluoikeudet-without-index))

(defn get-opiskeluoikeudet-without-index-count []
  (:count (first (db/select-hoks-opiskeluoikeudet-without-index-count))))

(defn get-oppija-opiskeluoikeudet [oppija-oid]
  (db/select-opiskeluoikeudet-by-oppija-oid oppija-oid))

(defn get-oppija-by-oid [oppija-oid]
  (db/select-oppija-by-oid oppija-oid))

(defn get-opiskeluoikeus-by-oid [oid]
  (db/select-opiskeluoikeus-by-oid oid))

(defn get-oppilaitos-oids []
  (filter some? (db/select-oppilaitos-oids)))

(defn get-oppilaitos-oids-by-koulutustoimija-oid [koulutustoimija-oid]
  (filter some?
          (db/select-oppilaitos-oids-by-koulutustoimija-oid
            koulutustoimija-oid)))

(defn- get-opiskeluoikeus-info [oid oppija-oid]
  (let [opiskeluoikeus (k/get-opiskeluoikeus-info-raw oid)]
    (when (> (count (:suoritukset opiskeluoikeus)) 1)
      (log/warnf "Opiskeluoikeus %s has multiple suoritukset.
                    First one is used for tutkinto"))
    {:oid oid
     :oppija_oid oppija-oid
     :oppilaitos_oid (get-in opiskeluoikeus [:oppilaitos :oid])
     :koulutustoimija_oid (get-in opiskeluoikeus [:koulutustoimija :oid])
     :tutkinto (get-in
                 opiskeluoikeus
                 [:suoritukset 0 :koulutusmoduuli :tunniste :nimi :fi]
                 "")
     :osaamisala ""}))

(defn add-new-opiskeluoikeus! [oid oppija-oid]
  (try
    (db/insert-opiskeluoikeus (get-opiskeluoikeus-info oid oppija-oid))
    (catch Exception e
      (log/errorf
        "Error adding opiskeluoikeus %s of oppija %s" oid oppija-oid)
      (throw e))))

(defn update-opiskeluoikeus! [oid oppija-oid]
  (try
    (db/update-opiskeluoikeus! (get-opiskeluoikeus-info oid oppija-oid))
    (catch Exception e
      (log/errorf
        "Error updating opiskeluoikeus %s of oppija %s" oid oppija-oid)
      (throw e))))

(defn add-opiskeluoikeus! [oid oppija-oid]
  (when (empty? (get-opiskeluoikeus-by-oid oid))
    (add-new-opiskeluoikeus! oid oppija-oid)))

(defn add-new-oppija! [oid]
  (try
    (let [oppija (:body (onr/find-student-by-oid oid))]
      (db/insert-oppija
        {:oid oid
         :nimi (format "%s %s" (:etunimet oppija) (:sukunimi oppija))}))
    (catch Exception e
      (log/errorf "Error adding oppija %s" oid)
      (throw e))))

(defn add-oppija! [oid]
  (when (empty? (get-oppija-by-oid oid))
    (add-new-oppija! oid)))

(defn update-oppija! [oid]
  (try
    (let [oppija (:body (onr/find-student-by-oid oid))]
      (db/update-oppija!
        oid
        {:nimi (format "%s %s" (:etunimet oppija) (:sukunimi oppija))}))
    (catch Exception e
      (log/errorf "Error updating oppija %s" oid)
      (throw e))))

(defn update-oppijat-without-index! []
  (log/info "Start indexing oppijat")
  (doseq [{oid :oppija_oid} (get-oppijat-without-index)]
    (add-oppija! oid))
  (log/info "Indexing oppijat finished"))

(defn update-opiskeluoikeudet-without-index! []
  (log/info "Start indexing opiskeluoikeudet")
  (doseq [{oid :opiskeluoikeus_oid oppija-oid :oppija_oid}
          (get-opiskeluoikeudet-without-index)]
    (add-opiskeluoikeus! oid oppija-oid))
  (log/info "Indexing opiskeluoikeudet finished"))
