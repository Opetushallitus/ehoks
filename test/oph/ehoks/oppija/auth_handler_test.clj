(ns oph.ehoks.oppija.auth-handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.oppija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :refer [parse-body]]
            [oph.ehoks.external.http-client :as client]))

(def base-url "/ehoks-oppija-backend/api/v1/oppija/session")

(defn authenticate [app]
  (client/with-mock-responses
    [(fn [url options]
       (cond
         (.endsWith url "/serviceValidate")
         {:status 200
          :body
          (str "<cas:serviceResponse"
               "  xmlns:cas='http://www.yale.edu/tp/cas'>"
               "<cas:authenticationSuccess><cas:user>ehoks</cas:user>"
               "<cas:attributes>"
               "<cas:longTermAuthenticationRequestTokenUsed>false"
               "</cas:longTermAuthenticationRequestTokenUsed>"
               "<cas:isFromNewLogin>false</cas:isFromNewLogin>"
               "<cas:authenticationDate>2019-02-20T10:14:24.046+02:00"
               "</cas:authenticationDate></cas:attributes>"
               "</cas:authenticationSuccess></cas:serviceResponse>")}
         (.contains url "oppijanumerorekisteri-service")
         {:status 200
          :body {:results
                 [{:oidHenkilo "1.2.246.562.24.44651722625"
                   :hetu "250103-5360"
                   :etunimet "Aarto Maurits"
                   :kutsumanimi "Aarto"
                   :sukunimi "Väisänen-perftest"}]}}))
     (fn [url options]
       (cond
         (.endsWith url "/v1/tickets")
         {:status 201
          :headers {"location" "http://test.ticket/1234"}}
         (= url "http://test.ticket/1234")
         {:status 200
          :body "ST-1234-testi"}))]
    (app (-> (mock/request :get (str base-url "/opintopolku/"))
             (mock/header "FirstName" "Teuvo Testi")
             (mock/header "cn" "Teuvo")
             (mock/header "givenname" "Teuvo")
             (mock/header "hetu" "190384-9245")
             (mock/header "sn" "Testaaja")))))

(deftest session-without-authentication
  (testing "GET current session without authentication"
    (let [app (common-api/create-app handler/app-routes nil)
          response (app (mock/request
                          :get
                          base-url))]
      (is (= (:status response) 401))
      (is (empty? (:body response))))))

(deftest session-authenticate
  (testing "POST authenticate"
    (let [response (authenticate
                     (common-api/create-app handler/app-routes nil))]
      (is (= (:status response) 303)))))

(deftest prevent-malformed-authentication
  (testing "Prevents malformed authentication"
    (let [app (common-api/create-app handler/app-routes nil)
          response
          (app (-> (mock/request
                     :get (str base-url "/opintopolku/")
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
    (let [app (common-api/create-app handler/app-routes nil)
          auth-response (authenticate app)
          session-cookie (first (get-in auth-response [:headers "Set-Cookie"]))
          response (app (-> (mock/request
                              :get
                              base-url)
                            (mock/header :cookie session-cookie)))
          body (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (= (:data body) [{:oid "1.2.246.562.24.44651722625"
                            :first-name "Teuvo Testi"
                            :common-name "Teuvo"
                            :surname "Testaaja"}])))))

(deftest session-delete-authenticated
  (testing "DELETE authenticated session"
    (let [app (common-api/create-app handler/app-routes nil)
          auth-response (authenticate app)
          session-cookie (first (get-in auth-response [:headers "Set-Cookie"]))
          authenticated-response
          (app (-> (mock/request
                     :get
                     base-url)
                   (mock/header :cookie session-cookie)))
          authenticated-body (parse-body (:body authenticated-response))
          delete-response
          (app (-> (mock/request
                     :delete
                     base-url)
                   (mock/header :cookie session-cookie)))
          response (app (-> (mock/request
                              :get
                              base-url)
                            (mock/header :cookie session-cookie)))]
      (is (= (:status authenticated-response) 200))
      (is (= (:data authenticated-body)
             [{:oid "1.2.246.562.24.44651722625"
               :first-name "Teuvo Testi"
               :common-name "Teuvo"
               :surname "Testaaja"}]))
      (is (= (:status response) 401))
      (is (= (:status delete-response) 200))
      (is (= (:status delete-response) 200)))))