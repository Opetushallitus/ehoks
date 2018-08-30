(ns oph.ehoks.auth.handler-test
  (:require [cheshire.core :as cheshire]
            [clojure.test :refer [deftest testing is]]
            [oph.ehoks.handler :refer [app]]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :refer [parse-body]]
            [oph.ehoks.config :refer [config]]
            [cheshire.core :as cheshire]))

(defn authenticate []
  (app (-> (mock/request
             :post "/api/v1/session/opintopolku/"
             {"FirstName" "Teuvo Taavetti"
              "cn" "Teuvo"
              "givenName" "Teuvo"
              "hetu" "010203-XXXX"
              "sn" "Testaaja"})
           (mock/header "referer" (:opintopolku-login-url config)))))

(deftest session-without-authentication
  (testing "GET current session without authentication"
    (let [response (app (mock/request :get "/api/v1/session/opintopolku/"))
          body (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (empty? (:data body)))
      (is (= (get-in body [:meta :opintopolku-login-url])
             (:opintopolku-login-url config))))))

(deftest session-authenticate
  (testing "POST authenticate"
    (let [response (authenticate)]
      (is (= (:status response) 303)))))

(deftest prevent-illegal-authentication
  (testing "Prevents illegal authentication"
    (let [response (app (-> (mock/request
                              :post "/api/v1/session/opintopolku/"
                              {"FirstName" "Teuvo Taavetti"
                               "cn" "Teuvo"
                               "givenName" "Teuvo"
                               "hetu" "010203-XXXX"
                               "sn" "Testaaja"})))]
      (is (= (:status response) 400)))))

(deftest session-authenticated
  (testing "GET current authenticated session"
    (let [auth-response (authenticate)
          session-cookie (first (get-in auth-response [:headers "Set-Cookie"]))
          response (app (-> (mock/request :get "/api/v1/session/opintopolku/")
                            (mock/header :cookie session-cookie)))
          body (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (= (:data body) [{:first-name "Teuvo Taavetti"
                            :common-name "Teuvo"
                            :surname "Testaaja"}])))))

(deftest session-delete-unauthenticated
  (testing "DELETE unauthenticated session"
    (let [response (app (mock/request :delete "/api/v1/session/opintopolku/"))
          body (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (empty? (:data body))))))
0
(deftest session-delete-authenticated
  (testing "DELETE authenticated session"
    (let [auth-response (authenticate)
          session-cookie (first (get-in auth-response [:headers "Set-Cookie"]))
          authenticated-response
          (app (-> (mock/request :get "/api/v1/session/opintopolku/")
                   (mock/header :cookie session-cookie)))
          authenticated-body (parse-body (:body authenticated-response))
          delete-response
          (app (-> (mock/request :delete "/api/v1/session/opintopolku/")
                   (mock/header :cookie session-cookie)))
          response (app (-> (mock/request :get "/api/v1/session/opintopolku/")
                            (mock/header :cookie session-cookie)))
          body (parse-body (:body response))]
      (is (= (:status authenticated-response) 200))
      (is (= (:data authenticated-body)
             [{:first-name "Teuvo Taavetti"
               :common-name "Teuvo"
               :surname "Testaaja"}]))
      (is (= (:status response) 200))
      (is (= (:status delete-response) 200))
      (is (empty? (:data body))))))
