(ns oph.ehoks.external.aws-sqs
  (:require [clojure.data.json :as json]
            [oph.ehoks.config :refer [config]]
            [environ.core :refer [env]]
            [clojure.tools.logging :as log]
            [clj-time.format :as f])
  (:import (software.amazon.awssdk.services.sqs SqsClient)
           (software.amazon.awssdk.regions Region)
           (software.amazon.awssdk.services.sqs.model SendMessageRequest
                                                      GetQueueUrlRequest)))

(def sqs-client (-> (SqsClient/builder)
                    (.region (Region/EU_WEST_1))
                    (.build)))

(def queue-url
  (if (nil? (:env-stage env))
    (log/warn "Stage missing from env variables")
    (do
      (log/info (str (:env-stage env) "-"
                     (:heratepalvelu-queue config)))
      (.queueUrl (.getQueueUrl sqs-client
                               (-> (GetQueueUrlRequest/builder)
                                   (.queueName
                                     (str (:env-stage env) "-"
                                          (:heratepalvelu-queue config)))
                                   (.build)))))))

(defn build-hoks-hyvaksytty-msg [id hoks]
  {:ehoks-id id
   :kyselytyyppi "Amis_HOKS_hyväksytty"
   :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
   :oppija-oid (:oppija-oid hoks)
   :sahkoposti (:sahkoposti hoks)
   :alkupvm (f/unparse-local-date
              (:date f/formatters)
              (:ensikertainen-hyvaksyminen hoks))})

(defn send-message [msg]
  (when (some? queue-url)
    (.sendMessage sqs-client (-> (SendMessageRequest/builder)
                                 (.queueUrl queue-url)
                                 (.messageBody (json/write-str msg))
                                 (.build)))))
