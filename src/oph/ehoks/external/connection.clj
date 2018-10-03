(ns oph.ehoks.external.connection
  (:require [oph.ehoks.config :refer [config]]
            [clj-http.client :as client]
            [cheshire.core :as cheshire]
            [clj-time.core :as t]
            [clojure.core.async :as a])
  (:import [com.fasterxml.jackson.core JsonParseException]))

(defonce service-ticket
  (atom {:url nil
         :expires nil}))

(defn refresh-service-ticket! []
  (let [response (client/post (:cas-service-ticket-url config)
                              {:debug (:debug config false)
                               :form-params {:username (:cas-username config)
                                             :password (:cas-password config)}})
        url (get-in response [:headers "location"])]
    (if (and (= (:status response) 201)
             (seq url))
      (reset! service-ticket
              {:url url
               :expires (t/plus (t/now) (t/hours 2))})
      (throw (ex-info "Failed to refresh CAS Service Ticket"
                      {:response response})))))

(defn get-service-ticket [url service]
  (:body (client/post
           url
           {:debug (:debug config false)
            :form-params
            {:service (str service "/j_spring_cas_security_check")}})))

(defn add-cas-ticket [service data]
  (when (or (nil? (:url @service-ticket))
            (t/after? (t/now) (:expires @service-ticket)))
    (refresh-service-ticket!))
  (let [ticket (get-service-ticket (:url @service-ticket) service)]
    (-> data
        (assoc-in
          [:headers "clientSubSystemCode"] (:client-sub-system-code config))
        (assoc-in [:headers "accept"] "*/*")
        (assoc-in [:query-params :ticket] ticket))))

(defn api-get [service path data]
  (let [response (client/get (format "%s/%s" service path)
                             (assoc (add-cas-ticket service data)
                                    :debug (:debug config false)))]
    (try
      (update response :body cheshire/parse-string true)
      (catch JsonParseException _
        response))))

(defmacro with-timeout
  "Simple macro for creating asyncronous timeout-safe function calls.
   `body` will be wrapped inside go block.
   Returns either result of body or error depending on if given time exceeds."
  [time-ms body error]
  `(a/go
     (let [c# (a/go ~body)
           [v# p#] (a/alts! [c# (a/timeout ~time-ms)])]
       (if (and (not= p# c#) (nil? v#))
         ~error
         v#))))
