(ns oph.ehoks.external.connection
  (:require [oph.ehoks.config :refer [config]]
            [ring.util.http-predicates :as http-predicates]
            [clj-http.client :as client]
            [clj-time.core :as t]
            [ring.util.codec :as codec]
            [clojure.tools.logging :as log])
  (:import [com.fasterxml.jackson.core JsonParseException]))

(defonce service-ticket
  (atom {:url nil
         :expires nil}))

(defonce cache
  (atom {}))

(def client-functions
  {:get client/get
   :post client/post})

(defn expired? [response]
  (and (some? (:timestamp response))
       (t/before?
         (:timestamp response)
         (t/minus (t/now) (t/minutes (:ext-cache-lifetime-minutes config))))))

(defn expire-response! [url]
  (swap! cache dissoc url)
  nil)

(defn clean-cache! []
  (let [non-expired (reduce (fn [n [k v]]
                              (if (expired? v)
                                n
                                (assoc n k v)))
                            {}
                            @cache)]
    (reset! cache non-expired)))

(defn get-cached! [url]
  (when-let [response (get @cache url)]
    (if (expired? response)
      (expire-response! url)
      (do
        (log/debug "Using cached version for " url)
        response))))

(defn add-cached-response! [url response]
  (swap! cache assoc url
         (assoc response
                :timestamp (t/now)
                :ehoks-cached true
                :cached :HIT)))

(defn sanitaze-params [options]
  ; TODO Sanitaze all personal info https://jira.csc.fi/browse/EH-150
  ;      Add tests as well
  (if (some? (get-in options [:query-params :hetu]))
    (assoc-in options [:query-params :hetu] "XXXXXXxXXXX")
    options))

(defn with-api-headers [method url options]
  (try
    (let [client-method-fn (get client-functions method)]
      (client-method-fn url
                        (-> options
                            (assoc-in [:headers "Caller-Id"]
                                      (:client-sub-system-code config))
                            (assoc :debug (:debug config false))
                            (assoc :cookie-policy :standard))))
    (catch Exception e
      (throw (ex-info "HTTP request error"
                      {:log-data {:method method :url url}}
                      e)))))

(defn encode-url [url params]
  (if (empty? params)
    url
    (format "%s?%s" url (codec/form-encode params))))

(defn with-cache! [method url options]
  (if-some [cached-response (get-cached!
                              (encode-url url (:query-params options)))]
    cached-response
    (let [response (with-api-headers method url options)]
      (add-cached-response! (encode-url url (:query-params options)) response)
      (assoc response :cached :MISS))))

(defn refresh-service-ticket! []
  (let [response (with-api-headers
                   :post
                   (:cas-service-ticket-url config)
                   {:form-params {:username (:cas-username config)
                                  :password (:cas-password config)}})
        url (get-in response [:headers "location"])]
    (if (and (http-predicates/created? response)
             (seq url))
      (reset! service-ticket
              {:url url
               :expires (t/plus (t/now) (t/hours 2))})
      (throw (ex-info "Failed to refresh CAS Service Ticket"
                      {:response response
                       :log-data {:status (:status response)
                                  :body (:body response)}})))))

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
