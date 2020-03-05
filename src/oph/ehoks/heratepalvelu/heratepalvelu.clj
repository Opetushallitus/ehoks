(ns oph.ehoks.heratepalvelu.heratepalvelu
  (:require [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.aws-sqs :as sqs]
            [clojure.tools.logging :as log]))

(defn find-finished-workplace-periods
  "Queries for all finished workplace periods"
  []
  (let [hytos (db-hoks/select-paattyneet-tyoelamajaksot "hyto")
        hptos (db-hoks/select-paattyneet-tyoelamajaksot "hpto")
        hatos (db-hoks/select-paattyneet-tyoelamajaksot "hato")]
    (log/info hytos)
    (log/info hptos)
    (log/info hatos)
    (concat hytos hptos hatos)))

(defn send-workplace-periods
  "Formats and sends a list of periods to a SQS queue"
  [periods]
  (log/info periods)
  (map
    #(sqs/send-tyoelamapalaute-message (sqs/build-tyoelamapalaute-msg %))
    periods))

(defn process-finished-workplace-periods
  "Finds all finished workplace periods and sends them to a SQS queue"
  []
  (let [periods (find-finished-workplace-periods)]
    (send-workplace-periods periods)
    periods))
