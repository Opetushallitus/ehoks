(ns oph.ehoks.heratepalvelu
  (:require [clojure.set :refer [rename-keys]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [medley.core :refer [find-first]]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.opiskelija :as op]
            [oph.ehoks.palaute.opiskelija.kyselylinkki :as kyselylinkki]
            [oph.ehoks.palaute.tyoelama.nippu :as nippu]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.utils.date :as date]
            [oph.ehoks.utils.string :as u-str])
  (:import (java.time LocalDate)))

(defn send-workplace-periods!
  "Formats and sends a list of periods to a SQS queue"
  [periods]
  (doseq [period periods]
    (sqs/send-tyoelamapalaute-message! (sqs/build-tyoelamapalaute-msg period))))

(defn get-oppija-kyselylinkit
  "Returns all feedback links for oppija"
  [oppija-oid]
  (filter
    some?
    (map #(if-not (:vastattu %1)
            (when-let [status (arvo/get-kyselylinkki-status-catch-404
                                (:kyselylinkki %1))]
              (let [loppupvm (LocalDate/parse
                               (first
                                 (string/split (:voimassa_loppupvm status)
                                               #"T")))]
                (kyselylinkki/update! {:kyselylinkki (:kyselylinkki %1)
                                       :voimassa_loppupvm loppupvm
                                       :vastattu (:vastattu status)})
                (assoc %1
                       :voimassa-loppupvm loppupvm
                       :vastattu (:vastattu status))))
            %1)
         (kyselylinkki/get-by-oppija-oid! oppija-oid))))

(defn set-tep-kasitelty
  "Marks an osaamisen hankkimistapa as handled (käsitelty)."
  [hankkimistapa-id to]
  (db-hoks/update-osaamisen-hankkimistapa-tep-kasitelty hankkimistapa-id to))

(defn process-hoksit-without-kyselylinkit
  "Finds all HOKSit for which kyselylinkit haven't been created and sends them
  to the SQS queue"
  [start end limit]
  (let [aloittaneet
        (db-hoks/select-hoksit-with-kasittelemattomat-aloitusheratteet
          start end limit)
        paattyneet
        (db-hoks/select-hoksit-with-kasittelemattomat-paattoheratteet
          start end limit)]
    (log/infof
      "Sending %d (limit %d) hoksit between %s and %s"
      (+ (count aloittaneet) (count paattyneet)) (* 2 limit) start end)
    (op/initiate-every-needed! :aloituskysely aloittaneet)
    (op/initiate-every-needed! :paattokysely  paattyneet)
    (concat aloittaneet paattyneet)))

(defn set-aloitusherate-kasitelty
  "Marks aloitusheräte handled (käsitelty) for a given HOKS."
  [hoks-id to]
  (db-hoks/update-amisherate-kasittelytilat-aloitusherate-kasitelty hoks-id to))

(defn set-paattoherate-kasitelty
  "Marks päättöheräte handled (käsitelty) for a given HOKS."
  [hoks-id to]
  (db-hoks/update-amisherate-kasittelytilat-paattoherate-kasitelty hoks-id to))

(defn select-tyoelamajaksot-active-between
  "Finds all workplace periods active between start and end dates for student
  oppija."
  [oppija start end]
  (concat
    (db-hoks/select-tyoelamajaksot-active-between "hato" oppija start end)
    (db-hoks/select-tyoelamajaksot-active-between "hpto" oppija start end)
    (db-hoks/select-tyoelamajaksot-active-between "hyto" oppija start end)))

(defn build-jaksoherate-record-for-heratepalvelu
  [{:keys [existing-palaute opiskeluoikeus koulutustoimija hoks jakso
           toimipiste niputuspvm suoritus vastaamisajan-alkupvm
           request-id arvo-response] :as ctx}]
  (let [heratepvm (:heratepvm existing-palaute)
        tjk (:tyopaikalla-jarjestettava-koulutus jakso)
        ohjaaja (:vastuullinen-tyopaikka-ohjaaja tjk)]
    (utils/remove-nils
      {:yksiloiva_tunniste (:jakson-yksiloiva-tunniste existing-palaute)
       :alkupvm (str vastaamisajan-alkupvm)
       :hankkimistapa_id (:hankkimistapa-id existing-palaute)
       :hankkimistapa_tyyppi (arvo/koodiuri->koodi
                               (:osaamisen-hankkimistapa-koodi-uri jakso))
       :oppisopimuksen_perusta (arvo/koodiuri->koodi
                                 (:oppisopimuksen-perusta-koodi-uri jakso))
       :hoks_id (:hoks-id existing-palaute)
       :jakso_alkupvm (str (:alku jakso))
       :jakso_loppupvm (str (:loppu jakso))
       :koulutustoimija koulutustoimija
       :niputuspvm (str niputuspvm)
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
       :tallennuspvm (str (date/now))
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

; Helper functions that can be mocked in tests
(def sync-jakso!*     (partial ddb/sync-item! :jakso))
(def sync-tpo-nippu!* (partial ddb/sync-item! :nippu))

(defn sync-tpo-nippu!
  "Update the Herätepalvelu nipputable to have the same content for given heräte
  as palaute-backend has in its own database."
  [nippu tunnus]
  {:pre [(:koulutuksenjarjestaja nippu) (:niputuspvm nippu) (:tutkinto nippu)]}
  (if-not (contains? (set (:heratepalvelu-responsibities config))
                     :sync-jakso-heratteet)
    (log/warn "sync-tpo-nippu!: configured to not do anything")
    (let [tunnisteet (select-keys nippu [:ohjaaja_ytunnus_kj_tutkinto
                                         :niputuspvm])]
      (try
        (if (ddb/get-item! :nippu tunnisteet)
          (log/infof "Nippu `%s` already exists" tunnisteet)
          (sync-tpo-nippu!* nippu))
        (catch Exception e
          (throw (ex-info (format (str "Failed to sync TPO-nippu with "
                                       "tunnisteet %s to Herätepalvelu")
                                  tunnisteet)
                          {:type        ::tpo-nippu-sync-failed
                           :arvo-tunnus tunnus}
                          e)))))))

(defn delete-jakso-herate!
  [tep-palaute]
  (ddb/delete-item! :jakso {:hankkimistapa_id (:hankkimistapa-id tep-palaute)}))
