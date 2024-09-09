(ns oph.ehoks.palaute.tyoelama
  (:require [chime.core :as chime]
            [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as string]
            [medley.core :refer [find-first]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.db-helpers :as db-helpers]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.organisaatio :as org]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.hoks.common :as c]
            [oph.ehoks.hoks.osaamisen-hankkimistapa :as oht]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.tapahtuma :as palautetapahtuma]
            [oph.ehoks.utils.date :as date])
  (:import (clojure.lang ExceptionInfo)
           (java.text Normalizer Normalizer$Form)
           (java.time LocalDate)
           (java.lang AutoCloseable)
           (java.util UUID)))

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
  (->> (concat
         (:hankittavat-ammat-tutkinnon-osat hoks)
         (:hankittavat-paikalliset-tutkinnon-osat hoks)
         (mapcat :osa-alueet (:hankittavat-yhteiset-tutkinnon-osat hoks)))
       (mapcat :osaamisen-hankkimistavat)
       (filter oht/tyopaikkajakso?)))

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

(defn fully-keskeytynyt?
  "Palauttaa true, jos TEP-jakso on keskeytynyt sen loppupäivämäärällä."
  [tyoelamajakso]
  (some (fn [k-jakso]
          (and (:loppu tyoelamajakso)
               (date/is-same-or-before (:alku k-jakso) (:loppu tyoelamajakso))
               (or (not (:loppu k-jakso))
                   (date/is-same-or-before (:loppu tyoelamajakso)
                                           (:loppu k-jakso)))))
        (:keskeytymisajanjaksot tyoelamajakso)))

(defn initial-palaute-state-and-reason
  "Runs several checks against tyopaikkajakso and opiskeluoikeus to determine if
  tyoelamapalaute process should be initiated for jakso. Returns the initial
  state of the palaute (or nil if it cannot be formed at all), the field the
  decision was based on, and the reason for picking that state."
  [jakso hoks opiskeluoikeus existing-heratteet]
  (or
    (palaute/initial-palaute-state-and-reason-if-not-kohderyhma
      :loppu jakso opiskeluoikeus)
    (cond
      (palaute/already-initiated? existing-heratteet)
      [nil :yksiloiva-tunniste :jo-lahetetty]

      (not (oht/palautteenkeruu-allowed-tyopaikkajakso? jakso))
      [:ei-laheteta :tyopaikalla-jarjestettava-koulutus :puuttuva-yhteystieto]

      (not (oht/has-required-osa-aikaisuustieto? jakso))
      [:ei-laheteta :osa-aikaisuustieto :ei-ole]

      (fully-keskeytynyt? jakso)
      [:ei-laheteta :keskeytymisajanjaksot :jakso-keskeytynyt]

      (c/tuva-related-hoks? hoks)
      [:ei-laheteta :tuva-opiskeluoikeus-oid :tuva-opiskeluoikeus]

      :else
      [:odottaa-kasittelya nil :hoks-tallennettu])))

(defn initiate-if-needed!
  [jakso hoks opiskeluoikeus]
  (jdbc/with-db-transaction
    [tx db/spec]
    (let [existing-heratteet  ; always 0 or 1 herate
          (palaute/get-by-hoks-id-and-yksiloiva-tunniste!
            tx
            {:hoks-id            (:id hoks)
             :yksiloiva-tunniste (:yksiloiva-tunniste jakso)})
          [init-state field reason]
          (initial-palaute-state-and-reason
            jakso hoks opiskeluoikeus existing-heratteet)]
      (log/info "Initial state for jakso" (:yksiloiva-tunniste jakso)
                "of HOKS" (:id hoks) "will be"
                (or init-state :ei-luoda-ollenkaan)
                "because of" reason "in" field)
      (when init-state
        (let [voimassa-alkupvm (next-niputus-date (:loppu jakso))
              suoritus         (find-first suoritus/ammatillinen?
                                           (:suoritukset opiskeluoikeus))
              koulutustoimija  (palaute/koulutustoimija-oid! opiskeluoikeus)
              toimipiste-oid   (palaute/toimipiste-oid! suoritus)
              heratepvm        (:loppu jakso)
              other-info       (select-keys (merge jakso hoks) [field])]
          (palaute/upsert!
            tx
            {:kyselytyyppi       "tyopaikkajakson_suorittaneet"
             :tila               "odottaa_kasittelya"
             :hoks-id            (:id hoks)
             :yksiloiva-tunniste (:yksiloiva-tunniste jakso)
             :heratepvm          heratepvm
             :voimassa-alkupvm   voimassa-alkupvm
             :voimassa-loppupvm  (palaute/vastaamisajan-loppupvm
                                   heratepvm voimassa-alkupvm)
             :koulutustoimija    koulutustoimija
             :toimipiste-oid     toimipiste-oid
             :tutkintonimike     (suoritus/tutkintonimike suoritus)
             :tutkintotunnus     (suoritus/tutkintotunnus suoritus)
             :herate-source      "ehoks_update"}
            existing-heratteet
            reason
            other-info))))))

