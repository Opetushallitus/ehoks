(ns oph.ehoks.external.aws-sqs
  (:require [clojure.data.json :as json]
            [oph.ehoks.config :refer [config]]
            [environ.core :refer [env]]
            [clojure.tools.logging :as log])
  (:import (software.amazon.awssdk.services.sqs SqsClient)
           (software.amazon.awssdk.regions Region)
           (software.amazon.awssdk.services.sqs.model
             SendMessageRequest
             GetQueueUrlRequest
             QueueDoesNotExistException)))

(def ^:private sqs-client
  (when (:send-herate-messages? config)
    (-> (SqsClient/builder)
        (.region (Region/EU_WEST_1))
        (.build))))

(defn- get-queue-url [queue-name]
  (when (some? sqs-client)
    (if (nil? (:env-stage env))
      (log/warn "Stage missing from env variables")
      (.queueUrl (.getQueueUrl
                   sqs-client
                   (-> (GetQueueUrlRequest/builder)
                       (.queueName
                         (str (:env-stage env) "-"
                              queue-name))
                       (.build)))))))

(def ^:private herate-queue-url
  (get-queue-url (:heratepalvelu-queue config)))

(def ^:private tyoelamapalaute-queue-url
  (get-queue-url (:heratepalvelu-tyoelamapalaute-queue config)))

(def ^:private resend-queue-url
  (get-queue-url (:resend-queue config)))

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
   :opiskeluoikeus-oid (:opiskeluoikeus_oid msg)
   :oppija-oid (:oppija_oid msg)
   :hankkimistapa-id (:hankkimistapa_id msg)
   :hankkimistapa-tyyppi (:hankkimistapa_tyyppi msg)
   :tutkinnonosa-id (:tutkinnonosa_id msg)
   :tyopaikan-nimi (:tyopaikan_nimi msg)
   :tyopaikan-ytunnus (:tyopaikan_ytunnus msg)
   :tyopaikkaohjaaja-email (:tyopaikkaohjaaja_email msg)
   :tyopaikkaohjaaja-nimi (:tyopaikkaohjaaja_nimi msg)})

(defn send-message [msg queue-url]
  (.sendMessage sqs-client (-> (SendMessageRequest/builder)
                               (.queueUrl queue-url)
                               (.messageBody (json/write-str msg))
                               (.build))))

(defn send-amis-palaute-message [msg]
  (if (some? herate-queue-url)
    (send-message msg herate-queue-url)
    (log/error "No AMIS-palaute queue!")))

(defn send-tyoelamapalaute-message [msg]
  (if (some? tyoelamapalaute-queue-url)
    (send-message msg tyoelamapalaute-queue-url)
    (log/error "No työelämäpalaute queue!")))

(defn send-palaute-resend-message [msg]
  (if (some? resend-queue-url)
    (send-message msg resend-queue-url)
    (log/error "No resend queue!")))
