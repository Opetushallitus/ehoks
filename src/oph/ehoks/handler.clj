(ns oph.ehoks.handler
  (:require [clojure.tools.logging :as log]
            [compojure.api.sweet :as c-api]
            [compojure.api.exception :as c-ex]
            [compojure.core :refer [GET]]
            [compojure.route :as compojure-route]
            [ring.util.http-response :as response]
            [ring.middleware.session :as session]
            [ring.middleware.session.memory :as mem]
            [oph.ehoks.middleware :as middleware]
            [oph.ehoks.healthcheck.handler :as healthcheck-handler]
            [oph.ehoks.auth.handler :as auth-handler]
            [oph.ehoks.lokalisointi.handler :as lokalisointi-handler]
            [oph.ehoks.external.handler :as external-handler]
            [oph.ehoks.misc.handler :as misc-handler]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.redis :refer [redis-store]]
            [oph.ehoks.hoks.handler :as hoks-handler]
            [oph.ehoks.tyopaikan-toimija.handler :as tt-handler]
            [oph.ehoks.oppija.handler :as oppija-handler]))

(def app-routes
  (c-api/api
    {:swagger
     {:ui "/ehoks-backend/doc"
      :spec "/ehoks-backend/doc/swagger.json"
      :data {:info {:title "eHOKS backend"
                    :description "Backend for eHOKS"}
             :tags [{:name "api", :description ""}]}}
     :exceptions
     {:handlers
      {:not-found
       (fn [_ __ ___] (response/not-found))

       ::c-ex/default
       (fn [^Exception ex ex-data _]
         (if (contains? ex-data :log-data)
           (log/errorf ex "%s (data=%s)" (.getMessage ex) (:log-data ex-data))
           (log/error ex (.getMessage ex)))
         (response/internal-server-error {:type "unknown-exception"}))}}}

    (c-api/context "/ehoks-backend" []
      :tags ["ehoks"]
      (c-api/context "/api" []
        :tags ["api"]
        (c-api/context "/v1" []
          :tags ["v1"]
          healthcheck-handler/routes
          auth-handler/routes
          hoks-handler/routes
          lokalisointi-handler/routes
          external-handler/routes
          misc-handler/routes
          tt-handler/routes
          oppija-handler/routes))

      (c-api/undocumented
        (GET "/buildversion.txt" _
          (response/content-type
            (response/resource-response "buildversion.txt") "text/plain"))))

    (c-api/undocumented
      (compojure-route/not-found
        (response/not-found {:reason "Route not found"})))))

(def public-routes
  [{:uri #"^/ehoks-backend/buildversion.txt$"
    :request-method :get}
   {:uri #"^/ehoks-backend/api/v1/session/opintopolku/$"
    :request-method :get}
   {:uri #"^/ehoks-backend/api/v1/session$"
    :request-method :options}
   {:uri #"^/ehoks-backend/api/v1/healthcheck$"
    :request-method :get}
   {:uri #"^/ehoks-backend/doc/*"
    :request-method :get}
   {:uri #"^/ehoks-backend/api/v1/lokalisointi$"
    :request-method :get}
   {:uri #"^/ehoks-backend/api/v1/external/eperusteet/$"
    :request-method :get}
   {:uri #"^/ehoks-backend/api/v1/misc/environment$"
    :request-method :get}
   {:uri #"^/ehoks-backend/api/v1/tyopaikan-toimija/auth$"
    :request-method :get}])

(def app
  (-> app-routes
      (middleware/wrap-cache-control-no-cache)
      (middleware/wrap-public public-routes)
      (session/wrap-session
        {:store (if (seq (:redis-url config))
                  (redis-store {:pool {}
                                :spec {:uri (:redis-url config)}})
                  (mem/memory-store))
         :cookie-attrs {:max-age (:session-max-age config (* 60 60 4))}})))
