(ns oph.ehoks.heratepalvelu.heratepalvelu
  (:require [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.aws-sqs :as sqs]
            [clojure.tools.logging :as log]
            [oph.ehoks.hoks.hoks :as h]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [oph.ehoks.external.arvo :as arvo]))

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
  (log/info periods)
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
  "Returns all active links and removes answered and outdated links from db"
  [oppija-oid]
  (reduce
    (fn [linkit linkki]
      (try
        (let [status (arvo/get-kyselylinkki-status
                       (:kyselylinkki linkki))
              voimassa (f/parse
                         (:date-time f/formatters)
                         (:voimassa_loppupvm status))]
          (if (or (:vastattu status)
                  (t/after? (t/now) voimassa))
            (do (h/delete-kyselylinkki!
                  (:kyselylinkki linkki))
                linkit)
            (conj linkit (assoc
                           linkki
                           :voimassa-loppupvm
                           (:voimassa_loppupvm status)))))
        (catch Exception e
          (print e)
          (throw e))))
    []
    (h/get-kyselylinkit-by-oppija-oid oppija-oid)))

(defn set-tep-kasitelty [hankkimistapa-id to]
  (db-hoks/update-osaamisen-hankkimistapa-tep-kasitelty hankkimistapa-id to))

(defn resend-aloituskyselyherate-between [from to]
  (let [hoksit (db-hoks/select-hoksit-created-between from to)]
    (loop [hoks (first hoksit)
           r (rest hoksit)
           c 0]
      (if (:osaamisen-hankkimisen-tarve hoks)
        (do
          (sqs/send-amis-palaute-message (sqs/build-hoks-hyvaksytty-msg
                                           (:id hoks) hoks))
          (recur (first r)
                 (rest r)
                 (inc c)))
        (if (not-empty r)
          (recur (first r)
                 (rest r)
                 c)
          c)))))
