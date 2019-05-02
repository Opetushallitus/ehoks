(ns oph.ehoks.main
  (:gen-class)
  (:require [oph.ehoks.db.migrations :as m]
            [clojure.string :refer [lower-case]]
            [clojure.tools.logging :as log]))

(defn has-arg? [args s]
  (some? (some #(when (= (lower-case %) s) %) args)))

(defn -main [& args]
  (require 'ring.adapter.jetty)
  (require 'oph.ehoks.config)
  (require 'oph.ehoks.handler)
  (cond
    (has-arg? args "--help")
    (do (println "eHOKS")
        (println "Usage: java -jar {uberjar-filename}.jar [options]")
        (println "Options:")
        (println "--run-migrations    Run migrations")
        (println "--help              Print this help"))
    (has-arg? args "--run-migrations")
    (do (log/info "Running migrations")
        (m/migrate!)
        0)
    :else
    (let [run-jetty (resolve 'ring.adapter.jetty/run-jetty)
          config (var-get (resolve 'oph.ehoks.config/config))
          app (resolve 'oph.ehoks.handler/app)]
      (run-jetty app {:port  (:port config)
                      :join? true
                      :async? true}))))
