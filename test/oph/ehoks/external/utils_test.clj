(ns oph.ehoks.external.utils-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.utils :as u]))

(deftest test-with-timeout
  (testing "With timeout"
    (is (= (u/with-timeout 100000 :success :timeout) :success))
    (is (= (u/with-timeout 1 (do (Thread/sleep 100000) :success) :timeout)
           :timeout))))
