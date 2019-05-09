(ns oph.ehoks.handler
  (:require [clojure.tools.logging :as log]
            [compojure.api.sweet :as c-api]
            [compojure.api.exception :as c-ex]
            [compojure.core :refer [GET]]
            [compojure.route :as compojure-route]
            [oph.ehoks.common.api :as common-api]
            [ring.util.http-response :as response]
            [oph.ehoks.resources :as resources]
            [oph.ehoks.healthcheck.handler :as healthcheck-handler]
            [oph.ehoks.lokalisointi.handler :as lokalisointi-handler]
            [oph.ehoks.external.handler :as external-handler]
            [oph.ehoks.misc.handler :as misc-handler]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.tyopaikan-toimija.handler :as tt-handler]
            [oph.ehoks.oppija.handler :as oppija-handler]
            [oph.ehoks.oppija.auth-handler :as auth-handler]
            [oph.ehoks.validation.handler :as validation-handler]))

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

    (c-api/context "/ehoks-backend" []
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
