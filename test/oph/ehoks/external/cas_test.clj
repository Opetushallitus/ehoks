(ns oph.ehoks.external.cas-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.cas :as c]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.http-client :as client]
            [clj-time.core :as t]
            [clojure.data.xml :as xml]))

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

(deftest test-refresh-service-ticket
  (testing "Refresh service ticket successfully"
    (reset! c/service-ticket {:url nil :expires nil})
    (is (= (deref c/service-ticket) {:url nil :expires nil}))
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
    (client/set-post! (fn [_ options]
                        {:status 404}))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Failed to refresh CAS Service Ticket"
                          (c/refresh-service-ticket!)))))

(deftest test-get-service-ticket
  (testing "Get service ticket"
    (client/set-post!
      (fn [_ options]
        (is (= (get-in options [:form-params :service])
               "http://test-service/j_spring_cas_security_check"))
        {:body "test-ticket"}))
    (is (= (c/get-service-ticket "http://url" "http://test-service")
           "test-ticket"))))

(deftest test-add-cas-ticket
  (testing "Add service ticket"
    (client/set-post! (fn [_ options] {:body "test-ticket"}))

    (reset! c/service-ticket {:url "http://ticket.url"
                              :expires (t/plus (t/now) (t/hours 2))})
    (let [data (c/add-cas-ticket {} "http://test-service")]
      (is (= (get-in data [:headers "accept"]) "*/*"))
      (is (= (get-in data [:query-params :ticket]) "test-ticket")))))

(deftest test-with-service-ticket
  (testing "Request with API headers"
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

(def xml-map '{:v ({:k ("something")} {:o ("other")})})

(deftest test-xml->map
  (testing "Conversion of XML response to map"
    (is (= (c/xml->map
             (xml/sexp-as-element
               [:v
                [:k "something"]
                [:o "other"]]))
           xml-map))))

(deftest test-find-value
  (testing "Finding value in XML map"
    (is (= (first (c/find-value xml-map [:v :o]))
           "other"))
    (is (= (first (c/find-value xml-map [:v :k]))
           "something"))))
