(ns oph.ehoks.external.connection
  (:require [oph.ehoks.config :refer [config]]
            [clj-http.client :as client]
            [clj-time.core :as t])
  (:import [com.fasterxml.jackson.core JsonParseException]))

(defonce service-ticket
  (atom {:url nil
         :expires nil}))

(def client-functions
  {:get client/get
   :post client/post})

(defn with-api-headers [method url options]
  (let [client-method-fn (get client-functions method)]
    (client-method-fn
      url
      (-> options
          (assoc-in [:headers "Caller-Id"] (:client-sub-system-code config))
          (assoc :debug (:debug config false))
          (assoc :cookie-policy :standard)))))

(defn refresh-service-ticket! []
  (let [response (with-api-headers
                   :post
                   (:cas-service-ticket-url config)
                   {:form-params {:username (:cas-username config)
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
  (:body (with-api-headers
           :post
           url
           {:form-params
            {:service (str service "/j_spring_cas_security_check")}})))

(defn add-cas-ticket [service data]
  (when (or (nil? (:url @service-ticket))
            (t/after? (t/now) (:expires @service-ticket)))
    (refresh-service-ticket!))
  (let [ticket (get-service-ticket (:url @service-ticket) service)]
    (-> data
        (assoc-in [:headers "accept"] "*/*")
        (assoc-in [:query-params :ticket] ticket))))

(defn with-service-ticket [method service path options]
  (with-api-headers
    method
    (format "%s/%s" service path)
    (add-cas-ticket service options)))
