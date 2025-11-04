(ns oph.ehoks.virkailija.auth
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :as response]
            [oph.ehoks.virkailija.schema :as schema]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.oppijaindex :as oi]
            [oph.ehoks.virkailija.cas-handler :as cas-handler]
            [schema.core :as s]
            [oph.ehoks.user :as user]))

(defn org->child-organisations
  "Fetch all child organisation of given organisation from oppijaindex"
  [org]
  (if (= org "1.2.246.562.10.00000000001")
    (oi/get-oppilaitos-oids-cached)
    (oi/get-oppilaitos-oids-by-koulutustoimija-oid org)))

(def enrich-with-child-organisations
  (partial map #(assoc % :child-organisations
                       (org->child-organisations (:oid %)))))

(def routes
  "Virkailija auth routes"
  (c-api/context "/session" []
    (c-api/context "/opintopolku" []
      cas-handler/routes)

    (c-api/GET "/" request
      :summary "Virkailijan istunto"
      :header-params [caller-id :- s/Str]
      :return (restful/response schema/VirkailijaSession)
      (if-let [virkailija-user (get-in request [:session :virkailija-user])]
        (-> virkailija-user
            (select-keys [:oidHenkilo :organisation-privileges])
            (update :organisation-privileges enrich-with-child-organisations)
            (assoc :isSuperuser (user/oph-super-user? virkailija-user))
            (restful/ok))
        (response/unauthorized {:info "User is not authorized"})))

    (c-api/DELETE "/" []
      :summary "Virkailijan istunnon päättäminen"
      :header-params [caller-id :- s/Str]
      (assoc (response/ok) :session nil))))
