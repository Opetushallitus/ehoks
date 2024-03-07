(ns oph.ehoks.oppija.handler
  (:require [clojure.tools.logging :as log]
            [compojure.api.core :refer [route-middleware]]
            [compojure.api.sweet :as c-api]
            [compojure.core :refer [GET]]
            [compojure.route :as compojure-route]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.user :as user]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.heratepalvelu.heratepalvelu :as heratepalvelu]
            [oph.ehoks.middleware :refer [wrap-require-user-type-and-auth
                                          wrap-hoks]]
            [oph.ehoks.oppija.auth-handler :as auth-handler]
            [oph.ehoks.lokalisointi.handler :as lokalisointi-handler]
            [oph.ehoks.healthcheck.handler :as healthcheck-handler]
            [oph.ehoks.external.handler :as external-handler]
            [oph.ehoks.logging.audit :refer [wrap-audit-logger]]
            [oph.ehoks.misc.handler :as misc-handler]
            [oph.ehoks.oppija.oppija-external :as oppija-external]
            [oph.ehoks.oppija.share-handler :as share-handler]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.restful :as rest]
            [ring.util.http-response :as response]
            [schema.core :as s])
  (:import (java.time LocalDate)))

(defn wrap-match-user
  "Allow request to be handled if route params OID equals session user OID"
  [handler]
  (fn
    ([request respond raise]
      (if (= (:oid (user/get request ::user/oppija))
             (get-in request [:route-params :oid]))
        (handler request respond raise)
        (respond (response/forbidden))))
    ([request]
      (if (= (:oid (user/get request ::user/oppija))
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
          [wrap-hoks wrap-audit-logger]

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
                  [(wrap-require-user-type-and-auth ::user/oppija)
                   wrap-match-user]

                  (c-api/GET "/" []
                    :summary "Oppijan perustiedot"
                    :return (rest/response common-schema/Oppija)
                    (assoc
                      (if-let [oppija (oppijaindex/get-oppija-by-oid oid)]
                        (rest/rest-ok oppija)
                        (response/not-found))
                      :audit-data {:target {:oppija-oid oid}}))

                  (c-api/GET "/opiskeluoikeudet" [:as request]
                    :summary "Oppijan opiskeluoikeudet"
                    :return (rest/response [s/Any])
                    (let [opiskeluoikeudet (koski/get-oppija-opiskeluoikeudet
                                             oid)]
                      (assoc
                        (rest/rest-ok opiskeluoikeudet)
                        :audit-data
                        {:target-info {:oppija-oid oid
                                       :opiskeluoikeus-oids
                                       (map :oid opiskeluoikeudet)}})))

                  (c-api/GET "/hoks" [:as request]
                    :summary "Oppijan HOKSit kokonaisuudessaan"
                    :return (rest/response [s/Any])
                    (let [hokses (h/get-hokses-by-oppija oid)]
                      (assoc
                        (if (empty? hokses)
                          (response/not-found {:message "No HOKSes found"})
                          (rest/rest-ok (map #(dissoc % :id) hokses)))
                        :audit-data {:target {:oppija-oid oid
                                              :hoks-ids   (map :id hokses)}})))

                  (c-api/GET "/kyselylinkit" []
                    :summary "Palauttaa oppijan aktiiviset kyselylinkit"
                    :return (rest/response [s/Any])
                    (try
                      (let [linkit
                            (->> (heratepalvelu/get-oppija-kyselylinkit oid)
                                 (filter #(and (not (:vastattu %))
                                               (not (.isAfter
                                                      (LocalDate/now)
                                                      (:voimassa-loppupvm %)))))
                                 (map :kyselylinkki))]
                        (assoc (rest/rest-ok linkit)
                               :audit-data {:target {:oppija-oid oid
                                                     :kyselylinkit linkit}}))
                        (catch Exception e
                          (log/error e) (throw e)))))))

            (c-api/context "/jaot" []
              :header-params [caller-id :- s/Str]
              :tags ["jaot"]
              (route-middleware
                [(wrap-require-user-type-and-auth ::user/oppija)]
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
