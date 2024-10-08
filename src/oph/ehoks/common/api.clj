(ns oph.ehoks.common.api
  (:require [compojure.api.exception :as c-ex]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.external.cas :as cas]
            [oph.ehoks.external.koodisto :as koodisto]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.logging.access :refer [wrap-access-logger]]
            [oph.ehoks.logging.audit :as audit]
            [oph.ehoks.middleware :as middleware]
            [oph.ehoks.oppijaindex :as oi]
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

(defn custom-ex-handler
  "Returns an exception handler that will return with a response `resp`.
  By default, response body will be a map with a key `:error` and the value is
  the error message extracted from the exception object. A `custom-key` can be
  provided if `:error` isn't okay for any reason."
  ([resp]
    (custom-ex-handler resp nil))
  ([resp custom-key]
    (if custom-key
      (fn [^ExceptionInfo e _ _] (resp {custom-key (ex-message e)}))
      (fn [^ExceptionInfo e _ _] (resp {:error (ex-message e)})))))

;; `bad-request` is currently the most commonly used response we want to return.
;; Opiskeluoikeus, oppija, and organisation information are prerequisites for
;; many request handlers (e.g., HOKS should always have an existing oppija and
;; opiskeluoikeus linked to it) so if the user provides an OID which doesn't
;; link to any existing entity, it's a bad request. Logging level in these cases
;; should be `:warn`. We don't need to do audit logging in this handler because
;; `audit/wrap-logger` is higher in the middleware stack than `wrap-exception`.
(def bad-request-handler
  (c-ex/with-logging (custom-ex-handler response/bad-request) :warn))

(def handlers
  "Map of custom exception handlers"
  {::c-ex/request-parsing                (audit/with-logging
                                           (c-ex/with-logging
                                             c-ex/request-parsing-handler
                                             :info))
   ::c-ex/request-validation             (c-ex/with-logging
                                           dissoc-schema-validation-handler!
                                           :info)
   ;; Do log response body validation errors, but don't give the user internal
   ;; server errors in response. We don't need to do audit logging here because
   ;; response validation exception thrown passes through `audit/wrap-logger`
   ;; middleware.
   ::c-ex/response-validation            (c-ex/with-logging
                                           c-ex/http-response-handler :error)

   ;; We don't need to do audit logging in the handlers below because
   ;; exceptions thrown go through `audit/wrap-logger` middleware.
   ::organisaatio/organisation-not-found bad-request-handler
   ::hoks/disallowed-update              bad-request-handler
   :opiskeluoikeus-already-exists        bad-request-handler
   ::koski/opiskeluoikeus-not-found      bad-request-handler
   ::onr/oppija-not-found                bad-request-handler
   ::oi/opiskeluoikeus-not-found         bad-request-handler
   ::oi/invalid-opiskeluoikeus           bad-request-handler
   :shared-link-validation-error         bad-request-handler
   :shared-link-expired                  (c-ex/with-logging
                                           (custom-ex-handler response/gone
                                                              :message)
                                           :warn)
   :shared-link-inactive                 (c-ex/with-logging
                                           (custom-ex-handler response/locked
                                                              :message)
                                           :warn)
   ::koodisto/code-element-not-found     not-found-handler
   ::cas/unauthorized                    unauthorized-handler

   ::c-ex/default                        c-ex/safe-handler})

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
