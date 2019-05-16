(ns oph.ehoks.ehoks-app
  (:require [oph.ehoks.common.api :as common-api]
            [oph.ehoks.redis :refer [redis-store]]
            [oph.ehoks.handler :as handler]
            [oph.ehoks.config :refer [config]]))

(def app
  (common-api/create-app
    handler/app-routes
    (when (seq (:redis-url config))
      (redis-store {:pool {}
                    :spec {:uri (:redis-url config)}}))))
