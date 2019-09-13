(ns oph.ehoks.hoks.hoks-test-utils
  (:require [ring.mock.request :as mock]
            [oph.ehoks.virkailija.handler :as handler]
            [clojure.test :refer [is]]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.external.cache :as cache]
            [oph.ehoks.utils :as utils :refer [eq]]))

(def base-url "/ehoks-virkailija-backend/api/v1/hoks")

(defn create-app [session-store]
  (cache/clear-cache!)
  (common-api/create-app handler/app-routes session-store))

(defn get-authenticated [url]
  (-> (utils/with-service-ticket
        (create-app nil)
        (mock/request :get url))
      :body
      utils/parse-body))

(defn create-hoks [app]
  (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                   :oppija-oid "1.2.246.562.24.12312312312"
                   :ensikertainen-hyvaksyminen
                   (java.time.LocalDate/of 2019 3 18)
                   :osaamisen-hankkimisen-tarve false}]
    (-> app
        (utils/with-service-ticket
          (-> (mock/request :post base-url)
              (mock/json-body hoks-data)))
        :body
        utils/parse-body
        (get-in [:data :uri])
        get-authenticated
        :data)))

(defmacro with-hoks-and-app [[hoks app] & body]
  `(let [~app (create-app nil)
         ~hoks (create-hoks ~app)]
     (do ~@body)))

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

(defn mock-st-put [app full-url data]
  (mock-st-request app full-url :put data))

(defn mock-st-get [app full-url]
  (mock-st-request app full-url))

(defn mock-st-patch [app full-url data]
  (mock-st-request app full-url :patch data))

(defn get-hoks-url [hoks path]
  (format "%s/%d/%s" base-url (:id hoks) path))

(defn create-mock-hoks-osa-get-request [path app hoks]
  (mock-st-get app (get-hoks-url hoks (str path "/1"))))

(defn create-mock-post-request
  ([path body app hoks]
    (create-mock-post-request (format "%d/%s" (:id hoks) path) body app))
  ([path body app]
    (mock-st-post app (format "%s/%s" base-url path) body)))

(defn create-mock-hoks-put-request [hoks-id updated-data app]
  (mock-st-put app (format "%s/%d" base-url hoks-id) updated-data))

(defn create-mock-hoks-get-request [hoks-id app]
  (mock-st-get app (format "%s/%d" base-url hoks-id)))

(defn create-mock-hoks-patch-request [hoks-id patched-data app]
  (mock-st-patch app (format "%s/%d" base-url hoks-id) patched-data))

(defn create-mock-hoks-osa-patch-request [path app patched-data]
  (mock-st-patch app (format "%s/1/%s/1" base-url path) patched-data))

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

(defn assert-post-response-is-ok [post-path post-response]
  (is (= (:status post-response) 200))
  (eq (utils/parse-body (:body post-response))
      {:meta {:id 1}
       :data {:uri
              (format
                "%1s/1/%2s/1"
                base-url post-path)}}))

(defn test-post-and-get-of-aiemmin-hankittu-osa [osa-path osa-data]
  (with-hoks-and-app
    [hoks app]
    (let [post-response (create-mock-post-request
                          osa-path osa-data app hoks)
          get-response (create-mock-hoks-osa-get-request osa-path app hoks)]
      (assert-post-response-is-ok osa-path post-response)
      (is (= (:status get-response) 200))
      (eq (utils/parse-body
            (:body get-response))
          {:meta {} :data (assoc osa-data :id 1)}))))

(defn compare-tarkentavat-tiedot-naytto-values
  [updated original selector-function]
  (let [ttn-after-update
        (selector-function (:tarkentavat-tiedot-naytto updated))
        ttn-patch-values
        (assoc (selector-function (:tarkentavat-tiedot-naytto original))
               :osa-alueet [] :tyoelama-osaamisen-arvioijat [])]
    (eq ttn-after-update ttn-patch-values)))
