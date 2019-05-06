(ns oph.ehoks.handler
  (:require [clojure.tools.logging :as log]
            [compojure.api.sweet :as c-api]
            [compojure.api.exception :as c-ex]
            [compojure.core :refer [GET]]
            [compojure.route :as compojure-route]
            [ring.util.http-response :as response]
            [ring.middleware.session :as session]
            [ring.middleware.session.memory :as mem]
            [oph.ehoks.resources :as resources]
            [oph.ehoks.middleware :as middleware]
            [oph.ehoks.healthcheck.handler :as healthcheck-handler]
            [oph.ehoks.lokalisointi.handler :as lokalisointi-handler]
            [oph.ehoks.external.handler :as external-handler]
            [oph.ehoks.misc.handler :as misc-handler]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.redis :refer [redis-store]]
            [oph.ehoks.hoks.handler :as hoks-handler]
            [oph.ehoks.tyopaikan-toimija.handler :as tt-handler]
            [oph.ehoks.oppija.handler :as oppija-handler]
            [oph.ehoks.oppija.auth-handler :as auth-handler]
            [oph.ehoks.validation.handler :as validation-handler]
            [oph.ehoks.virkailija.handler :as virkailija-handler]))

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
          oppija-handler/routes
          (c-api/undocumented auth-handler/routes)
          hoks-handler/routes
          healthcheck-handler/routes
          lokalisointi-handler/routes
          external-handler/routes
          misc-handler/routes
          tt-handler/routes
          validation-handler/routes
          virkailija-handler/routes))

      (c-api/undocumented
        (GET "/buildversion.txt" _
          (response/content-type
            (response/resource-response "buildversion.txt") "text/plain"))
        (resources/create-routes "/hoks-doc" "hoks-doc")
        (resources/create-routes "/json-viewer" "json-viewer")))

    (c-api/undocumented
      (compojure-route/not-found
        (response/not-found {:reason "Route not found"})))))

(defn create-app [session-store]
  (-> app-routes
      (middleware/wrap-cache-control-no-cache)
      (session/wrap-session
        {:store (if (seq (:redis-url config))
                  (redis-store {:pool {}
                                :spec {:uri (:redis-url config)}})
                  (or session-store (mem/memory-store)))
         :cookie-attrs {:max-age (:session-max-age config (* 60 60 4))}})))

(def app (create-app nil))
