(ns oph.ehoks.oppija.auth-handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [ring.util.http-response :as response]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.oppija.opintopolku :as opintopolku]
            [oph.ehoks.external.oph-url :as u]
            [schema.core :as s]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [oph.ehoks.middleware :refer [wrap-authorize]]
            [oph.ehoks.oppija.settings-handler :as settings-handler]
            [clojure.tools.logging :as log]
            [oph.ehoks.external.cas :as cas]))

(defn- get-user-info-from-onr [oid]
  (let [response (onr/find-student-by-oid oid)
        user-info (:body response)]
    (-> user-info
        (select-keys [:oidHenkilo :hetu :etunimet :sukunimi :kutsumanimi])
        (clojure.set/rename-keys {:oidHenkilo :oid}))))

(defn- respond-with-successful-authentication
  "Adds user-info and ticket to response to store them to session-store"
  [user-info ticket]
  (-> (response/see-other
        (u/get-url "ehoks-oppija-frontend-after-login"))
      (assoc-in [:session :user] user-info)
      (assoc-in [:session :ticket] ticket)))

(defn- respond-with-failed-authentication [cas-ticket-validation-result]
  (do (log/warnf "Ticket validation failed: %s"
                 (:error cas-ticket-validation-result))
      (response/unauthorized {:error "Invalid ticket"})))

(def routes
  (c-api/context "/session" []

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

      (c-api/GET "/" [:as request]
        :summary "Käyttäjän istunto"
        :return (rest/response [schema/User])
        (let [{{:keys [user]} :session} request]
          (rest/rest-ok
            [(select-keys user [:oid :first-name :common-name :surname])])))

      (c-api/DELETE "/" []
        :summary "Uloskirjautuminen."
        (assoc
          (response/ok)
          :session nil))

      settings-handler/routes)

    (c-api/OPTIONS "/" []
      (assoc-in (response/ok) [:headers "Allow"] "OPTIONS, GET, DELETE"))

    (c-api/GET "/opintopolku/" [:as request]
      :summary "Opintopolkutunnistautumisen päätepiste"
      :description "Opintopolkutunnistautumisen jälkeen päädytään tänne.
                    Sovellus ottaa käyttäjän tunnistetiedot headereista ja
                    huolimatta metodin tyypistä luodaan uusi istunto. Tämä
                    ulkoisen järjestelmän vuoksi.
                    Lopuksi käyttäjä ohjataan käyttöliittymän urliin."
      (let [headers (:headers request)
            locale (get-in request [:query-params "locale"])]
        (if-let [result (opintopolku/validate headers)]
          (do
            (log/errorf "Invalid headers: %s" result)
            (response/bad-request))
          (let [user (opintopolku/parse headers)]
            (let [user-info-response (onr/find-student-by-nat-id (:hetu user))
                  user-info (first (get-in user-info-response [:body :results]))
                  oid (:oidHenkilo user-info)]
              (when-not (= (:status user-info-response) 200)
                (throw (ex-info
                         "External integration error" user-info-response)))
              (if (seq user-info)
                (assoc-in
                  (response/see-other
                    (format "%s/%s?lang=%s"
                            ((keyword (str "frontend-url-" locale)) config)
                            (:frontend-url-path config)
                            (str locale)))
                  [:session :user] (assoc user :oid oid))
                (throw (ex-info "No user found" user-info-response))))))))

    (c-api/GET "/opintopolku2/" []
      :summary "Oppijan Opintopolku-kirjautumisen endpoint (CAS)"
      :query-params [ticket :- s/Str]
      (let [cas-ticket-validation-result (cas/validate-oppija-ticket ticket)
            user-info (get-user-info-from-onr
                        (:user-oid cas-ticket-validation-result))]
        (if (:success? cas-ticket-validation-result)
          (respond-with-successful-authentication user-info ticket)
          (respond-with-failed-authentication cas-ticket-validation-result))))))
