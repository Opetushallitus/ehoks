(ns oph.ehoks.common.api
  (:require [ring.util.http-response :as response]
            [oph.ehoks.logging.core :as log]
            [compojure.api.exception :as c-ex]
            [ring.middleware.session :as session]
            [ring.middleware.session.memory :as mem]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.middleware :as middleware]
            [clojure.string :as cstr]))

(defn not-found-handler [_ __ ___]
  (response/not-found {:reason "Route not found"}))

(defn exception-handler [^Exception ex & other]
  (let [ex-data (if (map? (first other)) (first other) (ex-data ex))]
    (log/errorf
      "Error: %s\nData: %s\nLog-data: %s\nStacktrace:\n%s"
      (.getMessage ex)
      ex-data
      (:log-data ex-data)
      (cstr/join "\n" (.getStackTrace ex))))
  (response/internal-server-error {:type "unknown-exception"}))

(def handlers
  {:not-found not-found-handler
   ::c-ex/default exception-handler})

(defn create-app
  ([app-routes session-store]
    (-> app-routes
        (middleware/wrap-cache-control-no-cache)
        (session/wrap-session
          {:store (or session-store (mem/memory-store))
           :cookie-attrs {:max-age (:session-max-age config (* 60 60 4))}})))
  ([app-routes] (create-app app-routes nil)))
