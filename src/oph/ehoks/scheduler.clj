(ns oph.ehoks.scheduler
  (:require [clojure.core.async :as a]))

(def jobs ^:private (atom {}))

(defn add-job
  "Schedule job"
  [id interval f & args]
  (let [c (a/chan (a/sliding-buffer 1))]
    (swap! jobs assoc id c)
    (a/go-loop []
      (let [start (System/currentTimeMillis)]
        (when-some [result (try
                             (apply f args)
                             (catch Exception e
                               e))]
          (a/>! c result))
        (let [time-left (- interval (- (System/currentTimeMillis) start))]
          (when (pos? time-left)
            (Thread/sleep time-left)))
        (when (some? (get @jobs id))
          (recur))))
    c))

(defn remove-job
  "Remove scheduled job"
  [id]
  (if-some [c (get @jobs id)]
    (do (a/close! c)
        (swap! jobs dissoc id)
        true)
    false))

(defn get-jobs
  "Get map of all scheduled jobs"
  []
  @jobs)

(defn get-job
  "Get one specific job by ID"
  [id]
  (get @jobs id))
