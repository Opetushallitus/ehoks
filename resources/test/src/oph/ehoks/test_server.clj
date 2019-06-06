(ns oph.ehoks.test-server
  (:require [oph.ehoks.virkailija.handler :as virkailija-handler]
            [oph.ehoks.oppija.handler :as oppija-handler]
            [oph.ehoks.common.api :as common-api]
            [compojure.core :refer [routes]]))

(def virkailija-app
  (common-api/create-app
    (routes
      #'mock/mock-routes
      #'virkailija-handler/app-routes)))

(def oppija-app
  (common-api/create-app
    (routes
      #'mock/mock-routes
      #'oppija-handler/app-routes)))
