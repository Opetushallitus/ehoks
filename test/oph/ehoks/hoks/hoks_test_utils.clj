(ns oph.ehoks.hoks.hoks-test-utils
  (:require [ring.mock.request :as mock]
            [oph.ehoks.virkailija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.external.cache :as cache]
            [oph.ehoks.utils :as utils]))

(defn create-app [session-store]
  (cache/clear-cache!)
  (common-api/create-app handler/app-routes session-store))

(defn- mock-st-request
  ([app full-url method data]
   (let [req (mock/request
               method
               full-url)]
     (utils/with-service-ticket
       app
       (if (some? data)
         (mock/json-body req data)
         req))))
  ([app full-url]
   (mock-st-request app full-url :get nil)))
