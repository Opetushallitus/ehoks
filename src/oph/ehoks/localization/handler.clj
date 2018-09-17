(ns oph.ehoks.localization.handler
  (:require [compojure.api.sweet :refer [context GET]]
            [oph.ehoks.restful :as restful]
            [ring.util.http-response :as http-response]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.external.localization :as localization]))

(def routes
  (context "/localization" []

    (GET "/" [:as request]
      :summary "Localizations for ehoks"
      :return (restful/response common-schema/Localization)
      :query-params [{category :- String "ehoks"}]
      (restful/rest-ok
        (localization/get-localization-results :category category)))

    (GET "/healthcheck" [:as request]
      :return (restful/response common-schema/LocalizationHealtcheckStatus)
      :summary "Localization Service healthcheck status"
      (if-let [response (localization/get-localization-results)]
        (restful/rest-ok {})
        (http-response/not-found)))))