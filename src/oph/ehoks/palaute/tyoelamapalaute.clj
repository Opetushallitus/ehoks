(ns oph.ehoks.palaute.tyoelamapalaute
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hugsql.core :as hugsql]
            [medley.core :refer [find-first]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.opiskeluoikeus :as opiskeluoikeus]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.utils.date :as date])
  (:import (java.time LocalDate)))

(hugsql/def-db-fns "oph/ehoks/db/sql/tyoelamapalaute.sql")

(defn finished-workplace-periods!
  "Queries for all finished workplace periods between start and end"
  [start end limit]
  (let [hytos (db-hoks/select-paattyneet-tyoelamajaksot "hyto" start end limit)
        hptos (db-hoks/select-paattyneet-tyoelamajaksot "hpto" start end limit)
        hatos (db-hoks/select-paattyneet-tyoelamajaksot "hato" start end limit)]
    (concat hytos hptos hatos)))

(defn next-niputus-date
  "Palauttaa seuraavan niputuspäivämäärän annetun päivämäärän jälkeen.
  Niputuspäivämäärät ovat kuun ensimmäinen ja kuudestoista päivä."
  ^LocalDate [pvm-str]
  (let [[^int year ^int month ^int day] (map #(Integer/parseInt %)
                                             (string/split pvm-str #"-"))]
    (if (< day 16)
      (LocalDate/of year month 16)
      (cond
        (= 6 month) (LocalDate/of year 6 30)
        (= 12 month) (LocalDate/of (inc year) 1 1)
        :else (LocalDate/of year (inc month) 1)))))

(defn osa-aikaisuus-missing?
  "Puuttuuko tieto osa-aikaisuudesta jaksosta, jossa sen pitäisi olla?"
  [jakso]
  (and (not (:osa-aikaisuustieto jakso))
       (date/is-after (LocalDate/parse (:loppu jakso))
                      (LocalDate/of 2023 6 30))))

(defn alku-and-loppu-to-localdate
  "Muuntaa parametrina annetun hashmapin :alku ja :loppu -avaimien
  merkkijonomuotoiset päivämäärät LocalDate:iksi."
  [jakso]
  (cond-> jakso
    (:alku jakso)  (update :alku  #(LocalDate/parse %))
    (:loppu jakso) (update :loppu #(LocalDate/parse %))))

(defn sort-process-keskeytymisajanjaksot
  "Järjestää TEP-jakso keskeytymisajanjaksot, parsii niiden alku- ja
  loppupäivämäärät LocalDateiksi, ja palauttaa tuloslistan."
  [jakso]
  (map alku-and-loppu-to-localdate
       (sort-by :alku (:keskeytymisajanjaksot jakso))))

(defn fully-keskeytynyt?
  "Palauttaa true, jos TEP-jakso on keskeytynyt sen loppupäivämäärällä."
  [jakso]
  (let [kjaksot (sort-process-keskeytymisajanjaksot jakso)]
    (when-let [kjakso-loppu (:loppu (last kjaksot))]
      (not (date/is-after (LocalDate/parse (:loppu jakso)) kjakso-loppu)))))

(defn- reason-for-not-initiating
  [jakso opiskeluoikeus]
  (cond
    (opiskeluoikeus/in-terminal-state? opiskeluoikeus (:loppu jakso))
    (str "Opiskeluoikeus is in terminal state" opiskeluoikeus)

    (osa-aikaisuus-missing? jakso)
    "Osa-aikaisuus missing from työpaikkajakso"

    (fully-keskeytynyt? jakso)
    "Työpaikkajakso is fully interrupted."

    (not-any? suoritus/ammatillinen? (:suoritukset opiskeluoikeus))
    (format "No ammatillinen suoritus in opiskeluoikeus: `%s`."
            opiskeluoikeus)

    (palaute/feedback-collecting-prevented? opiskeluoikeus (:loppu jakso))
    (str "Feedback won't be collected because of funding basis:" opiskeluoikeus)

    (opiskeluoikeus/linked-to-another? opiskeluoikeus)
    (format "Opiskeluoikeus is linked to another opiskeluoikeus: %s"
            opiskeluoikeus)))

(defn initiate?
  [jakso opiskeluoikeus]
  (if-let [reason (reason-for-not-initiating jakso opiskeluoikeus)]
    (log/infof (str "Not initiating tyoelamapalautekysely for jakso with HOKS "
                    "ID `%s` and yksiloiva tunniste `%s`. %s")
               (:hoks-id jakso)
               (:yksiloiva-tunniste jakso)
               reason)
    true))

(defn already-initiated?!
  [jakso]
  (some? (get-jakso-by-hoks-id-and-yksiloiva-tunniste!
           db/spec
           {:hoks-id            (:hoks-id jakso)
            :yksiloiva-tunniste (:yksiloiva-tunniste jakso)})))

(defn initiate!
  [jakso opiskeluoikeus]
  (let [suoritus         (->> opiskeluoikeus
                              :suoritukset
                              (find-first suoritus/ammatillinen?))
        voimassa-alkupvm (next-niputus-date (:loppu jakso))]
    (if (already-initiated?! jakso)
      (log/warnf (str "Palaute has already been initiated for työpaikkajakso "
                      "with HOKS ID `%d` and yksiloiva tunniste `%s`.")
                 (:hoks-id jakso)
                 (:yksiloiva-tunniste jakso))
      (insert!
        db/spec
        {:hoks-id            (:hoks-id jakso)
         :yksiloiva-tunniste (:yksiloiva-tunniste jakso)
         :heratepvm          (LocalDate/parse (:loppu jakso))
         :koulutustoimija    (palaute/koulutustoimija-oid! opiskeluoikeus)
         :voimassa-alkupvm   voimassa-alkupvm
         :voimassa-loppupvm  (.plusDays voimassa-alkupvm 60)
         :tutkintonimike     (suoritus/tutkintonimike suoritus)
         :tutkintotunnus     (suoritus/tutkintotunnus suoritus)
         :herate-source      "ehoks_update"}))))
