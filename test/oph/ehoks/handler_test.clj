(ns oph.ehoks.handler-test
  (:require [cheshire.core :as cheshire]
            [clojure.test :refer [deftest testing is]]
            [oph.ehoks.handler :refer [app]]
            [ring.mock.request :as mock]))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(deftest education-info
  (testing "GET education info"
    (let [response (app (mock/request :get "/api/v1/education/info/"))
          body (parse-body (:body response))
          info (first (:data body))]
      (is (= (:status response) 200))
      (is (= (count (:data body)) 1))
      (is (some? (get-in info [:basic-information :fi])))
      (is (some? (get-in info [:hoks-process :fi]))))))

(deftest work-info
  (testing "GET work info"
    (let [response (app (mock/request :get "/api/v1/work/info/"))
          body (parse-body (:body response))
          info (first (:data body))]
      (is (= (:status response) 200))
      (is (= (count (:data body)) 1))
      (is (some? (get-in info [:basic-information :fi])))
      (is (some? (get-in info [:hoks-process :fi]))))))

(deftest student-info
  (testing "GET student info"
    (let [response (app (mock/request :get "/api/v1/student/info/"))
          body (parse-body (:body response))
          info (first (:data body))]
      (is (= (:status response) 200))
      (is (= (count (:data body)) 1))
      (is (some? (get-in info [:basic-information :fi])))
      (is (some? (get-in info [:hoks-process :fi]))))))
