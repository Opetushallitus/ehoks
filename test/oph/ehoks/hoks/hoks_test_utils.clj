(ns oph.ehoks.hoks.hoks-test-utils
  (:require [ring.mock.request :as mock]
            [oph.ehoks.virkailija.handler :as handler]
            [clojure.test :refer [is]]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.external.cache :as cache]
            [oph.ehoks.utils :as utils :refer [eq]]))

;TODO laita privateiksi sopivat
;TODO poista handler-testista duplikaatti url
(def base-url "/ehoks-virkailija-backend/api/v1/hoks")

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
   (mock-st-post app (format "%s/%s" base-url path) body)))

(defn create-mock-hoks-put-request [hoks-id updated-data app]
  (mock-st-put app (format "%s/%d" base-url hoks-id) updated-data))

(defn create-mock-hoks-get-request [hoks-id app]
  (mock-st-get app (format "%s/%d" base-url hoks-id)))

(defn assert-partial-put-of-hoks [updated-hoks hoks-part initial-hoks-data]
  (let [app (create-app nil)
        post-response (create-mock-post-request "" initial-hoks-data app)
        put-response (create-mock-hoks-put-request 1 updated-hoks app)
        get-response (create-mock-hoks-get-request 1 app)
        get-response-data (:data (utils/parse-body (:body get-response)))]
    (is (= (:status post-response) 200))
    (is (= (:status put-response) 204))
    (is (= (:status get-response) 200))
    (eq (hoks-part get-response-data)
        (hoks-part updated-hoks))))
