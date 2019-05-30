(ns oph.ehoks.external.aws-sqs
  (:require [clojure.data.json :as json]
            [oph.ehoks.config :refer [config]])
  (:import (software.amazon.awssdk.services.sqs SqsClient)
           (software.amazon.awssdk.regions Region)
           (software.amazon.awssdk.services.sqs.model SendMessageRequest GetQueueUrlRequest)))

(def sqs-client (.build (.region (SqsClient/builder) (Region/EU_WEST_1))))

(def queue-url
  (.sendMessage sqs-client (-> (GetQueueUrlRequest/builder)
                               (.queueName (:heratepalvelu-queue config))
                               .build)))
(defn build-hoks-hyvaksytty-msg [id hoks]
  {:ehoks-id id
   :kyselytyyppi "HOKS_hyvaksytty"
   :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
   :oppija-oid (:oppija-oid hoks)
   :sahkoposti (:sahkoposti hoks)
   :ensikertainen-hyvaksyminen (:ensikertainen-hyvaksyminen hoks)})

(defn send-message [msg]
  (.sendMessage sqs-client (-> (SendMessageRequest/builder)
                               (.queueUrl queue-url)
                               (.messageBody (json/write-str msg))
                               .build)))
