(ns oph.ehoks.palaute-test
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.tools.logging.test :refer [logged? with-log]]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.koski-test :as koski-test]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.external.organisaatio-test :as organisaatio-test]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.oppijaindex-test :as oppijaindex-test]
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

(deftest test-koulutustoimija-oid!
  (testing "Get koulutustoimija oid"
    (with-redefs
     [organisaatio/get-organisaatio! organisaatio-test/mock-get-organisaatio!]
      (do
        (is (= "1.2.246.562.10.346830761110"
               (palaute/koulutustoimija-oid!
                 {:oid "1.2.246.562.15.43634207518"
                  :koulutustoimija {:oid "1.2.246.562.10.346830761110"}})))
        (is (= "1.2.246.562.10.346830761110"
               (palaute/koulutustoimija-oid!
                 {:oid "1.2.246.562.15.43634207512"
                  :oppilaitos {:oid "1.2.246.562.10.52251087186"}})))))))

(deftest test-vastaamisajan-alkupvm
  (with-redefs [date/now (constantly (LocalDate/of 2024 4 26))]
    (testing "Vaustausajan alkupvm is same as herate date if it's in the future"
      (are [date-str] (let [date (LocalDate/parse date-str)]
                        (= (palaute/vastaamisajan-alkupvm date) date))
        "2024-04-26" "2024-04-28" "2024-12-12" "2025-01-01"))
    (testing (str "Vaustausajan alkupvm is same as `LocalDate/now` if herate "
                  "date is in the past")
      (are [date-str] (let [date (LocalDate/parse date-str)]
                        (= (palaute/vastaamisajan-alkupvm date) (date/now)))
        "2024-04-25" "2024-04-20" "2024-01-01" "2023-12-12"))))

(deftest test-vastaamisajan-loppupvm
  (are [herate-date-str alkupvm-str expected-str]
       (let [herate-date (LocalDate/parse herate-date-str)
             alkupvm     (LocalDate/parse alkupvm-str)
             expected    (LocalDate/parse expected-str)]
         (= (palaute/vastaamisajan-loppupvm herate-date alkupvm) expected))
    "2024-04-26" "2024-03-28" "2024-04-26"
    "2024-02-24" "2024-03-28" "2024-04-23"))

(deftest test-get-toimipiste
  (with-redefs [organisaatio/get-organisaatio!
                organisaatio-test/mock-get-organisaatio!]
    (testing (str "`toimipiste-oid!` returns the OID when organisaatio has "
                  "\"organisaatiotyyppi_03\" in `:tyypit`.")
      (is (= "1.2.246.562.10.12312312312"
             (palaute/toimipiste-oid!
               {:toimipiste {:oid "1.2.246.562.10.12312312312"}}))))
    (testing "`toimipiste-oid! returns `nil` when`"
      (testing "`:toimipiste` doesn't have \"organisaatiotyyppi_03\" code"
        (is (nil? (palaute/toimipiste-oid!
                    {:toimipiste {:oid "1.2.246.562.10.23423423427"}}))))
      (testing "`there is no :toimipiste` in `suoritus`"
        (is (nil? (palaute/toimipiste-oid! {})))))))

(deftest test-get-hankintakoulutuksen-toteuttaja
  (with-redefs [oppijaindex/get-hankintakoulutus-oids-by-master-oid
                oppijaindex-test/mock-get-hankintakoulutus-oids-by-master-oid
                koski/get-opiskeluoikeus-info-raw
                koski-test/mock-get-opiskeluoikeus-raw]
    (testing "The function returns `nil` when` there"
      (testing "is no hankintakoulutus linked to given HOKS."
        (is (nil? (palaute/hankintakoulutuksen-toteuttaja!
                    {:opiskeluoikeus-oid "1.2.246.562.15.12345678903"}))))
      (testing "are more than one linked opiskeluoikeus."
        (with-log
          (is (nil? (palaute/hankintakoulutuksen-toteuttaja!
                      {:opiskeluoikeus-oid "1.2.246.562.15.23456789017"})))
          (is (logged? 'oph.ehoks.palaute
                       :warn #"Enemmän kuin yksi linkitetty")))))
    (testing (str "The function retuns hankintakolutus when there is exactly one
                  opiskeluoikeus linked to given HOKS.")
      (is (= (palaute/hankintakoulutuksen-toteuttaja!
               {:opiskeluoikeus-oid "1.2.246.562.15.34567890123"})
             "1.2.246.562.10.34567890123")))))
