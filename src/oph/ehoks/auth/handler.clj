(ns oph.ehoks.auth.handler
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :as response]
            [schema.core :as s]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.auth.opintopolku :as opintopolku]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]))

(def routes
  (c-api/context "/session" []

    (c-api/GET "/user-info" [:as request]
      :summary "Get current user info"
      :return (rest/response [schema/UserInfo])
      (let [user-info-response
            (onr/find-student-by-oid
              (get-in request [:session :user :oid]))
            user-info (:body user-info-response)]
        (if (and (= (:status user-info-response) 200)
                 (seq user-info))
          (rest/rest-ok [(onr/convert-student-info user-info)])
          (throw (ex-info "External system error" user-info-response)))))

    (c-api/GET "/opintopolku/" [:as request]
      :summary "Get current Opintopolku session"
      :return (rest/response [schema/User] :opintopolku-login-url s/Str)
      (let [{{:keys [user]} :session} request]
        (rest/rest-ok
          (if (some? user)
            [(select-keys user [:first-name :common-name :surname])]
            [])
          :opintopolku-login-url (:opintopolku-login-url config))))

    (c-api/DELETE "/opintopolku/" []
      :summary "Delete Opintopolku session (logout)"
      :return (rest/response [s/Any])
      (assoc (rest/rest-ok []) :session nil))

    (c-api/POST "/opintopolku/" [:as request]
      :summary "Creates new Opintopolku session and redirects to frontend"
      :description "Creates new Opintopolku session. After storing session
                    http status 'See Other' (303) will be returned with url of
                    frontend in configuration.
                    User info is being downloaded from Oppijanumerorekisteri."
      (when (not= (get-in request [:headers "referer"])
                  (:opintopolku-login-url config))
        (response/bad-request! "Misconfigured authentication"))
      (let [values (opintopolku/parse (:form-params request))
            user-info-response (onr/find-student-by-nat-id (:hetu values))
            user-info (get-in user-info-response [:body :results])
            user (assoc values :oid (:oidHenkilo (first user-info)))]
        (if (and (= (:status user-info-response) 200)
                 (seq user-info))
          (assoc-in (response/see-other (:frontend-url config))
                    [:session :user] user)
          (throw (ex-info "No user found" user-info-response)))))))
