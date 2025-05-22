(ns oph.ehoks.palaute.tyoelama
  (:require [clojure.tools.logging :as log]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.heratepalvelu :as heratepalvelu]
            [oph.ehoks.hoks.osaamisen-hankkimistapa :as oht]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.tyoelama.nippu :as nippu]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.utils.date :as date]
            [oph.ehoks.utils.string :as u-str]))

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

(defn initiate-all-uninitiated!
  "Takes a `hoks` and `opiskeluoikeus` and initiates tyoelamapalaute for all
  tyopaikkajaksos in HOKS for which palaute has not been already initiated."
  [{:keys [hoks] :as ctx}]
  (run! #(palaute/initiate-if-needed! (assoc ctx :jakso %))
        (tyopaikkajaksot hoks)))

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
  {:arvo-builder #'arvo/build-jaksotunnus-request-body
   :arvo-caller #'arvo/create-jaksotunnus!
   :heratepalvelu-builder #'build-jaksoherate-record-for-heratepalvelu
   :heratepalvelu-caller #'heratepalvelu/sync-jakso!
   :extra-handlers [#'ensure-tpo-nippu!]})
