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
            [oph.ehoks.oppija.schema :as oppija-schema]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.middleware :refer [wrap-authorize]]
            [oph.ehoks.oppija.auth-handler :as auth-handler]
            [oph.ehoks.lokalisointi.handler :as lokalisointi-handler]
            [oph.ehoks.healthcheck.handler :as healthcheck-handler]
            [oph.ehoks.external.handler :as external-handler]
            [oph.ehoks.misc.handler :as misc-handler]
            [oph.ehoks.logging.audit :refer [wrap-audit-logger]]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.oppija.share-handler :as share-handler]
            [oph.ehoks.oppija.middleware :as m]
            [oph.ehoks.oppija.oppija-external :as oppija-external]
            [clj-time.format :as f]
            [clj-time.core :as t]
            [cheshire.core :as cheshire]))

(defn- json-response [value]
  (assoc-in
    (response/ok
      (cheshire/generate-string
        value))
    [:headers "Content-Type"] "application/json"))

(defn wrap-match-user [handler]
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
        (c-api/undocumented auth-handler/routes)

        (c-api/context "/oppija" []
          :tags ["oppija"]

          auth-handler/routes

          (c-api/context "/external" []
            :tags ["oppija-external"]

            lokalisointi-handler/routes
            oppija-external/routes)

          (c-api/context "/oppijat" []
            :tags ["oppijat"]

            (c-api/context "/:oid" [oid]

              (route-middleware
                [wrap-authorize wrap-match-user]
                (c-api/GET "/" []
                  :summary "Oppijan perustiedot"
                  :return (rest/response common-schema/Oppija)
                  (if-let [oppija (oppijaindex/get-oppija-by-oid oid)]
                    (rest/rest-ok oppija)
                    (response/not-found)))

                (c-api/GET "/opiskeluoikeudet" [:as request]
                  :summary "Oppijan opiskeluoikeudet"
                  :return (rest/response [s/Any])
                  (rest/rest-ok
                    (koski/get-oppija-opiskeluoikeudet oid)))

                (c-api/GET "/hoks" [:as request]
                  :summary "Oppijan HOKSit kokonaisuudessaan"
                  :return (rest/response [oppija-schema/OppijaHOKS])
                  (let [hokses (h/get-hokses-by-oppija oid)]
                    (if (empty? hokses)
                      (response/not-found {:message "No HOKSes found"})
                      (rest/rest-ok (map #(dissoc % :id) hokses)))))

                (c-api/GET "/kyselylinkit" []
                  :summary "Palauttaa oppijan aktiiviset kyselylinkit"
                  :return (rest/response [s/Any])
                  (try
                    (let [kyselylinkit
                          (reduce
                            (fn [linkit linkki]
                              (let [status (arvo/get-kyselylinkki-status
                                             (:kyselylinkki linkki))
                                    voimassa (f/parse
                                               (:date-time f/formatters)
                                               (:voimassa_loppupvm status))]
                                (if (or (:vastattu status)
                                        (t/after? (t/now) voimassa))
                                  (do (h/delete-kyselylinkki!
                                        (:kyselylinkki linkki))
                                      linkit)
                                  (conj linkit (:kyselylinkki linkki)))))
                            []
                            (h/get-kyselylinkit-by-oppija-oid oid))]
                      (rest/rest-ok kyselylinkit))
                    (catch Exception e
                      (print e)
                      (throw e)))))))

          (c-api/context "/hoksit" []
            :tags ["hoksit"]
            (c-api/context "/:eid" []
              (route-middleware
                [wrap-authorize m/wrap-hoks-access]
                share-handler/routes))

            (c-api/GET "/share/:uuid" request
              :summary "Palauttaa jakolinkit"
              :path-params [uuid :- s/Str]
              (json-response
                [{:jako-uuid "f4cb451f-d72f-4235-b376-3ce646bc0613"
                  :uuid uuid
                   :alku "2020-01-30"
                   :loppu "2020-02-12"
                   :tyyppi "HankittavaAmmatTutkinnonOsa"}
                 {:jako-uuid "f4cb451f-d72f-4235-b376-3ce646bc0614"
                  :uuid uuid
                  :alku "2020-01-24"
                  :loppu "2020-02-16"
                  :tyyppi "HankittavaAmmatTutkinnonOsa"}]))

            (c-api/POST "/share/:eid" [:as request]
              :summary "Luo linkinjaon"
              :body [body {:voimassaolo-alku s/Str
                           :voimassaolo-loppu s/Str
                           :uuid s/Str
                           :tyyppi s/Str}]
              (json-response
                {:jako-uuid "f4cb451f-d72f-4235-b376-3ce646bc0614"
                 :uuid (:uuid body)
                  :uri "uri.fi"
                  :alku "2020-01-30"
                  :loppu "2020-02-12"}))

            ))))

    (c-api/undocumented
      (GET "/buildversion.txt" []
        (response/content-type
          (response/resource-response "buildversion.txt") "text/plain")))))

(def app-routes
  (c-api/api
    {:swagger
     {:ui "/ehoks-oppija-backend/doc"
      :spec "/ehoks-oppija-backend/doc/swagger.json"
      :data {:info {:title "eHOKS Oppija backend"
                    :description "Oppija backend for eHOKS"}
             :tags [{:name "api", :description ""}]}}
     :exceptions
     {:handlers common-api/handlers}}

    (route-middleware
      [wrap-audit-logger]
      routes
      (c-api/undocumented
        (compojure-route/not-found
          (response/not-found {:reason "Route not found"}))))))
