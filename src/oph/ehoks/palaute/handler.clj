(ns oph.ehoks.palaute.handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [compojure.core :refer [GET]]
            [compojure.route :as compojure-route]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.db :as db]
            [oph.ehoks.healthcheck.handler :as healthcheck-handler]
            [oph.ehoks.hoks.middleware :as m]
            [oph.ehoks.logging.audit :as audit]
            [oph.ehoks.middleware :refer [wrap-user-details]]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.vastaajatunnus :as vt]
            [oph.ehoks.restful :as restful]
            [ring.util.http-response :as response]
            [schema.core :as s]))

(def routes
  "Palaute routes"
  (c-api/context "/ehoks-palaute-backend" []
    :tags ["ehoks"]
    (c-api/context "/api" []
      :tags ["api"]
      (c-api/context "/v1" []
        :tags ["v1"]
        healthcheck-handler/routes

        (route-middleware
          [wrap-user-details m/wrap-require-service-user
           audit/wrap-logger m/wrap-require-oph-privileges]

          (c-api/context "/opiskelijapalaute" []
            :tags ["opiskelijapalaute"]

            (c-api/POST "/:hoks-id/kyselylinkki" [hoks-id]
              :summary "Luo yhden HOKSin kyselylinkit, jos niitä ei ole luotu."
              (let [amis-palautteet
                    (palaute/get-palautteet-waiting-for-vastaajatunnus!
                      db/spec {:kyselytyypit ["aloittaneet" "valmistuneet"
                                              "osia_suorittaneet"]
                               :hoks-id hoks-id
                               :palaute-id nil})
                    vastaajatunnukset
                    (map vt/handle-palaute-waiting-for-heratepvm!
                         amis-palautteet)]
                (assoc (restful/ok {:vastaajatunnukset vastaajatunnukset})
                       ::audit/target {:vastaajatunnukset vastaajatunnukset
                                       :hoks-id hoks-id})))

            (c-api/POST "/kyselylinkit" []
              :summary "Luo kyselylinkit niille palautteille, jotka
                       odottavat käsittelyä."
              (let [palautteet
                    (vt/handle-amis-palautteet-on-heratepvm! {})]
                (-> {:kyselylinkit palautteet}
                    (restful/ok)
                    (assoc ::audit/target {:palautteet palautteet})))))

          (c-api/context "/tyoelamapalaute" []
            :tags ["tyoelamapalaute"]

            (c-api/POST "/:palaute-id/vastaajatunnus" []
              :summary "Luo vastaajatunnuksen yksittäiselle palautteelle."
              :header-params [caller-id :- s/Str
                              ticket :- s/Str]
              :path-params [palaute-id :- s/Int]
              (let [tep-palautteet
                    (palaute/get-palautteet-waiting-for-vastaajatunnus!
                      db/spec
                      {:kyselytyypit ["tyopaikkajakson_suorittaneet"]
                       :palaute-id palaute-id
                       :hoks-id nil})
                    vastaajatunnukset
                    (map vt/handle-palaute-waiting-for-heratepvm!
                         tep-palautteet)]
                (assoc (restful/ok {:vastaajatunnukset vastaajatunnukset})
                       ::audit/target {:vastaajatunnukset vastaajatunnukset
                                       :hoks-id nil
                                       :palaute-id palaute-id})))

            (c-api/POST "/vastaajatunnukset" []
              :summary "Luo vastaajatunnukset niille palautteille, jotka
                        odottavat käsittelyä."
              :header-params [caller-id :- s/Str
                              ticket :- s/Str]
              (let [vastaajatunnukset
                    (vt/handle-tep-palautteet-on-heratepvm! {})]
                (assoc
                  (restful/ok {:vastaajatunnukset vastaajatunnukset})
                  ::audit/target {:vastaajatunnukset vastaajatunnukset})))))))

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
