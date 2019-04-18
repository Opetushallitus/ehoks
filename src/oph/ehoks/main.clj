(ns oph.ehoks.main
  (:gen-class)
  (:require [oph.ehoks.db.migrations :as m]
            [clojure.string :refer [lower-case]]
            [clojure.tools.logging :as log]))

(defn -main [& args]
  (require 'ring.adapter.jetty)
  (require 'oph.ehoks.config)
  (require 'oph.ehoks.handler)
  (let [run-jetty (resolve 'ring.adapter.jetty/run-jetty)
        config (var-get (resolve 'oph.ehoks.config/config))
        app (resolve 'oph.ehoks.handler/app)]
    (when-not (some #(when (= (lower-case %) "--no-migrations") %) args)
      (log/info "Running migrations")
      (m/migrate!))
    (run-jetty app {:port  (:port config)
                    :join? true
                    :async? true})))
