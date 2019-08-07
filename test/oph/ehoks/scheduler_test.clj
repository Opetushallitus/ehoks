(ns oph.ehoks.scheduler-test
  (:require [oph.ehoks.scheduler :as sut]
            [clojure.test :as t]
            [clojure.core.async :as a]))

(t/deftest run-and-stop-job
  (t/testing "Running scheduled job"
    (sut/add-job "test" 10 #(str "Hello " %1) "testing")
    (t/is (= (a/<!! (sut/get-job "test")) "Hello testing"))
    (t/is (sut/remove-job "test"))))

(t/deftest job-exception-handling
  (t/testing "Scheduled job exception handling"
    (sut/add-job "test" 10 #(throw (Exception. "Test exception")))
    (t/is (= (.getMessage (a/<!! (sut/get-job "test"))) "Test exception"))
    (t/is (sut/remove-job "test"))))