(ns oph.ehoks.oppijaindex
  (:require [clojure.string :as cs]
            [oph.ehoks.db.postgresql :as db]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [clojure.tools.logging :as log]))

(defonce oppijat (atom []))

(defn- lower-values [m]
  (reduce
    (fn [c [k v]]
      (assoc c k (cs/lower-case v)))
    {}
    m))

(defn- matches-all? [o params]
  (nil?
    (some
      (fn [[k v]]
        (when-not (cs/includes? (cs/lower-case (get o k)) v) k))
      params)))

; TODO support for either includes or strict match

(defn search
  ([search-params sort-key-fn comp-fn]
    (let [lowered-params (lower-values search-params)]
      (sort-by
        sort-key-fn
        comp-fn
        (if (empty? search-params)
          @oppijat
          (filter
            (fn [o]
              (matches-all? o lowered-params))
            @oppijat)))))
  ([search-params sort-key-fn]
    (search search-params sort-key-fn compare))
  ([search-params]
    (search search-params :nimi compare))
  ([]
    @oppijat))

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
         :oppilaitos-oid (get-in opiskeluoikeus [:oppilaitos :oid])
         :tutkinto nil
         :osaamisala nil
         :alku nil
         :loppu nil}))
    (catch Exception e
      (if (= (:status (ex-data e)) 404)
        (log/warnf "Opiskeluoikeus %s not found in Oppijanumerorekisteri" oid)
        (throw e)))))

(defn update-oppija! [oid]
  (try
    (let [oppija (onr/find-student-by-oid oid)]
      (db/insert-oppija
        {:oid oid
         :etunimi (:etunimet oppija)
         :sukunimi (:sukunimi oppija)}))
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