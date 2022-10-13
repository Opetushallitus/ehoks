(ns oph.ehoks.main
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [oph.ehoks.db.migrations :as m]
            [clojure.string :refer [lower-case]]
            [clojure.tools.logging :as log]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.ehoks-app :as ehoks-app]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.db.session-store :as session-store]))

(defn has-arg?
  "Is arg present"
  [args s]
  (some? (some #(when (= (lower-case %) s) %) args)))

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
          hoks-app
          (common-api/create-app
            (ehoks-app/create-app app-name)
            (session-store/db-store))]
      (log/infof "Starting %s listening to port %d" app-name (:port config))
      (log/info "Running migrations")
      (m/migrate!)
      (log/info "Migrations done.")
      (log/info "Starting oppijaindex update in another thread.")
      (future
        (log/info "Updating oppijaindex")
        ;(oppijaindex/update-oppijat-without-index!)
        ;(oppijaindex/update-opiskeluoikeudet-without-index!)
        (log/info "Updating oppijaindex finished"))
      (jetty/run-jetty hoks-app {:port (:port config)
                                 :join? true
                                 :async? true}))))
