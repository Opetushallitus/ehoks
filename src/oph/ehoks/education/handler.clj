(ns oph.ehoks.education.handler
  (:require [compojure.api.sweet :refer [context GET]]
            [oph.ehoks.restful :refer [response rest-ok]]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.info :as info]))

(def routes
  (context "/education" []
    (GET "/info/" []
      :return (response [common-schema/Information])
      :summary "System information for education provider"
      (rest-ok [(info/get-ehoks-info :education)]))))
