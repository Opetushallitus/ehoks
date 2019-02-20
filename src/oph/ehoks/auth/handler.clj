(ns oph.ehoks.auth.handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [ring.util.http-response :as response]
            [schema.core :as s]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.auth.opintopolku :as opintopolku]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [oph.ehoks.middleware :refer [wrap-authorize]]
            [clojure.tools.logging :as log]))

(def routes
  (c-api/context "/session" []
    :tags ["auth"]

    (route-middleware
      [wrap-authorize]
      (c-api/GET "/user-info" [:as request]
        :summary "Palauttaa istunnon käyttäjän tiedot"
        :return (rest/response [schema/UserInfo])
        (let [session-user (get-in request [:session :user])
              user-info-response (onr/find-student-by-oid (:oid session-user))
              user-info (:body user-info-response)]
          (if (and (= (:status user-info-response) 200)
                   (seq user-info))
            (rest/rest-ok [(onr/convert-student-info user-info)])
            (throw (ex-info "External system error" user-info-response)))))

      (c-api/POST "/update-user-info" [:as request]
        :summary "Päivittää istunnon käyttäjän tiedot Oppijanumerorekisteristä"
        :return (rest/response [schema/User])
        (let [session-user (get-in request [:session :user])
              user-info-response (onr/find-student-by-nat-id
                                   (:hetu session-user))
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
        :summary "Käyttäjän istunto"
        :return (rest/response [schema/User] :opintopolku-login-url s/Str)
        (let [{{:keys [user]} :session} request]
          (rest/rest-ok
            (if (some? user)
              [(select-keys user [:oid :first-name :common-name :surname])]
              [])
            :opintopolku-login-url (:opintopolku-login-url config))))

      (c-api/DELETE "/" []
        :summary "Uloskirjautuminen."
        (assoc
          (response/ok)
          :session nil)))

    (c-api/OPTIONS "/" []
      (assoc-in (response/ok) [:headers "Allow"] "OPTIONS, GET, DELETE"))

    (c-api/GET "/opintopolku/" [:as request]
      :summary "Opintopolkutunnistautumisen päätepiste"
      :description "Opintopolkutunnistautumisen jälkeen päädytään tänne.
                    Sovellus ottaa käyttäjän tunnistetiedot headereista ja
                    huolimatta metodin tyypistä luodaan uusi istunto. Tämä
                    ulkoisen järjestelmän vuoksi.
                    Lopuksi käyttäjä ohjataan käyttöliittymän urliin."
      (let [headers (:headers request)]
        (if-let [result (opintopolku/validate headers)]
          (do
            (log/error "Invalid headers: " result)
            (response/bad-request))
          (let [user (opintopolku/parse headers)]
            (assoc-in (response/see-other
                        (format "%s/%s"
                                (:frontend-url config)
                                (:frontend-url-path config)))
                      [:session :user] user)))))))
