(ns oph.ehoks.virkailija.handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [compojure.route :as compojure-route]
            [schema.core :as s]
            [ring.util.http-response :as response]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.external.cache :as c]
            [oph.ehoks.virkailija.auth :as auth]
            [oph.ehoks.user :as user]
            [oph.ehoks.db.postgresql :as db]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.healthcheck.handler :as healthcheck-handler]
            [oph.ehoks.misc.handler :as misc-handler]
            [oph.ehoks.hoks.handler :as hoks-handler]))

(defn- virkailija-authenticated? [request]
  (some? (get-in request [:session :virkailija-user])))

(defn wrap-virkailija-authorize [handler]
  (fn
    ([request respond raise]
      (if (virkailija-authenticated? request)
        (handler request respond raise)
        (respond (response/unauthorized))))
    ([request]
      (if (virkailija-authenticated? request)
        (handler request)
        (response/unauthorized)))))

(defn wrap-oph-super-user [handler]
  (fn
    ([request respond raise]
      (if (user/oph-super-user? (get-in request [:session :virkailija-user]))
        (handler request respond raise)
        (respond (response/forbidden))))
    ([request]
      (if (user/oph-super-user? (get-in request [:session :virkailija-user]))
        (handler request)
        (response/forbidden)))))

(def routes
  (c-api/context "/ehoks-virkailija-backend" []
    :tags ["ehoks"]
    (c-api/context "/api" []
      :tags ["api"]
      (c-api/context "/v1" []
        :tags ["v1"]

        hoks-handler/routes

        (c-api/context "/virkailija" []
          :tags ["virkailija"]
          auth/routes

          (route-middleware
            [wrap-virkailija-authorize]

            (c-api/context "/oppijat" []
              (c-api/GET "/" []
                :return (restful/response [common-schema/Oppija])
                :summary "Listaa virkailijan oppilaitoksen oppijat,
                          joilla on HOKS luotuna"
                (restful/rest-ok []))

              (c-api/GET "/:oid" []
                :return (restful/response common-schema/Oppija)
                :summary "Oppijan tiedot"
                (restful/rest-ok {}))))

          (route-middleware
            [wrap-virkailija-authorize wrap-oph-super-user]

            (c-api/context "/hoksit" []

              (c-api/GET "/" []
                :summary "Kaikki hoksit (perustiedot)"
                (restful/rest-ok (db/select-hoksit)))

              (c-api/GET "/:hoks-id" []
                :path-params [hoks-id :- s/Int]
                :summary "Hoksin tiedot"
                (restful/rest-ok (h/get-hoks-by-id hoks-id))))

            (c-api/DELETE "/cache" []
              :summary "Välimuistin tyhjennys"
              (c/clear-cache!)
              (response/ok))))

        healthcheck-handler/routes
        misc-handler/routes))

    (c-api/undocumented
      (c-api/GET "/buildversion.txt" []
        (response/content-type
          (response/resource-response "buildversion.txt") "text/plain")))))

(def app-routes
  (c-api/api
    {:swagger
     {:ui "/ehoks-virkailija-backend/doc"
      :spec "/ehoks-virkailija-backend/doc/swagger.json"
      :data {:info {:title "eHOKS virkailija backend"
                    :description "eHOKS virkailijan näkymän backend"}
             :tags [{:name "api", :description ""}]}}
     :exceptions
     {:handlers common-api/handlers}}

    routes

    (c-api/undocumented
      (compojure-route/not-found
        (response/not-found {:reason "Route not found"})))))
