(ns oph.ehoks.oppija.handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [compojure.core :refer [GET]]
            [compojure.route :as compojure-route]
            [schema.core :as s]
            [ring.util.http-response :as response]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.heratepalvelu :as heratepalvelu]
            [oph.ehoks.middleware :refer [wrap-authorize]]
            [oph.ehoks.oppija.auth-handler :as auth-handler]
            [oph.ehoks.lokalisointi.handler :as lokalisointi-handler]
            [oph.ehoks.healthcheck.handler :as healthcheck-handler]
            [oph.ehoks.external.handler :as external-handler]
            [oph.ehoks.misc.handler :as misc-handler]
            [oph.ehoks.logging.audit :as audit]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.oppija.share-handler :as share-handler]
            [oph.ehoks.oppija.oppija-external :as oppija-external])
  (:import (java.time LocalDate)))

(defn wrap-match-user
  "Allow request to be handled if route params OID equals session user OID"
  [handler]
  (fn
    ([request respond raise]
      (if (= (get-in request [:session :user :oid])
             (get-in request [:route-params :oid]))
        (handler request respond raise)
        (respond (response/forbidden))))
    ([request]
      (if (= (get-in request [:session :user :oid])
             (get-in request [:route-params :oid]))
        (handler request)
        (response/forbidden)))))

(def routes
  "Oppija routes"
  (c-api/context "/ehoks-oppija-backend" []
    :tags ["ehoks"]
    (c-api/context "/api" []
      :tags ["api"]
      (c-api/context "/v1" []
        :tags ["v1"]

        healthcheck-handler/routes
        external-handler/routes
        misc-handler/routes

        (c-api/undocumented lokalisointi-handler/routes)

        (route-middleware
          [audit/wrap-logger]

          (c-api/undocumented auth-handler/routes)

          (c-api/context "/oppija" []
            :tags ["oppija"]

            auth-handler/routes

            (c-api/context "/external" []
              :header-params [caller-id :- s/Str]
              :tags ["oppija-external"]

              lokalisointi-handler/routes
              oppija-external/routes)

            (c-api/context "/oppijat" []
              :header-params [caller-id :- s/Str]
              :tags ["oppijat"]

              (c-api/context "/:oid" [oid]

                (route-middleware
                  [wrap-authorize wrap-match-user]
                  (c-api/GET "/" []
                    :summary "Oppijan perustiedot"
                    :return (rest/response common-schema/Oppija)
                    (if-let [oppija (oppijaindex/get-oppija-by-oid oid)]
                      (rest/ok oppija)
                      (response/not-found)))

                  (c-api/GET "/opiskeluoikeudet" [:as request]
                    :summary "Oppijan opiskeluoikeudet"
                    :return (rest/response [s/Any])
                    (rest/ok (koski/get-oppija-opiskeluoikeudet oid)))

                  (c-api/GET "/hoks" [:as request]
                    :summary "Oppijan HOKSit kokonaisuudessaan"
                    :return (rest/response [s/Any])
                    (let [hokses (hoks/get-by-oppija oid)]
                      (if (empty? hokses)
                        (response/not-found {:message "No HOKSes found"})
                        (rest/ok (map #(dissoc % :id) hokses)))))

                  (c-api/GET "/kyselylinkit" []
                    :summary "Palauttaa oppijan aktiiviset kyselylinkit"
                    :return (rest/response [s/Any])
                    (->> (heratepalvelu/get-oppija-kyselylinkit oid)
                         (filter #(and (not (:vastattu %))
                                       (not (.isAfter
                                              (LocalDate/now)
                                              (:voimassa-loppupvm %)))))
                         (map :kyselylinkki)
                         rest/ok)))))

            (c-api/context "/jaot" []
              :header-params [caller-id :- s/Str]
              :tags ["jaot"]
              (route-middleware
                [wrap-authorize]
                share-handler/routes))))))

    (c-api/undocumented
      (GET "/buildversion.txt" []
        (response/content-type
          (response/resource-response "buildversion.txt") "text/plain")))))

(def app-routes
  "Oppija app routes"
  (c-api/api
    {:swagger
     {:ui "/ehoks-oppija-backend/doc"
      :spec "/ehoks-oppija-backend/doc/swagger.json"
      :data {:info {:title "eHOKS Oppija backend"
                    :description "Oppija backend for eHOKS"}
             :tags [{:name "api", :description ""}]}}
     :exceptions
     {:handlers common-api/handlers}}

    routes
    (c-api/undocumented
      (compojure-route/not-found
        (response/not-found {:reason "Route not found"})))))
