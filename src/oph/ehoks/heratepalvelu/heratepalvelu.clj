(ns oph.ehoks.heratepalvelu.heratepalvelu
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [clojure.tools.logging :as log]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.hoks.hoks :as h]
            [clojure.string :as str])
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
  (map
    #(try
       (if-not (:vastattu %1)
         (let [status (arvo/get-kyselylinkki-status
                        (:kyselylinkki %1))
               loppupvm (LocalDate/parse
                          (first
                            (str/split (:voimassa_loppupvm status) #"T")))]
           (h/update-kyselylinkki!
             {:kyselylinkki (:kyselylinkki %1)
              :voimassa_loppupvm loppupvm
              :vastattu (:vastattu status)})
           (assoc
             %1
             :voimassa-loppupvm loppupvm
             :vastattu (:vastattu status)))
         %1)
       (catch Exception e
         (log/error e)
         (throw e)))
    (h/get-kyselylinkit-by-oppija-oid oppija-oid)))

(defn get-paivitetyt-tyoelamajaksot
  (str "Returns necessary työelämäjakso info (currently just oppisopimuksen"
       "perusta) to retroactively update jakso in herätepalvelu")
  [opiskeluoikeus ohjaajan-nimi tyopaikan-nimi tyopaikan-y-tunnus]
    (db-hoks/select-paivitetyt-tyoelamajaksot opiskeluoikeus
                                              ohjaajan-nimi
                                              tyopaikan-nimi
                                              tyopaikan-y-tunnus))

(defn set-tep-kasitelty [hankkimistapa-id to]
  (db-hoks/update-osaamisen-hankkimistapa-tep-kasitelty hankkimistapa-id to))

(defn- send-kyselyt-for-hoksit [hoksit build-msg]
  (loop [hoks (first hoksit)
         r (rest hoksit)
         c 0]
    (if (:osaamisen-hankkimisen-tarve hoks)
      (do
        (if-let [msg (build-msg hoks)]
          (sqs/send-amis-palaute-message msg))
        (recur (first r) (rest r) (inc c)))
      (if (not-empty r)
        (recur (first r) (rest r) c)
        c))))

(defn resend-aloituskyselyherate-between [from to]
  (send-kyselyt-for-hoksit (db-hoks/select-hoksit-created-between from to)
                           #(sqs/build-hoks-hyvaksytty-msg (:id %) %)))

(defn resend-paattokyselyherate-between [from to]
  (send-kyselyt-for-hoksit
    (db-hoks/select-hoksit-finished-between from to)
    (fn [hoks]
      (if-let [opiskeluoikeus (k/get-opiskeluoikeus-info
                                (:opiskeluoikeus-oid hoks))]
        (if-let [kyselytyyppi (h/get-kysely-type opiskeluoikeus)]
          (sqs/build-hoks-osaaminen-saavutettu-msg
            (:id hoks)
            (:osaamisen-saavuttamisen-pvm hoks)
            hoks
            kyselytyyppi))))))
