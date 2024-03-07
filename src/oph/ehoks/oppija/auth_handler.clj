(ns oph.ehoks.oppija.auth-handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [ring.util.http-response :as response]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.config :refer [config]]
            [schema.core :as s]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [oph.ehoks.middleware :refer [wrap-require-user-type-and-auth]]
            [oph.ehoks.user :as user]
            [oph.ehoks.oppija.settings-handler :as settings-handler]
            [clojure.tools.logging :as log]
            [oph.ehoks.external.cas :as cas]
            [ring.middleware.session.store :refer [delete-session]]
            [oph.ehoks.db.db-operations.session :as db-session]
            [clojure.set :as set]
            [clojure.string :as cstr]
            [clojure.data.xml :as xml]))

(defn- get-user-info-from-onr
  "Get user information from oppijanumerorekisteri"
  [oid]
  (let [response (onr/find-student-by-oid oid)
        user-info (:body response)]
    (-> user-info
        (select-keys [:oidHenkilo :etunimet :sukunimi :kutsumanimi])
        (set/rename-keys {:oidHenkilo :oid
                          :etunimet :first-name
                          :sukunimi :surname
                          :kutsumanimi :common-name}))))

(defn- respond-with-successful-authentication
  "Adds user-info and ticket to response to store them to session-store"
  [user-info ticket ^String domain]
  ; There propably should be localized versions of /misc/environment route
  ; return value for cas-oppija-login-url (same as logout urls). We could then
  ; redirect here to the correct hostname based on locale.
  ; Now when the student begins with www.studieinfo.fi/ehoks, after login
  ; cas will call /api/v1/oppija/session/opintopolku which here below
  ; redirects the student to www.opintopolku.fi/ehoks. However only the
  ; hostname changes and user still has swedish ui as locale is
  ; stored to cookie.
  (let [user (if-not (:usingValtuudet user-info)
               user-info
               (dissoc user-info :oid))]
    (-> (response/see-other
          (format
            "%s/%s"
            (if (.contains domain "studieinfo")
              (:frontend-url-sv config)
              (:frontend-url-fi config))
            (:frontend-url-path config)))
        (assoc-in [:session :user] user)
        (assoc-in [:session :ticket] ticket))))

(defn- respond-with-failed-authentication
  "Send unauthorized response when authentication has failed"
  [cas-ticket-validation-result]
  (do (log/warnf "Ticket validation failed: %s"
                 (:error cas-ticket-validation-result))
      (response/unauthorized {:error "Invalid ticket"})))

(def routes
  "Oppija auth handler routes"
  (c-api/context "/session" []

    (c-api/GET "/opintopolku/" [:as request]
      :summary "Oppijan Opintopolku-kirjautumisen endpoint (CAS)"
      :query-params [ticket :- s/Str]
      (let [cas-ticket-validation-result (cas/validate-oppija-ticket
                                           ticket (:server-name request))
            using-valtuudet (:usingValtuudet cas-ticket-validation-result)
            user-info (assoc
                        (get-user-info-from-onr
                          (:user-oid cas-ticket-validation-result))
                        :usingValtuudet using-valtuudet)]
        (if (:success? cas-ticket-validation-result)
          (respond-with-successful-authentication
            user-info ticket (:server-name request))
          (respond-with-failed-authentication cas-ticket-validation-result))))

    (c-api/POST "/opintopolku/" []
      :summary "Oppijan Opintopolku-uloskirjautumisen endpoint (CAS)"
      :form-params [logoutRequest :- s/Str]
      (if-let [ticket (some #(when (= (:tag %) :SessionIndex)
                               (first (:content %)))
                            (:content (xml/parse-str logoutRequest)))]
        (let [res (first (db-session/delete-sessions-by-ticket!
                           (cstr/trim ticket)))]
          (if (zero? res)
            (response/not-found)
            (response/ok)))
        (do
          (log/error (str "Could not parse service ticket from logout request "
                          logoutRequest))
          (response/bad-request))))

    (c-api/OPTIONS "/" []
      (assoc-in (response/ok) [:headers "Allow"] "OPTIONS, GET, DELETE"))

    (route-middleware
      [(wrap-require-user-type-and-auth ::user/oppija)]
      (c-api/GET "/user-info" [:as request]
        :summary "Palauttaa istunnon käyttäjän tiedot"
        :header-params [caller-id :- s/Str]
        :return (rest/response [schema/UserInfo])
        (let [session-user    (user/get request ::user/oppija)
              using-valtuudet (:usingValtuudet session-user)]
          (if-not using-valtuudet
            (let [user-info-response (onr/find-student-by-oid
                                       (:oid session-user))
                  user-info (:body user-info-response)]
              (if (and (= (:status user-info-response) 200)
                       (seq user-info))
                (rest/rest-ok [(onr/convert-student-info user-info)])
                (throw (ex-info "External system error" user-info-response))))
            (rest/rest-ok [session-user]))))

      (c-api/GET "/" [:as request]
        :summary "Käyttäjän istunto"
        :header-params [caller-id :- s/Str]
        :return (rest/response [schema/User])
        (let [{{:keys [user]} :session} request]
          (rest/rest-ok
            [(select-keys user [:oid :first-name :common-name :surname
                                :usingValtuudet])])))

      (c-api/DELETE "/" []
        :summary "Uloskirjautuminen."
        (assoc
          (response/ok)
          :session nil))

      settings-handler/routes)))
