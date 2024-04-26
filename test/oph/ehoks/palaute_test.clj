(ns oph.ehoks.palaute-test
  (:require [clojure.test :refer [deftest is testing]]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.utils.date :as date])
  (:import [java.time LocalDate]))

(defn- mock-get-organisaatio [oid]
  (cond
    (= oid "1.2.246.562.10.52251087186")
    {:parentOid "1.2.246.562.10.346830761110"}))

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

(deftest test-koulutustoimija-oid!
  (testing "Get koulutustoimija oid"
    (with-redefs
     [organisaatio/get-organisaatio! mock-get-organisaatio]
      (do
        (is (= "1.2.246.562.10.346830761110"
               (palaute/koulutustoimija-oid!
                 {:oid "1.2.246.562.15.43634207518"
                  :koulutustoimija {:oid "1.2.246.562.10.346830761110"}})))
        (is (= "1.2.246.562.10.346830761110"
               (palaute/koulutustoimija-oid!
                 {:oid "1.2.246.562.15.43634207512"
                  :oppilaitos {:oid "1.2.246.562.10.52251087186"}})))))))
