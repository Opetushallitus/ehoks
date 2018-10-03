(ns oph.ehoks.external.connection-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.connection :as c]
            [oph.ehoks.config :refer [config]]
            [clj-time.core :as t]))

(deftest test-refresh-service-ticket
  (testing "Refresh service ticket successfully"
    (reset! c/service-ticket {:url nil :expires nil})
    (is (= (deref c/service-ticket) {:url nil :expires nil}))
    (with-redefs [clj-http.client/post
                  (fn [_ options]
                    (is (= (get-in options [:form-params :username])
                           (:cas-username config)))
                    (is (= (get-in options [:form-params :password])
                           (:cas-password config)))
                    {:status 201
                     :headers {"location" "test-url"}})]
      (c/refresh-service-ticket!)
      (is (= (:url (deref c/service-ticket)) "test-url"))
      (is (some? (:expires (deref c/service-ticket))))))

  (testing "Refresh service ticket unsuccessfully"
    (reset! c/service-ticket {:url nil :expires nil})
    (with-redefs [clj-http.client/post
                  (fn [_ options]
                    {:status 404})]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Failed to refresh CAS Service Ticket"
                            (c/refresh-service-ticket!))))))

(deftest test-get-service-ticket
  (testing "Get service ticket"
    (with-redefs [clj-http.client/post
                  (fn [_ options]
                    (is (= (get-in options [:form-params :service])
                           "http://test-service/j_spring_cas_security_check"))
                    {:body "test-ticket"})]
      (is (= (c/get-service-ticket "url" "http://test-service")
             "test-ticket")))))

(deftest test-add-cas-ticket
  (testing "Add service ticket"
    (with-redefs [clj-http.client/post (fn [_ options] {:body "test-ticket"})]
      (reset! c/service-ticket {:url "http://ticket.url"
                                :expires (t/plus (t/now) (t/hours 2))})
      (let [data (c/add-cas-ticket "http://test-service" {})]
        (is (= (get-in data [:headers "clientSubSystemCode"])
               (:client-sub-system-code config)))
        (is (= (get-in data [:headers "accept"]) "*/*"))
        (is (= (get-in data [:query-params :ticket]) "test-ticket"))))))

(deftest test-api-get
  (testing "API GET"
    (with-redefs [clj-http.client/post (fn [_ __] {:body "test-ticket"})
                  clj-http.client/get (fn [_ __] {:body "{\"value\": true}"
                                                  :status 200})]
      (reset! c/service-ticket {:url "http://ticket.url"
                                :expires (t/plus (t/now) (t/hours 2))})
      (let [response (c/api-get "http://test-service" "/" {})]
        (is (= (:body response) {:value true}))))))
