(ns oph.ehoks.palaute.scheduler
  (:require [chime.core :as chime]
            [clojure.tools.logging :as log]
            [oph.ehoks.palaute.opiskelija :as amis]
            [oph.ehoks.palaute.tyoelama :as tep])
  (:import (java.lang AutoCloseable)))

(defn- run-sequence
  "Run these tasks sequentially."
  [opts]
  (amis/create-and-save-arvo-kyselylinkki-for-all-needed! opts)
  (tep/create-and-save-arvo-vastaajatunnus-for-all-needed! opts))

(defn run-scheduler!
  "Simple (daily) scheduler for palaute scheduled tasks. Will be replaced with
  more configurable one later on."
  ^AutoCloseable [start-time rate]
  (log/infof "Starting palaute scheduler with start time %s and rate %s."
             start-time
             rate)
  (let [scheduler
        (chime/chime-at (chime/periodic-seq start-time rate)
                        run-sequence
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
