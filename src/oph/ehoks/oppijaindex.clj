(ns oph.ehoks.oppijaindex
  (:require [clojure.string :as cs]
            [oph.ehoks.db.postgresql.common :as db]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [clojure.tools.logging :as log]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-opiskeluoikeus]
            [oph.ehoks.db.db-operations.oppija :as db-oppija]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]))

(defn- get-like [v]
  (format "%%%s%%" (or v "")))

(def translated-oppija-columns
  {:tutkinto "tutkinto_nimi->>" :osaamisala "osaamisala_nimi->>"})

(def default-locale "fi")

(defn- get-locale [params]
  (str "'" (get params :locale default-locale) "'"))

(defn- get-translated-oppija-column [column params]
  (str (get translated-oppija-columns column) (get-locale params)))

(defn- get-oppija-order-by-column [params]
  (let [column (:order-by-column params)]
    (case column
      :nimi "nimi"
      :tutkinto (get-translated-oppija-column column params)
      :osaamisala (get-translated-oppija-column column params)
      "nimi")))

(defn- get-translated-column-filter [column params]
  (str
    "AND "
    (get-translated-oppija-column column params)
    " ILIKE '"
    (get-like (column params))
    "'"))

(defn- set-oppijat-query [params]
  (as-> queries/select-oppilaitos-oppijat query
        (cs/replace query ":order-by-column" (get-oppija-order-by-column params))
        (cs/replace query ":desc" (if (:desc params) "DESC" "ASC"))
        (cs/replace query
                    ":tutkinto-filter"
                    (if (:tutkinto params)
                      (get-translated-column-filter :tutkinto params)
                      ""))
        (cs/replace query
                    ":osaamisala-filter"
                    (if (:osaamisala params)
                      (get-translated-column-filter :osaamisala params)
                      ""))))

(defn search
  "Search oppijat with given params"
  [params]
  (db-ops/query
    [(set-oppijat-query params)
     (:oppilaitos-oid params)
     (:koulutustoimija-oid params)
     (get-like (:nimi params))
     (:item-count params)
     (:offset params)]
    {:row-fn db-ops/from-sql}))

(defn get-count
  "Get total count of results"
  [params]
  (:count
    (first
      (db-ops/query
        [queries/select-oppilaitos-oppijat-search-count
         (:oppilaitos-oid params)
         (:koulutustoimija-oid params)
         (get-like (:nimi params))
         (get-like (:tutkinto params))
         (get-like (:osaamisala params))]))))

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
        "Opiskeluoikeus %s has multiple osaamisala. First one is used."))
    (let [tutkinto (get-tutkinto-nimi opiskeluoikeus)
          osaamisala (get-osaamisala-nimi opiskeluoikeus)]
      {:oid oid
       :oppija_oid oppija-oid
       :oppilaitos_oid (get-in opiskeluoikeus [:oppilaitos :oid])
       :koulutustoimija_oid (get-in opiskeluoikeus [:koulutustoimija :oid])
       :tutkinto (:fi tutkinto)
       :tutkinto_nimi tutkinto
       :osaamisala (:fi osaamisala)
       :osaamisala_nimi osaamisala})))

(defn add-new-opiskeluoikeus! [oid oppija-oid]
  (try
    (db-opiskeluoikeus/insert-opiskeluoikeus
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
      (db-oppija/insert-oppija
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