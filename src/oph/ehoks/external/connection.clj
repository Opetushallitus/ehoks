(ns oph.ehoks.external.connection
  (:require [oph.ehoks.config :refer [config]]
            [ring.util.http-predicates :as http-predicates]
            [oph.ehoks.external.http-client :as client]
            [clj-time.core :as t]
            [ring.util.codec :as codec]
            [clojure.tools.logging :as log]
            [clojure.string :as cstr])
  (:import [com.fasterxml.jackson.core JsonParseException]))

(defonce service-ticket
  (atom {:url nil
         :expires nil}))

(defonce cache
  (atom {}))

(def allowed-params
  #{:tutkintonimikkeet :tutkinnonosat :osaamisalat :category})

(def oid-pattern
  #"(\d+\.){5}\d+")

(defn expired? [response]
  (and (some? (:timestamp response))
       (t/before?
         (:timestamp response)
         (t/minus (t/now) (t/minutes (:ext-cache-lifetime-minutes config))))))

(defn expire-response! [url]
  (swap! cache dissoc url)
  nil)

(defn clean-cache! []
  (let [non-expired
        (reduce
          (fn [n [k v]]
            (if (expired? v)
              n
              (assoc n k v)))
          {}
          @cache)]
    (reset! cache non-expired)))

(defn get-cached [url]
  (when-let [response (get @cache url)]
    (when-not (expired? response)
      (log/debug "Using cached version for " url)
      response)))

(defn add-cached-response! [url response]
  (swap! cache assoc url
         (assoc response
                :timestamp (t/now)
                :ehoks-cached true
                :cached :HIT)))

(defn sanitaze-path [path]
  (when (some? path)
    (cstr/replace
      path
      oid-pattern
      "*FILTERED*")))

(defn sanitaze-params [options]
  (if (and (some? options) (some? (:query-params options)))
    (assoc
      options
      :query-params
      (reduce
        (fn [n [k v]]
          (if (contains? allowed-params k)
            (assoc n k v)
            (assoc n k "*FILTERED*")))
        {}
        (:query-params options)))
    options))

(defn- get-client-fn [method]
  (if (= method :post)
    client/post
    client/get))

(defn with-api-headers
  [{method :method service :service options :options path :path}]
  (try
    (let [client-method-fn (get-client-fn method)]
      (client-method-fn (if (some? path)
                          (format "%s/%s" service path)
                          service)
                        (-> options
                            (assoc-in [:headers "Caller-Id"]
                                      (:client-sub-system-code config))
                            (assoc :debug (:debug config false))
                            (assoc :cookie-policy :standard))))
    (catch Exception e
      (throw (ex-info "HTTP request error"
                      {:log-data {:method method
                                  :service service
                                  :path (sanitaze-path path)
                                  :query-params (sanitaze-params
                                                  (:query-params options))}}
                      e)))))

(defn encode-url [url params]
  (if (empty? params)
    url
    (format "%s?%s" url (codec/form-encode params))))

(defn with-cache!
  [{service :service options :options :as data}]
  (or (get-cached (encode-url service (:query-params options)))
      (let [response (with-api-headers data)]
        (add-cached-response!
          (encode-url service (:query-params options)) response)
        (assoc response :cached :MISS))))

(defn refresh-service-ticket! []
  (let [response (with-api-headers
                   {:method :post
                    :service (:cas-service-ticket-url config)
                    :options {:form-params {:username (:cas-username config)
                                            :password (:cas-password config)}}})
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
           {:method :post
            :service url
            :options {:form-params
                      {:service (str
                                  service "/j_spring_cas_security_check")}}})))

(defn add-cas-ticket [data service]
  (when (or (nil? (:url @service-ticket))
            (t/after? (t/now) (:expires @service-ticket)))
    (refresh-service-ticket!))
  (let [ticket (get-service-ticket (:url @service-ticket) service)]
    (-> data
        (assoc-in [:headers "accept"] "*/*")
        (assoc-in [:query-params :ticket] ticket))))

(defn with-service-ticket [data]
  (with-api-headers
    (update data :options add-cas-ticket (:service data))))
