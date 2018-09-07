(ns oph.ehoks.auth.handler
  (:require [compojure.api.sweet :refer [context GET POST DELETE]]
            [ring.util.http-response :refer [bad-request! see-other]]
            [schema.core :as s]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.restful :refer [response rest-ok]]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.auth.opintopolku :as opintopolku]))

(def routes
  (context "/session" []

    (GET "/opintopolku/" [:as request]
      :summary "Get current Opintopolku session"
      :return (response [schema/User] :opintopolku-login-url s/Str)
      (let [{{:keys [user]} :session} request]
        (rest-ok
          (if (some? user)
            [(select-keys user [:first-name :common-name :surname])]
            [])
          :opintopolku-login-url (:opintopolku-login-url config))))

    (DELETE "/opintopolku/" []
      :summary "Delete Opintopolku session (logout)"
      :return (response [s/Any])
      (assoc (rest-ok []) :session nil))

    (POST "/opintopolku/" [:as request]
      :summary "Creates new Opintopolku session and redirects to frontend"
      :description "Creates new Opintopolku session. After storing session
                    http status 'See Other' (303) will be returned with url of
                    frontend in configuration."
      (when (not= (get-in request [:headers "referer"])
                  (:opintopolku-login-url config))
        (bad-request! "Misconfigured authentication"))
      (let [values (opintopolku/parse (:form-params request))]
        (assoc-in
          (see-other (:frontend-url config))
          [:session :user] values)))))
