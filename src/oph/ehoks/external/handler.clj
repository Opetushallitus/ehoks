(ns oph.ehoks.external.handler
  (:require [compojure.api.sweet :refer [context GET]]
            [oph.ehoks.restful :refer [response rest-ok]]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.external.localization :as localization]))

(def routes
  (context "/localization" []
    (GET "/" [:as request]
      :summary "Localizations for ehoks"
      :query-params [{category :- String "ehoks"}]
      (rest-ok
        (localization/get-localization-results :category category)))))
