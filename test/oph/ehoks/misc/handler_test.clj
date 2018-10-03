(ns oph.ehoks.misc.handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.utils :refer [parse-body]]
            [oph.ehoks.handler :refer [app]]
            [ring.mock.request :as mock]))

(deftest test-environment
  (testing "GET environment info"
    (let [response (app
                     (mock/request
                       :get "/ehoks-backend/api/v1/misc/environment"))]
      (is (= (:status response) 200))
      (let [data (-> response :body parse-body :data)]
        (is (some? (:opintopolku-login-url data)))
        (is (some? (:opintopolku-logout-url data)))
        (is (some? (:eperusteet-peruste-url data)))))))
