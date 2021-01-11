(ns oph.ehoks.oppijaindex
  (:require [oph.ehoks.db.postgresql.common :as db]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [clojure.tools.logging :as log]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-opiskeluoikeus]
            [oph.ehoks.db.db-operations.oppija :as db-oppija]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.config :refer [config]])
  (:import [java.time LocalDate]))

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

(defn get-amount-of-hoks
  "Get total amount of hokses now"
  []
  (first (db-hoks/select-count-all-hoks)))

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

(defn get-hankintakoulutus-oids-by-master-oid [oid]
  (db-opiskeluoikeus/select-hankintakoulutus-oids-by-master-oid oid))

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

(defn- opiskeluoikeus-to-sql [opiskeluoikeus oppija-oid]
  (let [tutkinto (get-tutkinto-nimi opiskeluoikeus)
        osaamisala (get-osaamisala-nimi opiskeluoikeus)]
    {:oid (:oid opiskeluoikeus)
     :oppija_oid oppija-oid
     :oppilaitos_oid (get-in opiskeluoikeus [:oppilaitos :oid])
     :koulutustoimija_oid (get-in opiskeluoikeus [:koulutustoimija :oid])
     :tutkinto_nimi tutkinto
     :osaamisala_nimi osaamisala}))

(defn- get-opiskeluoikeus-info [oid oppija-oid]
  (let [opiskeluoikeus (k/get-opiskeluoikeus-info-raw oid)]
    (when (:sisältyyOpiskeluoikeuteen opiskeluoikeus)
      (log/warnf
        "Opiskeluoikeus %s has sisältyyOpiskeluoikeuteen information" oid)
      (throw (ex-info "Opiskeluoikeus sisältyy toiseen opiskeluoikeuteen"
                      {:error :hankintakoulutus})))
    (when (> (count (:suoritukset opiskeluoikeus)) 1)
      (log/warnf
        "Opiskeluoikeus %s has multiple suoritukset. First is used for tutkinto"
        oid))
    (when (> (count (get-in opiskeluoikeus [:suoritukset 0 :osaamisala])) 1)
      (log/warnf
        "Opiskeluoikeus %s has multiple osaamisala. First one is used." oid))
    (opiskeluoikeus-to-sql opiskeluoikeus oppija-oid)))

(defn- log-opiskeluoikeus-insert-error!
  ([oid oppija-oid exception]
    (log/errorf
      "Error adding opiskeluoikeus %s of oppija %s: %s"
      oid oppija-oid (.getMessage exception)))
  ([oid oppija-oid exception skip-indexing]
    (log/errorf
      "%sError adding opiskeluoikeus %s of oppija %s: %s"
      (if skip-indexing
        "Skipped indexing. "
        "")
      oid oppija-oid (.getMessage exception))))

(defn- log-opiskeluoikeus-insert-error-for-indexing! [oid oppija-oid exception]
  (log-opiskeluoikeus-insert-error! oid oppija-oid exception true))

(defn- insert-opiskeluoikeus [oid oppija-oid]
  (db-opiskeluoikeus/insert-opiskeluoikeus!
    (get-opiskeluoikeus-info oid oppija-oid)))

(defn- opiskeluoikeus-doesnt-exist [oid]
  (empty? (get-opiskeluoikeus-by-oid oid)))

