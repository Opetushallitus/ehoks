(ns oph.ehoks.external.aws-sqs
  (:require [clojure.data.json :as json]
            [oph.ehoks.config :refer [config]]
            [clojure.tools.logging :as log])
  (:import (software.amazon.awssdk.services.sqs SqsClient)
           (software.amazon.awssdk.regions Region)
           (software.amazon.awssdk.services.sqs.model SendMessageRequest GetQueueUrlRequest)))

(def sqs-client (-> (SqsClient/builder)
                    (.region (Region/EU_WEST_1))
                    (.build)))

(def queue-url
  (if (nil? (:heratepalvelu-queue config))
    (log/warn "Heratepalvelu-queue name missing from config")
    (.queueUrl (.getQueueUrl sqs-client (-> (GetQueueUrlRequest/builder)
                                            (.queueName (:heratepalvelu-queue config))
                                            (.build))))))

(defn build-hoks-hyvaksytty-msg [id hoks]
  {:ehoks-id id
   :kyselytyyppi "HOKS_hyvaksytty"
   :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
   :oppija-oid (:oppija-oid hoks)
   :sahkoposti (:sahkoposti hoks)
   :ensikertainen-hyvaksyminen (:ensikertainen-hyvaksyminen hoks)})

(defn send-message [msg]
  (when (some? queue-url)
    (.sendMessage sqs-client (-> (SendMessageRequest/builder)
                                 (.queueUrl queue-url)
                                 (.messageBody (json/write-str msg))
                                 (.build)))))
