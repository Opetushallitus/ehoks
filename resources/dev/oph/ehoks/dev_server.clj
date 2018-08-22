(ns oph.ehoks.dev-server
  (:require [oph.ehoks.handler :refer [app]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [oph.ehoks.config :refer [config]]))

(defn wrap-dev-cors [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
          (assoc-in [:headers "Access-Control-Allow-Origin"]
                    (format "%s:%d"
                            (get-in config [:frontend :url])
                            (get-in config [:frontend :port])))
          (assoc-in [:headers "Access-Control-Allow-Credentials"] "true")))))

(def dev-app
  (wrap-dev-cors app))

(defn start-server []
  (jetty/run-jetty (wrap-reload dev-app)
     {:port (:port config) :join? false}))
