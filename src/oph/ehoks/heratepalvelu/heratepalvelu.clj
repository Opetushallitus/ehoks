(ns oph.ehoks.heratepalvelu.heratepalvelu
  (:require [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.aws-sqs :as sqs]
            [clojure.tools.logging :as log]))

(defn find-finished-workplace-periods
  "Queries for all finished workplace periods between start and end"
  [start end]
  (let [hytos (db-hoks/select-paattyneet-tyoelamajaksot "hyto" start end)
        hptos (db-hoks/select-paattyneet-tyoelamajaksot "hpto" start end)
        hatos (db-hoks/select-paattyneet-tyoelamajaksot "hato" start end)]
    (log/info hytos)
    (log/info hptos)
    (log/info hatos)
    (concat hytos hptos hatos)))

(defn send-workplace-periods
  "Formats and sends a list of periods to a SQS queue"
  [periods]
  (log/info periods)
  (doseq [period periods]
    (sqs/send-tyoelamapalaute-message (sqs/build-tyoelamapalaute-msg period))))

(defn process-finished-workplace-periods
  "Finds all finished workplace periods between dates start and
  end and sends them to a SQS queue"
  [start end]
  (let [periods (find-finished-workplace-periods start end)]
    (send-workplace-periods periods)
    periods))
