(ns oph.ehoks.virkailija.auth
  (:require [compojure.api.sweet :as c-api]
            [schema.core :as s]
            [ring.util.http-response :as response]
            [oph.ehoks.virkailija.schema :as schema]
            [oph.ehoks.restful :as restful]
            [clojure.data.xml :as xml]
            [oph.ehoks.db.postgresql :as db]
            [oph.ehoks.virkailija.cas-handler :as cas-handler]))

(def routes
  (c-api/context "/session" []
    (c-api/context "/opintopolku" []
      cas-handler/routes)

    (c-api/POST "/opintopolku" []
      :summary "Virkailijan CAS SLO endpoint"
      :form-params [logoutRequest :- s/Str]
      (when-let [ticket (some #(when (= (:tag %) :SessionIndex)
                                 (first (:content %)))
                              (:content (xml/parse-str logoutRequest)))]
        (db/delete-sessions-by-ticket! ticket))
      (response/ok))

    (c-api/GET "/" request
      :summary "Virkailijan istunto"
      :return (restful/response schema/VirkailijaSession)
      (if-let [virkailija-user (get-in request [:session :virkailija-user])]
        (restful/rest-ok
          (select-keys virkailija-user [:oidHenkilo :organisation-privileges]))
        (response/unauthorized {:info "User is not authorized"})))

    (c-api/DELETE "/" []
      :summary "Virkailijan istunnon päättäminen"
      (assoc (response/ok) :session nil))))
