(ns oph.ehoks.lokalisointi.handler
  (:require [compojure.api.sweet :as c-api]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.external.lokalisointi :as lokalisointi]
            [clojure.core.async :as a]))

(def routes
  "Lokalisointireitit"
  (c-api/context "/lokalisointi" []
    (c-api/GET "/" [:as request]
      :summary "Hakee lokalisoinnin tulokset lokalisointipalvelusta"
      :return (restful/response common-schema/Lokalisointi)
      :query-params [{category :- String "ehoks"}]
      (a/go (restful/ok
              (lokalisointi/get-localization-results :category category))))))
