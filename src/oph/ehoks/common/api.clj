(ns oph.ehoks.common.api
  (:require [compojure.api.exception :as c-ex]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.logging.access :refer [wrap-access-logger]]
            [oph.ehoks.middleware :as middleware]
            [ring.middleware.session :as session]
            [ring.middleware.session.memory :as mem]
            [ring.util.http-response :as response])
  (:import [clojure.lang ExceptionInfo]))

(defn dissoc-schema-validation-handler!
  [e data req]
  (update (c-ex/request-validation-handler e data req)
          :body
          dissoc
          :schema))

(defn not-found-handler
  "Käsittelee tapauksen, jossa reittiä ei löydy."
  [_ __ ___]
  (response/not-found {:reason "Route not found"}))

(defn unauthorized-handler
  "Käsittelee tapauksen, jossa ei (ole/saada tarkistettua) käyttöoikeuksia."
  [_ __ ___]
  (response/unauthorized {:reason "Unable to check access rights"}))

(defn bad-request-handler
  "Opiskeluoikeus and organisation information are prerequisites for many
  handlers. This is why we want to default to giving `response/bad-request` in
  cases where the prerequisite is missing. E.g., HOKS should always have an
  existing opiskeluoikeus linked to it, the organisation of the user accessing
  a HOKS should be found, etc."
  [^ExceptionInfo e data _]
  (response/bad-request {:error (ex-message e)}))

(def handlers
  "Map of request handlers"
  {::c-ex/request-parsing
   (c-ex/with-logging c-ex/request-parsing-handler :info)

   ::c-ex/request-validation
   (c-ex/with-logging dissoc-schema-validation-handler! :info)

   ; Lokitetaan response bodyn validoinnissa esiin nousseet virheet, mutta ei
   ; välitetä virheitä käyttäjälle "500 Internal Server Error" -koodilla.
   ::c-ex/response-validation
   (c-ex/with-logging c-ex/http-response-handler :error)

   ::organisaatio/organisation-not-found
   (c-ex/with-logging bad-request-handler :warn)

   :disallowed-update (c-ex/with-logging bad-request-handler :warn)

   :opiskeluoikeus-already-exists (c-ex/with-logging bad-request-handler :warn)

   ::koski/opiskeluoikeus-not-found
   (c-ex/with-logging bad-request-handler :warn)

   ::onr/oppija-not-found
   (c-ex/with-logging bad-request-handler :warn)

   :not-found not-found-handler
   :unauthorized unauthorized-handler
   ::c-ex/default c-ex/safe-handler})

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
