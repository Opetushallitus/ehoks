(ns oph.ehoks.healthcheck.handler
  (:require [compojure.api.sweet :refer [context GET]]
            [ring.util.http-response :refer [ok]]
            [oph.ehoks.common.schema :as common-schema]))

(def routes
  (context "/healthcheck" []
    (GET "/" []
      :return common-schema/HealthcheckStatus
      :summary "Service healthcheck status"
      (ok {}))))
