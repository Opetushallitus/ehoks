(ns oph.ehoks.oppija.share-handler
  (:require [compojure.api.sweet :as c-api]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.oppija.schema :as oppija-schema]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.db.db-operations.oppija :as db]))

(def routes
  (c-api/context "/share" []
    :tags ["share"]

    (c-api/context "/:koodi-uri" []
      :path-params [koodi-uri :- String]
      (c-api/GET "/" request
        :return (rest/response [oppija-schema/Jakolinkki])
        :summary "Tutkinnon osan jakolinkkien listaus"
        (rest/rest-ok
          (db/select-hoks-tutkinnon-osa-shares
            (get-in request [:hoks :id]) koodi-uri)))

      (c-api/POST "/" request
        :body [values oppija-schema/JakolinkkiLuonti]
        :return (rest/response schema/POSTResponse :uuid java.util.UUID)
        (let [jakolinkki (db/insert-tutkinnon-osa-share!
                           (assoc values
                                  :hoks-id (get-in request [:hoks :id])
                                  :koodisto-koodi koodi-uri))]
          (rest/rest-ok
            {:uri (format "%s/%s" (:uri request) (:uuid jakolinkki))}
            :uuid (:uuid jakolinkki)))))))