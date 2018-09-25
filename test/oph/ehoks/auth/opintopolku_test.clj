(ns oph.ehoks.auth.opintopolku-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.edn :as edn]
            [oph.ehoks.auth.opintopolku :as o]
            [clojure.java.io :as io])
  (:import [java.nio.charset StandardCharsets]))

(deftest convert-test
  (testing "Converting string encoding"
    (let [str-utf-8 "Example string"]
      (is (= (-> str-utf-8
                 (o/convert StandardCharsets/UTF_8 StandardCharsets/ISO_8859_1)
                 (o/convert StandardCharsets/ISO_8859_1 StandardCharsets/UTF_8))
             str-utf-8)))))

(deftest convert-to-utf-8-test
  (testing "Converting ISO-8859-1 string to UTF-8"
    (is (= (o/convert-to-utf-8 "Example string") "Example string"))))

(deftest parse-test
  (testing "Parsing authentication headers"
    (let [auth-header {"FirstName" "Teuvo Taavetti"
                       "cn" "Teuvo"
                       "givenName" "Teuvo"
                       "hetu" "010203-XXXX"
                       "sn" "Testaaja"}]
      (is (= (o/parse auth-header)
             {:first-name "Teuvo Taavetti"
              :common-name "Teuvo"
              :given-name "Teuvo"
              :hetu "010203-XXXX"
              :surname "Testaaja"})))))
