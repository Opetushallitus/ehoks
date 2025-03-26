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
            [oph.ehoks.palaute.scheduler :as schedule]
            [oph.ehoks.utils.date :as date])
  (:import [java.time LocalDate]))

(deftest test-scheduler-runs
  (testing "Can be called"
    (is (schedule/daily-actions! {}))))

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
        (is (nil? (palaute/koulutustoimija-oid! nil)))
        (is (= "1.2.246.562.10.346830761110"
               (palaute/koulutustoimija-oid!
                 {:oid "1.2.246.562.15.43634207518"
                  :koulutustoimija {:oid "1.2.246.562.10.346830761110"}})))
        (is (= "1.2.246.562.10.346830761110"
               (palaute/koulutustoimija-oid!
                 {:oid "1.2.246.562.15.43634207512"
                  :oppilaitos {:oid "1.2.246.562.10.52251087186"}})))))))

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

(defn- construct-opiskeluoikeus [jaksot]
  {:tila {:opiskeluoikeusjaksot
          (for [[alku tila rahoitus] jaksot]
            {:alku alku
             :tila {:koodiarvo tila}
             :opintojenRahoitus {:koodiarvo (str rahoitus)}})}})

(deftest test-feedback-collecting-prevented?
  (testing
   "Tunnistetaan oikein opiskeluoikeuden tila, jossa palautekyselyitä
   ei lähetetä."
    (are [input result]
         (-> input
             (construct-opiskeluoikeus)
             (palaute/feedback-collecting-prevented? "2021-07-15")
             (= result))
      [["2021-07-30" "lasna" 3]] false
      [["2018-06-20" "valmistunut" 1]] false
      [["2020-06-20" "lasna" 14]] true
      [["2019-01-03" "lasna" 1] ["2022-09-01" "lasna" 1]] false
      [["2019-01-03" "lasna" 6] ["2019-09-01" "valmistunut" 6]] true
      [["2020-03-15" "lasna" 3] ["2019-09-01" "lasna" 6]] false
      [["2021-03-15" "valmistunut" 2]
       ["2020-03-15" "lasna" 14]
       ["2019-09-01" "lasna" 14]] false
      [["2019-01-03" "lasna" 5] ["2019-09-01" "lasna" 15]] true)
    (are [input date result] (-> input
                                 (construct-opiskeluoikeus)
                                 (palaute/feedback-collecting-prevented? date)
                                 (= result))
      [["2020-06-20" "lasna" 14]] "2019-01-01" false
      [["2021-03-15" "valmistunut" 2]
       ["2020-03-15" "lasna" 14]
       ["2019-09-01" "lasna" 14]] "2020-07-01" true
      [["2021-03-15" "valmistunut" 2]
       ["2020-03-15" "lasna" 14]
       ["2019-09-01" "lasna" 14]] "2019-12-01" true
      [["2021-03-15" "valmistunut" 2]
       ["2020-03-15" "lasna" 14]
       ["2019-09-01" "lasna" 14]] "2021-07-01" false)))
