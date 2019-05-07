(ns oph.ehoks.healthcheck.handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :refer [parse-body]]))

(deftest healthcheck
  (testing "GET healthcheck"
    (let [app (common-api/create-app handler/app-routes)
          response (app (mock/request :get "/ehoks-backend/api/v1/healthcheck"))
          body (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (= body {})))))
