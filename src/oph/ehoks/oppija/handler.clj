(ns oph.ehoks.oppija.handler
  (:require [compojure.api.sweet :as c-api]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.common.schema :as common-schema]))

(def routes
  (c-api/context "/oppijat" []
    :tags ["oppijat"]

    (c-api/context "/:oid" [oid]

      (c-api/GET "/" []
        :summary "Oppijan perustiedot"
        :return (rest/response [common-schema/Oppija])
        (rest/rest-ok []))

      (c-api/GET "/hoks" []
        :summary "Oppijan HOKS kokonaisuudessaan"
        :return (rest/response [hoks-schema/HOKS])
        (rest/rest-ok {})))))
