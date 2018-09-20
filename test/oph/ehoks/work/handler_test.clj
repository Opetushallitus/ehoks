(ns oph.ehoks.work.handler-test
  (:require [cheshire.core :as cheshire]
            [clojure.test :refer [deftest testing is]]
            [oph.ehoks.handler :refer [app]]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :refer [parse-body]]))

(deftest work-info
  (testing "GET work info"
    (let [response (app (mock/request :get "/ehoks/api/v1/work/info/"))
          body (parse-body (:body response))
          info (first (:data body))]
      (is (= (:status response) 200))
      (is (= (count (:data body)) 1))
      (is (some? (get-in info [:basic-information :fi])))
      (is (some? (get-in info [:hoks-process :fi]))))))
