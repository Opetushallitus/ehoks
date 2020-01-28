(ns oph.ehoks.external.aws-sqs
  (:require [clojure.data.json :as json]
            [oph.ehoks.config :refer [config]]
            [environ.core :refer [env]]
            [clojure.tools.logging :as log])
  (:import (software.amazon.awssdk.services.sqs SqsClient)
           (software.amazon.awssdk.regions Region)
           (software.amazon.awssdk.services.sqs.model SendMessageRequest
                                                      GetQueueUrlRequest)))

(def ^:private sqs-client
  (when (:send-herate-messages? config)
    (-> (SqsClient/builder)
        (.region (Region/EU_WEST_1))
        (.build))))

(def ^:private queue-url
  (when (some? sqs-client)
    (if (nil? (:env-stage env))
      (log/warn "Stage missing from env variables")
      (.queueUrl (.getQueueUrl
                   sqs-client
                   (-> (GetQueueUrlRequest/builder)
                       (.queueName
                         (str (:env-stage env) "-"
                              (:heratepalvelu-queue config)))
                       (.build)))))))

(def ^:private tyoelamapalaute-queue-url
  (when (some? sqs-client)
    (if (nil? (:env-stage env))
      (log/warn "Stage missing from env variables")
      (.queueUrl (.getQueueUrl
                   sqs-client
                   (-> (GetQueueUrlRequest/builder)
                       (.queueName
                         (str (:env-stage env) "-"
                              (:heratepalvelu-tyoelamapalaute-queue config)))
                       (.build)))))))

(defn build-hoks-hyvaksytty-msg [id hoks]
  {:ehoks-id id
   :kyselytyyppi "aloittaneet"
   :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
   :oppija-oid (:oppija-oid hoks)
   :sahkoposti (:sahkoposti hoks)
   :alkupvm (str (:ensikertainen-hyvaksyminen hoks))})

(defn build-tyoelamapalaute-msg [msg]
  {:tyyppi (:tyyppi msg)
   :alkupvm (str (:alkupvm msg))
   :loppupvm (str (:loppupvm msg))
   :hoks-id (:hoks_id msg)
   :opiskeluoikeus_oid (:opiskeluoikeus_oid msg)
   :oppija-oid (:oppija_oid msg)
   :hankkimistapa-id (:hankkimistapa_id msg)
   :hankkimistapa-tyyppi (:hankkimistapa_tyyppi msg)
   :tutkinnonosa_id (:tutkinnonosa_id msg)
   :tyopaikan-nimi (:tyopaikan_nimi msg)
   :tyopaikan-ytunnus (:tyopaikan_ytunnus msg)
   :tyopaikkaohjaaja-email (:tyopaikkaohjaaja_email msg)
   :tyopaikkaohjaaja-nimi (:tyopaikkaohjaaja_nimi msg)})

(defn send-message [msg]
  (when (some? queue-url)
    (.sendMessage sqs-client (-> (SendMessageRequest/builder)
                                 (.queueUrl queue-url)
                                 (.messageBody (json/write-str msg))
                                 (.build)))))

(defn send-tyoelamapalaute-message [msg]
  (when (some? tyoelamapalaute-queue-url)
    (.sendMessage sqs-client (-> (SendMessageRequest/builder)
                                 (.queueUrl tyoelamapalaute-queue-url)
                                 (.messageBody (json/write-str msg))
                                 (.build)))))
