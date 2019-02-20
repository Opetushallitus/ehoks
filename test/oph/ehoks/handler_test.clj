(ns oph.ehoks.handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.handler :refer [app]]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :refer [with-authentication]]))

(deftest buildversion
  (testing "GET /buildversion.txt"
    (let [response (app (mock/request
                          :get "/ehoks-backend/buildversion.txt"))
          body (slurp (:body response))]
      (is (= (:status response) 200))
      (is (re-find #"^artifactId=" body)))))

(deftest not-found
  (testing "GET route which does not exist"
    (let [response (with-authentication
                     app
                     (mock/request
                       :get "/ehoks-backend/api/v1/non-existing-resource/"))]
      (is (= (:status response) 404)))))
