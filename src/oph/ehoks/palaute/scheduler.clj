(ns oph.ehoks.palaute.scheduler
  (:require [chime.core :as chime]
            [clojure.tools.logging :as log]
            [oph.ehoks.palaute.vastaajatunnus :as palaute])
  (:import (java.lang AutoCloseable)))

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

(defn run-scheduler!
  "Simple (daily) scheduler for palaute scheduled tasks. Will be replaced with
  more configurable one later on."
  ^AutoCloseable [start-time rate]
  (log/infof "Starting palaute scheduler with start time %s and rate %s."
             start-time
             rate)
  (let [scheduler
        (chime/chime-at (chime/periodic-seq start-time rate)
                        daily-actions!
                        {:on-finished
                         (fn []
                           (log/info "Palaute scheduler stopped."))
                         :error-handler
                         (fn [e]
                           (log/warn e "Error in scheduler"))})]
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. (fn []
                                 (log/info "Stopping palaute scheduler.")
                                 (.close scheduler))))))
