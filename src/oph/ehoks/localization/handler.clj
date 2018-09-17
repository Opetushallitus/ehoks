(ns oph.ehoks.localization.handler
  (:require [compojure.api.sweet :refer [context GET]]
            [oph.ehoks.restful :refer [response rest-ok]]
            [ring.util.http-response :refer [not-found]]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.external.localization :as localization]))

(def routes
  (context "/localization" []

    (GET "/" [:as request]
      :summary "Localizations for ehoks"
      :return (response common-schema/Localization)
      :query-params [{category :- String "ehoks"}]
      (rest-ok
        (localization/get-localization-results :category category)))

    (GET "/healthcheck" [:as request]
      :return (response common-schema/LocalizationHealtcheckStatus)
      :summary "Localization Service healthcheck status"
      (if-let [response (localization/get-localization-results)]
        (rest-ok {})
        (not-found)))))