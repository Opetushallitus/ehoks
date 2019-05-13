(ns oph.ehoks.handler
  (:require [clojure.tools.logging :as log]
            [compojure.api.sweet :as c-api]
            [compojure.api.exception :as c-ex]
            [compojure.core :refer [GET]]
            [compojure.route :as compojure-route]
            [clojure.string :as cstr]
            [oph.ehoks.common.api :as common-api]
            [ring.util.http-response :as response]
            [ring.util.request :as ring-request]
            [oph.ehoks.resources :as resources]
            [oph.ehoks.healthcheck.handler :as healthcheck-handler]
            [oph.ehoks.lokalisointi.handler :as lokalisointi-handler]
            [oph.ehoks.external.handler :as external-handler]
            [oph.ehoks.misc.handler :as misc-handler]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.tyopaikan-toimija.handler :as tt-handler]
            [oph.ehoks.oppija.handler :as oppija-handler]
            [oph.ehoks.oppija.auth-handler :as auth-handler]
            [oph.ehoks.validation.handler :as validation-handler]
            [oph.ehoks.external.oph-url :as u]
            [oph.ehoks.virkailija.handler :as virkailija-handler]
            [oph.ehoks.redis :refer [redis-store]]))

(defn move-to [request service]
  (response/moved-permanently
    (cstr/replace
      (ring-request/request-url request) "ehoks-backend" service)))

(def app-routes
  (c-api/api
    {:swagger
     {:ui "/ehoks-backend/doc"
      :spec "/ehoks-backend/doc/swagger.json"
      :data {:info {:title "eHOKS backend"
                    :description "Backend for eHOKS"}
             :tags [{:name "api", :description ""}]}}
     :exceptions
     {:handlers common-api/handlers}}

    (c-api/undocumented
      (c-api/context "/ehoks-backend" []
        (c-api/context "/api/v1" []
          (c-api/GET "/lokalisointi/*" request
            (move-to request "ehoks-oppija-backend"))
          (c-api/GET "/oppija-external/*" request
            (move-to request "ehoks-oppija-backend"))
          (c-api/GET "/oppija/*" request
            (move-to request "ehoks-oppija-backend"))
          (c-api/GET "/misc/*" request
            (move-to request "ehoks-oppija-backend"))
          (c-api/GET "/hoks/*" request
            (move-to request "ehoks-virkailija-backend")))))

    virkailija-handler/routes

    (c-api/context "/ehoks-oppija-backend" []
      :tags ["ehoks"]
      (c-api/context "/api" []
        :tags ["api"]
        (c-api/context "/v1" []
          :tags ["v1"]
          oppija-handler/routes
          (c-api/undocumented auth-handler/routes)

          healthcheck-handler/routes
          lokalisointi-handler/routes
          external-handler/routes
          misc-handler/routes
          validation-handler/routes))

      (c-api/undocumented
        (GET "/buildversion.txt" _
          (response/content-type
            (response/resource-response "buildversion.txt") "text/plain"))
        (resources/create-routes "/hoks-doc" "hoks-doc")
        (resources/create-routes "/json-viewer" "json-viewer")))

    (c-api/undocumented
      (compojure-route/not-found
        (response/not-found {:reason "Route not found"})))))

(def app
  (common-api/create-app
    app-routes
    (when (seq (:redis-url config))
      (redis-store {:pool {}
                    :spec {:uri (:redis-url config)}}))))
