(ns oph.ehoks.palaute
  (:require [clojure.tools.logging :as log]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.utils.date :as date])
  (:import [java.time LocalDate]))

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
  (if-let [koulutustoimija-oid (:oid (:koulutustoimija opiskeluoikeus))]
    koulutustoimija-oid
    (:parentOid (organisaatio/get-existing-organisaatio!
                  (get-in opiskeluoikeus [:oppilaitos :oid])))))

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

(defn vastaamisajan-alkupvm
  "Laskee vastausajan alkupäivämäärän: annettu päivämäärä jos se on vielä
  tulevaisuudessa; muuten tämä päivä."
  [^LocalDate herate-date]
  (let [now (date/now)]
    (if (.isAfter herate-date now)
      herate-date
      now)))

(defn vastaamisajan-loppupvm
  "Laskee vastausajan loppupäivämäärän: 30 päivän päästä (inklusiivisesti),
  mutta ei myöhempi kuin 60 päivää (inklusiivisesti) herätepäivän jälkeen."
  [^LocalDate herate ^LocalDate alku]
  (let [last   (.plusDays herate 59)
        normal (.plusDays alku 29)]
    (if (.isBefore last normal)
      last
      normal)))
