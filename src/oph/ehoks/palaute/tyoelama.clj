(ns oph.ehoks.palaute.tyoelama
  (:require [chime.core :as chime]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [medley.core :refer [find-first]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.db-helpers :as db-helpers]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.organisaatio :as org]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.hoks.common :as c]
            [oph.ehoks.opiskeluoikeus :as opiskeluoikeus]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.utils.date :as date])
  (:import (java.text Normalizer Normalizer$Form)
           (java.time LocalDate)
           (java.lang AutoCloseable)
           (java.util UUID)))

(def kyselytyypit #{"tyopaikkajakson_suorittaneet"})
(def tyopaikkajakso-types
  #{"osaamisenhankkimistapa_koulutussopimus"
    "osaamisenhankkimistapa_oppisopimus"})

(defn tyopaikkajakso?
  "Returns `true` if osaamisen hankkimistapa `oht` is tyopaikkajakso."
  [oht]
  (and (some? (tyopaikkajakso-types (:osaamisen-hankkimistapa-koodi-uri oht)))
       (every?
         #(get-in oht %)
         [[:tyopaikalla-jarjestettava-koulutus :vastuullinen-tyopaikka-ohjaaja]
          [:tyopaikalla-jarjestettava-koulutus :tyopaikan-nimi]
          [:tyopaikalla-jarjestettava-koulutus :tyopaikan-y-tunnus]])))

(defn finished-workplace-periods!
  "Queries for all finished workplace periods between start and end"
  [start end limit]
  (let [hytos (db-hoks/select-paattyneet-tyoelamajaksot "hyto" start end limit)
        hptos (db-hoks/select-paattyneet-tyoelamajaksot "hpto" start end limit)
        hatos (db-hoks/select-paattyneet-tyoelamajaksot "hato" start end limit)]
    (concat hytos hptos hatos)))

(defn tyopaikkajaksot
  "Takes `hoks` as an input and extracts from it all osaamisen hankkimistavat
  that are tyopaikkajaksos. Returns a lazy sequence."
  [hoks]
  (->> (lazy-cat
         (:hankittavat-ammat-tutkinnon-osat hoks)
         (:hankittavat-paikalliset-tutkinnon-osat hoks)
         (mapcat :osa-alueet (:hankittavat-yhteiset-tutkinnon-osat hoks)))
       (mapcat :osaamisen-hankkimistavat)
       (filter tyopaikkajakso?)))

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

(defn voimassa-loppupvm
  "Given `voimassa-alkupvm`, calculates `voimassa-loppupvm`, which is currently
  60 days after `voimassa-alkupvm`."
  [^LocalDate voimassa-alkupvm]
  (.plusDays voimassa-alkupvm 60))

(defn osa-aikaisuus-missing?
  "Puuttuuko tieto osa-aikaisuudesta jaksosta, jossa sen pitäisi olla?"
  [jakso]
  (and (not (:osa-aikaisuustieto jakso))
       (date/is-after (:loppu jakso) (LocalDate/of 2023 6 30))))

(defn fully-keskeytynyt?
  "Palauttaa true, jos TEP-jakso on keskeytynyt sen loppupäivämäärällä."
  [jakso]
  (let [kjaksot (sort-by :alku (:keskeytymisajanjaksot jakso))]
    (when-let [kjakso-loppu (:loppu (last kjaksot))]
      (not (date/is-after (:loppu jakso) kjakso-loppu)))))

(defn jakso-in-the-past?
  [jakso]
  (not (.isBefore (date/now) (:loppu jakso))))

(defn initial-palaute-state-and-reason
  "Runs several checks against tyopaikkajakso and opiskeluoikeus to determine if
  tyoelamapalaute process should be initiated for jakso. Returns the initial
  state of the palaute (or nil if it cannot be formed at all), the field the
  decision was based on, and the reason for picking that state."
  ([jakso hoks opiskeluoikeus]
    (initial-palaute-state-and-reason jakso hoks opiskeluoikeus nil))
  ([jakso hoks opiskeluoikeus existing-herate]
    (cond
      (palaute/already-initiated? (vec existing-herate))
      [nil nil :jaksolle-loytyy-jo-herate]

      (jakso-in-the-past? jakso)
      [:ei-laheteta :loppu :menneisyydessa]

      (opiskeluoikeus/in-terminal-state? opiskeluoikeus (:loppu jakso))
      [:ei-laheteta :opiskeluoikeus-oid :opiskeluoikeus-terminaalitilassa]

      (osa-aikaisuus-missing? jakso)
      [:ei-laheteta nil :osa-aikaisuus-puuttuu]

      (fully-keskeytynyt? jakso)
      [:ei-laheteta nil :tyopaikkajakso-keskeytynyt]

      (not-any? suoritus/ammatillinen? (:suoritukset opiskeluoikeus))
      [:ei-laheteta :opiskeluoikeus-oid :ei-ammatillinen]

      (palaute/feedback-collecting-prevented? opiskeluoikeus (:loppu jakso))
      [:ei-laheteta :opiskeluoikeus-oid :rahoitusperuste]

      (c/tuva-related-hoks? hoks)
      [:ei-laheteta :tuva-opiskeluoikeus-oid :tuva-opiskeluoikeus]

      (opiskeluoikeus/tuva? opiskeluoikeus)
      [:ei-laheteta :opiskeluoikeus-oid :tuva-opiskeluoikeus]

      (opiskeluoikeus/linked-to-another? opiskeluoikeus)
      [:ei-laheteta :opiskeluoikeus-oid :liittyva-opiskeluoikeus]

      :else
      [:odottaa-kasittelya nil :hoks-tallennettu])))

