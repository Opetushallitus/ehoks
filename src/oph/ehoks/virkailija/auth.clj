(ns oph.ehoks.virkailija.auth
  (:require [compojure.api.sweet :as c-api]
            [schema.core :as s]
            [ring.util.http-response :as response]
            [oph.ehoks.external.kayttooikeus :as kayttooikeus]
            [oph.ehoks.external.cas :as cas]
            [oph.ehoks.user :as user]
            [oph.ehoks.external.oph-url :as u]
            [clojure.tools.logging :as log]))

(def routes
  (c-api/context "/session" []
    (c-api/GET "/" []
      :summary "Virkailijan istunnon luonti"
      :query-params [ticket :- s/Str]
      (let [validation-data (cas/validate-ticket
                              (u/get-url "ehoks.virkailija-login-return")
                              ticket)]
        (if (:success? validation-data)
          (let [ticket-user (kayttooikeus/get-user-details
                              (:user validation-data))]
            (assoc-in
              (response/ok)
              [:session :virkailija-user]
              (merge ticket-user (user/get-auth-info ticket-user))))
          (do (log/warnf "Ticket validation failed: %s"
                         (:error validation-data))
              (response/unauthorized {:error "Invalid ticket"})))))

    (c-api/DELETE "/" []
      :summary "Virkailijan istunnon päättäminen"
      (dissoc (response/ok) :session))))
