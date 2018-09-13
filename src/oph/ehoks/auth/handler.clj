(ns oph.ehoks.auth.handler
  (:require [compojure.api.sweet :refer [context GET POST DELETE]]
            [ring.util.http-response :refer [bad-request! see-other]]
            [schema.core :as s]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.restful :refer [response rest-ok]]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.auth.opintopolku :as opintopolku]
            [oph.ehoks.external.oppijanumerorekisteri
             :refer [find-student-by-nat-id convert-student-info
                     find-student-by-oid]]))

(def routes
  (context "/session" []

    (GET "/user-info" [:as request]
      :summary "Get current user info"
      :return (response [schema/UserInfo])
      (let [user-info-response
            (find-student-by-oid
              (get-in request [:session :user :oid]))
            user-info (:body user-info-response)]
        (if (and (= (:status user-info-response) 200)
                 (seq user-info))
          (rest-ok [(convert-student-info user-info)])
          (throw (ex-info "External system error" user-info-response)))))

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
                    frontend in configuration.
                    User info is being downloaded from Oppijanumerorekisteri."
      (when (not= (get-in request [:headers "referer"])
                  (:opintopolku-login-url config))
        (bad-request! "Misconfigured authentication"))
      (let [values (opintopolku/parse (:form-params request))
            user-info-response (find-student-by-nat-id (:hetu values))
            user-info (get-in user-info-response [:body :results])
            user (assoc values :oid (:oidHenkilo (first user-info)))]
        (if (and (= (:status user-info-response) 200)
                 (seq user-info))
          (assoc-in (see-other (:frontend-url config))
                    [:session :user] user)
          (throw (ex-info "No user found" user-info-response)))))))
