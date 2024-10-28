(ns oph.ehoks.palaute
  (:require [clojure.tools.logging :as log]
            [hugsql.core :as hugsql]
            [medley.core :refer [find-first map-vals]]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.opiskeluoikeus :as opiskeluoikeus]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.palaute.tapahtuma :as palautetapahtuma]
            [oph.ehoks.utils.date :as date])
  (:import [java.time LocalDate]))

(hugsql/def-db-fns "oph/ehoks/db/sql/palaute.sql")

(def unhandled? (comp #{"odottaa_kasittelya" "ei_laheteta"} :tila))

(defn already-initiated?
  "Returns `true` if palautekysely has already been initiated, i.e., there
  already exists a herate for kysely that has already been handled."
  [palaute]
  (not-every? unhandled? (:existing-heratteet palaute)))

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
  (or (:oid (:koulutustoimija opiskeluoikeus))
      (do
        (log/info "Ei koulutustoimijaa opiskeluoikeudessa "
                  (:oid opiskeluoikeus) ", haetaan Organisaatiopalvelusta")
        (:parentOid (organisaatio/get-organisaatio!
                      (get-in opiskeluoikeus [:oppilaitos :oid]))))))

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
    :tyopaikkakysely "tyopaikkajakson_suorittaneet"))

(defn upsert!
  "Add new palaute in the database, or set the values of an already
  created palaute to correspond to the current values from HOKS. Also insert
  palautetapahtuma entry."
  [{:keys [hoks opiskeluoikeus jakso koulutustoimija tx] :as ctx}
   {:keys [type existing-heratteet alkupvm heratepvm state tapahtuma]
    :or {state :odottaa-kasittelya}}]
  (let [suoritus (find-first suoritus/ammatillinen?
                             (:suoritukset opiskeluoikeus))
        updateable-herate (find-first unhandled? existing-heratteet)
        palaute {:id                 (:id updateable-herate)
                 :kyselytyyppi       (kyselytyyppi type opiskeluoikeus)
                 :hoks-id            (:id hoks)
                 :yksiloiva-tunniste (:yksiloiva-tunniste jakso)
                 :tila               (utils/to-underscore-str state)
                 :heratepvm          heratepvm
                 :voimassa-alkupvm   alkupvm
                 :voimassa-loppupvm  (vastaamisajan-loppupvm heratepvm alkupvm)
                 :suorituskieli      (suoritus/kieli suoritus)
                 :koulutustoimija    (or koulutustoimija
                                         (koulutustoimija-oid! opiskeluoikeus))
                 :toimipiste-oid     (toimipiste-oid! suoritus)
                 :tutkintonimike     (suoritus/tutkintonimike suoritus)
                 :tutkintotunnus     (suoritus/tutkintotunnus suoritus)
                 :hankintakoulutuksen-toteuttaja
                 (hankintakoulutuksen-toteuttaja! hoks)
                 :herate-source      "ehoks_update"}]
    (log/info "Upserting palaute"
              ((juxt :hoks-id :kyselytyyppi
                     :yksiloiva-tunniste :heratepvm) palaute)
              "with already existing heratteet"
              (map (juxt :tila :created-at) existing-heratteet)
              "of which we will update" updateable-herate)
    (let [db-handler (if (:id updateable-herate) update! insert!)
          palaute-id (:id (db-handler tx palaute))]
      (palautetapahtuma/insert!
        tx
        {:palaute-id      palaute-id
         :vanha-tila      (or (:tila updateable-herate) (:tila palaute))
         :uusi-tila       (:tila palaute)
         :tapahtumatyyppi "hoks_tallennus"
         :syy             (utils/to-underscore-str (or (:reason tapahtuma)
                                                       :hoks-tallennettu))
         :lisatiedot      (map-vals str (:other-info tapahtuma))}))))

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

(defn initial-palaute-state-and-reason-if-not-kohderyhma
  "Partial function; returns initial state, field causing it, and why the
  field causes the initial state - but only if the palaute is not to be
  collected because it's not part of kohderyhmä; otherwise returns nil."
  [herate-date-field hoks-or-jakso opiskeluoikeus]
  (let [herate-date (get hoks-or-jakso herate-date-field)]
    (cond
      (not opiskeluoikeus)
      [nil :opiskeluoikeus-oid :ei-loydy]

      (not herate-date)
      [nil herate-date-field :ei-ole]

      (not (valid-herate-date? herate-date))
      [:ei-laheteta herate-date-field :eri-rahoituskaudella]

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

(defn save-arvo-tunniste!
  [tx palaute arvo-vastaus lisatiedot]
  (let [new-state
        (if (= (:kyselytyyppi palaute) "tyopaikkajakson_suorittaneet")
          "vastaajatunnus_muodostettu" "kysely_muodostettu")]
    (-> arvo-vastaus
        (assoc :id (:id palaute) :tila new-state)
        (update :url identity)  ; ensure key exists
        (->> (update-arvo-tunniste! tx))
        (assert))
    (palautetapahtuma/insert!
      tx
      {:palaute-id      (:id palaute)
       :vanha-tila      (:tila palaute)
       :uusi-tila       new-state
       :tapahtumatyyppi "arvo_luonti"
       :syy             "arvo_kutsu_onnistui"
       :lisatiedot      lisatiedot})))