(defn initiate-if-needed!
  [jakso hoks opiskeluoikeus]
  (jdbc/with-db-transaction
    [tx db/spec]
    (let [existing-herate (palaute/get-by-hoks-id-and-yksiloiva-tunniste!
                            tx
                            {:hoks-id            (:id hoks)
                             :yksiloiva-tunniste (:yksiloiva-tunniste jakso)})
          [init-state field reason]
          (initial-palaute-state-and-reason
            jakso hoks opiskeluoikeus existing-herate)]
      (log/infof (str "Initial state for jakso `%s` of HOKS `%d` will be `%s` "
                      "because of `%s` in `%s`.")
                 (:yksiloiva-tunniste jakso)
                 (:id hoks)
                 (or init-state :ei-luoda-ollenkaan)
                 reason
                 field)
      (when init-state
        (let [voimassa-alkupvm (next-niputus-date (:loppu jakso))
              suoritus         (find-first suoritus/ammatillinen?
                                           (:suoritukset opiskeluoikeus))
              koulutustoimija  (palaute/koulutustoimija-oid! opiskeluoikeus)
              toimipiste-oid   (palaute/toimipiste-oid! suoritus)
              other-info       (select-keys hoks [field])]
          (palaute/upsert!
            tx
            {:kyselytyyppi       "tyopaikkajakson_suorittaneet"
             :tila               "odottaa_kasittelya"
             :hoks-id            (:id hoks)
             :yksiloiva-tunniste (:yksiloiva-tunniste jakso)
             :heratepvm          (:loppu jakso)
             :voimassa-alkupvm   voimassa-alkupvm
             :voimassa-loppupvm  (voimassa-loppupvm voimassa-alkupvm)
             :koulutustoimija    koulutustoimija
             :toimipiste-oid     toimipiste-oid
             :tutkintonimike     (suoritus/tutkintonimike suoritus)
             :tutkintotunnus     (suoritus/tutkintotunnus suoritus)
             :herate-source      "ehoks_update"}
            [existing-herate]
            reason
            other-info))))))

