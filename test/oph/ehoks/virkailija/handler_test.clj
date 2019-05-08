(ns oph.ehoks.virkailija.handler-test
  (:require [oph.ehoks.virkailija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [clojure.test :as t]))

(t/deftest buildversion
  (t/testing "GET /buildversion.txt"
    (let [app (common-api/create-app handler/app-routes)
          response (app (mock/request
                          :get "/ehoks-backend/buildversion.txt"))
          body (slurp (:body response))]
      (t/is (= (:status response) 200))
      (t/is (re-find #"^artifactId=" body)))))

(t/deftest not-found
  (t/testing "GET route which does not exist"
    (let [app (common-api/create-app handler/app-routes)
          response (app
                     (mock/request
                       :get "/ehoks-virkailija-backend/api/v1/non-existing-resource/"))]
      (t/is (= (:status response) 404)))))

(deftest healthcheck
  (testing "GET healthcheck"
    (let [app (common-api/create-app handler/app-routes)
          response (app
                     (mock/request
                       :get "/ehoks-virkailija-backend/api/v1/healthcheck"))
          body (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (= body {})))))
