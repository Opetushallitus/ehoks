(ns oph.ehoks.hoks.hoks-test-utils
  (:require [ring.mock.request :as mock]
            [oph.ehoks.virkailija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.external.cache :as cache]
            [oph.ehoks.utils :as utils]))

;TODO laita privateiksi sopivat
;TODO poista handler-testista duplikaatti url
(def url "/ehoks-virkailija-backend/api/v1/hoks")

(defn create-app [session-store]
  (cache/clear-cache!)
  (common-api/create-app handler/app-routes session-store))

(defn mock-st-request
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

(defn mock-st-post [app full-url data]
  (mock-st-request app full-url :post data))

(defn- mock-st-put [app full-url data]
  (mock-st-request app full-url :put data))

(defn mock-st-get [app full-url]
  (mock-st-request app full-url))

(defn create-mock-post-request
  ([path body app hoks]
   (create-mock-post-request (format "%d/%s" (:id hoks) path) body app))
  ([path body app]
   (mock-st-post app (format "%s/%s" url path) body)))

(defn create-mock-hoks-put-request [hoks-id updated-data app]
  (mock-st-put app (format "%s/%d" url hoks-id) updated-data))

(defn create-mock-hoks-get-request [hoks-id app]
  (mock-st-get app (format "%s/%d" url hoks-id)))
