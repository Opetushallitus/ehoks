(ns oph.ehoks.logging
  (:require [clojure.string :as cstr]
            [clojure.data.json :as json]
            [clj-time.core :as t]
            ;[oph.ehoks.config :refer [config]]
   ;         [oph.ehoks.ehoks-app :as ehoks-app]
   )
  (:import org.apache.logging.log4j.LogManager))

(defn- get-audit-logger []
  (LogManager/getLogger "audit"))

(defn- get-access-logger []
  (LogManager/getLogger "access"))

(def audit-logger ^:private (when-not *compile-files* (get-audit-logger)))

(def access-logger ^:private (when-not *compile-files* (get-access-logger)))

(defn format-message
  "Format message. All {{:key}} are replaced with value of :key in given map"
  [f m]
  (reduce
   (fn [s [k v]] (cstr/replace s (format "{{:%s}}" (name k)) (str v)))
   f
   m))

(defn- log-access-info [msg]
  (.info access-logger msg))
  
(defn- log-audit-info [msg]
  (.info audit-logger msg))

(defn accessf [f m]
  (log-access-info (format-message f m)))

(defn access [m]
  (log-access-info (json/json-str m)))

(defn- get-header [request k]
  (get-in request [:headers k]))

(defn- request-to-str [method request]
  (format "%s %s%s" 
          method 
          (:uri request) 
          (if (:query-string request) 
            (str "?" (:query-string request))
            "")))

(defn get-full-app-name []
  (let [app-name "" ;(ehoks-app/get-app-name)
        ]
    (if (= app-name "both")
      "ehoks"
      (format "ehoks_%s" app-name))))

(def service-name ^:private (get-full-app-name))

(defn get-session [request]
  "")

(defn to-access-map [request response total-time]
  (let [method (-> request :request-method name cstr/upper-case)]
    {:timestamp (str
                 (t/to-time-zone
                  (t/now)
                  (t/time-zone-for-id "Europe/Helsinki")))
     :responseCode (:status response)
     :request (request-to-str method request)
     :responseTime total-time
     :requestMethod method
     :service service-name
     :environment "" ;(:opintopolku-host config)
     :user-agent (get-header request "user-agent")
     :caller-id (get-header request "caller-id")
     :clientSubsystemCode (get-header request "clientsubsystemcode")
     :x-forwarded-for (get-header request "x-forwarded-for")
     :x-real-ip (get-header request "x-real-ip")
     :remote-ip (:remote-addr request)
     :session (get-session request)
     :response-size (get-header request "Content-Length")
     :referer (get-header request "referer")}))

(defn auditf [f m]
  (.info audit-logger (format-message f m)))

(defn audit [m]
  (log-audit-info (json/json-str m)))

(defn spy-access [request respond]  
  (let [current-ms (System/currentTimeMillis)]
    (fn [response]      
      (access (to-access-map 
               request response (- (System/currentTimeMillis) current-ms)))
      (respond response))))

(defn wrap-access-logger [handler]
  (fn
    ([request respond raise]
     (handler request (spy-access request respond) raise))
    ([request]
     (prn "Sync")
     (handler request))))