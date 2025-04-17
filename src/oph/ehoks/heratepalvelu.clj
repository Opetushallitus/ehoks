(ns oph.ehoks.heratepalvelu
  (:require [clojure.set :refer []]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [medley.core :refer []]
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
            [oph.ehoks.utils.date :as date])
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

; Helper functions that can be mocked in tests
(def sync-jakso!*     (partial ddb/sync-item! :jakso))
(def sync-tpo-nippu!* (partial ddb/sync-item! :nippu))

(defn sync-jakso!
  "Put information about single työelämäpalaute to herätepalvelu DDB."
  [jaksorecord]
  (if-not (contains? (set (:heratepalvelu-responsibities config))
                     :sync-jakso-heratteet)
    (log/info "sync-jakso!: configured to not write to DDB.")
    (sync-jakso!* jaksorecord)))

(defn sync-tpo-nippu!
  "Update the Herätepalvelu nipputable to have the same content for given heräte
  as palaute-backend has in its own database."
  [nippu tunnus]
  {:pre [(:koulutuksenjarjestaja nippu) (:niputuspvm nippu) (:tutkinto nippu)]}
  (if-not (contains? (set (:heratepalvelu-responsibities config))
                     :sync-jakso-heratteet)
    (log/warn "sync-tpo-nippu!: configured to not do anything")
    (try
      (if (ddb/get-item! :nippu nippu)
        (log/infof "Nippu already exists:" nippu)
        (sync-tpo-nippu!* nippu))
      (catch Exception e
        (throw (ex-info (str "Failed to sync TPO-nippu to Herätepalvelu: "
                             nippu)
                        {:type        ::tpo-nippu-sync-failed
                         :arvo-tunnus tunnus}
                        e))))))

(defn delete-jakso-herate!
  [tep-palaute]
  (ddb/delete-item! :jakso {:hankkimistapa_id (:hankkimistapa-id tep-palaute)}))
