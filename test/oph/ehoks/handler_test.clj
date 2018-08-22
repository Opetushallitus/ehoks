(ns oph.ehoks.handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.handler :refer [app]]
            [ring.mock.request :as mock]))

(deftest not-found
  (testing "GET route which does not exists"
    (let [response (app (mock/request :get "/api/v1/non-existing-resource/"))]
      (is (= (:status response) 404)))))
