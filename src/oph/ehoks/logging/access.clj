(ns oph.ehoks.logging.access
  (:require [clojure.tools.logging :as log]
            [clojure.string :as cstr]
            [oph.ehoks.config :refer [config]]
            [environ.core :refer [env]]
            [clojure.data.json :as json])
  (:import (java.time ZonedDateTime ZoneId)))

(def ^:private service-name
  "Global service name"
  (cstr/lower-case (:name env (or (System/getProperty "name") "both"))))

(defn- get-header
  "Get header k from request headers"
  [request k]
  (get-in request [:headers k]))

(defn- request-to-str
  "Convert request to string"
  [method request]
  (format "%s %s%s"
          method
          (:uri request)
          (if (:query-string request)
            (str "?" (:query-string request))
            "")))

(defn get-session
  "Get ring session from request cookies"
  [request]
  (get-in request [:cookies "ring-session" :value]))

(defn current-fin-time-str
  "Get current time in Helsinki as string"
  []
  (str (ZonedDateTime/now (ZoneId/of "Europe/Helsinki"))))

(defn to-access-map
  "Convert request, response, and total time taken to access map"
  [request response total-time]
  (let [method (-> request :request-method name cstr/upper-case)]
    {:timestamp (current-fin-time-str)
     :response-code (:status response)
     :request (request-to-str method request)
     :response-time total-time
     :request-method method
     :service service-name
     :environment (:opintopolku-host config)
     :user-agent (get-header request "user-agent")
     :caller-id (get-header request "caller-id")
     :x-forwarded-for (get-header request "x-forwarded-for")
     :x-real-ip (get-header request "x-real-ip")
     :remote-ip (:remote-addr request)
     :session (get-session request)
     :content-length (get-header request "Content-Length")
     :referer (get-header request "referer")}))

(defn- log-access-map
  "Log access map at level INFO"
  [m]
  (log/log "access" :info nil (json/write-str m)))

(defn- spy-access
  "Wraps response in function that handles logging using log-access-map, with
  the request's time computed from the point when that function is created"
  [request respond]
  (let [current-ms (System/currentTimeMillis)]
    (fn [response]
      (log-access-map (to-access-map
                        request response (- (System/currentTimeMillis)
                                            current-ms)))
      (respond response))))

(defn- spy-access-sync
  "Performs request, and logs request and respones before returning response"
  [handler request]
  (let [current-ms (System/currentTimeMillis)
        response (handler request)]
    (log-access-map
      (to-access-map
        request response (- (System/currentTimeMillis) current-ms)))
    response))

(defn wrap-access-logger
  "Wrapper handler in logging function"
  [handler]
  (fn
    ([request respond raise]
      (try
        (handler request (spy-access request respond) raise)
        (catch Exception e
          (log-access-map
            (to-access-map request {} -1))
          (throw e))))
    ([request]
      (try
        (spy-access-sync handler request)
        (catch Exception e
          (log-access-map
            (to-access-map request {} -1))
          (throw e))))))
