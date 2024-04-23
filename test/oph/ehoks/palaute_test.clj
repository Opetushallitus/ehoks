(ns oph.ehoks.palaute-test
  (:require [clojure.test :refer [deftest is testing]]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.utils.date :as date])
  (:import [java.time LocalDate]))

(deftest test-valid-herate-date?
  (testing "True if heratepvm is >= [rahoituskausi start year]-07-01"
    (with-redefs [date/now (constantly (LocalDate/of 2023 6 22))]
      (is (true? (palaute/valid-herate-date? (LocalDate/of 2022 7 2))))
      (is (true? (palaute/valid-herate-date? (LocalDate/of 2022 7 1))))
      (is (not (true? (palaute/valid-herate-date? (LocalDate/of 2022 6 1)))))))
  (testing "Not true if heratepvm is < [rahoituskausi start year]-07-01"
    (with-redefs [date/now (constantly (LocalDate/of 2023 7 22))]
      (is (not (true? (palaute/valid-herate-date? (LocalDate/of 2022 7 2)))))
      (is (not (true? (palaute/valid-herate-date? (LocalDate/of 2022 7 1))))))))
