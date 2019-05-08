(ns oph.ehoks.oppija.opintopolku-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.oppija.opintopolku :as o])
  (:import [java.nio.charset StandardCharsets]))

(deftest keys-to-lower-test
  (testing "Converting map keys to lower"
    (is (= (o/keys-to-lower
             {"camelCase" "camelCase-value"
              "snake-case" "snake-case-value"
              "UPPER" "UPPER value"
              "lower" "lower value"})
           {"camelcase" "camelCase-value"
            "snake-case" "snake-case-value"
            "upper" "UPPER value"
            "lower" "lower value"}))
    (is (= (o/keys-to-lower {}) {}))
    (is (= (o/keys-to-lower nil) {}))))

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

(deftest valid-headers
  (testing "Validating Opintopolku headers"
    (is (nil?
          (o/validate {"FirstName" "Teuvo Taavetti"
                       "cn" "Teuvo"
                       "givenName" "Teuvo"
                       "hetu" "010203-XXXX"
                       "sn" "Testaaja"
                       "other" "Header"})))
    (is (= (o/validate {"FirstName" "Teuvo Taavetti"
                        "givenName" "Teuvo"
                        "hetu" "010203-XXXX"
                        "sn" "Testaaja"})
           "Header cn is missing"))
    (is (some? (o/validate {})))
    (is (some? (o/validate nil)))))
