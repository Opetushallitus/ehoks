(ns oph.ehoks.palaute.tyoelama
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [medley.core :refer [find-first map-vals]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.heratepalvelu :as heratepalvelu]
            [oph.ehoks.hoks.osaamisen-hankkimistapa :as oht]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.tapahtuma :as tapahtuma]
            [oph.ehoks.palaute.tyoelama.nippu :as nippu]
            [oph.ehoks.utils.date :as date])
  (:import (clojure.lang ExceptionInfo)
           (java.time LocalDate)
           (java.util UUID)))

(defn finished-workplace-periods!
  "Queries for all finished workplace periods between start and end"
  [start end limit]
  (let [hytos (db-hoks/select-paattyneet-tyoelamajaksot "hyto" start end limit)
        hptos (db-hoks/select-paattyneet-tyoelamajaksot "hpto" start end limit)
        hatos (db-hoks/select-paattyneet-tyoelamajaksot "hato" start end limit)]
    (concat hytos hptos hatos)))

(defn process-finished-workplace-periods!
  "Finds all finished workplace periods between dates start and
  end and sends them to a SQS queue"
  [start end limit]
  (let [periods (finished-workplace-periods! start end limit)]
    (log/infof
      "Sending %d  (limit %d) finished workplace periods between %s - %s"
      (count periods) limit start end)
    (heratepalvelu/send-workplace-periods! periods)
    periods))

(defn tyopaikkajaksot
  "Takes `hoks` as an input and extracts from it all osaamisen hankkimistavat
  that are tyopaikkajaksos. Returns a lazy sequence."
  [hoks]
  (filter oht/tyopaikkajakso? (oht/osaamisen-hankkimistavat hoks)))

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
  [{:keys [jakso] :as ctx} kysely-type]
  {:pre [(some? jakso)]}
  (or
    (palaute/initial-palaute-state-and-reason-if-not-kohderyhma ctx :loppu)
    (cond
      (not (oht/palautteenkeruu-allowed-tyopaikkajakso? jakso))
      [:ei-laheteta :tyopaikalla-jarjestettava-koulutus :puuttuva-yhteystieto]

      (not (oht/has-required-osa-aikaisuustieto? jakso))
      [:ei-laheteta :osa-aikaisuustieto :ei-ole]

      (fully-keskeytynyt? jakso)
      [:ei-laheteta :keskeytymisajanjaksot :jakso-keskeytynyt]

      :else
      [:odottaa-kasittelya :loppu :hoks-tallennettu])))

(defn build!
  "Builds tyoelamapalaute to be inserted to DB. Uses `palaute/build!` to build
  an initial `palaute` map, then `assoc`s tyoelamapalaute specific values to
  that."
  [{:keys [jakso] :as ctx} tila]
  {:pre [(some? tila)]}
  (let [heratepvm (:loppu jakso)
        alkupvm   (next-niputus-date heratepvm)]
    (assoc (palaute/build! ctx tila)
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
    (let [ctx (assoc ctx
                     :tapahtumatyyppi :hoks-tallennus
                     :tx              tx
                     :jakso           jakso
                     :existing-palaute
                     (palaute/get-by-hoks-id-and-yksiloiva-tunniste!
                       tx {:hoks-id            (:id hoks)
                           :yksiloiva-tunniste (:yksiloiva-tunniste jakso)}))
          [state field reason]
          (initial-palaute-state-and-reason ctx :ohjaajakysely)
          lisatiedot (map-vals str (select-keys (merge jakso hoks) [field]))]
      (log/info "Initial state for jakso" (:yksiloiva-tunniste jakso)
                "of HOKS" (:id hoks) "will be" (or state :ei-luoda-ollenkaan)
                "because of" reason "in" field)
      (when state
        (->> (build! ctx state)
             (palaute/upsert! tx)
             (tapahtuma/build-and-insert! ctx state reason lisatiedot))
        state))))

(defn initiate-all-uninitiated!
  "Takes a `hoks` and `opiskeluoikeus` and initiates tyoelamapalaute for all
  tyopaikkajaksos in HOKS for which palaute has not been already initiated."
  [{:keys [hoks] :as ctx}]
  (run! #(initiate-if-needed! ctx %) (tyopaikkajaksot hoks)))

(defn ensure-tpo-nippu!
  "Makes sure that the nippu for the työelämäpalaute exists in
  herätepalvelu's data base.  Without this, herätepalvelu doesn't know
  what to niputtaa when it's time."
  [{:keys [arvo-response] :as ctx}]
  (heratepalvelu/sync-tpo-nippu!
    (nippu/build-tpo-nippu-for-heratepalvelu ctx)
    (:tunnus arvo-response)))