(defn insert-hankintakoulutus-opiskeluoikeus!
  "Insert hankintakoulutus opiskeluoikeus or update if already exists"
  [opiskeluoikeus-oid oppija-oid hankintakoulutus-opiskeluoikeus]
  (let [jarjestaja-oid (get-in hankintakoulutus-opiskeluoikeus
                               [:sisältyyOpiskeluoikeuteen :oppilaitos :oid])
        hankintakoulutus-opiskeluoikeus-oid
        (get hankintakoulutus-opiskeluoikeus :oid)]
    (try
      (if (opiskeluoikeus-doesnt-exist hankintakoulutus-opiskeluoikeus-oid)
        (db-opiskeluoikeus/insert-opiskeluoikeus!
          (assoc
            (opiskeluoikeus-to-sql hankintakoulutus-opiskeluoikeus oppija-oid)
            :hankintakoulutus_jarjestaja_oid
            jarjestaja-oid
            :hankintakoulutus_opiskeluoikeus_oid
            opiskeluoikeus-oid))
        (do (log/infof
              "Oppija %s already has hankintakoulutus opiskeluoikeus %s for
              opiskeluoikeus %s. Updating the existing opiskeluoikeus."
              oppija-oid hankintakoulutus-opiskeluoikeus-oid opiskeluoikeus-oid)
            (db-opiskeluoikeus/update-opiskeluoikeus!
              hankintakoulutus-opiskeluoikeus-oid
              (assoc
                (opiskeluoikeus-to-sql hankintakoulutus-opiskeluoikeus
                                       oppija-oid)
                :hankintakoulutus_jarjestaja_oid jarjestaja-oid
                :hankintakoulutus_opiskeluoikeus_oid opiskeluoikeus-oid))))
      (catch Exception e
        (log-opiskeluoikeus-insert-error! opiskeluoikeus-oid oppija-oid e)
        (throw e)))))

(defn- insert-new-opiskeluoikeus-without-error-forwarding! [oid oppija-oid]
  (try
    (insert-opiskeluoikeus oid oppija-oid)
    (catch Exception e
      (log-opiskeluoikeus-insert-error-for-indexing! oid oppija-oid e))))

(defn- insert-new-opiskeluoikeus! [oid oppija-oid]
  (try
    (insert-opiskeluoikeus oid oppija-oid)
    (catch Exception e
      (log-opiskeluoikeus-insert-error! oid oppija-oid e)
      (throw e))))

(defn add-opiskeluoikeus-without-error-forwarding! [oid oppija-oid]
  (when (opiskeluoikeus-doesnt-exist oid)
    (insert-new-opiskeluoikeus-without-error-forwarding! oid oppija-oid)))

(defn add-opiskeluoikeus! [oid oppija-oid]
  (when (opiskeluoikeus-doesnt-exist oid)
    (insert-new-opiskeluoikeus! oid oppija-oid)))

(defn update-opiskeluoikeus-without-error-forwarding! [oid oppija-oid]
  (try
    (db-opiskeluoikeus/update-opiskeluoikeus!
      oid
      (dissoc (get-opiskeluoikeus-info oid oppija-oid) :oid :oppija_oid))
    (catch Exception e
      (log/errorf
        "Skipped indexing. Error updating opiskeluoikeus %s of oppija %s: %s"
        oid oppija-oid (.getMessage e)))))

(defn- log-opiskelija-insert-error!
  ([oid exception]
    (log/errorf "Error adding oppija %s: %s" oid (.getMessage exception)))
  ([oid exception skip-indexing]
    (log/errorf "%sError adding oppija %s: %s"
                (if skip-indexing
                  "Skipped indexing. "
                  "")
                oid (.getMessage exception))))

(defn- log-opiskelija-insert-error-for-indexing! [oid exception]
  (log-opiskelija-insert-error! oid exception true))

(defn- insert-oppija! [oid]
  (let [oppija (:body (onr/find-student-by-oid oid))]
    (db-oppija/insert-oppija!
      {:oid oid
       :nimi (format "%s %s" (:etunimet oppija) (:sukunimi oppija))})))

(defn- insert-new-oppija-without-error-forwarding! [oid]
  (try
    (insert-oppija! oid)
    (catch Exception e
      (log-opiskelija-insert-error-for-indexing! oid e))))

(defn- insert-new-oppija! [oid]
  (try
    (insert-oppija! oid)
    (catch Exception e
      (log-opiskelija-insert-error! oid e)
      (throw e))))

(defn- oppija-doesnt-exist [oid]
  (empty? (get-oppija-by-oid oid)))

(defn add-oppija-without-error-forwarding! [oid]
  (when (oppija-doesnt-exist oid)
    (insert-new-oppija-without-error-forwarding! oid)))

(defn add-oppija! [oid]
  (when (oppija-doesnt-exist oid)
    (insert-new-oppija! oid)))

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
    (add-oppija-without-error-forwarding! oid))
  (log/info "Indexing oppijat finished"))