(defn initiate-all-uninitiated!
  "Takes a `hoks` and `opiskeluoikeus` and initiates tyoelamapalaute for all
  tyopaikkajaksos in HOKS for which palaute has not been already initiated."
  [hoks opiskeluoikeus]
  (run! #(initiate-if-needed! % hoks opiskeluoikeus) (tyopaikkajaksot hoks)))

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
  (or (:oid (:koulutustoimija opiskeluoikeus))
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
           :ohjaaja-ytunnus-kj-tutkinto (str
                                          (:vastuullinen-tyopaikka-ohjaaja-nimi
                                            tep-palaute) "/"
                                          (:tyopaikan-y-tunnus tep-palaute) "/"
                                          koulutustoimija "/" tutkinto)
           :oppilaitos (:oid (:oppilaitos opiskeluoikeus))
           :osaamisala (str (seq (suoritus/get-osaamisalat
                                   oo-suoritus (:oid opiskeluoikeus)
                                   (:heratepvm tep-palaute))))
           :request-id request-id
           :toimipiste-oid (str (palaute/toimipiste-oid! oo-suoritus))
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
    (-> (palaute/get-for-heratepalvelu-by-hoks-id-and-yksiloiva-tunniste!
          tx query)
        (first)
        (not-empty)
        (or (throw (ex-info "palaute not found" query)))
        (add-keys opiskeluoikeus request-id tunnus)
        (dissoc :internal-kyselytyyppi :jakson-yksiloiva-tunniste)
        (db-helpers/remove-nils)
        (ddb/sync-jakso-herate!))))

(defn save-arvo-tunniste!
  [tx tep-palaute tunnus lisatiedot]
  (assert (palaute/update-arvo-tunniste! tx {:id (:id tep-palaute)
                                             :tunnus tunnus}))
  (palautetapahtuma/insert!
    tx
    {:palaute-id      (:id tep-palaute)
     :vanha-tila      (:tila tep-palaute)
     :uusi-tila       "vastaajatunnus_muodostettu"
     :tapahtumatyyppi "arvo_luonti"
     :syy             (db-helpers/to-underscore-str
                        :vastaajatunnus-muodostettu)
     :lisatiedot      lisatiedot}))

(defn create-and-save-arvo-vastaajatunnus!
  [tx tep-palaute]
  (let [opiskeluoikeus (koski/get-opiskeluoikeus!
                         (:opiskeluoikeus-oid tep-palaute))]
    (if-not opiskeluoikeus
      (log/warnf
        "Opiskeluoikeus not found for palaute %d, skipping processing"
        (:id tep-palaute))
      (let [koulutustoimija (opiskeluoikeus-koulutustoimija-oid
                              opiskeluoikeus)
            alkupvm         (next-niputus-date (:loppupvm tep-palaute))
            request-id      (str (UUID/randomUUID))
            suoritus        (find-first suoritus/ammatillinen?
                                        (:suoritukset opiskeluoikeus))
            arvo-request    (arvo/build-jaksotunnus-request-body
                              tep-palaute
                              (normalize-string
                                (:tyopaikan-nimi tep-palaute))
                              opiskeluoikeus
                              request-id
                              koulutustoimija
                              (palaute/toimipiste-oid! suoritus)
                              suoritus
                              (str alkupvm))
            tunnus          (:tunnus (arvo/create-jaksotunnus
                                       arvo-request))]
        (try
          (save-arvo-tunniste!
            tx tep-palaute tunnus {:arvo-request arvo-request})
          (sync-jakso-to-heratepalvelu!
            tx tep-palaute opiskeluoikeus request-id tunnus)

          ; TODO lisää nipputunniste palautteet-tauluun (uusi kantamigraatio)
          ; TODO päättele jaksosta nipputunniste
          ; TODO lisää uusi nippu, jos sitä ei löydy nipputunnisteella kannasta
          ; TODO synkkaa uusi nippu herätepalveluun

          ; Aseta tep_kasitelty arvoon true jotta herätepalvelu ei yritä
          ; tehdä vastaavaa operaatiota siirtymävaiheen/asennuksien aikana.
          ; FIXME poista tep_kasitelty-logiikka siirtymävaiheen jälkeen?
          (if-not tunnus
            (log/warn "No vastaajatunnus got from arvo, so not marking handled")
            (assert
              (palaute/update-tep-kasitelty!
                tx {:tep-kasitelty true :id (:hankkimistapa-id tep-palaute)})))
          tunnus
          (catch ExceptionInfo e
            (log/errorf e
                        (str "Error updating palaute %d, trying to remove "
                             "vastaajatunnus from Arvo: %s")
                        (:id tep-palaute)
                        tunnus)
            (arvo/delete-jaksotunnus tunnus)
            (throw e)))))))

(defn create-and-save-arvo-vastaajatunnus-for-all-needed!
  "Creates vastaajatunnus for all herates that are waiting for processing,
  do not have vastaajatunnus and have heratepvm today or in the past."
  [_]
  (log/info
    "Creating vastaajatunnus for unprocessed työelämäpalaute.")
  (->> (palaute/get-tep-palautteet-waiting-for-vastaajatunnus!
         db/spec {:heratepvm (str (date/now))})
       (map (fn [tep-palaute]
              (try
                (jdbc/with-db-transaction
                  [tx db/spec]
                  (log/infof "Creating vastaajatunnus for %d"
                             (:id tep-palaute))
                  (create-and-save-arvo-vastaajatunnus! tx tep-palaute))
                (catch ExceptionInfo e
                  (log/errorf e
                              "Error processing tep-palaute %s"
                              tep-palaute)))))
       doall))
