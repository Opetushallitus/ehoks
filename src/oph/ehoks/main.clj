(ns oph.ehoks.main
  (:gen-class))

(defn -main []
  (require 'ring.adapter.jetty)
  (require 'oph.ehoks.config)
  (require 'oph.ehoks.handler)
  (let [run-jetty (resolve 'ring.adapter.jetty/run-jetty)
        config    (var-get (resolve 'oph.ehoks.config/config))
        app       (resolve 'oph.ehoks.handler/app)]
    (run-jetty app {:port  (:port config)
                    :join? true})))
