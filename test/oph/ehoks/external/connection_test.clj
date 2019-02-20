(ns oph.ehoks.external.connection-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.connection :as c]))

(deftest test-sanitaze-params
  (testing "Sanitazing params"
    (is (= (c/sanitaze-params {:query-params {:user-id "12345.12345"
                                              :category "user"}})
           {:query-params {:user-id "*FILTERED*"
                           :category "user"}}))))

(deftest test-sanitaze-path
  (testing "Sanitizing path"
    (is (= (c/sanitaze-path "/hello/1.2.345.678.90.12345678901/")
           "/hello/*FILTERED*/"))
    (is (= (c/sanitaze-path "/hello/1.2.345.678.90.12345678901")
           "/hello/*FILTERED*"))))
