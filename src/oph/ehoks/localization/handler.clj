(ns oph.ehoks.localization.handler
  (:require [compojure.api.sweet :refer [context GET]]
            [oph.ehoks.restful :as restful]
            [ring.util.http-response :as http-response]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.external.localization :as localization]))

(def routes
  (context "/lokalisaatio" []

    (GET "/" [:as request]
      :summary "Localizations for ehoks"
      :return (restful/response common-schema/Localization)
      :query-params [{category :- String "ehoks"}]
      (restful/rest-ok
        (localization/get-localization-results :category category)))))
