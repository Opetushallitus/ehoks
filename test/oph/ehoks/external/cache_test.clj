(ns oph.ehoks.external.cache-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.cache :as c]
            [oph.ehoks.config :refer [config]])
  (:import (java.time Instant)))

(def example-responses
  {"https://some.url/"
   {:status 200
    :body {}
    :timestamp (Instant/now)}
   "https://someother.url/"
   {:status 200
    :body {}
    :timestamp
    (.minusSeconds
      (Instant/now)
      (* 60 (inc (:ext-cache-lifetime-minutes config))))}})

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
    (is (not (c/expired? {:timestamp (Instant/now)})))
    (is (c/expired?
          {:timestamp
           (.minusSeconds
             (Instant/now)
             (* 60 (inc (:ext-cache-lifetime-minutes config))))}))))

(deftest test-get-expire-response
  (testing "Expiring cached response"
    (reset! c/cache example-responses)
    (is (= (c/get-cached "https://some.url/")
           (get example-responses "https://some.url/")))
    (c/expire-response! "https://some.url/")
    (is (nil? (c/get-cached "https://some.url/")))))

(deftest test-clean-cache
  (testing "Clean cache"
    (reset! c/cleaning? false)
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
