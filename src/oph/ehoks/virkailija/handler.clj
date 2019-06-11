(ns oph.ehoks.virkailija.handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [compojure.route :as compojure-route]
            [schema.core :as s]
            [ring.util.http-response :as response]
            [oph.ehoks.logging.access :refer [wrap-access-logger]]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.external.cache :as c]
            [oph.ehoks.virkailija.auth :as auth]
            [oph.ehoks.user :as user]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.db.postgresql :as db]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.healthcheck.handler :as healthcheck-handler]
            [oph.ehoks.misc.handler :as misc-handler]
            [oph.ehoks.hoks.handler :as hoks-handler]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [oph.ehoks.external.koodisto :as koodisto]
            [oph.ehoks.external.eperusteet :as eperusteet]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.lokalisointi.handler :as lokalisointi-handler]))

(defn- virkailija-authenticated? [request]
  (some? (get-in request [:session :virkailija-user])))

(defn wrap-require-virkailija-user [handler]
  (fn
    ([request respond raise]
      (if (= (get-in request [:session :virkailija-user :kayttajaTyyppi])
             "VIRKAILIJA")
        (handler request respond raise)
        (respond (response/forbidden
                   {:error "User type 'VIRKAILIJA' is required"}))))
    ([request]
      (if (= (get-in request [:session :virkailija-user :kayttajaTyyppi])
             "VIRKAILIJA")
        (handler request)
        (response/forbidden
          {:error "User type 'VIRKAILIJA' is required"})))))

(defn wrap-virkailija-authorize [handler]
  (fn
    ([request respond raise]
      (if (virkailija-authenticated? request)
        (handler request respond raise)
        (respond (response/unauthorized))))
    ([request]
      (if (virkailija-authenticated? request)
        (handler request)
        (response/unauthorized)))))

(defn wrap-oph-super-user [handler]
  (fn
    ([request respond raise]
      (if (user/oph-super-user? (get-in request [:session :virkailija-user]))
        (handler request respond raise)
        (respond (response/forbidden))))
    ([request]
      (if (user/oph-super-user? (get-in request [:session :virkailija-user]))
        (handler request)
        (response/forbidden)))))

(defn virkailija-has-privilege? [ticket-user oppija-oid privilege]
  (some?
    (some
      (fn [opiskeluoikeus]
        (when
         (contains?
           (user/get-organisation-privileges
             ticket-user (:oppilaitos-oid opiskeluoikeus))
           privilege)
          opiskeluoikeus))
      (oppijaindex/get-oppija-opiskeluoikeudet oppija-oid))))

(defn virkailija-has-access? [virkailija-user oppija-oid]
  (virkailija-has-privilege? virkailija-user oppija-oid :read))

(defn wrap-virkailija-oppija-access [handler]
  (fn
    ([request respond raise]
      (if (virkailija-has-access?
            (get-in request [:session :virkailija-user])
            (get-in request [:params :oid]))
        (handler request respond raise)
        (response/forbidden
          {:error (str "User privileges does not match oppija opiskeluoikeus "
                       "organisation")})))
    ([request]
      (if (virkailija-has-access?
            (get-in request [:session :virkailija-user])
            (get-in request [:params :oid]))
        (handler request)
        (response/forbidden
          {:error (str "User privileges does not match oppija opiskeluoikeus "
                       "organisation")})))))

