(ns oph.ehoks.external.http-client-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.utils :refer [reload-config!]]))

(deftest test-set-get
  (testing "Prevent setting get"
    (is (thrown-with-msg?
          Exception
          #"Mocking HTTP is not allowed"
          (client/set-get! (fn [_ __])))))

  (testing "Setting mock get"
    (reload-config! "config/test.edn")
    (client/set-get! (fn [_ __] "Test"))
    (is (= "Test"(client/get "" {})))))

(deftest test-set-get
  (testing "Prevent setting post"
    (is (thrown-with-msg?
          Exception
          #"Mocking HTTP is not allowed"
          (client/set-post! (fn [_ __])))))

  (testing "Setting mock get"
    (reload-config! "config/test.edn")
    (client/set-post! (fn [_ __] "Test"))
    (is (= "Test"(client/post "" {})))))
