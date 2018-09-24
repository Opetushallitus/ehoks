(ns oph.ehoks.lokalisaatio.handler
  (:require [compojure.api.sweet :refer [context GET]]
            [oph.ehoks.restful :as restful]
            [ring.util.http-response :as http-response]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.external.lokalisaatio :as lokalisaatio]))

(def routes
  (context "/lokalisaatio" []

    (GET "/" [:as request]
      :summary "Localizations for ehoks"
      :return (restful/response common-schema/Lokalisaatio)
      :query-params [{category :- String "ehoks"}]
      (restful/rest-ok
        (lokalisaatio/get-localization-results :category category)))))
