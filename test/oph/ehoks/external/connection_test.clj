(ns oph.ehoks.external.connection-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.connection :as c]
            [oph.ehoks.config :refer [config] :as conf]
            [oph.ehoks.external.http-client :as client]
            [clj-time.core :as t]))

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

(deftest test-sanitaze-params
  (testing "Sanitazing params"
    (is (= (c/sanitaze-params {:query-params {:user-id "12345.12345"
                                              :category "user"}})
           {:query-params {:user-id "*FILTERED*"
                           :category "user"}}))))

(deftest test-sanitaze-path
  (testing "Sanitizing path"
    (is (= (c/sanitaze-path "/hello/1.2.345.678.90.12345678901/")
           "/hello/*FILTERED*/"))
    (is (= (c/sanitaze-path "/hello/1.2.345.678.90.12345678901")
           "/hello/*FILTERED*"))))

(deftest test-encode-url
  (testing "Encoding URL"
    (is (= (c/encode-url "http://example.com"
                         {})
           "http://example.com"))
    (is (= (c/encode-url "http://example.com"
                         {:param1 "Param1" :param2 "Param2"})
           "http://example.com?param1=Param1&param2=Param2"))))

(deftest test-refresh-service-ticket
  (testing "Refresh service ticket successfully"
    (reset! c/service-ticket {:url nil :expires nil})
    (is (= (deref c/service-ticket) {:url nil :expires nil}))
    (conf/reload-config! "config/test.edn")
    (client/set-post!
      (fn [_ options]
        (is (= (get-in options [:form-params :username])
               (:cas-username config)))
        (is (= (get-in options [:form-params :password])
               (:cas-password config)))
        {:status 201
         :headers {"location" "test-url"}}))
    (c/refresh-service-ticket!)
    (is (= (:url (deref c/service-ticket)) "test-url"))
    (is (some? (:expires (deref c/service-ticket)))))

  (testing "Refresh service ticket unsuccessfully"
    (reset! c/service-ticket {:url nil :expires nil})
    (conf/reload-config! "config/test.edn")
    (client/set-post! (fn [_ options]
                        {:status 404}))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Failed to refresh CAS Service Ticket"
                          (c/refresh-service-ticket!)))))

(deftest test-get-service-ticket
  (testing "Get service ticket"
    (conf/reload-config! "config/test.edn")
    (client/set-post!
      (fn [_ options]
        (is (= (get-in options [:form-params :service])
               "http://test-service/j_spring_cas_security_check"))
        {:body "test-ticket"}))
    (is (= (c/get-service-ticket "http://url" "http://test-service")
           "test-ticket"))))

(deftest test-add-cas-ticket
  (testing "Add service ticket"
    (conf/reload-config! "config/test.edn")
    (client/set-post! (fn [_ options] {:body "test-ticket"}))

    (reset! c/service-ticket {:url "http://ticket.url"
                                :expires (t/plus (t/now) (t/hours 2))})
      (let [data (c/add-cas-ticket {} "http://test-service")]
        (is (= (get-in data [:headers "accept"]) "*/*"))
        (is (= (get-in data [:query-params :ticket]) "test-ticket")))))

(deftest test-with-service-ticket
  (testing "Request with API headers"
    (conf/reload-config! "config/test.edn")
    (client/set-get! (fn [_ __] {:body {:value true}
                                 :status 200}))
    (client/set-post! (fn [_ __] {:body "test-ticket"}))
    (reset! c/service-ticket {:url "http://ticket.url"
                              :expires (t/plus (t/now) (t/hours 2))})
    (let [response (c/with-service-ticket
                     {:method :get
                      :service "http://test-service"
                      :path "/"
                      :options {}})]
      (is (= (:body response) {:value true})))))
