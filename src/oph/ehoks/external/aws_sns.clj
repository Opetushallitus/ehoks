(ns oph.ehoks.external.aws-sns
  (:require [cheshire.core :as json]
            [clojure.tools.logging :as log])
  (:import [com.amazonaws.services.sns.message
            SnsMessageManager
            SnsNotification
            SnsSubscriptionConfirmation
            SnsUnknownMessage
            SnsUnsubscribeConfirmation]
           [java.io InputStream ByteArrayInputStream]
           java.time.ZoneId))

(def manager (new SnsMessageManager "eu-west-1"))

(defmulti handle-message (fn [manager s] (class s)))

(defmethod handle-message InputStream [sns-message-manager input-stream]
  (->> input-stream
       (.parseMessage (:sns-message-manager sns-message-manager))
       (handle-message sns-message-manager)))

(defmethod handle-message String [sns-message-manager string]
  (->> (.getBytes string "UTF-8")
       (new ByteArrayInputStream)
       (handle-message sns-message-manager)))

(defmethod handle-message SnsNotification
  [_ message]
  message)

(defmethod handle-message SnsSubscriptionConfirmation
  [_ message]
  (log/info "Confirming subscription to" (.getTopicArn message))
  (.confirmSubscription message)
  (log/info "Subscription to" (.getTopicArn message) "confirmed"))

(defmethod handle-message SnsUnknownMessage
  [_ message]
  (log/error "Unknown SNS message" message))

(defmethod handle-message SnsUnsubscribeConfirmation
  [_ message]
  (log/warn "Unsubscribed from" (.getTopicArn message)))

(defn- parse-henkilo-modified-message
  [s]
  (if-let [oid (:oidHenkilo (json/parse-string s true))]
    oid
    (throw (new RuntimeException
                (str "Could not find key oidHenkilo from message '" s "'")))))

(defn try-handle-message
  [message]
  (try
    (some->> message
             .getBody
             (handle-message manager)
             .getMessage
             parse-henkilo-modified-message)
    message
    (catch Exception e
      (log/warn e "Handling henkilö modified message failed"))))
