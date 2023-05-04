(ns oph.ehoks.oppijaindex
  (:require [clojure.core.memoize :as memo]
            [clojure.string :as cs]
            [clojure.tools.logging :as log]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-opiskeluoikeus]
            [oph.ehoks.db.db-operations.oppija :as db-oppija]
            [oph.ehoks.db.postgresql.common :as db]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.external.oppijanumerorekisteri :as onr])
  (:import [java.time LocalDate]))

(defn- field-matcher
  "create a SQL ILIKE pattern from a search value of a field"
  [field-search]
  (when field-search (str "%" (re-find #"[^%_]+" field-search) "%")))

(defn- nimi-matcher
  "create an SQL array of SQL ILIKE patterns from a search value for name"
  [name-search]
  (when name-search
    (str "{" (cs/join "," (map field-matcher
                               (cs/split name-search #"[\s_%,]+"))) "}")))

(defn search!
  "Search oppijat with given params"
  [params]
  (let [nimi-ilike (nimi-matcher (:nimi params))
        tutkinto-ilike (field-matcher (:tutkinto params))
        osaamisala-ilike (field-matcher (:osaamisala params))
        hoks-id (:hoks-id params)
        lang (:locale params)
        order-by (str (:order-by-column params) "_"
                      (if (:desc params) "desc" "asc"))
        oppijat
        (db-ops/query [queries/select-oppilaitos-oppijat
                       (:oppilaitos-oid params)
                       nimi-ilike nimi-ilike
                       tutkinto-ilike lang tutkinto-ilike
                       osaamisala-ilike lang osaamisala-ilike
                       hoks-id hoks-id
                       order-by lang lang
                       order-by lang lang
                       (:item-count params)
                       (:offset params)]
                      {:row-fn db-ops/from-sql})
        total-count
        (-> [queries/select-oppilaitos-oppijat-search-count
             (:oppilaitos-oid params)
             nimi-ilike nimi-ilike
             tutkinto-ilike lang tutkinto-ilike
             osaamisala-ilike lang osaamisala-ilike
             hoks-id hoks-id]
            (db-ops/query)
            (first)
            :count)]
    [oppijat total-count]))

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

(defn get-oppija-with-oo-oid-by-oid
  "Get oppija by oppija-oid, opiskeluoikeus-oid included."
  [oppija-oid]
  (db-oppija/select-oppija-with-opiskeluoikeus-oid-by-oid oppija-oid))

(defn get-opiskeluoikeus-by-oid
  "Get opiskeluoikeus by OID"
  [oid]
  (db-opiskeluoikeus/select-opiskeluoikeus-by-oid oid))

(defn get-hankintakoulutus-oids-by-master-oid
  "Get hankintakoulutus by master OID"
  [oid]
  (db-opiskeluoikeus/select-hankintakoulutus-oids-by-master-oid oid))

(defn get-oppilaitos-oids
  "Get oppilaitos OIDs, filtering out nils"
  []
  (filter some? (db/select-oppilaitos-oids)))

(def get-oppilaitos-oids-cached
  "Memoized get oppilaitos OIDs"
  (memo/ttl
    get-oppilaitos-oids
    {}
    :ttl/threshold 10000))

(defn get-oppilaitos-oids-by-koulutustoimija-oid
  "Get oppilaitos OIDs by koulutustoimija OID, filtering out nils"
  [koulutustoimija-oid]
  (filter some?
          (db/select-oppilaitos-oids-by-koulutustoimija-oid
            koulutustoimija-oid)))

(defn get-tutkinto-nimi
  "Extract tutkinto nimi from opiskeluoikeus"
  [opiskeluoikeus]
  (get-in
    opiskeluoikeus
    [:suoritukset 0 :koulutusmoduuli :tunniste :nimi]
    {:fi "" :sv ""}))

(defn get-osaamisala-nimi
  "Extract osaamisala nimi from opiskeluoikeus"
  [opiskeluoikeus]
  (or
    (get-in
      opiskeluoikeus
      [:suoritukset 0 :osaamisala 0 :nimi])
    (get-in
      opiskeluoikeus
      [:suoritukset 0 :osaamisala 0 :osaamisala :nimi])
    {:fi "" :sv ""}))

(defn- opiskeluoikeus-to-sql
  "Convert opiskeluoikeus to an object format that can be saved to the database"
  [opiskeluoikeus oppija-oid]
  (let [tutkinto (get-tutkinto-nimi opiskeluoikeus)
        osaamisala (get-osaamisala-nimi opiskeluoikeus)]
    {:oid (:oid opiskeluoikeus)
     :oppija_oid oppija-oid
     :oppilaitos_oid (get-in opiskeluoikeus [:oppilaitos :oid])
     :koulutustoimija_oid (get-in opiskeluoikeus [:koulutustoimija :oid])
     :tutkinto_nimi tutkinto
     :osaamisala_nimi osaamisala}))

(defn- get-opiskeluoikeus-info
  "Get opiskeluoikeus info from Koski and convert to SQL-compatible format"
  [oid oppija-oid]
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
  "Log errors that occur while inserting opiskeluoikeus for oppija"
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

(defn- log-opiskeluoikeus-insert-error-for-indexing!
  "Log errors that occur while inserting opiskeluoikeus for oppija during
  indexing"
  [oid oppija-oid exception]
  (log-opiskeluoikeus-insert-error! oid oppija-oid exception true))

(defn- insert-opiskeluoikeus
  "Insert opiskeluoikeus with given OID into database for oppija"
  [oid oppija-oid]
  (db-opiskeluoikeus/insert-opiskeluoikeus!
    (get-opiskeluoikeus-info oid oppija-oid)))

(defn- opiskeluoikeus-doesnt-exist
  "Check whether opiskeluoikeus is not present in database"
  [oid]
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

(defn- insert-new-opiskeluoikeus-without-error-forwarding!
  "Insert new opiskeluoikeus for oppija without passing errors up the call
  stack"
  [oid oppija-oid]
  (try
    (insert-opiskeluoikeus oid oppija-oid)
    (catch Exception e
      (log-opiskeluoikeus-insert-error-for-indexing! oid oppija-oid e))))

(defn- insert-new-opiskeluoikeus!
  "Insert new opiskeluoikeus for oppija"
  [oid oppija-oid]
  (try
    (insert-opiskeluoikeus oid oppija-oid)
    (catch Exception e
      (log-opiskeluoikeus-insert-error! oid oppija-oid e)
      (throw e))))

(defn add-opiskeluoikeus-without-error-forwarding!
  "Add opiskeluoikeus for oppija if it doesn't already exist, without passing
  errors up the call stack"
  [oid oppija-oid]
  (when (opiskeluoikeus-doesnt-exist oid)
    (insert-new-opiskeluoikeus-without-error-forwarding! oid oppija-oid)))

(defn add-opiskeluoikeus!
  "Add opiskeluoikeus for oppija, if it doesn't already exist"
  [oid oppija-oid]
  (when (opiskeluoikeus-doesnt-exist oid)
    (insert-new-opiskeluoikeus! oid oppija-oid)))

(defn update-opiskeluoikeus-without-error-forwarding!
  "Update opsikeluoikeus for oppija without passing errors up the call stack"
  [oid oppija-oid]
  (try
    (db-opiskeluoikeus/update-opiskeluoikeus!
      oid
      (dissoc (get-opiskeluoikeus-info oid oppija-oid) :oid :oppija_oid))
    (catch Exception e
      (log/errorf
        "Skipped indexing. Error updating opiskeluoikeus %s of oppija %s: %s"
        oid oppija-oid (.getMessage e)))))

(defn- log-opiskelija-insert-error!
  "Log errors that occur when inserting a student"
  ([oid exception]
    (log/errorf "Error adding oppija %s: %s" oid (.getMessage exception)))
  ([oid exception skip-indexing]
    (log/errorf "%sError adding oppija %s: %s"
                (if skip-indexing
                  "Skipped indexing. "
                  "")
                oid (.getMessage exception))))

(defn- log-opiskelija-insert-error-for-indexing!
  "Log errors that occur when inserting a student during indexing"
  [oid exception]
  (log-opiskelija-insert-error! oid exception true))

(defn- insert-oppija!
  "Insert student into database"
  [oid]
  (let [oppija (:body (onr/find-student-by-oid oid))]
    (db-oppija/insert-oppija!
      {:oid oid
       :nimi (format "%s %s" (:etunimet oppija) (:sukunimi oppija))})))

(defn- insert-new-oppija-without-error-forwarding!
  "Insert new student into database without passing errors up the call stack"
  [oid]
  (try
    (insert-oppija! oid)
    (catch Exception e
      (log-opiskelija-insert-error-for-indexing! oid e))))

(defn- insert-new-oppija!
  "Insert new student into database"
  [oid]
  (try
    (insert-oppija! oid)
    (catch Exception e
      (log-opiskelija-insert-error! oid e)
      (throw e))))

(defn- oppija-doesnt-exist
  "Check that student doesn't already exist in database"
  [oid]
  (empty? (get-oppija-by-oid oid)))

(defn add-oppija-without-error-forwarding!
  "Add student if a student with the same ID doesn't already exist, without
  passing errors up the call stack"
  [oid]
  (when (oppija-doesnt-exist oid)
    (insert-new-oppija-without-error-forwarding! oid)))

(defn add-oppija!
  "Add student if a student with the same ID doesn't already exist"
  [oid]
  (when (oppija-doesnt-exist oid)
    (insert-new-oppija! oid)))

(defn format-oppija-name
  "Formats oppija name from fields etunimet and sukunimi"
  [oppija]
  (format "%s %s" (:etunimet oppija) (:sukunimi oppija)))

(defn update-oppija!
  "Update existing student in database. Adding 2nd param skips cache."
  ([oid]
    (try
      (let [oppija (:body (onr/find-student-by-oid oid))]
        (db-oppija/update-oppija!
          oid
          {:nimi (format-oppija-name oppija)}))
      (catch Exception e
        (log/errorf "Error updating oppija %s" oid)
        (throw e))))
  ([oid _]
    (try
      (let [oppija (:body (onr/find-student-by-oid-no-cache oid))]
        (db-oppija/update-oppija!
          oid
          {:nimi (format-oppija-name oppija)}))
      (catch Exception e
        (log/errorf "Error updating oppija %s" oid)
        (throw e)))))

(defn update-oppijat-without-index!
  "Update students without indexes in database"
  []
  (log/info "Start indexing oppijat")
  (doseq [{oid :oppija_oid} (get-oppijat-without-index)]
    (add-oppija-without-error-forwarding! oid))
  (log/info "Indexing oppijat finished"))

(defn update-opiskeluoikeudet-without-index!
  "Update opiskeluoikeudet without indexes in database"
  []
  (log/info "Start indexing opiskeluoikeudet")
  (doseq [{oid :opiskeluoikeus_oid oppija-oid :oppija_oid}
          (get-opiskeluoikeudet-without-index)]
    (add-opiskeluoikeus-without-error-forwarding! oid oppija-oid))
  (log/info "Indexing opiskeluoikeudet finished"))

(defn update-opiskeluoikeudet-without-tutkinto!
  "Update opiskeluoikeudet without tutkinnot in database"
  []
  (log/info "Start indexing opiskeluoikeudet without tutkinto")
  (doseq [{oid :oid oppija-oid :oppija_oid}
          (get-opiskeluoikeudet-without-tutkinto)]
    (update-opiskeluoikeus-without-error-forwarding! oid oppija-oid))
  (log/info "Indexing opiskeluoikeudet finished"))

(defn set-opiskeluoikeus-paattynyt!
  "Set opiskeluoikeus as finished (päättynyt) as of a particular timestamp"
  [oid timestamp]
  (db-opiskeluoikeus/update-opiskeluoikeus! oid {:paattynyt timestamp}))

(defn set-opiskeluoikeus-koski404
  "Set koski404 field of opiskeluoikeus, indicating that trying to fetch that
  opiskeluoikeus from Koski returns a 404 error"
  [oid]
  (db-opiskeluoikeus/update-opiskeluoikeus! oid {:koski404 true}))

(defn oppija-opiskeluoikeus-match?
  "Check that opiskeluoikeus belongs to oppija"
  [opiskeluoikeudet opiskeluoikeus-oid]
  (if (:enforce-opiskeluoikeus-match? config)
    (some #(= opiskeluoikeus-oid (:oid %)) opiskeluoikeudet)
    true))

(defn filter-hankintakoulutukset-for-current-opiskeluoikeus
  "Filters hankintakoulutukset from opiskeluoikeudet for current opiskeluoikeus"
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

(defn get-opiskeluoikeus-tila
  "Extract tila from opiskeluoikeus"
  [opiskeluoikeus]
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

(defn opiskeluoikeus-tila-inactive?
  "Check whether opiskeluoikeus tila is inactive"
  [tila]
  (some #(= tila %) ["valmistunut"
                     "eronnut"
                     "katsotaaneronneeksi"
                     "peruutettu"]))

(defn opiskeluoikeus-active?
  "Checks if the given opiskeluoikeus is still valid, ie. not valmistunut,
  eronnut, katsotaaneronneeksi."
  [opiskeluoikeus]
  (if (some? opiskeluoikeus)
    (not (opiskeluoikeus-tila-inactive?
           (get-opiskeluoikeus-tila opiskeluoikeus)))
    false))

(defn opiskeluoikeus-still-active?
  "Checks if the given opiskeluoikeus is still valid, ie. not valmistunut,
  eronnut, katsotaaneronneeksi.
  Alternatively checks from the list of all opiskeluoikeudet held by the oppija
  that the opiskeluoikeus associated with the hoks is still valid."
  ([opiskeluoikeus-oid]
    (if (:prevent-finished-opiskeluoikeus-updates? config)
      (let [opiskeluoikeus (k/get-opiskeluoikeus-info opiskeluoikeus-oid)]
        (not (opiskeluoikeus-tila-inactive?
               (get-opiskeluoikeus-tila opiskeluoikeus))))
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
