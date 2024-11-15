(ns oph.ehoks.palaute.tyoelama
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [medley.core :refer [find-first]]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.organisaatio :as org]
            [oph.ehoks.hoks.common :as c]
            [oph.ehoks.hoks.osaamisen-hankkimistapa :as oht]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.tapahtuma :as palautetapahtuma]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.utils.date :as date])
  (:import (clojure.lang ExceptionInfo)
           (java.text Normalizer Normalizer$Form)
           (java.time LocalDate)
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
  (->> (mapcat :osa-alueet (:hankittavat-yhteiset-tutkinnon-osat hoks))
       (concat (:hankittavat-ammat-tutkinnon-osat hoks)
               (:hankittavat-paikalliset-tutkinnon-osat hoks))
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
  [{:keys [hoks opiskeluoikeus jakso] :as ctx} existing-palaute]
  (or
    (palaute/initial-palaute-state-and-reason-if-not-kohderyhma
      :loppu jakso opiskeluoikeus)
    (cond
      (and (some? existing-palaute) (not (palaute/unhandled? existing-palaute)))
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
      [:odottaa-kasittelya :loppu :hoks-tallennettu])))

(defn build!
  "Builds tyoelamapalaute to be inserted to DB. Uses `palaute/build!` to build
  an initial `palaute` map, then `assoc`s tyoelamapalaute specific values to
  that."
  [{:keys [jakso] :as ctx} tila existing-palaute]
  {:pre [(some? tila)
         (or (nil? existing-palaute) (palaute/unhandled? existing-palaute))]}
  (let [heratepvm (:loppu jakso)
        alkupvm   (next-niputus-date heratepvm)]
    (assoc (palaute/build! ctx tila existing-palaute)
           :kyselytyyppi              "tyopaikkajakson_suorittaneet"
           :jakson-yksiloiva-tunniste (:yksiloiva-tunniste jakso)
           :heratepvm                 heratepvm
           :voimassa-alkupvm          alkupvm
           :voimassa-loppupvm         (palaute/vastaamisajan-loppupvm
                                        heratepvm alkupvm))))

(defn initiate-if-needed!
  [{:keys [hoks] :as ctx} jakso]
  (jdbc/with-db-transaction
    [tx db/spec]
    (let [ctx (assoc ctx :jakso jakso)
          existing-palaute
          (palaute/get-by-hoks-id-and-yksiloiva-tunniste!
            tx {:hoks-id            (:id hoks)
                :yksiloiva-tunniste (:yksiloiva-tunniste jakso)})
          [state field reason]
          (initial-palaute-state-and-reason ctx existing-palaute)]
      (log/info "Initial state for jakso" (:yksiloiva-tunniste jakso)
                "of HOKS" (:id hoks) "will be"
                (or state :ei-luoda-ollenkaan)
                "because of" reason "in" field)
      (when state
        (->> (build! ctx state existing-palaute)
             (palaute/upsert! tx)
             (palautetapahtuma/build ctx state field reason existing-palaute)
             (palautetapahtuma/insert! tx))
        state))))

(defn initiate-all-uninitiated!
  "Takes a `hoks` and `opiskeluoikeus` and initiates tyoelamapalaute for all
  tyopaikkajaksos in HOKS for which palaute has not been already initiated."
  [{:keys [hoks] :as ctx}]
  (run! #(initiate-if-needed! ctx %) (tyopaikkajaksot hoks)))

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
  ;; TODO: check this niputuspvm rule when palaute-backend is
  ;; responsible for niputus
  (let [niputuspvm (str (next-niputus-date (date/now)))
        alkupvm (next-niputus-date
                  (LocalDate/parse (:jakso-loppupvm tep-palaute)))
        koulutustoimija (palaute/koulutustoimija-oid! opiskeluoikeus)
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

;; FIXME: tältä puuttuu yksikkötesti.
;; test-create-and-save-arvo-vastaajatunnus-for-all-needed! sisältää
;; ylimalkaisen testin tälle funktiolle.
(defn sync-jakso-to-heratepalvelu!
  [tx tep-palaute opiskeluoikeus request-id tunnus]
  (let [query (select-keys tep-palaute [:jakson-yksiloiva-tunniste :hoks-id])]
    (-> (palaute/get-for-heratepalvelu-by-hoks-id-and-yksiloiva-tunniste!
          tx query)
        (first)
        (not-empty)
        (or (throw (ex-info "palaute not found" query)))
        (add-keys opiskeluoikeus request-id tunnus)
        (dissoc :internal-kyselytyyppi :jakson-yksiloiva-tunniste)
        (utils/remove-nils)
        (ddb/sync-jakso-herate!))))

(defn create-and-save-arvo-vastaajatunnus!
  [tep-palaute]
  (let [opiskeluoikeus (koski/get-opiskeluoikeus!
                         (:opiskeluoikeus-oid tep-palaute))]
    (if-not opiskeluoikeus
      (log/warnf
        "Opiskeluoikeus not found for palaute %d, skipping processing"
        (:id tep-palaute))  ; FIXME: create tapahtuma and update state
      (let [koulutustoimija (palaute/koulutustoimija-oid! opiskeluoikeus)
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
            arvo-response   (arvo/create-jaksotunnus arvo-request)
            tunnus          (:tunnus arvo-response)]
        (jdbc/with-db-transaction
          [tx db/spec]
          (try
            (palaute/save-arvo-tunniste!
              tx tep-palaute arvo-response {:arvo-response arvo-response})
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
              (log/warn "No vastaajatunnus got from arvo,"
                        "so not marking handled")
              (assert
                (oht/update! tx {:id            (:hankkimistapa-id tep-palaute)
                                 :tep-kasitelty true})))
            tunnus
            (catch ExceptionInfo e
              (log/errorf e
                          (str "Error updating palaute %s, trying to remove "
                               "vastaajatunnus from Arvo: %s")
                          (:id tep-palaute)
                          tunnus)
              ;; FIXME: tapahtuma
              (arvo/delete-jaksotunnus tunnus)
              (throw e))))))))

(defn create-and-save-arvo-vastaajatunnus-for-all-needed!
  "Creates vastaajatunnus for all herates that are waiting for processing,
  do not have vastaajatunnus and have heratepvm today or in the past."
  [_]
  (if-not (contains? (set (:arvo-responsibilities config)) :create-jaksotunnus)
    (log/warn "`create-and-save-arvo-vastaajatunnus-for-all-needed!` "
              "configured not to do anything")
    (do (log/info "Creating vastaajatunnus for unprocessed työelämäpalaute.")
        (->> (palaute/get-tep-palautteet-waiting-for-vastaajatunnus!
               db/spec {:heratepvm (str (date/now))})
             (map (fn [tep-palaute]
                    (try (log/infof "Creating vastaajatunnus for %d"
                                    (:id tep-palaute))
                         (create-and-save-arvo-vastaajatunnus! tep-palaute)
                         (catch ExceptionInfo e
                           (log/errorf e "Error processing tep-palaute %s"
                                       tep-palaute)))))
             doall))))
