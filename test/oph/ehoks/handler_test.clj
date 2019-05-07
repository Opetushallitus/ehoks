(ns oph.ehoks.handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :refer [with-authentication]]))

(deftest buildversion
  (testing "GET /buildversion.txt"
    (let [app (common-api/create-app handler/app-routes)
          response (app (mock/request
                          :get "/ehoks-backend/buildversion.txt"))
          body (slurp (:body response))]
      (is (= (:status response) 200))
      (is (re-find #"^artifactId=" body)))))

(deftest not-found
  (testing "GET route which does not exist"
    (let [app (common-api/create-app handler/app-routes)
          response (with-authentication
                     app
                     (mock/request
                       :get "/ehoks-backend/api/v1/non-existing-resource/"))]
      (is (= (:status response) 404)))))
