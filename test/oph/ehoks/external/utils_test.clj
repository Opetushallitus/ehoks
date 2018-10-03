(ns oph.ehoks.external.utils-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.utils :as u]
            [clojure.core.async :as a]))

(deftest test-with-timeout
  (testing "With timeout"
    (is (= (a/<!! (u/with-timeout 100000 :success :timeout)) :success))
    (is (= (a/<!! (u/with-timeout 1
                    (do (a/<! (a/timeout 1000000)) :success) :timeout))
           :timeout))))
