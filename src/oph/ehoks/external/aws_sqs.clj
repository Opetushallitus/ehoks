(ns oph.ehoks.external.aws-sqs
  (:require [clojure.data.json :as json]
            [oph.ehoks.config :refer [config]]
            [clojure.tools.logging :as log])
  (:import (software.amazon.awssdk.services.sqs SqsClient)
           (software.amazon.awssdk.regions Region)
           (software.amazon.awssdk.services.sqs.model SendMessageRequest
                                                      GetQueueUrlRequest)))

(def sqs-client (-> (SqsClient/builder)
                    (.region (Region/EU_WEST_1))
                    (.build)))

(def queue-url
  (if (nil? (:env-stage config))
    (log/warn "Stage missing from env variables")
    (.queueUrl (.getQueueUrl sqs-client
                             (-> (GetQueueUrlRequest/builder)
                                 (.queueName
                                   (str (:env-stage config) "-"
                                        (:heratepalvelu-queue config)))
                                 (.build))))))

(defn build-hoks-hyvaksytty-msg [id hoks]
  {:ehoks-id id
   :kyselytyyppi "Amis_HOKS_hyväksytty"
   :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
   :oppija-oid (:oppija-oid hoks)
   :sahkoposti (:sahkoposti hoks)
   :alkupvm (:ensikertainen-hyvaksyminen hoks)})

(defn send-message [msg]
  (when (some? queue-url)
    (.sendMessage sqs-client (-> (SendMessageRequest/builder)
                                 (.queueUrl queue-url)
                                 (.messageBody (json/write-str msg))
                                 (.build)))))
