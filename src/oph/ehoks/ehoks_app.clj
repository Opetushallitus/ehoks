(ns oph.ehoks.ehoks-app
  (:require [oph.ehoks.common.api :as common-api]
            [oph.ehoks.redis :refer [redis-store]]
            [oph.ehoks.oppija.handler :as oppija-handler]
            [oph.ehoks.virkailija.handler :as virkailija-handler]
            [oph.ehoks.config :refer [config]]
            [clojure.string :refer [lower-case]]))

(def app
  (when-not *compile-files*
    (common-api/create-app
      (if (= (lower-case (or (System/getProperty "NAME") "")) "virkailija")
        virkailija-handler/app-routes
        oppija-handler/app-routes)
      (when (seq (:redis-url config))
        (redis-store {:pool {}
                      :spec {:uri (:redis-url config)}})))))
