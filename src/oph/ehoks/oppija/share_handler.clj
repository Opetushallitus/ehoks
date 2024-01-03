(ns oph.ehoks.oppija.share-handler
  (:require [compojure.api.sweet :as c-api]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.oppija.schema :as oppija-schema]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.db.db-operations.shared-modules :as db]
            [ring.util.http-response :as response]
            [oph.ehoks.hoks.hankittavat :as h])
  (:import (java.time LocalDate)
           (clojure.lang ExceptionInfo)))

(defn- get-tutkinnonosa-details
  "Get hankittava tutkinnon osa details by module ID"
  [tyyppi uuid]
  (cond
    (= tyyppi "HankittavaAmmatillinenTutkinnonOsa")
    (db/select-hankittavat-ammat-tutkinnon-osat-by-module-id uuid)
    (= tyyppi "HankittavaYTOOsaAlue")
    (db/select-hankittavat-yhteiset-tutkinnon-osat-by-module-id uuid)
    (= tyyppi "HankittavaPaikallinenTutkinnonOsa")
    (db/select-hankittavat-paikalliset-tutkinnon-osat-by-module-id uuid)))

(defn- jakolinkki-still-valid?
  "Check whether jakolinkki is still valid"
  [jakolinkki]
  (cond
    (.isAfter ^LocalDate (:voimassaolo-alku jakolinkki) (LocalDate/now))
    (throw (ex-info "Shared link not yet active" {:cause :inactive}))
    (.isBefore ^LocalDate (:voimassaolo-loppu jakolinkki) (LocalDate/now))
    (throw (ex-info "Shared link is expired" {:cause :expired}))))

(defn- fetch-shared-link-data
  "Queries and combines data associated with the shared link"
  [uuid]
  (when-let [jakolinkki (db/select-shared-link uuid)]
    (jakolinkki-still-valid? jakolinkki)
    (let [oppija (db/select-oppija-opiskeluoikeus-for-shared-link uuid)
          tutkinnonosa (get-tutkinnonosa-details
                         (:tutkinnonosa-tyyppi jakolinkki)
                         (:tutkinnonosa-module-uuid jakolinkki))
          module (first
                   (h/get-osaamisenosoittaminen-or-hankkimistapa-of-jakolinkki
                     jakolinkki))]
      (cond
        (= (:shared-module-tyyppi jakolinkki) "osaamisenhankkiminen")
        (assoc oppija
               :tutkinnonosa tutkinnonosa
               :osaamisen-osoittaminen nil
               :osaamisen-hankkimistapa module)
        (= (:shared-module-tyyppi jakolinkki) "osaamisenosoittaminen")
        (assoc oppija
               :tutkinnonosa tutkinnonosa
               :osaamisen-osoittaminen module
               :osaamisen-hankkimistapa nil)))))

(def routes
  "Oppija share handler routes"
  (c-api/context "/" []
    :tags ["jaot"]

    (c-api/context "/jakolinkit" []

      (c-api/POST "/" [:as request]
        :body [values oppija-schema/JakolinkkiLuonti]
        :return (rest/response schema/POSTResponse :uuid String)
        :summary "Luo uuden jakolinkin"
        (try
          (let [jakolinkki (db/insert-shared-module! values)
                share-id (str (:share_id jakolinkki))]
            (rest/rest-ok
              {:uri (format "%s/%s" (:uri request) share-id)}
              :uuid share-id))
          (catch Exception e
            (response/bad-request! {:error (ex-message e)}))))

      (c-api/GET "/:uuid" []
        :return (rest/response oppija-schema/Jakolinkki)
        :summary "Yksittäisen jakolinkin katselunäkymän tietojen haku"
        :path-params [uuid :- String]
        (try
          (if-let [jakolinkki (fetch-shared-link-data uuid)]
            (rest/rest-ok jakolinkki)
            (response/not-found))
          (catch ExceptionInfo ei
            (cond
              (= :expired (-> ei ex-data :cause))
              (response/gone! {:message (ex-message ei)})
              (= :inactive (-> ei ex-data :cause))
              (response/locked! {:message (ex-message ei)})))
          (catch Exception e
            (response/bad-request! {:error (ex-message e)}))))

      (c-api/DELETE "/:uuid" []
        :summary "Poistaa jakolinkin"
        :path-params [uuid :- String]
        (let [deleted (db/delete-shared-module! uuid)]
          (if (pos? (first deleted))
            (response/ok)
            (response/not-found)))))

    (c-api/GET "/moduulit/:module-uuid" []
      :return (rest/response [oppija-schema/JakolinkkiListaus])
      :summary "Jakolinkkien listausnäkymän tietojen haku"
      :path-params [module-uuid :- String]
      (let [jakolinkit (db/select-shared-module-links module-uuid)]
        (if (pos? (count jakolinkit))
          (rest/rest-ok jakolinkit)
          (rest/rest-ok []))))))
