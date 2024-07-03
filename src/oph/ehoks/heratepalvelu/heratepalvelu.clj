(ns oph.ehoks.heratepalvelu.heratepalvelu
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.palaute.opiskelija.kyselylinkki :as kyselylinkki]
            [oph.ehoks.palaute.opiskelija :as op]
            [oph.ehoks.palaute.tyoelama :as tep])
  (:import (java.time LocalDate)))

(defn send-workplace-periods
  "Formats and sends a list of periods to a SQS queue"
  [periods]
  (doseq [period periods]
    (sqs/send-tyoelamapalaute-message (sqs/build-tyoelamapalaute-msg period))))

(defn process-finished-workplace-periods
  "Finds all finished workplace periods between dates start and
  end and sends them to a SQS queue"
  [start end limit]
  (let [periods (tep/finished-workplace-periods! start end limit)]
    (log/infof
      "Sending %d  (limit %d) finished workplace periods between %s - %s"
      (count periods) limit start end)
    (send-workplace-periods periods)
    periods))

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
        (db-hoks/select-hoksit-with-kasittelemattomat-aloitusheratteet start
                                                                       end
                                                                       limit)
        paattyneet
        (db-hoks/select-hoksit-with-kasittelemattomat-paattoheratteet start
                                                                      end
                                                                      limit)
        hoksit (concat aloittaneet paattyneet)]
    (log/infof
      "Sending %d (limit %d) hoksit between %s and %s"
      (count hoksit) (* 2 limit) start end)
    (op/initiate-every-needed! :aloituskysely aloittaneet {:resend? true})
    (op/initiate-every-needed! :paattokysely  paattyneet {:resend? true})
    hoksit))

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
