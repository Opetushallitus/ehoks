(ns oph.ehoks.oppijaindex
  (:require [clojure.core.memoize :as memo]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as cs]
            [clojure.tools.logging :as log]
            [oph.ehoks.config :refer []]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-opiskeluoikeus]
            [oph.ehoks.db.db-operations.oppija :as db-oppija]
            [oph.ehoks.db.postgresql.common :as db]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [oph.ehoks.opiskeluoikeus :as opiskeluoikeus])
  (:import [java.sql Timestamp]
           [java.time LocalDate]))

(def ^:const opiskeluoikeus-refresh-interval-in-days 180)

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

(defn get-opiskeluoikeus-by-oid!
  "Get opiskeluoikeus by OID"
  [oid & keep-columns]
  (apply db-opiskeluoikeus/select-opiskeluoikeus-by-oid oid keep-columns))

(defn get-existing-opiskeluoikeus-by-oid!
  "Like `get-opiskeluoikeus-by-oid!` but expects that opiskeluoikeus with `oid`
  is found in index. Throws an exception if opiskeluoikeus is not found."
  [oid & keep-columns]
  (if-let [opiskeluoikeus (apply get-opiskeluoikeus-by-oid! oid keep-columns)]
    opiskeluoikeus
    (throw (ex-info (format "Opiskeluoikeus `%s` not in index" oid)
                    {:type               ::opiskeluoikeus-not-found
                     :opiskeluoikeus-oid oid}))))

(defn get-hankintakoulutus-oids-by-master-oid
  "Get hankintakoulutus by master OID"
  [oid]
  (map :oid (db-opiskeluoikeus/select-hankintakoulutus-oids-by-master-oid oid)))

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
  {:oid (:oid opiskeluoikeus)
   :oppija_oid oppija-oid
   :oppilaitos_oid (get-in opiskeluoikeus [:oppilaitos :oid])
   :koulutustoimija_oid (get-in opiskeluoikeus [:koulutustoimija :oid])
   :alkamispaiva (some-> opiskeluoikeus :alkamispäivä LocalDate/parse)
   :arvioitu_paattymispaiva
   (some-> opiskeluoikeus :arvioituPäättymispäivä LocalDate/parse)
   :tutkinto_nimi (get-tutkinto-nimi opiskeluoikeus)
   :osaamisala_nimi (get-osaamisala-nimi opiskeluoikeus)})

