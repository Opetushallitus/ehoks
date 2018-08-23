(ns oph.ehoks.work.handler
  (:require [compojure.api.sweet :refer [context GET]]
            [oph.ehoks.restful :refer [response rest-ok]]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.info :as info]))

(def routes
  (context "/work" []
    (GET "/info/" []
      :return (response [common-schema/Information])
      :summary "System information for workplace provider"
      (rest-ok [(info/get-ehoks-info :work)]))))
