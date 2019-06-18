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
            [oph.ehoks.external.koodisto :as koodisto]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.eperusteet :as eperusteet]
            [oph.ehoks.middleware :refer [wrap-authorize]]
            [oph.ehoks.oppija.auth-handler :as auth-handler]
            [oph.ehoks.lokalisointi.handler :as lokalisointi-handler]
            [oph.ehoks.healthcheck.handler :as healthcheck-handler]
            [oph.ehoks.external.handler :as external-handler]
            [oph.ehoks.misc.handler :as misc-handler]
            [oph.ehoks.logging.access :refer [wrap-access-logger]]))

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

            (route-middleware
              [wrap-authorize]
              (c-api/context "/koodisto" []
                (c-api/GET "/:koodi-uri" [koodi-uri]
                  :path-params [koodi-uri :- s/Str]
                  :summary "Koodiston haku Koodisto-Koodi-Urilla."
                  :return (rest/response s/Any)
                  (rest/rest-ok (koodisto/get-koodi koodi-uri))))

              (c-api/context "/eperusteet" []
                (c-api/GET "/tutkinnonosat/:id/viitteet" [id]
                  :path-params [id :- Long]
                  :summary "Tutkinnon osan viitteet."
                  :return (rest/response [s/Any])
                  (rest/rest-ok (eperusteet/get-tutkinnon-osa-viitteet id)))

                (c-api/GET "/tutkinnot" []
                  :query-params [diaarinumero :- String]
                  :summary "Tutkinnon haku diaarinumeron perusteella."
                  :return (rest/response s/Any)
                  (rest/rest-ok (eperusteet/find-tutkinto diaarinumero)))

                (c-api/GET "/tutkinnot/:id/suoritustavat/reformi/rakenne" [id]
                  :path-params [id :- Long]
                  :summary "Tutkinnon rakenne."
                  :return (rest/response s/Any)
                  (rest/rest-ok (eperusteet/get-suoritustavat id)))

                (c-api/GET "/:koodi-uri" [koodi-uri]
                  :path-params [koodi-uri :- s/Str]
                  :summary "Tutkinnon osan perusteiden
                           haku Koodisto-Koodi-Urilla."
                  :return (rest/response [s/Any])
                  (rest/rest-ok (eperusteet/find-tutkinnon-osat koodi-uri))))))

          (c-api/context "/oppijat" []
            :tags ["oppijat"]

            (c-api/context "/:oid" [oid]

              (route-middleware
                [wrap-authorize]
                (c-api/GET "/" []
                  :summary "Oppijan perustiedot"
                  :return (rest/response [common-schema/Oppija])
                  (rest/rest-ok []))

                (c-api/GET "/opiskeluoikeudet" [:as request]
                  :summary "Oppijan opiskeluoikeudet"
                  :return (rest/response [s/Any])
                  (if (= (get-in request [:session :user :oid]) oid)
                    (rest/rest-ok
                      (:opiskeluoikeudet (koski/get-student-info oid)))
                    (response/forbidden)))

                (c-api/GET "/hoks" [:as request]
                  :summary "Oppijan HOKSit kokonaisuudessaan"
                  :return (rest/response [oppija-schema/OppijaHOKS])
                  (if (= (get-in request [:session :user :oid]) oid)
                    (let [hokses (h/get-hokses-by-oppija oid)]
                      (if (empty? hokses)
                        (response/not-found {:message "No HOKSes found"})
                        (rest/rest-ok (map #(dissoc % :id) hokses))))
                    (response/forbidden)))))))))

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
      [wrap-access-logger]
      routes
      (c-api/undocumented
        (compojure-route/not-found
          (response/not-found {:reason "Route not found"}))))))