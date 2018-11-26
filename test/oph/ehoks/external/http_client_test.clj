(ns oph.ehoks.external.http-client-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.config :as config]))

(deftest test-set-get
  (testing "Prevent setting get"
    (config/reload-config! nil)
    (is (thrown-with-msg?
          Exception
          #"Mocking HTTP is not allowed"
          (client/set-get (fn [])))))

  (testing "Setting mock get"
    (config/reload-config! "config/test.edn")
    (client/set-get (fn [] "Test"))
    (is (= "Test"(client/get)))))
