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
             :get "/ehoks-backend/api/v1/session/opintopolku/")
           (mock/header "FirstName" "Teuvo Testi")
           (mock/header "cn" "Teuvo")
           (mock/header "givenname" "Teuvo")
           (mock/header "hetu" "190384-9245")
           (mock/header "sn" "Testaaja"))))

(deftest session-without-authentication
  (testing "GET current session without authentication"
    (let [response (app (mock/request
                          :get
                          "/ehoks-backend/api/v1/session/"))]
      (is (= (:status response) 401))
      (is (empty? (:body response))))))

(deftest session-authenticate
  (testing "POST authenticate"
    (let [response (authenticate)]
      (is (= (:status response) 303)))))

(deftest prevent-malformed-authentication
  (testing "Prevents malformed authentication"
    (let [response (app (-> (mock/request
                              :get "/ehoks-backend/api/v1/session/opintopolku/"
                              {"FirstName" "Teuvo Testi"
                               "cn" "Teuvo"
                               "hetu" "190384-9245"
                               "sn" "Testaaja"})
                            (mock/header "FirstName" "Teuvo Testi")
                            (mock/header "cn" "Teuvo")
                            (mock/header "givenname" "Teuvo")
                            (mock/header "sn" "Testaaja")))]
      (is (= (:status response) 400)))))

(deftest session-authenticated
  (testing "GET current authenticated session"
    (let [auth-response (authenticate)
          session-cookie (first (get-in auth-response [:headers "Set-Cookie"]))
          response (app (-> (mock/request
                              :get
                              "/ehoks-backend/api/v1/session/")
                            (mock/header :cookie session-cookie)))
          body (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (= (:data body) [{:first-name "Teuvo Testi"
                            :common-name "Teuvo"
                            :surname "Testaaja"}])))))

(deftest session-delete-authenticated
  (testing "DELETE authenticated session"
    (let [auth-response (authenticate)
          session-cookie (first (get-in auth-response [:headers "Set-Cookie"]))
          authenticated-response
          (app (-> (mock/request
                     :get
                     "/ehoks-backend/api/v1/session/")
                   (mock/header :cookie session-cookie)))
          authenticated-body (parse-body (:body authenticated-response))
          delete-response
          (app (-> (mock/request
                     :delete
                     "/ehoks-backend/api/v1/session/")
                   (mock/header :cookie session-cookie)))
          response (app (-> (mock/request
                              :get
                              "/ehoks-backend/api/v1/session/")
                            (mock/header :cookie session-cookie)))]
      (is (= (:status authenticated-response) 200))
      (is (= (:data authenticated-body)
             [{:first-name "Teuvo Testi"
               :common-name "Teuvo"
               :surname "Testaaja"}]))
      (is (= (:status response) 401))
      (is (= (:status delete-response) 200))
      (is (= (:status delete-response) 200)))))
