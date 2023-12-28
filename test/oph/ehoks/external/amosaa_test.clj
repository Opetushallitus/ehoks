(ns oph.ehoks.external.amosaa-test
  (:require [oph.ehoks.external.amosaa :as sut]
            [clojure.test :as t]
            [oph.ehoks.external.http-client :as client]))

(defn get-tutkinnon-osa-response [^String url __]
  {:status 200
   :body (if (.endsWith
               url
               "/paikallinen_tutkinnonosa_1.2.246.562.10.11111111111_1234")
           [{:id 10642
             :nimi {:fi "Testi"}}]
           [])})

(t/deftest test-get-tutkinnon-osa-by-koodi
  (t/testing "Get tutkinnon osa by koodi"
    (client/with-mock-responses
      [get-tutkinnon-osa-response]
      (t/is (= (sut/get-tutkinnon-osa-by-koodi
                 "1.2.246.562.10.11111111111_1234")
               [{:id 10642
                 :nimi {:fi "Testi"}}])))))

(t/deftest test-get-tutkinnon-osa-by-koodi-not-found
  (t/testing "Get tutkinnon osa by koodi not found"
    (client/with-mock-responses
      [get-tutkinnon-osa-response]
      (t/is (= (sut/get-tutkinnon-osa-by-koodi "404")
               [])))))
