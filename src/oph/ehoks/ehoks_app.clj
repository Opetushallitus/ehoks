(ns oph.ehoks.ehoks-app
  (:require [oph.ehoks.common.api :as common-api]
            [oph.ehoks.redis :refer [redis-store]]
            [oph.ehoks.oppija.handler :as oppija-handler]
            [oph.ehoks.virkailija.handler :as virkailija-handler]
            [oph.ehoks.config :refer [config]]
            [clojure.string :refer [lower-case]]))

(defn create-app [app-name]
  (common-api/create-app
    (if (= app-name "virkailija")
      virkailija-handler/app-routes
      oppija-handler/app-routes)
    (when (seq (:redis-url config))
      (redis-store {:pool {}
                    :spec {:uri (:redis-url config)}}))))

(def app (create-app (lower-case (or (System/getProperty "NAME") ""))))
