(ns oph.ehoks.handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.route :as compojure-route]
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

(def app-routes
  (c-api/api
    {:swagger
     {:ui "/ehoks/doc"
      :spec "/ehoks/doc/swagger.json"
      :data {:info {:title "eHOKS backend"
                    :description "Backend for eHOKS"}
             :tags [{:name "api", :description ""}]}}}

    (c-api/context "/ehoks" []
      (c-api/context "/api/v1" []
        :tags ["api-v1"]

        healthcheck-handler/routes
        education-handler/routes
        work-handler/routes
        student-handler/routes
        auth-handler/routes))

    (c-api/undocumented
      (compojure-route/not-found (not-found {:reason "Route not found"})))))

(def public-routes
  [{:uri "/ehoks/api/v1/session/opintopolku/" :request-method :get}
   {:uri "/ehoks/api/v1/session/opintopolku/" :request-method :post}
   {:uri "/ehoks/api/v1/healthcheck" :method :get}])

(def app
  (wrap-session app-routes
                (if (:redis-url config)
                  {:store (redis-store {:pool {}
                                        :spec {:uri (:redis-url config)}})}
                  {})))
