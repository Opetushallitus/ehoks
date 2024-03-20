(ns oph.ehoks.validation.handler
  (:require [compojure.api.sweet :as c-api]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.middleware :refer [wrap-opiskeluoikeus]]
            [schema.core :as s]))

(def routes
  "Validation routes"
  (c-api/context "/validointi" []
    :header-params [caller-id :- s/Str]
    :tags ["validointi"]

    (c-api/POST "/" [:as request]
      :middleware [wrap-opiskeluoikeus]
      :summary "Validointi uuden HOKSin luontiin"
      :body [hoks hoks-schema/HOKSLuonti]
      :return (rest/response {})
      (rest/ok {}))))
