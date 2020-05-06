(ns oph.ehoks.oppija.share-handler
  (:require [compojure.api.sweet :as c-api]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.oppija.schema :as oppija-schema]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.db.db-operations.oppija :as db]
            [ring.util.http-response :as response]))

(def routes
  (c-api/context "/" []
    :tags ["jaot"]

    (c-api/context "/jakolinkit" []

      (c-api/POST "/" [:as request]
        :body [values oppija-schema/JakolinkkiLuonti]
        :return (rest/response schema/POSTResponse :uuid String)
        :summary "Luo uuden jakolinkin"
        (let [jakolinkki (db/insert-shared-module! values)
              share-id (str (:share_id jakolinkki))]
          (rest/rest-ok
            {:uri (format "%s/%s" (:uri request) share-id)}
            :uuid share-id)))

      (c-api/GET "/:uuid" []
        :return (rest/response [oppija-schema/Jakolinkki])
        :summary "Jakolinkkiin liitettyjen tietojen haku"
        :path-params [uuid :- String]
        (rest/rest-ok
          (db/select-shared-module uuid)))

      (c-api/DELETE "/:uuid" []
        :summary "Poistaa jakolinkin"
        :path-params [uuid :- String]
        (db/delete-shared-module! uuid)
        (response/ok)))

    (c-api/GET "/moduulit/:uuid" []
      :return (rest/response [oppija-schema/ModuuliLinkit])
      :summary "Jaettuun moduuliin liitettyjen jakolinkkien haku"
      :path-params [uuid :- String]
      (rest/rest-ok
        (db/select-shared-module-links uuid)))))
