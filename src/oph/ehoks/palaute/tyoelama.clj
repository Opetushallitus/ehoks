(ns oph.ehoks.palaute.tyoelama
  (:require [clojure.tools.logging :as log]
            [medley.core :refer [find-first]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.opiskeluoikeus :as opiskeluoikeus]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.utils.date :as date])
  (:import (java.time LocalDate)))

(def tyopaikkajakso-types
  #{"osaamisenhankkimistapa_koulutussopimus"
    "osaamisenhankkimistapa_oppisopimus"})

(defn tyopaikkajakso?
  "Returns `true` if osaamisen hankkimistapa `oht` is tyopaikkajakso."
  [oht]
  (some? (tyopaikkajakso-types (:osaamisen-hankkimistapa-koodi-uri oht))))

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

(defn initial-palaute-state-and-reason
  "Runs several checks against tyopaikkajakso and opiskeluoikeus to determine if
  tyoelamapalaute process should be initiated for jakso. Returns the initial
  state of the palaute (or nil if it cannot be formed at all), the field the
  decision was based on, and the reason for picking that state."
  [jakso opiskeluoikeus]
  (cond
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

    (opiskeluoikeus/linked-to-another? opiskeluoikeus)
    [:ei-laheteta :opiskeluoikeus-oid :liittyva-opiskeluoikeus]

    :else
    [:odottaa-kasittelya nil :hoks-tallennettu]))

(defn initiate?
  [jakso hoks opiskeluoikeus]
  (let [[initial-state _ reason]
        (initial-palaute-state-and-reason jakso opiskeluoikeus)]
    (if (not= initial-state :odottaa-kasittelya)
      (log/infof (str "Not initiating tyoelamapalautekysely for jakso with HOKS "
                      "ID `%s` and yksiloiva tunniste `%s`. %s")
                 (:id hoks)
                 (:yksiloiva-tunniste jakso)
                 reason)
      true)))

(defn already-initiated?!
  [jakso hoks]
  (some? (palaute/get-by-hoks-id-and-yksiloiva-tunniste!
           db/spec
           {:hoks-id            (:id hoks)
            :yksiloiva-tunniste (:yksiloiva-tunniste jakso)})))

(defn initiate!
  [jakso hoks suoritus koulutustoimija toimipiste-oid]
  (let [voimassa-alkupvm (next-niputus-date (:loppu jakso))]
    (if (already-initiated?! jakso hoks)
      (log/warnf (str "Palaute has already been initiated for työpaikkajakso "
                      "with HOKS ID `%d` and yksiloiva tunniste `%s`.")
                 (:id hoks)
                 (:yksiloiva-tunniste jakso))
      (palaute/insert!
        db/spec
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
         :herate-source      "ehoks_update"}))))

(defn initiate-all-uninitiated!
  "Takes a `hoks` and `opiskeluoikeus` and initiates tyoelamapalaute for all
  tyopaikkajaksos in HOKS for which palaute has not been already initiated."
  [hoks opiskeluoikeus]
  (let [suoritus        (find-first suoritus/ammatillinen?
                                    (:suoritukset opiskeluoikeus))
        koulutustoimija (palaute/koulutustoimija-oid! opiskeluoikeus)
        toimipiste-oid  (palaute/toimipiste-oid! suoritus)]
    (->> (tyopaikkajaksot hoks)
         (filter #(initiate? % hoks opiskeluoikeus))
         (map #(initiate! % hoks suoritus koulutustoimija toimipiste-oid))
         doall)))