(defn initiate-all-uninitiated!
  "Takes a `hoks` and `opiskeluoikeus` and initiates tyoelamapalaute for all
  tyopaikkajaksos in HOKS for which palaute has not been already initiated."
  [hoks opiskeluoikeus]
  (->> (tyopaikkajaksot hoks)
       (map #(initiate-if-needed! % hoks opiskeluoikeus))
       doall))

(defn- deaccent-string
  "Poistaa diakriittiset merkit stringistä ja palauttaa muokatun stringin."
  [utf8-string]
  (string/replace (Normalizer/normalize utf8-string Normalizer$Form/NFD)
                  #"\p{InCombiningDiacriticalMarks}+"
                  ""))

(defn normalize-string
  "Muuttaa muut merkit kuin kirjaimet ja numerot alaviivaksi."
  [string]
  (string/lower-case (string/replace (deaccent-string string) #"\W+" "_")))

(defn opiskeluoikeus-koulutustoimija-oid
  "Hakee koulutustoimijan OID:n opiskeluoikeudesta, tai organisaatiopalvelusta
  jos sitä ei löydy opiskeluoikeudesta."
  [opiskeluoikeus]
  (if-let [koulutustoimija-oid (:oid (:koulutustoimija opiskeluoikeus))]
    koulutustoimija-oid
    (do
      (log/info "Ei koulutustoimijaa opiskeluoikeudessa "
                (:oid opiskeluoikeus) ", haetaan Organisaatiopalvelusta")
      (:parentOid (org/get-organisaatio!
                    (get-in opiskeluoikeus [:oppilaitos :oid]))))))

(defn add-keys
  [tep-palaute opiskeluoikeus request-id tunnus]
  (let [niputuspvm (str (next-niputus-date (date/now)))
        alkupvm (next-niputus-date
                  (LocalDate/parse (:jakso-loppupvm tep-palaute)))
        koulutustoimija (opiskeluoikeus-koulutustoimija-oid opiskeluoikeus)
        oo-suoritus (find-first suoritus/ammatillinen?
                                (:suoritukset opiskeluoikeus))
        tutkinto (get-in oo-suoritus
                         [:koulutusmoduuli :tunniste :koodiarvo])]
    (assoc tep-palaute
           :tallennuspvm (str (date/now))
           :alkupvm (str alkupvm)
           :koulutustoimija koulutustoimija
           :niputuspvm niputuspvm
           :ohjaaja-ytunnus-kj-tutkinto (str (:ohjaaja-nimi tep-palaute) "/"
                                             (:tyopaikan-ytunnus tep-palaute)
                                             "/" koulutustoimija "/" tutkinto)
           :oppilaitos (:oid (:oppilaitos opiskeluoikeus))
           :osaamisala (str (seq (suoritus/get-osaamisalat
                                   oo-suoritus (:oid opiskeluoikeus))))
           :request-id request-id
           :toimipiste-oid (str (org/get-toimipiste oo-suoritus))
           :tpk-niputuspvm "ei_maaritelty"
           :tunnus tunnus
           :tutkinto tutkinto
           :tutkintonimike (str (seq (map :koodiarvo
                                          (:tutkintonimike oo-suoritus))))
           :tyopaikan-normalisoitu-nimi (normalize-string
                                          (:tyopaikan-nimi tep-palaute))
           :viimeinen-vastauspvm (str (.plusDays alkupvm 60)))))

(defn sync-jakso-to-heratepalvelu!
  [tx tep-palaute opiskeluoikeus request-id tunnus]
  (let [query {:jakson-yksiloiva-tunniste
               (:jakson-yksiloiva-tunniste tep-palaute)
               :hoks-id (:hoks-id tep-palaute)}]
    (-> (get-for-heratepalvelu-by-hoks-id-and-yksiloiva-tunniste! tx query)
        (first)
        (not-empty)
        (or (throw (ex-info "palaute not found" query)))
        (add-keys opiskeluoikeus request-id tunnus)
        (dissoc :internal-kyselytyyppi :jakson-yksiloiva-tunniste)
        (db-helpers/remove-nils)
        (ddb/sync-jakso-herate!))))

(defn create-and-save-arvo-vastaajatunnus!
  [tep-palaute tx]
  (let [opiskeluoikeus (koski/get-opiskeluoikeus!
                         (:opiskeluoikeus-oid tep-palaute))]
    (if-not opiskeluoikeus
      (log/warnf "Opiskeluoikeus not found for palaute %d, skipping processing"
                 (:id tep-palaute))
      (let [koulutustoimija (opiskeluoikeus-koulutustoimija-oid opiskeluoikeus)
            alkupvm         (next-niputus-date (:loppupvm tep-palaute))
            request-id      (str (UUID/randomUUID))
            arvo-request    (arvo/build-jaksotunnus-request-body
                              tep-palaute
                              (normalize-string
                                (:tyopaikan-nimi tep-palaute))
                              opiskeluoikeus
                              request-id
                              koulutustoimija
                              (find-first suoritus/ammatillinen?
                                          (:suoritukset opiskeluoikeus))
                              (str alkupvm))
            tunnus          (:tunnus (arvo/create-jaksotunnus arvo-request))]
        (try
          (assert (update-arvo-tunniste! tx {:id (:id tep-palaute)
                                             :tunnus tunnus}))
          ; TODO lisää palaute_tapahtuma EH-1687:n logiikan avulla; aseta
          ;      arvo-request tapahtuman lisätietoihin, jotta mm. request-id
          ;      jää talteen
          (sync-jakso-to-heratepalvelu!
            tx tep-palaute opiskeluoikeus request-id tunnus)
          ; Aseta tep_kasitelty arvoon true jotta herätepalvelu ei yritä
          ; tehdä vastaavaa operaatiota siirtymävaiheen/asennuksien aikana.
          ; FIXME poista tep_kasitelty-logiikka siirtymävaiheen jälkeen?
          (assert
            (update-tep-kasitelty! tx {:tep-kasitelty true
                                       :id (:hankkimistapa-id tep-palaute)}))
          (catch Exception e
            (log/errorf e
                        (str "Error updating palaute %d, "
                             "trying to remove vastaajatunnus from Arvo: %s")
                        (:id tep-palaute)
                        tunnus)
            (arvo/delete-jaksotunnus tunnus)
            (throw e)))))))

(defn create-and-save-arvo-vastaajatunnus-for-all-needed!
  "Creates vastaajatunnus for all herates that are waiting for processing,
  do not have vastaajatunnus and have heratepvm today or in the past."
  [_]
  (log/info "Starting to create vastaajatunnus unprocessed työelämäpalaute.")
  (jdbc/with-db-transaction
    [tx db/spec]
    (doseq [palaute (get-tep-palautteet-needing-vastaajatunnus!
                      tx {:heratepvm (str (date/now))})]
      (create-and-save-arvo-vastaajatunnus! palaute tx)))
  (log/info "Done creating vastaajatunnus for unprocessed työelämäpalaute."))
