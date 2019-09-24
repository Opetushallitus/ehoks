(ns oph.ehoks.virkailija.handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [compojure.route :as compojure-route]
            [schema.core :as s]
            [ring.util.http-response :as response]
            [oph.ehoks.resources :as resources]
            [oph.ehoks.logging.audit :refer [wrap-audit-logger]]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.virkailija.auth :as auth]
            [oph.ehoks.user :as user]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.healthcheck.handler :as healthcheck-handler]
            [oph.ehoks.misc.handler :as misc-handler]
            [oph.ehoks.hoks.handler :as hoks-handler]
            [oph.ehoks.oppijaindex :as op]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.validation.handler :as validation-handler]
            [clojure.core.async :as a]
            [clojure.tools.logging :as log]
            [oph.ehoks.virkailija.middleware :as m]
            [oph.ehoks.virkailija.system-handler :as system-handler]
            [oph.ehoks.virkailija.external-handler :as external-handler]
            [oph.ehoks.virkailija.cas-handler :as cas-handler]))

(def routes
  (c-api/context "/ehoks-virkailija-backend" []
    :tags ["ehoks"]

    (c-api/context "/cas-security-check" []
      cas-handler/routes)

    (c-api/context "/api" []
      :tags ["api"]
      (c-api/context "/v1" []
        :tags ["v1"]

        (route-middleware
          [wrap-audit-logger]

          hoks-handler/routes
          validation-handler/routes

          (c-api/context "/virkailija" []
            :tags ["virkailija"]
            auth/routes

            (route-middleware
              [m/wrap-virkailija-authorize m/wrap-require-virkailija-user]

              external-handler/routes
              system-handler/routes

              (c-api/context "/oppijat" []
                (c-api/GET "/" request
                  :return (restful/response
                            [common-schema/OppijaSearchResult]
                            :total-count s/Int)
                  :query-params [{order-by-column :- s/Keyword :nimi}
                                 {desc :- s/Bool false}
                                 {nimi :- s/Str nil}
                                 {tutkinto :- s/Str nil}
                                 {osaamisala :- s/Str nil}
                                 {item-count :- s/Int 10}
                                 {page :- s/Int 0}
                                 oppilaitos-oid :- s/Str]
                  :summary "Listaa virkailijan oppilaitoksen oppijat, joilla on
                       HOKS luotuna. Käyttäjällä pitää olla READ käyttöoikeus
                       annettuun organisaatioon eHOKS-palvelussa."

                  (if-not (contains?
                            (user/get-organisation-privileges
                              (get-in
                                request
                                [:session :virkailija-user])
                              oppilaitos-oid)
                            :read)
                    (do
                      (log/warnf
                        "User %s privileges does not match oppilaitos %s"
                        (get-in request [:session
                                         :virkailija-user
                                         :oidHenkilo])
                        oppilaitos-oid)
                      (response/forbidden
                        {:error
                         (str "User has insufficient privileges for "
                              "given organisation")}))
                    (let [search-params
                          (cond->
                           {:desc desc
                            :item-count item-count
                            :order-by-column order-by-column
                            :offset (* page item-count)
                            :oppilaitos-oid oppilaitos-oid}
                            (some? nimi)
                            (assoc :nimi nimi)
                            (some? tutkinto)
                            (assoc :tutkinto tutkinto)
                            (some? osaamisala)
                            (assoc :osaamisala osaamisala))
                          oppijat (mapv
                                    #(dissoc
                                       % :oppilaitos-oid :koulutustoimija-oid)
                                    (op/search search-params))]
                      (restful/rest-ok
                        oppijat
                        :total-count (op/get-count search-params)))))

                (c-api/context "/:oppija-oid" []
                  :path-params [oppija-oid :- s/Str]

                  (c-api/POST "/index" []
                    :summary
                    "Indeksoi oppijan tiedot, jos on tarpeen. DEPRECATED"
                    (a/go
                      (response/ok {:message "Route is deprected."})))

                  (c-api/context "/hoksit" []
                    (c-api/POST "/" [:as request]
                      :summary "Luo uuden HOKSin.
                                Vaatii manuaalisyöttäjän oikeudet"
                      :body [hoks hoks-schema/HOKSLuonti]
                      :return (restful/response schema/POSTResponse :id s/Int)
                      (try
                        (op/add-oppija! (:oppija-oid hoks))
                        (catch Exception e
                          (if (= (:status (ex-data e)) 404)
                            (do
                              (log/warn "Oppija with oid "
                                        (:oppija-oid hoks)
                                        " not found in ONR")
                              (response/bad-request!
                                {:error
                                 (str "Oppija not found in"
                                      " Oppijanumerorekisteri")}))
                            (throw e))))
                      (try
                        (op/add-opiskeluoikeus!
                          (:opiskeluoikeus-oid hoks) (:oppija-oid hoks))
                        (catch Exception e
                          (if (= (:status (ex-data e)) 404)
                            (do
                              (log/warn "Opiskeluoikeus with oid "
                                        (:opiskeluoikeus-oid hoks)
                                        " not found in Koski")
                              (response/bad-request!
                                {:error "Opiskeluoikeus not found in Koski"}))
                            (throw e))))
                      (let [virkailija-user
                            (get-in request [:session :virkailija-user])]
                        (when-not
                         (m/virkailija-has-privilege-in-opiskeluoikeus?
                           virkailija-user (:opiskeluoikeus-oid hoks) :write)
                          (log/warnf "User %s privileges don't match oppija %s"
                                     (get-in request [:session
                                                      :virkailija-user
                                                      :oidHenkilo])
                                     (:oppija-oid hoks))
                          (response/forbidden!
                            {:error
                             (str "User has unsufficient privileges")})))
                      (try
                        (let [hoks-db (h/save-hoks!
                                        (assoc hoks :manuaalisyotto true))]
                          (restful/rest-ok
                            {:uri (format "%s/%d"
                                          (:uri request)
                                          (:id hoks-db))}
                            :id (:id hoks-db)))
                        (catch Exception e
                          (if (= (:error (ex-data e)) :duplicate)
                            (do
                              (log/warnf
                                "HOKS with opiskeluoikeus-oid %s already exists"
                                (:opiskeluoikeus-oid hoks))
                              (response/bad-request!
                                {:error
                                 (str "HOKS with the same "
                                      "opiskeluoikeus-oid already exists")}))
                            (throw e)))))

                    (route-middleware
                      [m/wrap-virkailija-oppija-access]
                      (c-api/GET "/" []
                        :return (restful/response [hoks-schema/HOKS])
                        :summary "Oppijan hoksit (perustiedot)"
                        (if-let [hoks
                                 (db-hoks/select-hoks-by-oppija-oid oppija-oid)]
                          (restful/rest-ok hoks)
                          (response/not-found {:message "HOKS not found"})))

                      (c-api/GET "/:hoks-id" request
                        :path-params [hoks-id :- s/Int]
                        :summary "Hoksin tiedot.
                                Vaatii manuaalisyöttäjän oikeudet"
                        (let [hoks (db-hoks/select-hoks-by-id hoks-id)
                              virkailija-user (get-in
                                                request
                                                [:session :virkailija-user])]
                          (if (m/virkailija-has-privilege?
                                virkailija-user (:oppija-oid hoks) :read)
                            (restful/rest-ok (h/get-hoks-by-id hoks-id))
                            (do
                              (log/warnf
                                "User %s privileges don't match oppija %s"
                                (get-in request [:session
                                                 :virkailija-user
                                                 :oidHenkilo])
                                (get-in request [:params :oppija-oid]))
                              (response/forbidden
                                {:error
                                 (str "User has insufficient privileges")})))))

                      (route-middleware
                        [m/wrap-virkailija-write-access]

                        (c-api/context "/:hoks-id" []
                          :path-params [hoks-id :- s/Int]

                          (c-api/PUT "/" request
                            :summary
                            "Ylikirjoittaa olemassa olevan HOKSin arvon tai arvot"
                            :body [hoks-values hoks-schema/HOKSKorvaus]
                            (h/replace-hoks!
                              hoks-id
                              (dissoc hoks-values
                                      :oppija-oid
                                      :opiskeluoikeus-oid))
                            (response/no-content))

                          (c-api/PATCH "/" request
                            :body [hoks-values hoks-schema/HOKSPaivitys]
                            :summary "Oppijan hoksin päätason arvojen päivitys"
                            (h/update-hoks!
                              hoks-id
                              (dissoc
                                hoks-values
                                :opiskeluoikeus-oid
                                :oppija-oid))
                            (response/no-content))))

                      (route-middleware
                        [m/wrap-oph-super-user]

                        (c-api/GET "/" []
                          :summary "Kaikki hoksit (perustiedot).
                        Tarvitsee OPH-pääkäyttäjän oikeudet"
                          (restful/rest-ok (db-hoks/select-hoksit))))))

                  (route-middleware
                    [m/wrap-virkailija-oppija-access]

                    (c-api/GET "/opiskeluoikeudet" [:as request]
                      :summary "Oppijan opiskeluoikeudet"
                      :return (restful/response [s/Any])
                      (restful/rest-ok
                        (koski/get-oppija-opiskeluoikeudet oppija-oid)))

                    (c-api/GET "/" []
                      :return (restful/response common-schema/Oppija)
                      :summary "Oppijan tiedot"
                      (if-let [oppija (op/get-oppija-by-oid oppija-oid)]
                        (restful/rest-ok oppija)
                        (response/not-found)))))))))

        healthcheck-handler/routes
        misc-handler/routes))

    (c-api/undocumented
      (c-api/GET "/buildversion.txt" []
        (response/content-type
          (response/resource-response "buildversion.txt") "text/plain"))
      (resources/create-routes "/json-viewer" "json-viewer"))))

(def app-routes
  (c-api/api
    {:swagger
     {:ui "/ehoks-virkailija-backend/doc"
      :spec "/ehoks-virkailija-backend/doc/swagger.json"
      :data {:info {:title "eHOKS virkailija backend"
                    :description "eHOKS virkailijan näkymän ja
                                  HOKS-rajapinnan backend"}
             :tags [{:name "api", :description ""}]}}
     :exceptions
     {:handlers common-api/handlers}}

    routes
    (c-api/undocumented
      (compojure-route/not-found
        (response/not-found {:reason "Route not found"})))))
