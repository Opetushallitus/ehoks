(ns oph.ehoks.palaute.handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.core :refer [GET]]
            [compojure.route :as compojure-route]
            [ring.util.http-response :as response]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.middleware :refer [wrap-authorize]]
            [oph.ehoks.healthcheck.handler :as healthcheck-handler]))

(def routes
  "Palaute routes"
  (c-api/context "/ehoks-palaute-backend" []
    :tags ["ehoks"]
    (c-api/context "/api" []
      :tags ["api"]
      (c-api/context "/v1" []
        :tags ["v1"]

        healthcheck-handler/routes))

      (c-api/undocumented
        (GET "/buildversion.txt" []
          (response/content-type
            (response/resource-response "buildversion.txt") "text/plain")))))

(def app-routes
  "Palaute app routes"
  (c-api/api
   {:swagger
    {:ui "/ehoks-palaute-backend/doc"
     :spec "/ehoks-palaute-backend/doc/swagger.json"
     :data {:info {:title "eHOKS Palaute backend"
                   :description "Palaute backend for eHOKS"}
            :tags [{:name "api", :description ""}]}}
    :exceptions
    {:handlers common-api/handlers}}

   routes
   (c-api/undocumented
     (compojure-route/not-found
       (response/not-found {:reason "Route not found"})))))
