(ns oph.ehoks.logging.core
  (:require [clojure.string :as cstr]
            [clojure.data.json :as json]
            [oph.ehoks.config :refer [config]])
  (:import org.apache.logging.log4j.LogManager))

(def ^:private audit-logger
  (when-not *compile-files* (LogManager/getLogger "audit")))

(def ^:private access-logger
  (when-not *compile-files* (LogManager/getLogger "access")))

(def ^:private root-logger
  (when-not *compile-files* (LogManager/getLogger)))

(defn format-message
  "Format message. All {{:key}} are replaced with value of :key in given map"
  [f m]
  (reduce
    (fn [s [k v]] (cstr/replace s (format "{{:%s}}" (name k)) (str v)))
    f
    m))

(defn- log-access-info [^String msg]
  (when (:logging? config) (.info access-logger msg)))

(defn- log-audit-info [^String msg]
  (when (:logging? config) (.info audit-logger msg)))

(defn- log-debug [^String msg]
  (when (:logging? config) (.debug root-logger msg)))

(defn- log-info [^String msg]
  (when (:logging? config) (.info root-logger msg)))

(defn- log-warn [^String msg]
  (when (:logging? config) (.warn root-logger msg)))

(defn- log-error [^String msg]
  (when (:logging? config) (.error root-logger msg)))

(defn audit [m]
  (log-audit-info (json/json-str m)))

(defn access [m]
  (log-access-info (json/json-str m)))

(defn debug [^String s]
  (log-debug s))

(defn debugf [^String s & args]
  (log-debug (apply format s args)))

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