(defn update-opiskeluoikeudet-without-index! []
  (log/info "Start indexing opiskeluoikeudet")
  (doseq [{oid :opiskeluoikeus_oid oppija-oid :oppija_oid}
          (get-opiskeluoikeudet-without-index)]
    (add-opiskeluoikeus-without-error-forwarding! oid oppija-oid))
  (log/info "Indexing opiskeluoikeudet finished"))

(defn update-opiskeluoikeudet-without-tutkinto! []
  (log/info "Start indexing opiskeluoikeudet without tutkinto")
  (doseq [{oid :oid oppija-oid :oppija_oid}
          (get-opiskeluoikeudet-without-tutkinto)]
    (update-opiskeluoikeus-without-error-forwarding! oid oppija-oid))
  (log/info "Indexing opiskeluoikeudet finished"))

(defn set-opiskeluoikeus-paattynyt! [oid timestamp]
  (db-opiskeluoikeus/update-opiskeluoikeus! oid {:paattynyt timestamp}))

(defn oppija-opiskeluoikeus-match?
  "Check that opiskeluoikeus belongs to oppija"
  [opiskeluoikeudet opiskeluoikeus-oid]
  (if (:enforce-opiskeluoikeus-match? config)
    (some #(= opiskeluoikeus-oid (:oid %)) opiskeluoikeudet)
    true))

(defn filter-hankintakoulutukset-for-current-opiskeluoikeus
  "Filters hankintakoulutukset from opiskeluoikeudet"
  [opiskeluoikeudet opiskeluoikeus-oid]
  (filter #(= (get-in % [:sisältyyOpiskeluoikeuteen :oid])
              opiskeluoikeus-oid)
          opiskeluoikeudet))

(defn add-oppija-hankintakoulutukset
  "Adds all hankintakoulutukset for oppija"
  [opiskeluoikeudet opiskeluoikeus-oid oppija-oid]
  (let [hankintakoulutukset
        (filter-hankintakoulutukset-for-current-opiskeluoikeus
          opiskeluoikeudet opiskeluoikeus-oid)]
    (doseq [hankintakoulutus hankintakoulutukset]
      (insert-hankintakoulutus-opiskeluoikeus!
        opiskeluoikeus-oid oppija-oid hankintakoulutus))))

(defn- get-opiskeluoikeus-tila [opiskeluoikeus]
  (let [opiskeluoikeusjaksot (get-in opiskeluoikeus
                                     [:tila :opiskeluoikeusjaksot])
        latest-jakso (reduce
                       (fn [latest jakso]
                         (if (.isAfter
                               (LocalDate/parse (:alku jakso))
                               (LocalDate/parse (:alku latest)))
                           jakso
                           latest))
                       opiskeluoikeusjaksot)]
    (get-in latest-jakso [:tila :koodiarvo])))

(defn- opiskeluoikeus-tila-inactive? [tila]
  (some #(= tila %) ["valmistunut"
                     "eronnut"
                     "katsotaaneronneeksi"]))

(defn opiskeluoikeus-still-active?
  "Checks if the given opiskeluoikeus is still valid, ie. not valmistunut,
  eronnut, katsotaaneronneeksi.
  Alternatively checks from the list of all opiskeluoikeudet held by the oppija
  that the opiskeluoikeus associated with the hoks is still valid."
  ([opiskeluoikeus-oid]
    (if (:prevent-finished-opiskeluoikeus-updates? config)
      (let [opiskeluoikeus (k/get-opiskeluoikeus-info opiskeluoikeus-oid)]
        (if-not (opiskeluoikeus-tila-inactive?
                  (get-opiskeluoikeus-tila opiskeluoikeus))
          true
          false))
      true))
  ([hoks opiskeluoikeudet]
    (if (:prevent-finished-opiskeluoikeus-updates? config)
      (let [opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
            opiskeluoikeus (reduce
                             (fn [active oo]
                               (if (= (:oid oo) opiskeluoikeus-oid)
                                 oo
                                 active))
                             opiskeluoikeudet)]
        (if-not (opiskeluoikeus-tila-inactive?
                  (get-opiskeluoikeus-tila opiskeluoikeus))
          true
          false))
      true)))
