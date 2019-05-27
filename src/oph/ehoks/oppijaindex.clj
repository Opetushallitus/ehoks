(ns oph.ehoks.oppijaindex
  (:require [clojure.string :as cs]
            [oph.ehoks.db.postgresql :as db]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [clojure.tools.logging :as log]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.hoks :refer [from-sql]])
  (:import java.time.LocalDate))

(defonce oppijat (atom []))

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
  (db/query
    [queries/select-oppilaitos-oppijat-search-count
     (:oppilaitos-oid params)
     (:koulutustoimija-oid params)
     (get-like (:nimi params))
     (get-like (:tutkinto params))
     (get-like (:osaamisala params))]))

(defn get-oppijat-without-index []
  (db/select-hoks-oppijat-without-index))

(defn get-opiskeluoikeudet-without-index []
  (db/select-hoks-opiskeluoikeudet-without-index))

(defn update-opiskeluoikeus! [oid oppija-oid]
  (try
    (let [opiskeluoikeus (k/get-opiskeluoikeus-info oid)]
      (db/insert-opiskeluoikeus
        {:oid oid
         :oppija_oid oppija-oid
         :oppilaitos_oid (get-in opiskeluoikeus [:oppilaitos :oid])
         :koulutustoimija_oid (get-in opiskeluoikeus [:koulutustoimija :oid])
         :tutkinto ""
         :osaamisala ""}))
    (catch Exception e
      (if (= (:status (ex-data e)) 404)
        (log/warnf "Opiskeluoikeus %s not found in Oppijanumerorekisteri" oid)
        (throw e)))))

(defn update-oppija! [oid]
  (try
    (let [oppija (:body (onr/find-student-by-oid oid))]
      (db/insert-oppija
        {:oid oid
         :nimi (format "%s %s" (:etunimet oppija) (:sukunimi oppija))}))
    (catch Exception e
      (if (= (:status (ex-data e)) 404)
        (log/warnf "Oppija %s not found in Oppijanumerorekisteri" oid)
        (throw e)))))

(defn update-oppijat-without-index! []
  (log/info "Start indexing oppijat")
  (doseq [{oid :oppija_oid} (get-oppijat-without-index)]
    (update-oppija! oid))
  (log/info "Indexing oppijat finished"))

(defn update-opiskeluoikeudet-without-index! []
  (log/info "Start indexing opiskeluoikeudet")
  (doseq [{oid :opiskeluoikeus_oid oppija-oid :oppija_oid} (get-opiskeluoikeudet-without-index)]
    (update-opiskeluoikeus! oid oppija-oid))
  (log/info "Indexing opiskeluoikeudet finished"))