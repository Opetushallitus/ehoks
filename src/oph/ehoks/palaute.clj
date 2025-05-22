(ns oph.ehoks.palaute
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [hugsql.core :as hugsql]
            [medley.core :refer [assoc-some find-first greatest map-vals]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.hoks.osaamisen-hankkimistapa :as oht]
            [oph.ehoks.opiskeluoikeus :as opiskeluoikeus]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.palaute.tapahtuma :as tapahtuma]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.utils.date :as date])
  (:import [java.time LocalDate]))

(hugsql/def-db-fns "oph/ehoks/db/sql/palaute.sql")

(defn unhandled?
  [palaute]
  {:pre [(:tila palaute)]}
  (#{"odottaa_kasittelya" "ei_laheteta"} (:tila palaute)))

(defn nil-or-unhandled?
  [palaute]
  (or (nil? palaute) (unhandled? palaute)))

(defn current-rahoituskausi-alkupvm
  ^LocalDate []
  (let [current-year            (.getYear (date/now))
        ^int rahoituskausi-year (if (< (.getMonthValue (date/now)) 7)
                                  (dec current-year)
                                  current-year)]
    (LocalDate/of rahoituskausi-year 7 1)))

(defn valid-herate-date?
  "onko herätteen päivämäärä aikaisintaan kuluvan rahoituskauden alkupvm
  (1.7.)?"
  [^LocalDate herate-date]
  (not (.isAfter (current-rahoituskausi-alkupvm) herate-date)))

(defn koulutustoimija-oid!
  "Hakee koulutustoimijan OID:n opiskeluoikeudesta, tai organisaatiopalvelusta
  jos sitä ei löydy opiskeluoikeudesta."
  [opiskeluoikeus]
  (if-not opiskeluoikeus
    (log/warn "Ei opiskeluoikeutta, ei haeta koulutustoimijaa")
    (or (:oid (:koulutustoimija opiskeluoikeus))
        (do
          (log/info "Ei koulutustoimijaa opiskeluoikeudessa "
                    (:oid opiskeluoikeus) ", haetaan Organisaatiopalvelusta")
          (some-> (get-in opiskeluoikeus [:oppilaitos :oid])
                  (organisaatio/get-organisaatio!)
                  :parentOid)))))

(defn toimipiste-oid!
  "Palauttaa toimipisteen OID jos sen organisaatiotyyppi on toimipiste. Tämä
  tarkistetaan tekemällä request organisaatiopalveluun. Jos organisaatiotyyppi
  ei ole toimipiste, palauttaa nil."
  [suoritus]
  (let [oid          (:oid (:toimipiste suoritus))
        organisaatio (organisaatio/get-organisaatio! oid)
        org-tyypit   (:tyypit organisaatio)]
    (when (some #{"organisaatiotyyppi_03"} org-tyypit)
      oid)))

(defn hankintakoulutuksen-toteuttaja!
  "Hakee hankintakoulutuksen toteuttajan OID:n eHOKS-palvelusta ja Koskesta."
  [hoks]
  (let [hoks-id (:id hoks)
        oids    (oppijaindex/get-hankintakoulutus-oids-by-master-oid
                  (:opiskeluoikeus-oid hoks))]
    (when (not-empty oids)
      (if (> (count oids) 1)
        (log/warn "Enemmän kuin yksi linkitetty opiskeluoikeus! HOKS-id:"
                  hoks-id)
        (let [opiskeluoikeus (koski/get-opiskeluoikeus! (first oids))
              toteuttaja-oid (get-in opiskeluoikeus [:koulutustoimija :oid])]
          (log/infof "Hoks `%d`, hankintakoulutuksen toteuttaja: %s"
                     hoks-id
                     toteuttaja-oid)
          toteuttaja-oid)))))

(defn vastaamisajan-loppupvm
  "Laskee vastausajan loppupäivämäärän: 30 päivän päästä (inklusiivisesti),
  mutta ei myöhempi kuin 60 päivää (inklusiivisesti) herätepäivän jälkeen."
  [^LocalDate heratepvm ^LocalDate alkupvm]
  (let [last   (.plusDays heratepvm 59)
        normal (.plusDays alkupvm 29)]
    (if (.isBefore last normal) last normal)))

(def ^:private koski-suoritustyyppi->kyselytyyppi
  {"ammatillinentutkinto"           "valmistuneet"
   "ammatillinentutkintoosittainen" "osia_suorittaneet"})

(defn kyselytyyppi
  [tyyppi opiskeluoikeus]
  (case tyyppi
    :aloituskysely "aloittaneet"
    :paattokysely  (-> (find-first suoritus/ammatillinen?
                                   (:suoritukset opiskeluoikeus))
                       (suoritus/tyyppi)
                       (koski-suoritustyyppi->kyselytyyppi)
                       (or "valmistuneet"))
    :ohjaajakysely "tyopaikkajakson_suorittaneet"))

(def translate-kyselytyyppi
  "Translate kyselytyyppi name to the equivalent one used in Herätepalvelu,
  i.e., `lhs` is the one used in eHOKS and `rhs` is the one used Herätepalvelu.
  This should not be needed when eHOKS-Herätepalvelu integration is done."
  {"aloittaneet"       "aloittaneet"
   "valmistuneet"      "tutkinnon_suorittaneet"
   "osia_suorittaneet" "tutkinnon_osia_suorittaneet"})

(def herate-date-basis {:aloituskysely :ensikertainen-hyvaksyminen
                        :paattokysely  :osaamisen-saavuttamisen-pvm})

(defn next-niputus-date
  "Palauttaa seuraavan niputuspäivämäärän annetun päivämäärän jälkeen.
  Niputuspäivämäärät ovat kuun ensimmäinen ja kuudestoista päivä."
  ^LocalDate [^LocalDate pvm]
  (let [year  (.getYear pvm)
        month (.getMonthValue pvm)
        day   (.getDayOfMonth pvm)]
    (if (< day 16)
      (LocalDate/of year month 16)
      (if (= 12 month)
        (LocalDate/of (inc year) 1 1)
        (LocalDate/of year (inc month) 1)))))

(defn- heratepvm
  "Returns the heratepvm for the palaute type."
  [{:keys [hoks jakso ::type] :as ctx}]
  {:post [(some? %)]}
  (if (= type :ohjaajakysely)
    (:loppu jakso)
    (get hoks (herate-date-basis type))))

(defn- alkupvm
  [kysely-type heratepvm]
  {:post [(some? %)]}
  (if (= kysely-type :ohjaajakysely)
    (next-niputus-date heratepvm)
    (greatest heratepvm (date/now))))

(defn build!
  "Builds a palaute to be inserted to DB."
  [{:keys [hoks jakso opiskeluoikeus koulutustoimija existing-palaute ::type]
    :as ctx}
   tila]
  {:pre [(some? tila) (nil-or-unhandled? existing-palaute)]}
  (let [heratepvm (heratepvm ctx)
        alkupvm   (alkupvm type heratepvm)
        suoritus  (find-first suoritus/ammatillinen?
                              (:suoritukset opiskeluoikeus))]
    (assoc-some
      {:hoks-id                        (:id hoks)
       :kyselytyyppi                   (kyselytyyppi type opiskeluoikeus)
       :heratepvm                      heratepvm
       :voimassa-alkupvm               alkupvm
       :voimassa-loppupvm              (vastaamisajan-loppupvm
                                         heratepvm alkupvm)
       :tila                           (utils/to-underscore-str tila)
       :suorituskieli                  (suoritus/kieli suoritus)
       :koulutustoimija                (or koulutustoimija
                                           (koulutustoimija-oid!
                                             opiskeluoikeus))
       :toimipiste-oid                 (toimipiste-oid! suoritus)
       :tutkintonimike                 (suoritus/tutkintonimike suoritus)
       :tutkintotunnus                 (suoritus/tutkintotunnus suoritus)
       :hankintakoulutuksen-toteuttaja (hankintakoulutuksen-toteuttaja! hoks)
       :herate-source                  "ehoks_update"
       :jakson-yksiloiva-tunniste      (:yksiloiva-tunniste jakso)}
      :id
      (:id existing-palaute))))

(defn upsert!
  "Insert `palaute` to DB or update it. If `palaute` contains key `:id`, it will
  be updated. Otherwise it will be inserted. Returns the `:id` of the
  inserted / updated `palaute`."
  [tx palaute]
  (let [db-handler (if (:id palaute) update! insert!)]
    (db-handler tx palaute)))

(defn update-tila!
  [{:keys [existing-palaute] :as ctx} tila reason lisatiedot]
  (jdbc/with-db-transaction
    [tx db/spec]
    (update! tx {:id (:id existing-palaute) :tila tila})
    (tapahtuma/build-and-insert! ctx tila reason lisatiedot)))

(defn feedback-collecting-prevented?
  "Jätetäänkö palaute keräämättä sen vuoksi, että opiskelijan opiskelu on
  tällä hetkellä rahoitettu muilla rahoituslähteillä?"
  [opiskeluoikeus heratepvm]
  (-> opiskeluoikeus
      (opiskeluoikeus/get-opiskeluoikeusjakso-for-date (str heratepvm))
      (get-in [:opintojenRahoitus :koodiarvo])
      #{"6" "14" "15"}
      (some?)))

(defn rahoituskausi
  "Takes a date `pvm` and returns rahoituskausi it belongs to in a string format
  \"YYYY-YYYY\", e.g., \"2023-2024\"."
  [^LocalDate pvm]
  (when pvm
    (let [year  (.getYear pvm)
          month (.getMonthValue pvm)]
      (if (> month 6)
        (str year "-" (inc year))
        (str (dec year) "-" year)))))

(defn kuuluu-palautteen-kohderyhmaan?
  "Kuuluuko opiskeluoikeus palautteen kohderyhmään?  Tällä hetkellä
  vain katsoo, onko kyseessä TELMA-opiskeluoikeus, joka ei ole tutkintoon
  tähtäävä koulutus (ks. OY-4433).  Muita mahdollisia kriteereitä
  ovat tulevaisuudessa koulutuksen rahoitus ja muut kriteerit, joista
  voidaan katsoa, onko koulutus tutkintoon tähtäävä."
  [opiskeluoikeus]
  (every? (complement suoritus/telma?) (:suoritukset opiskeluoikeus)))

(defn- initial-state-and-reason-if-not-kohderyhma
  "Partial function; returns initial state, field causing it, and why the
  field causes the initial state - but only if the palaute is not to be
  collected because it's not part of kohderyhmä; otherwise returns nil."
  [{:keys [hoks opiskeluoikeus jakso existing-ddb-herate] :as ctx}
   herate-date-field]
  (let [herate-date (get (or jakso hoks) herate-date-field)]
    (cond
      ;; do more efficient checks first

      (not (valid-herate-date? herate-date))
      [:ei-laheteta herate-date-field :eri-rahoituskaudella]

      (hoks/tuva-related? hoks)
      [:ei-laheteta :tuva-opiskeluoikeus-oid :tuva-opiskeluoikeus]

      ;; this is for the transition period with herätepalvelu
      (and existing-ddb-herate (seq @existing-ddb-herate))
      [:heratepalvelussa herate-date-field :heratepalvelun-vastuulla]

      ;; order dependency: :opiskeluoikeus-oid rules should come last

      (not opiskeluoikeus)
      [nil :opiskeluoikeus-oid :ei-loydy]

      (not-any? suoritus/ammatillinen? (:suoritukset opiskeluoikeus))
      [:ei-laheteta :opiskeluoikeus-oid :ei-ammatillinen]

      (not (kuuluu-palautteen-kohderyhmaan? opiskeluoikeus))
      [:ei-laheteta :opiskeluoikeus-oid :ei-ammatillinen]

      (opiskeluoikeus/in-terminal-state? opiskeluoikeus herate-date)
      [:ei-laheteta :opiskeluoikeus-oid :opiskelu-paattynyt]

      (feedback-collecting-prevented? opiskeluoikeus herate-date)
      [:ei-laheteta :opiskeluoikeus-oid :ulkoisesti-rahoitettu]

      (opiskeluoikeus/tuva? opiskeluoikeus)
      [:ei-laheteta :opiskeluoikeus-oid :tuva-opiskeluoikeus]

      (opiskeluoikeus/linked-to-another? opiskeluoikeus)
      [:ei-laheteta :opiskeluoikeus-oid :liittyva-opiskeluoikeus])))

(defn- dispatch-fn
  [ctx]
  {:pre [(::type ctx)]}
  (get {:aloituskysely :opiskelijapalaute
        :paattokysely  :opiskelijapalaute
        :ohjaajakysely :tyoelamapalaute}
       (::type ctx)))

(defmulti existing!
  "Returns an existing palaute if one already exists for palaute type."
  dispatch-fn)

(defmethod existing! :opiskelijapalaute
  [{:keys [tx hoks koulutustoimija ::type] :as ctx}]
  (let [rkausi        (rahoituskausi
                        (get hoks (herate-date-basis type)))
        kyselytyypit  (case type
                        :aloituskysely ["aloittaneet"]
                        :paattokysely  ["valmistuneet" "osia_suorittaneet"])
        params        {:kyselytyypit     kyselytyypit
                       :oppija-oid       (:oppija-oid hoks)
                       :koulutustoimija  koulutustoimija}]
    (->> (get-by-kyselytyyppi-oppija-and-koulutustoimija! tx params)
         (vec)
         (filterv #(= rkausi (rahoituskausi (:heratepvm %))))
         ((fn [existing-palautteet]
            (when (> (count existing-palautteet) 1)
              (log/errorf (str "Found more than one existing herate for "
                               "`%s` of HOKS `%d` in rahoituskausi `%s`.")
                          type
                          (:id hoks)
                          rkausi))
            existing-palautteet))
         first)))

(defmethod existing! :tyoelamapalaute
  [{:keys [hoks jakso tx] :as ctx}]
  (get-by-hoks-id-and-yksiloiva-tunniste!
    tx {:hoks-id            (:id hoks)
        :yksiloiva-tunniste (:yksiloiva-tunniste jakso)}))

(defmulti initial-state-and-reason
  "Runs several checks against HOKS and opiskeluoikeus to determine if
  opiskelijapalautekysely or tyoelamapalaute process for jakso should be
  initiated. Returns the initial state of the palaute (or nil if it cannot be
  formed at all), the field the decision was based on, and the reason for
  picking that state."
  dispatch-fn)

(defmethod initial-state-and-reason :opiskelijapalaute
  [{:keys [hoks existing-palaute ::type] :as ctx}]
  (let [herate-basis (herate-date-basis type)]
    (cond
      (not (nil-or-unhandled? existing-palaute))
      [nil herate-basis :jo-lahetetty]

      (and (:hoks-id existing-palaute)
           (not= (:hoks-id existing-palaute) (:id hoks)))
      [nil :id :ei-palautteen-alkuperainen-hoks]

      (not (get hoks herate-basis))
      [nil herate-basis :ei-ole]

      ;; order dependency: nil rules must come first

      (not (:osaamisen-hankkimisen-tarve hoks))
      [:ei-laheteta :osaamisen-hankkimisen-tarve :ei-ole]

      :else
      (or (initial-state-and-reason-if-not-kohderyhma ctx herate-basis)
          [:odottaa-kasittelya herate-basis :hoks-tallennettu]))))

(defmethod initial-state-and-reason :tyoelamapalaute
  [{:keys [jakso existing-palaute] :as ctx}]
  (cond
    (not (nil-or-unhandled? existing-palaute))
    [nil :yksiloiva-tunniste :jo-lahetetty]

    (nil? jakso)
    [nil :osaamisen-hankkimistapa :poistunut]

    (not (get jakso :loppu))
    [nil :loppu :ei-ole]

    ;; order dependency: nil rules must come first

    (not (oht/palautteenkeruu-allowed-tyopaikkajakso? jakso))
    [:ei-laheteta :tyopaikalla-jarjestettava-koulutus :puuttuva-yhteystieto]

    (not (oht/has-required-osa-aikaisuustieto? jakso))
    [:ei-laheteta :osa-aikaisuustieto :ei-ole]

    (oht/fully-keskeytynyt? jakso)
    [:ei-laheteta :keskeytymisajanjaksot :jakso-keskeytynyt]

    :else
    (or (initial-state-and-reason-if-not-kohderyhma ctx :loppu)
        [:odottaa-kasittelya :loppu :hoks-tallennettu])))

(defmulti enrich-ctx!
  "Add information needed by palaute initiation into context."
  dispatch-fn)

(defmethod enrich-ctx! :opiskelijapalaute
  [{:keys [hoks opiskeluoikeus ::type] :as ctx}]
  (let [koulutustoimija (koulutustoimija-oid! opiskeluoikeus)
        heratepvm (get hoks (herate-date-basis type))
        toimija-oppija (str koulutustoimija "/" (:oppija-oid hoks))
        kyselytyyppi (translate-kyselytyyppi
                       (kyselytyyppi type opiskeluoikeus))
        rahoituskausi (rahoituskausi heratepvm)
        tyyppi-kausi (str kyselytyyppi "/" rahoituskausi)
        ddb-key {:toimija_oppija toimija-oppija :tyyppi_kausi tyyppi-kausi}
        existing-palaute-ctx (assoc ctx :koulutustoimija koulutustoimija)]
    (assoc existing-palaute-ctx
           :existing-ddb-key ddb-key
           :existing-ddb-herate (delay (ddb/get-item! :amis ddb-key))
           :existing-palaute (existing! existing-palaute-ctx))))

(defmethod enrich-ctx! :tyoelamapalaute
  [{:keys [hoks jakso] :as ctx}]
  (assoc ctx
         :existing-ddb-herate
         (delay (ddb/get-jakso-by-hoks-id-and-yksiloiva-tunniste!
                  (:id hoks) (:yksiloiva-tunniste jakso)))
         :existing-palaute (existing! ctx)))

(defn initiate-if-needed!
  "Saves heräte data required for apalautekysely to database and sends it to
  Returns the initial state of kysely if it was created, `nil` otherwise."
  [{:keys [hoks jakso ::type] :as ctx}]
  (jdbc/with-db-transaction
    [tx db/spec {:isolation :serializable}]
    (let [ctx (enrich-ctx! (assoc ctx :tx tx))
          [proposed-state field reason] (initial-state-and-reason ctx)
          state (if (= field :opiskeluoikeus-oid)
                  :odottaa-kasittelya
                  proposed-state)
          lisatiedot (map-vals str (select-keys (merge jakso hoks) [field]))]
      (log/infof
        "Initial state for %s%s of HOKS %d will be %s because of %s in %s"
        type
        (if jakso (str " of jakso " (:yksiloiva-tunniste jakso)) "")
        (:id hoks)
        (or state :ei-luoda-ollenkaan)
        reason
        field)
      (if state
        (->> (build! ctx state)
             (upsert! tx)
             (tapahtuma/build-and-insert! ctx state reason lisatiedot))
        (when (:existing-palaute ctx)
          (tapahtuma/build-and-insert! ctx reason lisatiedot)))
      state)))

(defn initiate-all!
  "Initialise all palautteet (opiskelija & tyoelama) that should be."
  [{:keys [hoks] :as ctx}]
  (try
    (run! initiate-if-needed!
          (concat (map #(assoc ctx ::type %) [:aloituskysely :paattokysely])
                  (map #(assoc ctx ::type :ohjaajakysely :jakso %)
                       (oht/tyopaikkajaksot hoks))))
    (hoks/update! (assoc hoks :palaute-handled-at (date/now)))
    (catch clojure.lang.ExceptionInfo e
      (if (= ::organisaatio/organisation-not-found (:type (ex-data e)))
        (throw (ex-info (str "HOKS contains an unknown organisation"
                             (:organisation-oid (ex-data e)))
                        (assoc (ex-data e) :type ::disallowed-update)))
        (log/error e "exception in heräte initiation with" (ex-data e))))
    (catch Exception e
      (log/error e "exception in heräte initiation"))))

(defn initiate-by-hoks-ids!
  "Call initiate-all-palautteet! for the hokses with hoks-id's"
  [hoks-ids]
  (doseq [hoks-id hoks-ids]
    (log/info "initiate-all-palautteet-for-hoks-ids!: HOKS id" hoks-id)
    (let [hoks (hoks/get-by-id hoks-id)
          opiskeluoikeus (koski/get-opiskeluoikeus! (:opiskeluoikeus-oid hoks))
          ctx {:hoks            hoks
               :opiskeluoikeus  opiskeluoikeus
               ::tapahtuma/type :reinit-palaute}]
      (initiate-all! ctx))))

(defn reinit-for-uninitiated-hokses!
  "Fetch <batchsize> HOKSes from DB that do not have corresponding palaute
  records, and initiate palautteet for them to make sure that their
  palautteet will be handled when they are due (their heratepvm)."
  [batchsize]
  (log/info "reinit-palautteet-for-uninitiated-hokses!: making batch of"
            batchsize "HOKSes")
  (->> {:batchsize batchsize}
       (get-hokses-with-unhandled-palautteet! db/spec)
       (map :id)
       (initiate-by-hoks-ids!)))
