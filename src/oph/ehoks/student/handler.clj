(ns oph.ehoks.student.handler
  (:require [compojure.api.sweet :refer [context GET]]
            [oph.ehoks.restful :refer [response rest-ok]]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.info :as info]))

(def routes
  (context "/student" []
    (GET "/info/" []
      :return (response [common-schema/Information])
      :summary "System information for student"
      (rest-ok [(info/get-ehoks-info :student)]))))
