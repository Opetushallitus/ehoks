(ns oph.ehoks.misc.handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.utils :refer [parse-body]]
            [oph.ehoks.oppija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]))

(deftest test-environment
  (testing "GET environment info"
    (let [app (common-api/create-app handler/app-routes nil)
          response (app
                     (mock/request
                       :get "/ehoks-oppija-backend/api/v1/misc/environment"))]
      (is (= (:status response) 200))
      (let [data (-> response :body parse-body :data)]
        (is (some? (:opintopolku-login-url-fi data)))
        (is (some? (:opintopolku-login-url-sv data)))
        (is (some? (:opintopolku-logout-url-fi data)))
        (is (some? (:opintopolku-logout-url-sv data)))
        (is (some? (:eperusteet-peruste-url data)))
        (is (some? (:virkailija-login-url data)))))))