(def routes
  (c-api/context "/ehoks-virkailija-backend" []
    :tags ["ehoks"]
    (c-api/context "/api" []
      :tags ["api"]
      (c-api/context "/v1" []
        :tags ["v1"]

        hoks-handler/routes

        (c-api/context "/virkailija" []
          :tags ["virkailija"]
          auth/routes

          (route-middleware
            [wrap-virkailija-authorize wrap-require-virkailija-user]

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
                  (response/forbidden
                    {:error
                     (str "User has insufficient privileges "
                          "for given organisation")})
                  (let [search-params
                        (cond-> {:desc desc
                                 :item-count item-count
                                 :order-by-column order-by-column
                                 :offset (* page item-count)
                                 :oppilaitos-oid oppilaitos-oid}
                          (some? nimi) (assoc :nimi nimi)
                          (some? tutkinto) (assoc :tutkinto tutkinto)
                          (some? osaamisala) (assoc :osaamisala osaamisala))
                        oppijat (mapv
                                  #(dissoc
                                     % :oppilaitos-oid :koulutustoimija-oid)
                                  (oppijaindex/search search-params))]
                    (restful/rest-ok
                      oppijat
                      :total-count (oppijaindex/get-count search-params)))))

              (c-api/context "/:oid" []
                :path-params [oid :- s/Str]

                (route-middleware
                  [wrap-virkailija-oppija-access]

                  (c-api/GET "/hoksit" []
                    :return (restful/response [hoks-schema/HOKS])
                    :summary "Oppijan hoksit (perustiedot)"
                    (if-let [hoks (db/select-hoks-by-oppija-oid oid)]
                      (restful/rest-ok hoks)
                      (response/not-found {:message "HOKS not found"})))

                  (c-api/GET "/opiskeluoikeudet" [:as request]
                    :summary "Oppijan opiskeluoikeudet"
                    :return (restful/response [s/Any])
                    (restful/rest-ok
                      (:opiskeluoikeudet (koski/get-student-info oid))))

                  (c-api/GET "/" []
                    :return (restful/response schema/UserInfo)
                    :summary "Oppijan tiedot"
                    (let [oppija-response (onr/find-student-by-oid oid)]
                      (if (= (:status oppija-response) 200)
                        (restful/rest-ok
                          (-> oppija-response
                              :body
                              onr/convert-student-info))
                        (response/internal-server-error
                          {:error
                           "Error connecting to Oppijanumerorekisteri"})))))))

            (c-api/context "/external" []
              :tags ["virkailija-external"]

              lokalisointi-handler/routes

              (c-api/context "/koodisto" []
                (c-api/GET "/:koodi-uri" []
                  :path-params [koodi-uri :- s/Str]
                  :summary "Koodiston haku Koodisto-Koodi-Urilla."
                  :return (restful/response s/Any)
                  (restful/rest-ok (koodisto/get-koodi koodi-uri))))

              (c-api/context "/eperusteet" []
                (c-api/GET "/tutkinnonosat/:id/viitteet" []
                  :path-params [id :- Long]
                  :summary "Tutkinnon osan viitteet."
                  :return (restful/response [s/Any])
                  (restful/rest-ok (eperusteet/get-tutkinnon-osa-viitteet id)))

                (c-api/GET "/tutkinnot" []
                  :query-params [diaarinumero :- String]
                  :summary "Tutkinnon haku diaarinumeron perusteella."
                  :return (restful/response s/Any)
                  (restful/rest-ok (eperusteet/find-tutkinto diaarinumero)))

                (c-api/GET "/tutkinnot/:id/suoritustavat/reformi/rakenne" []
                  :path-params [id :- Long]
                  :summary "Tutkinnon rakenne."
                  :return (restful/response s/Any)
                  (restful/rest-ok (eperusteet/get-suoritustavat id)))

                (c-api/GET "/:koodi-uri" []
                  :path-params [koodi-uri :- s/Str]
                  :summary "Tutkinnon osan perusteiden haku
                            Koodisto-Koodi-Urilla."
                  :return (restful/response [s/Any])
                  (restful/rest-ok
                    (eperusteet/find-tutkinnon-osat koodi-uri)))))

            (c-api/context "/hoksit" []

              (c-api/POST "/" [:as request]
                :summary "Luo uuden HOKSin. Vaatii manuaalisyöttäjän oikeudet"
                :body [hoks hoks-schema/HOKSLuonti]
                :return (restful/response schema/POSTResponse :id s/Int)
                (let [virkailija-user (get-in
                                        request [:session :virkailija-user])]
                  (when-not (virkailija-has-privilege?
                              virkailija-user (:oppija-oid hoks) :write)
                    (response/forbidden!
                      {:error (str "User has unsufficient privileges")}))
                  (when (seq (db/select-hoksit-by-opiskeluoikeus-oid
                               (:opiskeluoikeus-oid hoks)))
                    (response/bad-request!
                      {:error (str "HOKS with the same opiskeluoikeus-oid "
                                   "already exists")})))
                (let [hoks-db (h/save-hoks! hoks)]
                  (restful/rest-ok
                    {:uri (format "%s/%d" (:uri request) (:id hoks-db))}
                    :id (:id hoks-db))))

              (c-api/GET "/:hoks-id" request
                :path-params [hoks-id :- s/Int]
                :summary "Hoksin tiedot. Vaatii manuaalisyöttäjän oikeudet"
                (let [hoks (db/select-hoks-by-id hoks-id)
                      virkailija-user (get-in
                                        request [:session :virkailija-user])]
                  (if (virkailija-has-privilege?
                        virkailija-user (:oppija-oid hoks) :write)
                    (restful/rest-ok (h/get-hoks-by-id hoks-id))
                    (response/forbidden
                      {:error (str "User has unsufficient privileges")}))))

              (route-middleware
                [wrap-oph-super-user]

                (c-api/GET "/" []
                  :summary "Kaikki hoksit (perustiedot).
                         Tarvitsee OPH-pääkäyttäjän oikeudet"
                  (restful/rest-ok (db/select-hoksit)))))

            (route-middleware
              [wrap-oph-super-user]

              (c-api/DELETE "/cache" []
                :summary "Välimuistin tyhjennys"
                (c/clear-cache!)
                (response/ok)))))

        healthcheck-handler/routes
        misc-handler/routes))

    (c-api/undocumented
      (c-api/GET "/buildversion.txt" []
        (response/content-type
          (response/resource-response "buildversion.txt") "text/plain")))))

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

    (route-middleware
      [wrap-access-logger]

      routes
      (c-api/undocumented
        (compojure-route/not-found
          (response/not-found {:reason "Route not found"}))))))
