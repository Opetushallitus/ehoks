(ns oph.ehoks.palaute.scheduler
  (:require [chime.core :as chime]
            [clojure.tools.logging :as log]
            [oph.ehoks.utils :as util]
            [oph.ehoks.palaute.initiation :as palaute]
            [oph.ehoks.palaute.vastaajatunnus :as vt])
  (:import (java.lang AutoCloseable)
           (java.time Instant LocalTime ZonedDateTime ZoneId Period Duration)))

(defn daily-actions!
  "Run all palaute checks that need to be run on a daily basis."
  [_]
  (log/info "Commencing palaute daily actions.")
  (try
    true  ;; nothing at the moment
    (catch Exception e
      (log/error e "Unhandled exception in daily-actions!.")))
  (log/info "Done palaute daily actions.")
  true)

(defn hourly-actions!
  "Run all palaute checks that are to be run every hour."
  [_]
  (log/info "Commencing palaute hourly actions.")
  (try
    (log/info "Initialising palautteet for next batch of uninitialised HOKSes.")
    (let [result (util/with-timeout
                   (* 45 60 1000)
                   #(palaute/reinit-palautteet-for-uninitiated-hokses! 300))]
      (log/info "reinit-palautteet-for-uninitiated-hokses!: ended with result"
                result))
    (catch Exception e
      (log/error e "Unhandled exception in hourly-actions!.")))
  (log/info "Done palaute hourly actions.")
  true)

(defn time->instant
  "Converts a specific time of day into an instant on today"
  [hour minute sec]
  (-> (LocalTime/of hour minute sec)
      (.adjustInto (ZonedDateTime/now (ZoneId/of "Europe/Helsinki")))
      (Instant/from)))

(def schedules
  [{:start (time->instant 6 0 0) :period (Period/ofDays 1)
    :action daily-actions!}

   {:start (time->instant 1 0 0) :period (Duration/ofHours 1)
    :action hourly-actions!}

   {:start (time->instant 1 0 0) :period (Duration/ofMinutes 5)
    :action (fn [_]
              (try
                (log/info (str "Handling palautteet that have reached "
                               "their heratepvm."))
                (vt/handle-palautteet-waiting-for-heratepvm!
                  ["aloittaneet" "valmistuneet" "osia_suorittaneet"
                   "tyopaikkajakson_suorittaneet"])
                (catch Exception e
                  (log/error e (str "Unhandled exception in scheduled "
                                    "handling of palautteet")))))}])

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
           :error-handler (fn [e]
                            (log/error e "Error in scheduler")
                            true)})]
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
