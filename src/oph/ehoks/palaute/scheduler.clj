(ns oph.ehoks.palaute.scheduler
  (:require [chime.core :as chime]
            [clojure.tools.logging :as log]
            [oph.ehoks.palaute.vastaajatunnus :as palaute])
  (:import (java.lang AutoCloseable)
           (java.time Instant LocalTime ZonedDateTime ZoneId Period)))

(defn daily-actions!
  "Run all palaute checks that need to be run on a daily basis."
  [opts]
  (log/info "Commencing palaute daily actions.")
  (try
    (log/info "Handling palautteet that have reached their heratepvm.")
    (palaute/handle-palautteet-waiting-for-heratepvm!
      ["aloittaneet" "valmistuneet" "osia_suorittaneet"
       "tyopaikkajakson_suorittaneet"])
    (catch Exception e
      (log/error e "Unhandled exception in daily-actions!.")))
  (log/info "Done palaute daily actions.")
  true)

(defn time->instant
  "Converts a specific time of day into an instant on today"
  [hour minute sec]
  (-> (LocalTime/of hour minute sec)
      (.adjustInto (ZonedDateTime/now (ZoneId/of "Europe/Helsinki")))
      Instant/from))

(def schedules
  [{:action daily-actions!
    :start (time->instant 6 0 0) :period (Period/ofDays 1)}])

(defn run-scheduler!
  "Run a given action periodically starting at start-time and repeating
  at rate."
  ^AutoCloseable [action start-time rate]
  (log/info "Starting palaute scheduler with start time" start-time
            "and rate" rate)
  (let [scheduler
        (chime/chime-at
          (chime/periodic-seq start-time rate)
          action
          {:on-finished (fn [] (log/info "Palaute scheduler stopped."))
           :error-handler (fn [e] (log/warn e "Error in scheduler"))})]
    (.addShutdownHook
      (Runtime/getRuntime)
      (Thread. (fn []
                 (log/info "Stopping palaute scheduler.")
                 (.close scheduler))))))

(defn run-schedulers!
  "Run all schedulers used by palaute-backend, possibly overriding
  the timing."
  [& [override-start override-rate]]
  (doseq [{:keys [action start period]} schedules]
    (run-scheduler!
      action (or override-start start) (or override-rate period))))
