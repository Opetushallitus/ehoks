(ns oph.ehoks.virkailija.auth
  (:require [compojure.api.sweet :as c-api]
            [schema.core :as s]
            [ring.util.http-response :as response]
            [oph.ehoks.external.kayttooikeus :as kayttooikeus]
            [oph.ehoks.external.cas :as cas]
            [oph.ehoks.user :as user]
            [oph.ehoks.external.oph-url :as u]
            [clojure.tools.logging :as log]
            [oph.ehoks.virkailija.schema :as schema]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.common.api :refer [delete-session]]
            [clojure.data.xml :as xml]))

(def routes
  (c-api/context "/session" []
    (c-api/GET "/opintopolku" []
      :summary "Virkailijan Opintopolku-kirjautumisen endpoint"
      :query-params [ticket :- s/Str]
      (let [validation-data (cas/validate-ticket
                              (u/get-url "ehoks.virkailija-login-return")
                              ticket)]
        (if (:success? validation-data)
          (let [ticket-user (kayttooikeus/get-user-details
                              (:user validation-data))]
            (assoc-in
              (assoc-in
                (response/see-other (u/get-url "ehoks-virkailija-frontend"))
                [:session :virkailija-user]
                (merge ticket-user (user/get-auth-info ticket-user)))
              [:session :ticket]
              ticket))
          (do (log/warnf "Ticket validation failed: %s"
                         (:error validation-data))
              (response/unauthorized {:error "Invalid ticket"})))))

    (c-api/POST "/opintopolku" []
      :summary "Virkailijan CAS SLO endpoint"
      :form-params [logoutRequest :- s/Str]
      (let [ticket (some #(when (= (:tag %) :SessionIndex)
                            (first (:content %)))
                         (:content (xml/parse-str logoutRequest)))]
        (delete-session ticket)
        (response/ok)))

    (c-api/GET "/" request
      :summary "Virkailijan istunto"
      :return (restful/response schema/VirkailijaSession)
      (if-let [virkailija-user (get-in request [:session :virkailija-user])]
        (restful/rest-ok
          (select-keys virkailija-user [:oidHenkilo :organisation-privileges]))
        (response/unauthorized {:info "User is not authorized"})))

    (c-api/DELETE "/" []
      :summary "Virkailijan istunnon päättäminen"
      (dissoc (response/ok) :session))))
