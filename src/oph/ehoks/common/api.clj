(ns oph.ehoks.common.api
  (:require [ring.util.http-response :as response]
            [clojure.tools.logging :as log]
            [compojure.api.exception :as c-ex]
            [ring.middleware.session :as session]
            [ring.middleware.session.memory :as mem]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.middleware :as middleware]
            [oph.ehoks.logging.access :refer [wrap-access-logger]]))

(defn not-found-handler [_ __ ___]
  (response/not-found {:reason "Route not found"}))

(defn log-exception [ex]
  (let [ex-map (Throwable->map ex)]
    (log/errorf
      "Unhandled exception\n%s\n%s\n%s\n-------------Exception end-------------"
      (str (:cause ex-map))
      (str (:via ex-map))
      (str (:trace ex-map)))))

(defn exception-handler [^Exception ex & other]
  (log-exception ex)
  (response/internal-server-error {:type "unknown-exception"}))

(def handlers
  {::c-ex/request-parsing (c-ex/with-logging
                            c-ex/request-parsing-handler :info)
   ::c-ex/request-validation (c-ex/with-logging
                               c-ex/request-validation-handler :info)
   ::c-ex/response-validation (c-ex/with-logging
                                c-ex/response-validation-handler :error)
   :not-found not-found-handler
   ::c-ex/default exception-handler})

(defn create-app
  "Creates application with given routes and session store.
   If store is nil memory store is being used"
  ([app-routes session-store]
    (-> app-routes
        (middleware/wrap-cache-control-no-cache)
        (session/wrap-session
          {:store (or session-store (mem/memory-store))
           :cookie-attrs {:max-age (:session-max-age config (* 60 60 4))}})
        (wrap-access-logger)))
  ([app-routes] (create-app app-routes nil)))
