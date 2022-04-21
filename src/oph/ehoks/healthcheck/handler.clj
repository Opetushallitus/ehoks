(ns oph.ehoks.healthcheck.handler
  (:require [compojure.api.sweet :refer [context GET]]
            [ring.util.http-response :refer [ok]]
            [oph.ehoks.common.schema :as common-schema]))

(def routes
  "Healthcheck-handlerin reitit"
  (context "/healthcheck" []
    :tags ["healthcheck"]
    (GET "/" []
      :return common-schema/HealthcheckStatus
      :summary "Service healthcheck status"
      (ok {}))))
