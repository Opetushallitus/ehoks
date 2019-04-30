(ns oph.ehoks.virkailija.auth
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [schema.core :as s]
            [ring.util.http-response :as response]
            [oph.ehoks.external.kayttooikeus :as kayttooikeus]
            [oph.ehoks.user :as user]
            [oph.ehoks.external.oph-url :as u]))

(def routes
  (c-api/context "/session" []
    (c-api/GET "/" []
      :summary "Virkailijan istunnon luonti"
      :query-params [ticket :- s/Str]
      (if-let [ticket-user (kayttooikeus/get-service-ticket-user
                             ticket (u/get-url "virkailijan-tyopoyta"))]
        (assoc-in
          (response/ok)
          [:session :virkailija-user]
          (merge ticket-user (user/get-auth-info ticket-user)))
        (response/unauthorized {:error "Invalid ticket"})))

    (c-api/DELETE "/" []
      :summary "Virkailijan istunnon päättäminen"
      (dissoc (response/ok) :session))))
