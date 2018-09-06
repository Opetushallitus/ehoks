(ns oph.ehoks.handler
  (:require [compojure.api.sweet :refer [api context GET]]
            [ring.util.http-response :refer [not-found]]
            [ring.middleware.session :refer [wrap-session]]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.healthcheck.handler :as healthcheck-handler]
            [oph.ehoks.education.handler :as education-handler]
            [oph.ehoks.work.handler :as work-handler]
            [oph.ehoks.student.handler :as student-handler]
            [oph.ehoks.auth.handler :as auth-handler]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.redis :refer [redis-store]]))

(def api-routes
  (api
    {:swagger
     {:ui "/doc/"
      :spec "/swagger.json"
      :data {:info {:title "eHOKS backend"
                    :description "Backend for eHOKS"}
             :tags [{:name "api", :description ""}]}}}

    (context "/api/v1" []
      :tags ["api" "v1"]

      healthcheck-handler/routes
      education-handler/routes
      work-handler/routes
      student-handler/routes
      auth-handler/routes)

    (context "*" []
      (GET "*" []
        (not-found {:reason "Route not found"})))))

(def app
  (wrap-session
    api-routes
    (if (:redis-url config)
      {:store (redis-store {:pool {} :spec {:uri (:redis-url config)}})}
      {})))
