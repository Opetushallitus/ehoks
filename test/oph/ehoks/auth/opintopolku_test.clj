(ns oph.ehoks.auth.opintopolku-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.edn :as edn]
            [oph.ehoks.auth.opintopolku :refer [convert convert-to-utf-8 parse]]
            [clojure.java.io :as io])
  (:import [java.nio.charset StandardCharsets]))

(deftest convert-test
  (testing "Converting string encoding"
    (let [str-utf-8 "Example string"]
      (is (= (-> str-utf-8
                 (convert StandardCharsets/UTF_8 StandardCharsets/ISO_8859_1)
                 (convert StandardCharsets/ISO_8859_1 StandardCharsets/UTF_8))
             str-utf-8)))))

(deftest convert-to-utf-8-test
  (testing "Converting ISO-8859-1 string to UTF-8"
    (is (= (convert-to-utf-8 "Example string") "Example string"))))

(deftest parse-test
  (testing "Parsing authentication headers"
    (let [auth-header {"FirstName" "Teuvo Taavetti"
                       "cn" "Teuvo"
                       "givenName" "Teuvo"
                       "hetu" "010203-XXXX"
                       "sn" "Testaaja"}]
      (is (= (parse auth-header)
             {:first-name "Teuvo"
              :common-name "Teuvo"
              :given-name "Teuvo"
              :hetu "010203-XXXX"
              :surname "Testaaja"})))))
