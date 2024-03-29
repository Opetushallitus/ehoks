(ns oph.ehoks.heratepalvelu.heratepalvelu
  (:require [clojure.tools.logging :as log]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.hoks.hoks :as h]
            [clojure.string :as str]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [oph.ehoks.oppijaindex :as oi]
            [oph.ehoks.opiskelijapalaute :as op]
            [oph.ehoks.db.db-operations.oppija :as db-oppija]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-oo]
            [clojure.java.jdbc :as jdbc]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops])
  (:import (java.time LocalDate)))

(defn find-finished-workplace-periods
  "Queries for all finished workplace periods between start and end"
  [start end limit]
  (let [hytos (db-hoks/select-paattyneet-tyoelamajaksot "hyto" start end limit)
        hptos (db-hoks/select-paattyneet-tyoelamajaksot "hpto" start end limit)
        hatos (db-hoks/select-paattyneet-tyoelamajaksot "hato" start end limit)]
    (concat hytos hptos hatos)))

(defn send-workplace-periods
  "Formats and sends a list of periods to a SQS queue"
  [periods]
  (doseq [period periods]
    (sqs/send-tyoelamapalaute-message (sqs/build-tyoelamapalaute-msg period))))

(defn process-finished-workplace-periods
  "Finds all finished workplace periods between dates start and
  end and sends them to a SQS queue"
  [start end limit]
  (let [periods (find-finished-workplace-periods start end limit)]
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
    (map
      #(try
         (if-not (:vastattu %1)
           (when-let [status (arvo/get-kyselylinkki-status-catch-404
                               (:kyselylinkki %1))]
             (let [loppupvm (LocalDate/parse
                              (first
                                (str/split (:voimassa_loppupvm status) #"T")))]
               (h/update-kyselylinkki!
                 {:kyselylinkki (:kyselylinkki %1)
                  :voimassa_loppupvm loppupvm
                  :vastattu (:vastattu status)})
               (assoc
                 %1
                 :voimassa-loppupvm loppupvm
                 :vastattu (:vastattu status))))
           %1)
         (catch Exception e
           (log/error e)
           (throw e)))
      (h/get-kyselylinkit-by-oppija-oid oppija-oid))))

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
    (op/send-every-needed! :aloituskysely aloittaneet)
    (op/send-every-needed! :paattokysely  paattyneet)
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
