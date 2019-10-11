(ns oph.ehoks.oppijaindex
  (:require [oph.ehoks.db.postgresql.common :as db]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [clojure.tools.logging :as log]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-opiskeluoikeus]
            [oph.ehoks.db.db-operations.oppija :as db-oppija]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]))

(defn search
  "Search oppijat with given params"
  [params]
  (db-ops/query
    [(db-opiskeluoikeus/set-oppijat-query params)
     (:oppilaitos-oid params)
     (:koulutustoimija-oid params)
     (db-opiskeluoikeus/get-like (:nimi params))
     (:item-count params)
     (:offset params)]
    {:row-fn db-ops/from-sql}))

(defn get-count
  "Get total count of results"
  [params]
  (:count
    (first
      (db-ops/query
        [(db-opiskeluoikeus/set-oppijat-count-query params)
         (:oppilaitos-oid params)
         (:koulutustoimija-oid params)
         (db-opiskeluoikeus/get-like (:nimi params))]))))

(defn get-oppijat-without-index
  "Get hoks oppijat without index"
  []
  (db-hoks/select-hoks-oppijat-without-index))

(defn get-oppijat-without-index-count
  "Get count of hoks oppijat without index"
  []
  (:count (first (db-hoks/select-hoks-oppijat-without-index-count))))

(defn get-opiskeluoikeudet-without-index
  "Get hoks opiskeluoikeudet without index"
  []
  (db-hoks/select-hoks-opiskeluoikeudet-without-index))

(defn get-opiskeluoikeudet-without-index-count
  "Get hoks opiskeluoikeudet without index count"
  []
  (:count (first (db-hoks/select-hoks-opiskeluoikeudet-without-index-count))))

(defn get-opiskeluoikeudet-without-tutkinto
  "Get opiskeluoikeudet which has no tutkinto info"
  []
  (db-opiskeluoikeus/select-opiskeluoikeudet-without-tutkinto))

(defn get-opiskeluoikeudet-without-tutkinto-count
  "Get count of opiskeluoikeudet which has no tutkinto info"
  []
  (:count
    (first (db-opiskeluoikeus/select-opiskeluoikeudet-without-tutkinto-count))))

(defn get-oppija-opiskeluoikeudet
  "List opiskeluoikeudet of oppija"
  [oppija-oid]
  (db-opiskeluoikeus/select-opiskeluoikeudet-by-oppija-oid oppija-oid))

(defn get-oppija-by-oid
  "Get oppija by oppija-oid"
  [oppija-oid]
  (db-oppija/select-oppija-by-oid oppija-oid))

(defn get-opiskeluoikeus-by-oid [oid]
  (db-opiskeluoikeus/select-opiskeluoikeus-by-oid oid))

(defn get-oppilaitos-oids []
  (filter some? (db/select-oppilaitos-oids)))

(defn get-oppilaitos-oids-by-koulutustoimija-oid [koulutustoimija-oid]
  (filter some?
          (db/select-oppilaitos-oids-by-koulutustoimija-oid
            koulutustoimija-oid)))

(defn get-tutkinto-nimi [opiskeluoikeus]
  (get-in
    opiskeluoikeus
    [:suoritukset 0 :koulutusmoduuli :tunniste :nimi]
    {:fi "" :sv ""}))

(defn get-osaamisala-nimi [opiskeluoikeus]
  (or
    (get-in
      opiskeluoikeus
      [:suoritukset 0 :osaamisala 0 :nimi])
    (get-in
      opiskeluoikeus
      [:suoritukset 0 :osaamisala 0 :osaamisala :nimi])
    {:fi "" :sv ""}))

(defn- get-opiskeluoikeus-info [oid oppija-oid]
  (let [opiskeluoikeus (k/get-opiskeluoikeus-info-raw oid)]
    (when (> (count (:suoritukset opiskeluoikeus)) 1)
      (log/warnf
        "Opiskeluoikeus %s has multiple suoritukset. First is used for tutkinto"
        oid))
    (when (> (count (get-in opiskeluoikeus [:suoritukset 0 :osaamisala])) 1)
      (log/warnf
        "Opiskeluoikeus %s has multiple osaamisala. First one is used." oid))
    (let [tutkinto (get-tutkinto-nimi opiskeluoikeus)
          osaamisala (get-osaamisala-nimi opiskeluoikeus)]
      (-> {:oid oid
           :oppija_oid oppija-oid
           :oppilaitos_oid (get-in opiskeluoikeus [:oppilaitos :oid])
           :koulutustoimija_oid (get-in opiskeluoikeus [:koulutustoimija :oid])
           :tutkinto_nimi tutkinto
           :osaamisala_nimi osaamisala}
          (cond-> (some? (:fi tutkinto)) (assoc :tutkinto (:fi tutkinto)))
          (cond-> (some? (:fi osaamisala))
                  (assoc :osaamisala (:fi osaamisala)))))))

(defn add-new-opiskeluoikeus! [oid oppija-oid]
  (try
    (db-opiskeluoikeus/insert-opiskeluoikeus!
      (get-opiskeluoikeus-info oid oppija-oid))
    (catch Exception e
      (log/errorf
        "Error adding opiskeluoikeus %s of oppija %s" oid oppija-oid)
      (throw e))))

(defn update-opiskeluoikeus! [oid oppija-oid]
  (try
    (db-opiskeluoikeus/update-opiskeluoikeus!
      oid
      (dissoc (get-opiskeluoikeus-info oid oppija-oid) :oid :oppija_oid))
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
      (db-oppija/insert-oppija!
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
      (db-oppija/update-oppija!
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

(defn update-opiskeluoikeudet-without-tutkinto! []
  (log/info "Start indexing opiskeluoikeudet without tutkinto")
  (doseq [{oid :oid oppija-oid :oppija_oid}
          (get-opiskeluoikeudet-without-tutkinto)]
    (update-opiskeluoikeus! oid oppija-oid))
  (log/info "Indexing opiskeluoikeudet finished"))

(defn set-opiskeluoikeus-paattynyt! [oid timestamp]
  (db-opiskeluoikeus/update-opiskeluoikeus! oid {:paattynyt timestamp}))
