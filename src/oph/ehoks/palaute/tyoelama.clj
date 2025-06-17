(ns oph.ehoks.palaute.tyoelama
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [medley.core :refer [map-vals]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.dynamodb :as dynamodb]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.heratepalvelu :as heratepalvelu]
            [oph.ehoks.hoks.osaamisen-hankkimistapa :as oht]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.tapahtuma :as tapahtuma]
            [oph.ehoks.palaute.tyoelama.nippu :as nippu]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.utils.date :as date]
            [oph.ehoks.utils.string :as u-str])
  (:import (java.time LocalDate)))

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
  Asetusmuutoksen myötä 1.7.2025 niputus tapahtuu kerran kuukaudessa, kuun
  ensimmäisenä päivänä."
  ^LocalDate [^LocalDate pvm]
  (let [year  (.getYear pvm)
        month (.getMonthValue pvm)]
    (if (= 12 month)
      (LocalDate/of (inc year) 1 1)
      (LocalDate/of year (inc month) 1))))

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
  [{:keys [jakso existing-palaute] :as ctx} kysely-type]
  (cond
    (not (palaute/nil-or-unhandled? existing-palaute))
    [nil :yksiloiva-tunniste :jo-lahetetty]

    (nil? jakso)
    [:ei-laheteta :osaamisen-hankkimistapa :poistunut]

    (not (get jakso :loppu))
    [nil :loppu :ei-ole]

    ;; order dependency: nil rules must come first

    (not (oht/palautteenkeruu-allowed-tyopaikkajakso? jakso))
    [:ei-laheteta :tyopaikalla-jarjestettava-koulutus :puuttuva-yhteystieto]

    (not (oht/has-required-osa-aikaisuustieto? jakso))
    [:ei-laheteta :osa-aikaisuustieto :ei-ole]

    (fully-keskeytynyt? jakso)
    [:ei-laheteta :keskeytymisajanjaksot :jakso-keskeytynyt]

    :else
    (or (palaute/initial-palaute-state-and-reason-if-not-kohderyhma ctx :loppu)
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

(defn enrich-ctx!
  "Add information needed by työelämäpalaute initiation into context."
  [{:keys [tx hoks jakso] :as ctx}]
  (assoc ctx
         :tapahtumatyyppi :hoks-tallennus
         :existing-ddb-herate
         (delay (dynamodb/get-jakso-by-hoks-id-and-yksiloiva-tunniste!
                  (:id hoks) (:yksiloiva-tunniste jakso)))
         :existing-palaute
         (palaute/get-by-hoks-id-and-yksiloiva-tunniste!
           tx {:hoks-id            (:id hoks)
               :yksiloiva-tunniste (:yksiloiva-tunniste jakso)})))

(defn initiate-if-needed!
  [{:keys [hoks] :as ctx} jakso]
  (jdbc/with-db-transaction
    [tx db/spec {:isolation :serializable}]
    (let [ctx (enrich-ctx! (assoc ctx :tx tx :jakso jakso))
          [proposed-state field reason]
          (initial-palaute-state-and-reason ctx :ohjaajakysely)
          state
          (if (= field :opiskeluoikeus-oid) :odottaa-kasittelya proposed-state)
          lisatiedot (map-vals str (select-keys (merge jakso hoks) [field]))]
      (log/info "Initial state for jakso" (:yksiloiva-tunniste jakso)
                "of HOKS" (:id hoks) "will be" (or state :ei-luoda-ollenkaan)
                "because of" reason "in" field)
      (if state
        (->> (build! ctx state)
             (palaute/upsert! tx)
             (tapahtuma/build-and-insert! ctx state reason lisatiedot))
        (when (:existing-palaute ctx)
          (tapahtuma/build-and-insert! ctx reason lisatiedot)))
      state)))

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

(defn build-jaksoherate-record-for-heratepalvelu
  [{:keys [existing-palaute opiskeluoikeus koulutustoimija hoks jakso
           toimipiste niputuspvm suoritus vastaamisajan-alkupvm
           request-id arvo-response] :as ctx}]
  (let [heratepvm (:heratepvm existing-palaute)
        tjk (:tyopaikalla-jarjestettava-koulutus jakso)
        ohjaaja (:vastuullinen-tyopaikka-ohjaaja tjk)]
    (utils/remove-nils
      {:yksiloiva_tunniste (:jakson-yksiloiva-tunniste existing-palaute)
       :alkupvm vastaamisajan-alkupvm
       :hankkimistapa_id (:hankkimistapa-id existing-palaute)
       :hankkimistapa_tyyppi (utils/koodiuri->koodi
                               (:osaamisen-hankkimistapa-koodi-uri jakso))
       :oppisopimuksen_perusta (utils/koodiuri->koodi
                                 (:oppisopimuksen-perusta-koodi-uri jakso))
       :hoks_id (:hoks-id existing-palaute)
       :jakso_alkupvm (:alku jakso)
       :jakso_loppupvm (:loppu jakso)
       :koulutustoimija koulutustoimija
       :niputuspvm niputuspvm
       :ohjaaja_email (:sahkoposti ohjaaja)
       :ohjaaja_nimi (:nimi ohjaaja)
       :ohjaaja_puhelinnumero (:puhelinnumero ohjaaja)
       :ohjaaja_ytunnus_kj_tutkinto (nippu/tunniste ctx)
       :opiskeluoikeus_oid (:opiskeluoikeus-oid hoks)
       :oppija_oid (:oppija-oid hoks)
       :oppilaitos (:oid (:oppilaitos opiskeluoikeus))
       :osaamisala (str (seq (suoritus/get-osaamisalat suoritus heratepvm)))
       :osa_aikaisuus (:osa-aikaisuustieto jakso)
       :rahoituskausi (palaute/rahoituskausi heratepvm)
       :request_id request-id
       :tallennuspvm (date/now)
       :toimipiste_oid toimipiste
       :tpk-niputuspvm "ei_maaritelty"  ; sic! this has a dash, not underscore
       :tunnus (:tunnus arvo-response)
       :tutkinnonosa_koodi (:tutkinnon-osa-koodi-uri jakso)
       :tutkinnonosa_nimi (:nimi jakso)
       :tutkinto (suoritus/tutkintotunnus suoritus)
       :tutkintonimike (str (seq (map :koodiarvo (:tutkintonimike suoritus))))
       :tyopaikan_nimi (:tyopaikan-nimi tjk)
       :tyopaikan_normalisoitu_nimi (u-str/normalize (:tyopaikan-nimi tjk))
       :tyopaikan_ytunnus (:tyopaikan-y-tunnus tjk)
       :viimeinen_vastauspvm (palaute/vastaamisajan-loppupvm
                               heratepvm vastaamisajan-alkupvm)})))

;; these use vars (#') because otherwise with-redefs doesn't work on
;; them (the map has the original definition even if the function in
;; its namespace is redef'd)

(def handlers
  {:check-palaute #'initial-palaute-state-and-reason
   :arvo-builder #'arvo/build-jaksotunnus-request-body
   :arvo-caller #'arvo/create-jaksotunnus!
   :heratepalvelu-builder #'build-jaksoherate-record-for-heratepalvelu
   :heratepalvelu-caller #'heratepalvelu/sync-jakso!
   :extra-handlers [#'ensure-tpo-nippu!]})
