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

(defn update-opiskeluoikeus! [oid oppija-oid]
  (when (empty? (get-opiskeluoikeus-by-oid oid))
    (try
      (let [opiskeluoikeus (k/get-opiskeluoikeus-info-raw oid)]
        (db/insert-opiskeluoikeus
          {:oid oid
           :oppija_oid oppija-oid
           :oppilaitos_oid (get-in opiskeluoikeus [:oppilaitos :oid])
           :koulutustoimija_oid (get-in opiskeluoikeus [:koulutustoimija :oid])
           :tutkinto ""
           :osaamisala ""}))
      (catch Exception e
        (log/errorf
          "Error updating opiskeluoikeus %s of oppija %s" oid oppija-oid)
        (throw e)))))

(defn update-oppija! [oid]
  (when (empty? (get-oppija-by-oid oid))
    (try
      (let [oppija (:body (onr/find-student-by-oid oid))]
        (db/insert-oppija
          {:oid oid
           :nimi (format "%s %s" (:etunimet oppija) (:sukunimi oppija))}))
      (catch Exception e
        (log/errorf "Error updating oppija %s" oid)
        (throw e)))))

(defn update-oppija-and-opiskeluoikeudet! [oppija-oid]
  (update-oppija! oppija-oid)
  (doseq [opiskeluoikeus (k/get-oppija-opiskeluoikeudet oppija-oid)]
    (update-opiskeluoikeus! (:oid opiskeluoikeus) oppija-oid)))

(defn update-oppijat-without-index! []
  (log/info "Start indexing oppijat")
  (doseq [{oid :oppija_oid} (get-oppijat-without-index)]
    (update-oppija! oid))
  (log/info "Indexing oppijat finished"))

(defn update-opiskeluoikeudet-without-index! []
  (log/info "Start indexing opiskeluoikeudet")
  (doseq [{oid :opiskeluoikeus_oid oppija-oid :oppija_oid}
          (get-opiskeluoikeudet-without-index)]
    (update-opiskeluoikeus! oid oppija-oid))
  (log/info "Indexing opiskeluoikeudet finished"))
