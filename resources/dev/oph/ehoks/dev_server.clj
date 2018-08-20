(ns oph.ehoks.dev-server
  (:require [oph.ehoks.handler :refer [app]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]))

(defn start-server []
  (jetty/run-jetty (wrap-reload #'app)
     {:port 3000 :join? false}))
