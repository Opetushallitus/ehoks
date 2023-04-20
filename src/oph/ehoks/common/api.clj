(ns oph.ehoks.common.api
  (:require [ring.util.http-response :as response]
            [clojure.string :as cstr]
            [clojure.tools.logging :as log]
            [compojure.api.exception :as c-ex]
            [ring.middleware.session :as session]
            [ring.middleware.session.memory :as mem]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.middleware :as middleware]
            [oph.ehoks.logging.access :refer [wrap-access-logger]]))

(defn not-found-handler
  "Käsittelee tapauksen, jossa reittiä ei löydy."
  [_ __ ___]
  (response/not-found {:reason "Route not found"}))

(defn log-exception
  "Logittaa virheen."
  [ex data]
  (log/errorf
    "Unhandled exception\n%s\n%s\n%s"
    (str ex)
    (str data)
    (cstr/join "\n" (.getStackTrace ex))))

(defn exception-handler
  "Käsittelee virhetilanteita."
  [^Exception ex & other]
  (let [exception-data (if (map? (first other)) (first other) (ex-data ex))]
    (log-exception ex exception-data))
  (response/internal-server-error {:type "unknown-exception"}))

(def handlers
  "Map of request handlers"
  {::c-ex/request-parsing (c-ex/with-logging
                            c-ex/request-parsing-handler :info)
   ::c-ex/request-validation (c-ex/with-logging
                               c-ex/request-validation-handler :info)
   ; Lokitetaan response bodyn validoinnissa esiin nousseet virheet, mutta ei
   ; välitetä virheitä käyttäjälle "500 Internal Server Error" -koodilla.
   ::c-ex/response-validation (c-ex/with-logging
                                c-ex/http-response-handler :error)
   :not-found not-found-handler
   ::c-ex/default exception-handler})

(defn create-app
  "Creates application with given routes and session store.
   If store is nil memory store is being used."
  ([app-routes session-store]
    (-> app-routes
        (middleware/wrap-cache-control-no-cache)
        (session/wrap-session
          {:store (or session-store (mem/memory-store))
           :cookie-attrs {:max-age (:session-max-age config (* 60 60 4))}})
        (wrap-access-logger)))
  ([app-routes] (create-app app-routes nil)))