(defn- get-opiskeluoikeus-info
  "Get opiskeluoikeus info from Koski and convert to SQL-compatible format"
  [oid oppija-oid]
  (let [opiskeluoikeus (k/get-existing-opiskeluoikeus! oid)]
    (when (opiskeluoikeus/linked-to-another? opiskeluoikeus)
      (throw (ex-info
               (format "Opiskeluoikeus `%s` sisältyy toiseen opiskeluoikeuteen."
                       oid)
               {:type               ::invalid-opiskeluoikeus
                :opiskeluoikeus-oid oid})))
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
  ([oid oppija-oid ^Exception exception]
    (log/errorf
      "Error adding opiskeluoikeus %s of oppija %s: %s"
      oid oppija-oid (.getMessage exception)))
  ([oid oppija-oid ^Exception exception skip-indexing]
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

(defn opiskeluoikeus-information-outdated?!
  "Check whether opiskeluoikeus does not have all information in the database.
  This checks specifically for alkamispäivä, since that was the latest
  addition to the index and so not having it implies that the opiskeluoikeus
  was fetched before the expansion of the schema."
  [oid]
  (let [oo (get-opiskeluoikeus-by-oid! oid :updated_at)]
    (or (empty? oo)
        (nil? (:alkamispaiva oo))
        (.isBefore (.toLocalDate (.toLocalDateTime
                                   ^Timestamp (:updated-at oo)))
                   (.minusDays (LocalDate/now)
                               opiskeluoikeus-refresh-interval-in-days)))))

(defn insert-hankintakoulutus-opiskeluoikeus!
  "Insert hankintakoulutus opiskeluoikeus or update if already exists"
  [opiskeluoikeus-oid oppija-oid hankintakoulutus-opiskeluoikeus]
  (let [jarjestaja-oid (get-in hankintakoulutus-opiskeluoikeus
                               [:sisältyyOpiskeluoikeuteen :oppilaitos :oid])
        hankintakoulutus-opiskeluoikeus-oid
        (get hankintakoulutus-opiskeluoikeus :oid)]
    (try
      (if (opiskeluoikeus-information-outdated?!
            hankintakoulutus-opiskeluoikeus-oid)
        (do
          (db-opiskeluoikeus/delete-opiskeluoikeus-from-index!
            hankintakoulutus-opiskeluoikeus-oid)
          (db-opiskeluoikeus/insert-opiskeluoikeus!
            (assoc
              (opiskeluoikeus-to-sql hankintakoulutus-opiskeluoikeus oppija-oid)
              :hankintakoulutus_jarjestaja_oid jarjestaja-oid
              :hankintakoulutus_opiskeluoikeus_oid opiskeluoikeus-oid)))
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
  (when (opiskeluoikeus-information-outdated?! oid)
    (db-opiskeluoikeus/delete-opiskeluoikeus-from-index! oid)
    (insert-new-opiskeluoikeus-without-error-forwarding! oid oppija-oid)))

(defn add-opiskeluoikeus!
  "Add opiskeluoikeus for oppija, if it doesn't already exist"
  [oid oppija-oid]
  (when (opiskeluoikeus-information-outdated?! oid)
    (db-opiskeluoikeus/delete-opiskeluoikeus-from-index! oid)
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
  ([oid ^Exception exception]
    (log/errorf "Error adding oppija %s: %s" oid (.getMessage exception)))
  ([oid ^Exception exception skip-indexing]
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
  (let [oppija (onr/get-existing-oppija! oid)]
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
      (let [oppija (onr/get-existing-oppija! oid)]
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

(defn set-opiskeluoikeus-koski404
  "Set koski404 field of opiskeluoikeus, indicating that trying to fetch that
  opiskeluoikeus from Koski returns a 404 error"
  [oid]
  (db-opiskeluoikeus/update-opiskeluoikeus! oid {:koski404 true}))

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

(defn add-hoks-dependents-in-index!
  "Adds oppija, opiskeluoikeus and hankintakoulutukset for given HOKS"
  [hoks]
  (let [oppija-oid         (:oppija-oid hoks)
        opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
        opiskeluoikeudet   (k/fetch-opiskeluoikeudet-by-oppija-id oppija-oid)]
    (add-oppija! oppija-oid)
    (add-opiskeluoikeus! opiskeluoikeus-oid oppija-oid)
    (add-oppija-hankintakoulutukset
      opiskeluoikeudet opiskeluoikeus-oid oppija-oid)))

(defn update-oppija-oid-in-db!
  "Change the OID of an oppija to a new one in all tables in the database."
  [old-oid new-oid]
  (log/infof (str "Changing duplicate oppija-oid %s to %s for tables "
                  "hoksit, oppijat and opiskeluoikeudet.") old-oid new-oid)
  (jdbc/with-db-transaction
    [db-conn (db-ops/get-db-connection)]
    (db-hoks/update-hoks-by-oppija-oid! old-oid {:oppija-oid new-oid} db-conn)
    (add-oppija! new-oid)
    (db-opiskeluoikeus/update-opiskeluoikeus-by-oppija-oid!
      old-oid {:oppija-oid new-oid})
    (db-ops/delete! :oppijat ["oid = ?" old-oid])))

(defn handle-onrmodified
  "Handles ONR-modified call from heratepalvelu which is triggered by
  data change in ONR service."
  [oid]
  (let [onr-oppija (:body (onr/find-student-by-oid-no-cache oid))]
    ;; Tarkistetaan, että oppijan nimi on oikein.
    (when-let [indexed-oppija (get-oppija-by-oid oid)]
      (let [indexed-oppija-nimi (:nimi indexed-oppija)
            onr-oppija-nimi (format-oppija-name onr-oppija)]
        (if (= indexed-oppija-nimi onr-oppija-nimi)
          (log/info "Update for" oid "from ONR but name not changed")
          (do (log/infof "Updating changed name for oppija %s" oid)
              (update-oppija! oid true)))))
    ;; Jos OID on henkilön pää-OID (ei duplikaatti eikä yhdistetty toiseen),
    ;; päivitetään mahdollisesti eHOKSista vanhoilla OIDeilla löytyvät tiedot
    ;; uudelle OIDille.
    (if (:duplicate onr-oppija)
      (log/warn "Update for" oid "from ONR but it's marked as duplicate:"
                onr-oppija)
      (let [slaves (:body (onr/get-slaves-of-master-oppija-oid oid))
            indexed-slaves (keep #(:oid (get-oppija-by-oid %))
                                 (map :oidHenkilo slaves))]
        (if (empty? indexed-slaves)
          (log/warn "Update for" oid "from ONR but no updatable oids found in"
                    (map :oidHenkilo slaves))
          (doseq [slave-oid indexed-slaves]
            (update-oppija-oid-in-db! slave-oid oid)))))))
