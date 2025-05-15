(ns oph.ehoks.palaute
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [hugsql.core :as hugsql]
            [medley.core :refer [assoc-some find-first]]
            [oph.ehoks.db :as db]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.hoks :as hoks]
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
    :tyopaikkakysely "tyopaikkajakson_suorittaneet"))

(defn build!
  "A helper function to build palaute, utilized by functions with a similar name
  in `palaute.opiskelija` and `palaute.tyoelama` namespaces. Fetches some of the
  required information from Organisaatiopalvelu and Koski.

  NOTE: This doesn't build a complete palaute that should be inserted to DB but
  only part of it."
  [{:keys [hoks opiskeluoikeus koulutustoimija existing-palaute] :as ctx} tila]
  {:pre [(some? tila) (nil-or-unhandled? existing-palaute)]}
  (let [suoritus (find-first suoritus/ammatillinen?
                             (:suoritukset opiskeluoikeus))]
    (assoc-some
      {:hoks-id                        (:id hoks)
       :tila                           (utils/to-underscore-str tila)
       :suorituskieli                  (suoritus/kieli suoritus)
       :koulutustoimija                (or koulutustoimija
                                           (koulutustoimija-oid!
                                             opiskeluoikeus))
       :toimipiste-oid                 (toimipiste-oid! suoritus)
       :tutkintonimike                 (suoritus/tutkintonimike suoritus)
       :tutkintotunnus                 (suoritus/tutkintotunnus suoritus)
       :hankintakoulutuksen-toteuttaja (hankintakoulutuksen-toteuttaja! hoks)
       :herate-source                  "ehoks_update"}
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

(defn initial-palaute-state-and-reason-if-not-kohderyhma
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
