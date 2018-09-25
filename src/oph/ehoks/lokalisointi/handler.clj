(ns oph.ehoks.lokalisointi.handler
  (:require [compojure.api.sweet :refer [context GET]]
            [oph.ehoks.restful :as restful]
            [ring.util.http-response :as http-response]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.external.lokalisointi :as lokalisointi]))

(def routes
  (context "/lokalisointi" []

    (GET "/" [:as request]
      :summary "Localizations for ehoks"
      :return (restful/response common-schema/Lokalisointi)
      :query-params [{category :- String "ehoks"}]
      (restful/rest-ok
        (lokalisointi/get-localization-results :category category)))))
