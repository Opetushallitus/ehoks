(ns oph.ehoks.validation.handler
  (:require [compojure.api.sweet :as c-api]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.hoks.schema :as hoks-schema]))

(def routes
  (c-api/context "/validointi" []
    :tags ["validointi"]

    (c-api/POST "/" [:as request]
      :summary "Validointi uuden HOKSin luontiin"
      :body [hoks hoks-schema/HOKSLuonti]
      :return (rest/response {})
      (rest/rest-ok {}))))
