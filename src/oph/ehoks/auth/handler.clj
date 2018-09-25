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
      (let [session-user (get-in request [:session :user])
            user-info-response (onr/find-student-by-oid (:oid session-user))
            user-info (:body user-info-response)]
        (if (and (= (:status user-info-response) 200)
                 (seq user-info))
          (rest/rest-ok [(onr/convert-student-info user-info)])
          (throw (ex-info "External system error" user-info-response)))))

    (c-api/POST "/update-user-info" [:as request]
      :summary "Updates session user info from Oppijanumerorekisteri"
      :return (rest/response [schema/User])
      (let [session-user (get-in request [:session :user])
            user-info-response (onr/find-student-by-nat-id (:hetu session-user))
            user-info (first (get-in user-info-response [:body :results]))
            oid (:oidHenkilo user-info)]
        (when-not (= (:status user-info-response) 200)
          (throw (ex-info "External integration error" user-info-response)))
        (if (seq user-info)
          (assoc-in
            (rest/rest-ok
              [(assoc
                 (select-keys session-user
                              [:first-name :common-name :surname])
                 :oid oid)])
            [:session :user] (assoc session-user :oid oid))
          (throw (ex-info "No user found" user-info-response)))))

    (c-api/GET "/" [:as request]
      :summary "Get current Opintopolku session"
      :return (rest/response [schema/User] :opintopolku-login-url s/Str)
      (let [{{:keys [user]} :session} request]
        (rest/rest-ok
          (if (some? user)
            [(select-keys user [:oid :first-name :common-name :surname])]
            [])
          :opintopolku-login-url (:opintopolku-login-url config))))

    (c-api/OPTIONS "/opintopolku/" []
      :summary "Options for session DELETE (logout)"
      (assoc-in (response/ok) [:headers "Allow"] "OPTIONS, GET, DELETE"))

    (c-api/DELETE "/opintopolku/" []
      :summary "Delete Opintopolku session (logout)"
      :return (rest/response [s/Any])
      (assoc (rest/rest-ok []) :session nil))

    (c-api/GET "/opintopolku/" [:as request]
      :summary "Tunnistaa Opintopolku-käyttäjän"
      :description "Tunnistaa Opintopolku-käyttäjän ja luo uuden session
                    proxyn headereista. Lopuksi käyttäjä ohjataan
                    käyttöliittymän urliin."
      (let [headers (:headers request)]
        (when-let [result (opintopolku/validate headers)]
          (throw (ex-info "Invalid headers"
                          {:validation result
                           :request request})))
        (let [user (opintopolku/parse headers)]
          (assoc-in (response/see-other (:frontend-url config))
                    [:session :user] user))))))
