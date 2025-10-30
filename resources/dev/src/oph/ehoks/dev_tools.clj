(ns oph.ehoks.dev-tools
  (:require [oph.ehoks.db.session-store :as store]
            [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [ring.util.http-response :as response]
            [schema.core :as s]
            [oph.ehoks.schema.oid :as oid-schema]
            [oph.ehoks.db.db-operations.oppija :as oppija-db]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as opiskeluoikeus-db]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(defn get-current-session-key [request]
  (get-in request [:cookies "ring-session" :value]))

(defn set-session-privileges [session-key privileges]
  (let [session (store/get-session session-key)]
    (store/save-session!
      session-key
      (assoc-in session
                [:virkailija-user :organisation-privileges]
                privileges))))

(defn add-session-privilege [session-key privileges]
  (let [session (store/get-session session-key)]
    (store/save-session!
      session-key
      (update-in session
                 [:virkailija-user :organisation-privileges]
                 conj
                 privileges))))

(defn remove-session-organisation [session-key organisation-oid]
  (let [session (store/get-session session-key)]
    (store/save-session!
      session-key
      (assoc-in
        session
        [:virkailija-user :organisation-privileges]
        (remove
          (fn [o]
            (= (:oid o) organisation-oid))
          (get-in session [:virkailija-user :organisation-privileges]))))))

(defn wrap-session-key [handler]
  (fn
    ([request respond raise]
      (if-let [session-key (get-current-session-key request)]
        (handler (assoc request :session-key session-key) respond raise)
        (respond (response/bad-request "Session key not in cookie"))))
    ([request]
      (if-let [session-key (get-current-session-key request)]
        (handler (assoc request :session-key session-key))
        (response/bad-request "Session key not in cookie")))))

(defn wrap-require-session [handler]
  (fn
    ([request respond raise]
      (if (some? (store/get-session (:session-key request)))
        (handler request respond raise)
        (respond (response/bad-request "Please login first"))))
    ([request]
      (if (some? (store/get-session (:session-key request)))
        (handler request)
        (response/bad-request "Please login first")))))

(s/defschema OrganisationPrivilege
             {:oid s/Str
              :roles [s/Str]
              :privileges [s/Str]})

(s/defschema Oppija
             {:oid s/Str
              :nimi s/Str})

(s/defschema Opiskeluoikeus
             {:oid s/Str
              :oppija-oid s/Str
              :oppilaitos-oid s/Str
              :koulutustoimija-oid s/Str
              :tutkinto-nimi {s/Keyword s/Str}
              :osaamisala-nimi {s/Keyword s/Str}
              :paattynyt s/Inst})

(def routes
  (c-api/context "/dev-tools" []
    :tags ["dev"]

    (route-middleware
      [wrap-session-key wrap-require-session]

      (c-api/context "/session" []
        (c-api/GET "/" request
          :summary "Get current session from db"
          (response/ok (store/get-session (:session-key request))))

        (c-api/PATCH "/" request
          :summary "Update current session in db. Be aware. This is a powerful
                     tool and might brake things easily."
          :body [session s/Any]
          (store/save-session! (:session-key request) session)
          (response/no-content)))

      (c-api/context "/organisation-privileges" []
        (c-api/DELETE "/:organisation-oid" request
          :path-params [organisation-oid :- oid-schema/OrganisaatioOID]
          :summary "Remove organisation from current user"
          (remove-session-organisation (:session-key request) organisation-oid)
          (response/no-content))

        (c-api/POST "/" request
          :summary "Add organisation privileges"
          :body [privileges OrganisationPrivilege]
          (add-session-privilege (:session-key request) privileges)
          (response/no-content)))

      (c-api/context "/oppijaindex" []

        (c-api/POST "/oppija" []
          :body [oppija Oppija]
          (response/ok (oppija-db/insert-oppija! oppija)))

        (c-api/DELETE "/oppija/:oppija-oid" []
          :path-params [oppija-oid :- oid-schema/OppijaOID]
          (response/ok (db-ops/delete! :oppijat ["oid = ?" oppija-oid])))

        (c-api/POST "/opiskeluoikeus" []
          :body [opiskeluoikeus Opiskeluoikeus]
          (response/ok
            (opiskeluoikeus-db/insert-opiskeluoikeus! opiskeluoikeus)))

        (c-api/DELETE "/opiskeluoikeus/:opiskeluoikeus-oid" []
          :path-params [opiskeluoikeus-oid :- oid-schema/OpiskeluoikeusOID]
          (response/ok
            (db-ops/delete!
              :opiskeluoikeudet ["oid = ?" opiskeluoikeus-oid])))))))

; Currently not in use. If this is enabled requests has no more body.
; Cost of this is no swagger docs
(def api-routes
  (c-api/api
    {:swagger
     {:ui "/dev-tools/doc"
      :spec "/dev-tools/doc/swagger.json"
      :data {:info {:title "Dev tools"
                    :description "Dev tools"}
             :tags [{:name "api", :description ""}]}}}
    routes))
