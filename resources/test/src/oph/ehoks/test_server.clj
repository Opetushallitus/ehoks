(ns oph.ehoks.test-server
  (:require [oph.ehoks.handler :refer [app]]
            [compojure.core :refer [routes]]))

(def test-app
  (routes
    #'mock/mock-routes
    #'app))
