(ns oph.ehoks.oppija.handler
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :as response]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.db.memory :as db]))

(def routes
  (c-api/context "/oppijat" []
    :tags ["oppijat"]

    (c-api/context "/:oid" [oid]

      (c-api/GET "/" []
        :summary "Oppijan perustiedot"
        :return (rest/response [common-schema/Oppija])
        (rest/rest-ok []))

      (c-api/GET "/hoks" [:as request]
        :summary "Oppijan HOKSit kokonaisuudessaan"
        :return (rest/response [hoks-schema/HOKS])
        (if (= (get-in request [:session :user :oid]) oid)
          (rest/rest-ok (db/get-all-hoks-by-oppija oid))
          (response/unauthorized))))))
