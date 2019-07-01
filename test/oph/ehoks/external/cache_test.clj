(ns oph.ehoks.external.cache-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.cache :as c]
            [oph.ehoks.config :refer [config]]
            [clj-time.core :as t]
            [clojure.data.json :as json]))

(def example-responses
  {"https://some.url/"
   {:status 200
    :body {}
    :timestamp (t/now)}
   "https://someother.url/"
   {:status 200
    :body {}
    :timestamp
    (t/minus
      (t/now)
      (t/minutes
        (inc (:ext-cache-lifetime-minutes config))))}})

; TODO Add test for url with and without params

(deftest test-get-cached
  (testing "Cache"
    (reset! c/cache example-responses)
    (is (= (c/get-cached "https://some.url/")
           (get example-responses "https://some.url/")))
    (is (nil? (c/get-cached "https://someother.url/")))))

(deftest test-add-cached-response
  (testing "Add cached response"
    (c/add-cached-response!
      "https://some.cached.url/ping?pong=true"
      {:status 200
       :body {}})
    (let [response (c/get-cached "https://some.cached.url/ping?pong=true")]
      (is (= (:status response) 200))
      (is (= (:body response) {}))
      (is (some? (:timestamp response)))
      (is (:ehoks-cached response))
      (is (= (:cached response) :HIT)))))

(deftest test-expired
  (testing "Expired"
    (is (not (c/expired? {})))
    (is (not (c/expired? {:timestamp (t/now)})))
    (is (c/expired?
          {:timestamp
           (t/minus
             (t/now)
             (t/minutes
               (inc (:ext-cache-lifetime-minutes config))))}))))

(deftest test-get-expire-response
  (testing "Expiring cached response"
    (reset! c/cache example-responses)
    (is (= (c/get-cached "https://some.url/")
           (get example-responses "https://some.url/")))
    (c/expire-response! "https://some.url/")
    (is (nil? (c/get-cached "https://some.url/")))))

(deftest test-clean-cache
  (testing "Clean cache"
    (reset! c/cache example-responses)
    (c/clean-cache!)
    (is (= @c/cache
           (dissoc example-responses "https://someother.url/")))))

(deftest test-encode-url
  (testing "Encoding URL"
    (is (= (c/encode-url "http://example.com" {})
           "http://example.com"))
    (is (= (c/encode-url "http://example.com"
                         {:param1 "Param1" :param2 "Param2"})
           "http://example.com?param1=Param1&param2=Param2"))))

(def stubbed-with-api-headers (constantly {:status 200
                                           :body {:test "testing"}}))

(deftest cached-url-is-identified-by-query-params
  (testing "If request has query params, those are appended to cached url"
    (with-redefs [oph.ehoks.external.connection/with-api-headers
                  stubbed-with-api-headers]
      (let [oids ["100" "200" "300"]
            stored-to-cache-response (c/with-cache!
                                       {:method :post
                                        :service "https://some.url/test"
                                        :url "https://some.url/"
                                        :options {:as :json
                                                  :body (json/write-str oids)
                                                  :query-params {:oids oids}
                                                  :content-type :json}})
            fetched-from-cache (c/get-cached-with-params
                                       "https://some.url/" {:oids oids})]
        (is (= (:cached stored-to-cache-response) :MISS))
        (is (= (:cached fetched-from-cache) :HIT))))))
