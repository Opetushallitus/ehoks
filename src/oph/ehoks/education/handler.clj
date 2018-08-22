(ns oph.ehoks.education.handler
  (:require [compojure.api.sweet :refer [context GET]]
            [ring.util.http-response :refer [ok]]
            [oph.ehoks.restful :refer [response]]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.info :as info]))

(def routes
  (context
    "/education" []
    (GET "/info/" []
         :return (response [common-schema/Information])
         :summary "System information for education provider"
         (ok (response [(info/get-ehoks-info :education)])))))
