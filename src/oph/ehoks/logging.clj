(ns oph.ehoks.logging
  (:require [clojure.string :as cstr]
            [clojure.data.json :as json]
            [clj-time.core :as t]
            [oph.ehoks.config :refer [config]]
            [environ.core :refer [env]])
  (:import org.apache.logging.log4j.LogManager))

(defn- get-audit-logger []
  (LogManager/getLogger "audit"))

(defn- get-access-logger []
  (LogManager/getLogger "access"))

(defn- get-root-logger []
  (LogManager/getLogger))

(def ^:private audit-logger (when-not *compile-files* (get-audit-logger)))

(def ^:private access-logger (when-not *compile-files* (get-access-logger)))

(def ^:private root-logger (when-not *compile-files* (get-root-logger)))

(def ^:private service-name
  (cstr/lower-case (:name env (or (System/getProperty "name") "ehoks-both"))))

(defn format-message
  "Format message. All {{:key}} are replaced with value of :key in given map"
  [f m]
  (reduce
    (fn [s [k v]] (cstr/replace s (format "{{:%s}}" (name k)) (str v)))
    f
    m))

(defn- log-access-info [msg]
  (when (:logging? config) (.info access-logger msg)))

(defn- log-audit-info [msg]
  (when (:logging? config) (.info audit-logger msg)))

(defn- log-debug [msg]
  (when (:logging? config) (.debug root-logger msg)))

(defn- log-info [msg]
  (when (:logging? config) (.info root-logger msg)))

(defn- log-warn [msg]
  (when (:logging? config) (.warn root-logger msg)))

(defn- log-error [msg]
  (when (:logging? config) (.error root-logger msg)))

(defn- audit [m]
  (log-audit-info (json/json-str m)))

(defn- access [m]
  (log-access-info (json/json-str m)))

(defn debug [^String s]
  (log-debug s))

(defn info [^String s]
  (log-info s))

(defn infof [^String s & args]
  (log-info (apply format s args)))

(defn error [^String s]
  (log-error s))

(defn errorf [^String s & args]
  (log-error (apply format s args)))

(defn warn [^String s]
  (log-warn s))

(defn warnf [^String s & args]
  (log-warn (apply format s args)))

(defn- get-header [request k]
  (get-in request [:headers k]))

(defn- request-to-str [method request]
  (format "%s %s%s"
          method
          (:uri request)
          (if (:query-string request)
            (str "?" (:query-string request))
            "")))

(defn get-session [request]
  (get-in request [:cookies "ring-session" :value]))

(defn- current-fin-time-str []
  (str (t/to-time-zone (t/now) (t/time-zone-for-id "Europe/Helsinki"))))

(defn to-access-map [request response total-time]
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

(defn- spy-access [request respond]
  (let [current-ms (System/currentTimeMillis)]
    (fn [response]
      (access (to-access-map
                request response (- (System/currentTimeMillis) current-ms)))
      (respond response))))

(defn- spy-access-sync [handler request]
  (let [current-ms (System/currentTimeMillis)
        response (handler request)]
    (access
      (to-access-map
        request response (- (System/currentTimeMillis) current-ms)))
    response))

(defn wrap-access-logger [handler]
  (fn
    ([request respond raise]
      (handler request (spy-access request respond) raise))
    ([request]
      (spy-access-sync handler request))))