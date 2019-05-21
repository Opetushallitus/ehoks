(ns oph.ehoks.virkailija.handler-test
  (:require [oph.ehoks.virkailija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [clojure.test :as t]
            [oph.ehoks.utils :refer [parse-body]]
            [oph.ehoks.session-store :refer [test-session-store]]))

(def base-url "/ehoks-virkailija-backend/api/v1")

(t/deftest buildversion
  (t/testing "GET /buildversion.txt"
    (let [app (common-api/create-app handler/app-routes)
          response (app (mock/request
                          :get "/ehoks-virkailija-backend/buildversion.txt"))
          body (slurp (:body response))]
      (t/is (= (:status response) 200))
      (t/is (re-find #"^artifactId=" body)))))

(t/deftest not-found
  (t/testing "GET route which does not exist"
    (let [app (common-api/create-app handler/app-routes)
          response (app
                     (mock/request
                       :get (str base-url "/non-existing-resource/")))]
      (t/is (= (:status response) 404)))))

(t/deftest healthcheck
  (t/testing "GET healthcheck"
    (let [app (common-api/create-app handler/app-routes)
          response (app
                     (mock/request
                       :get (str base-url "/healthcheck")))
          body (parse-body (:body response))]
      (t/is (= (:status response) 200))
      (t/is (= body {})))))

(t/deftest test-environment
  (t/testing "GET environment info"
    (let [app (common-api/create-app handler/app-routes nil)
          response (app
                     (mock/request
                       :get (str base-url "/misc/environment")))]
      (t/is (= (:status response) 200))
      (let [data (-> response :body parse-body :data)]
        (t/is (some? (:opintopolku-login-url data)))
        (t/is (some? (:opintopolku-logout-url data)))
        (t/is (some? (:eperusteet-peruste-url data)))
        (t/is (some? (:virkailija-login-url data)))))))

(t/deftest test-list-oppijat
  (t/testing "GET oppijat"
    (let [session "12345678-1234-1234-1234-1234567890ab"
          cookie (str "ring-session=" session)
          store (atom {session {:virkailija-user {:name "Test"}}})
          app (common-api/create-app
                handler/app-routes (test-session-store store))
          response (app (mock/header
                          (mock/request :get (str base-url "/virkailija/oppijat"))
                          :cookie cookie))]
      (t/is (= (:status response)

               200)))))