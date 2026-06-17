(ns oph.ehoks.main
  (:gen-class)
  (:require [clojure.string :refer [lower-case]]
            [clojure.tools.logging :as log]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.db.migrations :as m]
            [oph.ehoks.db.session-store :as session-store]
            [oph.ehoks.ehoks-app :as ehoks-app]
            [oph.ehoks.logging.audit :as audit]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.palaute.scheduler :as scheduler]
            [ring.adapter.jetty :as jetty])
  (:import (java.time Instant Duration)))

(defn has-arg?
  "Is arg present"
  [args s]
  (some? (some #(when (= (lower-case %) s) %) args)))

(defn populate-oppijaindex []
  (future
    (log/info "Updating oppijaindex")
    (oppijaindex/update-oppijat-without-index!)
    (oppijaindex/update-opiskeluoikeudet-without-index!)
    (log/info "Updating oppijaindex finished")))

(defn start-app-server! [app app-name config-file dev?]
  (when (some? config-file)
    (System/setProperty "config" config-file)
    (require 'oph.ehoks.config :reload)
    (when (.endsWith (:opintopolku-host config) "opintopolku.fi")
      (log/info "Using prod urls")
      (System/setProperty
        "services_file"
        "resources/prod/services-oph.properties"))
    (require 'oph.ehoks.external.oph-url :reload))
  (log/info "Running migrations")
  (m/migrate!)
  (log/info "Migrations done.")
  (when audit/enabled?
    (audit/start-heartbeat!))
  (when (= app-name "ehoks-palaute")
    (populate-oppijaindex)
    (if dev?
      (scheduler/run-schedulers! (Instant/now) (Duration/ofSeconds 60))
      (scheduler/run-schedulers!)))
  (log/infof "Starting %s listening to port %d" app-name (:port config))
  (jetty/run-jetty app {:port (:port config) :join? (not dev?) :async? true}))

(defn -main
  "Main entry point"
  [& args]
  (cond
    (has-arg? args "--help")
    (do (println "eHOKS")
        (println "Usage: java -jar {uberjar-filename}.jar [options]")
        (println "Options:")
        (println "--run-migrations    Run migrations")
        (println "--help              Print this help"))
    (has-arg? args "--run-migrations")
    (do (println "Running migrations")
        (m/migrate!)
        0)
    :else
    (let [app-name (ehoks-app/get-app-name)
          hoks-app (common-api/create-app
                     (ehoks-app/create-app app-name)
                     (session-store/db-store))]
      (start-app-server! hoks-app app-name nil false))))
