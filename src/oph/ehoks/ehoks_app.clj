(ns oph.ehoks.ehoks-app
  (:require [oph.ehoks.common.api :as common-api]
            [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [compojure.route :as compojure-route]
            [ring.util.http-response :as response]
            [oph.ehoks.db.session-store :as session-store]
            [oph.ehoks.oppija.handler :as oppija-handler]
            [oph.ehoks.virkailija.handler :as virkailija-handler]
            [clojure.string :refer [lower-case]]
            [environ.core :refer [env]]
            [oph.ehoks.logging.audit :refer [wrap-audit-logger]]))

(def both-app
  (c-api/api
    {:swagger
     {:ui "/ehoks-backend/doc"
      :spec "/ehoks-backend/doc/swagger.json"
      :data {:info {:title "eHOKS backend"
                    :description "Oppija for eHOKS"}
             :tags [{:name "api", :description ""}]}}
     :exceptions
     {:handlers common-api/handlers}}
    (route-middleware
      [wrap-audit-logger]
      oppija-handler/routes
      virkailija-handler/routes

      (c-api/undocumented
        (compojure-route/not-found
          (response/not-found {:reason "Route not found"}))))))

(defn create-app
  "Create ehoks web app of given name. Name will decide if system has oppija
  (ehoks), virkailija (ehoks-virkailija) or both routes."
  [app-name]
  (common-api/create-app
    (case app-name
      "ehoks-virkailija" virkailija-handler/app-routes
      "ehoks" oppija-handler/app-routes
      both-app)
    (session-store/db-store)))

(defn get-app-name []
  (lower-case (:name env (or (System/getProperty "name") "both"))))

(def app (create-app (get-app-name)))
