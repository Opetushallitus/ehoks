(ns oph.ehoks.common.api
  (:require [ring.util.http-response :as response]
            [clojure.tools.logging :as log]
            [compojure.api.exception :as c-ex]
            [ring.middleware.session :as session]
            [ring.middleware.session.memory :as mem]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.middleware :as middleware]
            [clojure.string :as cstr]))

(def sessions (atom {}))

(defn not-found-handler [_ __ ___]
  (response/not-found {:reason "Route not found"}))

(defn log-exception [ex data]
  (log/errorf
    "Unhandled exception\n%s\n%s"
    (str ex)
    (cstr/join "\n" (.getStackTrace ex))))

(defn exception-handler [^Exception ex & other]
  (let [exception-data (if (map? (first other)) (first other) (ex-data ex))]
    (log-exception ex exception-data))
  (response/internal-server-error {:type "unknown-exception"}))

(def handlers
  {:not-found not-found-handler
   ::c-ex/default exception-handler})

(defn create-app
  ([app-routes session-store]
    (-> app-routes
        (middleware/wrap-cache-control-no-cache)
        (session/wrap-session
          {:store (or session-store (mem/memory-store sessions))
           :cookie-attrs {:max-age (:session-max-age config (* 60 60 4))}})))
  ([app-routes] (create-app app-routes nil)))
