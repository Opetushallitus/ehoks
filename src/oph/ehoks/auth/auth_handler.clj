(ns oph.ehoks.auth.auth-handler
  (:require [compojure.api.sweet :refer [context GET POST DELETE]]
            [ring.util.http-response :refer [bad-request! no-content]]
            [schema.core :as s]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.restful :refer [response rest-ok]]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.auth.opintopolku :as opintopolku]))

(def routes
  (context "/session" []
    (context "/opintopolku" []
      (GET "/" [:as request]
           :summary "Get current Opintopolku session"
           :return (response [schema/User] :opintopolku-login-url s/Str)
           (let [{{:keys [user]} :session} request]
             (rest-ok
               (if (some? user)
                 [(select-keys user [:first-name :common-name :surname])]
                 [])
               :opintopolku-login-url (:opintopolku-login-url config))))

      (DELETE "/" []
              :summary "Delete Opintopolku session (logout)"
              :return (response [])
              (assoc (rest-ok []) :session nil))

      (POST "/" [:as request]
            :summary "Creates new Opintopolku session"
            :return (response [])
            (when (not= (get-in request [:headers "referer"])
                        (:opintopolku-login-url config))
              (bad-request! "Misconfigured authentication"))
            (let [values (opintopolku/parse (:form-params request))]
              (assoc-in (rest-ok []) [:session :user] values))))))
