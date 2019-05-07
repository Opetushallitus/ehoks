(ns oph.ehoks.common.api
  (:require [ring.util.http-response :as response]
            [clojure.tools.logging :as log]
            [compojure.api.exception :as c-ex]
            [ring.middleware.session :as session]
            [ring.middleware.session.memory :as mem]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.middleware :as middleware]))

(def handlers
  {:not-found
   (fn [_ __ ___] (response/not-found))

   ::c-ex/default
   (fn [^Exception ex ex-data _]
     (if (contains? ex-data :log-data)
       (log/errorf ex "%s (data=%s)" (.getMessage ex) (:log-data ex-data))
       (log/error ex (.getMessage ex)))
     (response/internal-server-error {:type "unknown-exception"}))})

(defn create-app [app-routes session-store]
  (-> app-routes
      (middleware/wrap-cache-control-no-cache)
      (session/wrap-session
        {:store (or session-store (mem/memory-store))
         :cookie-attrs {:max-age (:session-max-age config (* 60 60 4))}})))
