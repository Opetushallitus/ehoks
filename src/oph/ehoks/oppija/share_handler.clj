(ns oph.ehoks.oppija.share-handler
  (:require [compojure.api.sweet :as c-api]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.oppija.schema :as oppija-schema]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.db.db-operations.shared-modules :as db]
            [ring.util.http-response :as response]
            [oph.ehoks.hoks.hankittavat :as h]
            [clj-time.core :as t])
  (:import (java.time DateTimeException LocalDate)))

(defn- get-tutkinnonosa-details [tyyppi uuid]
  (cond
    (= tyyppi "HankittavaAmmatillinenTutkinnonOsa")
    (db/select-hankittavat-ammat-tutkinnon-osat-by-module-id uuid)
    (= tyyppi "HankittavaYTOOsaAlue")
    (db/select-hankittavat-yhteiset-tutkinnon-osat-by-module-id uuid)
    (= tyyppi "HankittavaPaikallinenTutkinnonOsa")
    (db/select-hankittavat-paikalliset-tutkinnon-osat-by-module-id uuid)))

(defn- jakolinkki-still-valid? [jakolinkki]
  (if (.isBefore (:voimassaolo-loppu jakolinkki) (LocalDate/now))
    (throw (DateTimeException. "Shared link is expired"))
    true))

(defn- fetch-shared-link-data
  "Queries and combines data associated with the shared link"
  [uuid]
  (when-let [jakolinkki (db/select-shared-link uuid)]
    (jakolinkki-still-valid? jakolinkki)
    (let [oppija (db/select-oppija-opiskeluoikeus-for-shared-link uuid)
          tutkinnonosa (get-tutkinnonosa-details
                         (:tutkinnonosa-tyyppi jakolinkki)
                         (:tutkinnonosa-module-uuid jakolinkki))
          module (h/get-osaamisenosoittaminen-or-hankkimistapa-of-jakolinkki
                   jakolinkki)]
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
          (catch DateTimeException dex
            (response/gone! {:message (ex-message dex)}))
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
