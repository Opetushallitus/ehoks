(ns oph.ehoks.common.api
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [compojure.api.exception :as c-ex]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.logging.access :refer [wrap-access-logger]]
            [oph.ehoks.logging.audit :as audit]
            [oph.ehoks.middleware :as middleware]
            [ring.middleware.session :as session]
            [ring.middleware.session.memory :as mem]
            [ring.util.http-response :as response]))

(defn not-found-handler
  "Käsittelee tapauksen, jossa reittiä ei löydy."
  [_ __ ___]
  (response/not-found {:reason "Route not found"}))

(defn unauthorized-handler
  "Käsittelee tapauksen, jossa ei (ole/saada tarkistettua) käyttöoikeuksia."
  [_ __ ___]
  (response/unauthorized {:reason "Unable to check access rights"}))

(defn log-exception
  "Logittaa virheen."
  [^Exception ex data]
  (log/errorf
    "Unhandled exception\n%s\n%s\n%s"
    (str ex)
    (str data)
    (str/join "\n" (.getStackTrace ex))))

(defn exception-handler
  "Käsittelee virhetilanteita."
  [^Exception ex & other]
  (let [exception-data (if (map? (first other)) (first other) (ex-data ex))]
    (log-exception ex exception-data))
  (response/internal-server-error {:type "unknown-exception"}))

(def handlers
  "Map of request handlers"
  {::c-ex/request-parsing     (-> c-ex/request-parsing-handler
                                  (c-ex/with-logging :info)
                                  audit/with-logging)
   ::c-ex/request-validation  (-> c-ex/request-validation-handler
                                (c-ex/with-logging :info)
                                audit/with-logging)
   ; Lokitetaan response bodyn validoinnissa esiin nousseet virheet, mutta ei
   ; välitetä virheitä käyttäjälle "500 Internal Server Error" -koodilla. Tästä
   ; syystä `c-ex/response-validation-handler` korvatu
   ; `c-ex/http-response-handler`illa.
   ::c-ex/response-validation (-> c-ex/http-response-handler
                                  (c-ex/with-logging :error)
                                  audit/with-logging)
   :not-found                 (audit/with-logging not-found-handler)
   :unauthorized              (audit/with-logging unauthorized-handler)
   ::c-ex/default             (audit/with-logging exception-handler)})

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
