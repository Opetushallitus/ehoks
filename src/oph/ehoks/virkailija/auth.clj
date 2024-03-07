(ns oph.ehoks.virkailija.auth
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :as response]
            [oph.ehoks.virkailija.schema :as schema]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.virkailija.cas-handler :as cas-handler]
            [schema.core :as s]
            [oph.ehoks.user :as user]))

(def routes
  "Virkailija auth routes"
  (c-api/context "/session" []
    (c-api/context "/opintopolku" []
      cas-handler/routes)

    (c-api/GET "/" request
      :summary "Virkailijan istunto"
      :header-params [caller-id :- s/Str]
      :return (restful/response schema/VirkailijaSession)
      (if-let [virkailija-user (user/get request ::user/virkailija)]
        (restful/rest-ok
          (assoc (select-keys virkailija-user
                              [:oidHenkilo :organisation-privileges])
                 :isSuperuser (boolean (user/oph-super-user?
                                         virkailija-user))))
        (response/unauthorized {:info "User is not authorized"})))

    (c-api/DELETE "/" []
      :summary "Virkailijan istunnon päättäminen"
      :header-params [caller-id :- s/Str]
      (assoc (response/ok) :session nil))))
