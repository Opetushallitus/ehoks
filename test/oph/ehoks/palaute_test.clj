(ns oph.ehoks.palaute-test
  (:require [clojure.data :as d]
            [clojure.test :refer [are deftest is testing use-fixtures]]
            [clojure.tools.logging.test :refer [logged? with-log]]
            [medley.core :refer [remove-vals]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.koski-test :as koski-test]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.external.organisaatio-test :as organisaatio-test]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.hoks-test :as hoks-test]
            [oph.ehoks.opiskeluoikeus-test :as oo-test]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.oppijaindex-test :as oppijaindex-test]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.tapahtuma :as tapahtuma]
            [oph.ehoks.test-utils :as test-utils]
            [oph.ehoks.utils.date :as date])
  (:import [java.time LocalDate]))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-both-dbs-after-test)

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

(deftest test-missing-opiskeluoikeus-reinit-palautteet-for-uninitiated-hokses!
  (hoks/save! hoks-test/hoks-1)
  (testing "palaute reinitiation succeeds with missing opiskeluoikeus"
    (with-redefs [koski/get-opiskeluoikeus! (fn [oid] nil)
                  date/now (constantly (LocalDate/of 2021 7 1))]
      (palaute/reinit-for-uninitiated-hokses! 2)
      (is (= (->> {:hoks-id (:id hoks-test/hoks-1)
                   :kyselytyypit ["aloittaneet" "valmistuneet"
                                  "tyopaikkajakson_suorittaneet"]}
                  (tapahtuma/get-all-by-hoks-id-and-kyselytyypit! db/spec)
                  (map (juxt :uusi-tila :syy)))
             [["odottaa_kasittelya" "ei_loydy"]
              ["odottaa_kasittelya" "ei_loydy"]
              ["odottaa_kasittelya" "ei_loydy"]
              ["odottaa_kasittelya" "ei_loydy"]
              ["odottaa_kasittelya" "ei_loydy"]
              ["odottaa_kasittelya" "ei_loydy"]
              ["odottaa_kasittelya" "ei_loydy"]]))
      (is (= (->> {:hoks-id (:id hoks-test/hoks-1)
                   :kyselytyypit ["aloittaneet" "valmistuneet"
                                  "tyopaikkajakson_suorittaneet"]}
                  (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                  (map (juxt :tila :kyselytyyppi)))
             [["odottaa_kasittelya" "aloittaneet"]
              ["odottaa_kasittelya" "valmistuneet"]
              ["odottaa_kasittelya" "tyopaikkajakson_suorittaneet"]
              ["odottaa_kasittelya" "tyopaikkajakson_suorittaneet"]
              ["odottaa_kasittelya" "tyopaikkajakson_suorittaneet"]
              ["odottaa_kasittelya" "tyopaikkajakson_suorittaneet"]
              ["odottaa_kasittelya" "tyopaikkajakson_suorittaneet"]]))
      (is (some? (-> (:id hoks-test/hoks-1)
                     (db-hoks/select-hoks-by-id #{:palaute_handled_at})
                     :palaute-handled-at))))))

(deftest test-reinit-palautteet-for-uninitiated-hokses!
  (with-redefs [koski/get-opiskeluoikeus-info-raw
                koski-test/mock-get-opiskeluoikeus-raw]
    (testing (str "Saved HOKS doesn't have palautteet and `palaute-handled-at` "
                  "timestamp.")
      (hoks/save! hoks-test/hoks-1)
      (is (= (palaute/get-by-hoks-id-and-kyselytyypit!
               db/spec {:hoks-id (:id hoks-test/hoks-1)
                        :kyselytyypit ["aloittaneet" "valmistuneet"
                                       "tyopaikkajakson_suorittaneet"]})
             []))
      (is (nil? (-> (:id hoks-test/hoks-1)
                    (db-hoks/select-hoks-by-id #{:palaute_handled_at})
                    :palaute-handled-at))))
    (testing "batchsize is honored"
      (palaute/reinit-for-uninitiated-hokses! 0)
      (is (= (palaute/get-by-hoks-id-and-kyselytyypit!
               db/spec {:hoks-id (:id hoks-test/hoks-1)
                        :kyselytyypit ["aloittaneet" "valmistuneet"
                                       "tyopaikkajakson_suorittaneet"]})
             [])))
    (testing (str "reinit-palautteet-for-uninitiated-hokses! makes palautteet "
                  "and sets `palaute-handled-at` timestamp.")
      (palaute/reinit-for-uninitiated-hokses! 7)
      (is (= (->> {:hoks-id (:id hoks-test/hoks-1)
                   :kyselytyypit ["aloittaneet" "valmistuneet"
                                  "tyopaikkajakson_suorittaneet"]}
                  (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                  (map (juxt :tila :kyselytyyppi)))
             [["ei_laheteta" "aloittaneet"]
              ["ei_laheteta" "valmistuneet"]
              ["ei_laheteta" "tyopaikkajakson_suorittaneet"]
              ["ei_laheteta" "tyopaikkajakson_suorittaneet"]
              ["ei_laheteta" "tyopaikkajakson_suorittaneet"]
              ["ei_laheteta" "tyopaikkajakson_suorittaneet"]
              ["ei_laheteta" "tyopaikkajakson_suorittaneet"]]))
      (is (some? (-> (:id hoks-test/hoks-1)
                     (db-hoks/select-hoks-by-id #{:palaute_handled_at})
                     :palaute-handled-at))))
    (testing "reinit-palautteet-for-uninitiated-hokses! is idemponent"
      (palaute/reinit-for-uninitiated-hokses! 7)
      (is (= (->> {:hoks-id (:id hoks-test/hoks-1)
                   :kyselytyypit ["aloittaneet" "valmistuneet"
                                  "tyopaikkajakson_suorittaneet"]}
                  (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                  (map (juxt :tila :kyselytyyppi)))
             [["ei_laheteta" "aloittaneet"]
              ["ei_laheteta" "valmistuneet"]
              ["ei_laheteta" "tyopaikkajakson_suorittaneet"]
              ["ei_laheteta" "tyopaikkajakson_suorittaneet"]
              ["ei_laheteta" "tyopaikkajakson_suorittaneet"]
              ["ei_laheteta" "tyopaikkajakson_suorittaneet"]
              ["ei_laheteta" "tyopaikkajakson_suorittaneet"]])))))

(defn- build-expected-herate
  [jakso hoks]
  (let [heratepvm (:loppu jakso)
        voimassa-alkupvm (palaute/next-niputus-date heratepvm)]
    {:tila                           "odottaa_kasittelya"
     :kyselytyyppi                   "tyopaikkajakson_suorittaneet"
     :hoks-id                        (:id hoks)
     :jakson-yksiloiva-tunniste      (:yksiloiva-tunniste jakso)
     :heratepvm                      heratepvm
     :suorituskieli                  "fi"
     :tutkintotunnus                 351407
     :tutkintonimike                 "(\"12345\",\"23456\")"
     :voimassa-alkupvm               voimassa-alkupvm
     :voimassa-loppupvm              (palaute/vastaamisajan-loppupvm
                                       heratepvm voimassa-alkupvm)
     :koulutustoimija                "1.2.246.562.10.346830761110"
     :toimipiste-oid                 "1.2.246.562.10.12312312312"
     :herate-source                  "ehoks_update"}))

(def #^{:private true} expected-for-initiate-all
  [{:tila "ei_laheteta"
    :kyselytyyppi "aloittaneet"
    :voimassa-loppupvm (LocalDate/of 2023 6 14)
    :suorituskieli "fi"
    :heratepvm (LocalDate/of 2023 4 16)
    :herate-source "ehoks_update"
    :toimipiste-oid "1.2.246.562.10.12312312312"
    :koulutustoimija "1.2.246.562.10.346830761110"
    :tutkintotunnus 351407
    :hoks-id 12345
    :tutkintonimike "(\"12345\",\"23456\")"
    :voimassa-alkupvm (LocalDate/of 2023 10 18)}
   {:tila "odottaa_kasittelya"
    :kyselytyyppi "valmistuneet"
    :voimassa-loppupvm (LocalDate/of  2024 3 5)
    :suorituskieli "fi"
    :heratepvm (LocalDate/of 2024 2 5)
    :herate-source "ehoks_update"
    :toimipiste-oid "1.2.246.562.10.12312312312"
    :koulutustoimija "1.2.246.562.10.346830761110"
    :tutkintotunnus 351407
    :hoks-id 12345
    :tutkintonimike "(\"12345\",\"23456\")"
    :voimassa-alkupvm (LocalDate/of 2024 2 5)}
   {:tila "odottaa_kasittelya"
    :kyselytyyppi "tyopaikkajakson_suorittaneet"
    :voimassa-loppupvm (LocalDate/of 2024 1 14)
    :suorituskieli "fi"
    :heratepvm (LocalDate/of 2023 12 5)
    :herate-source "ehoks_update"
    :toimipiste-oid "1.2.246.562.10.12312312312"
    :jakson-yksiloiva-tunniste "1"
    :koulutustoimija "1.2.246.562.10.346830761110"
    :tutkintotunnus 351407
    :hoks-id 12345
    :tutkintonimike "(\"12345\",\"23456\")"
    :voimassa-alkupvm (LocalDate/of 2023 12 16)}
   {:tila "odottaa_kasittelya"
    :kyselytyyppi "tyopaikkajakson_suorittaneet"
    :voimassa-loppupvm (LocalDate/of 2023 12 30)
    :suorituskieli "fi"
    :heratepvm (LocalDate/of 2023 11 25)
    :herate-source "ehoks_update"
    :toimipiste-oid "1.2.246.562.10.12312312312"
    :jakson-yksiloiva-tunniste "3"
    :koulutustoimija "1.2.246.562.10.346830761110"
    :tutkintotunnus 351407
    :hoks-id 12345
    :tutkintonimike "(\"12345\",\"23456\")"
    :voimassa-alkupvm (LocalDate/of 2023 12 1)}
   {:tila "odottaa_kasittelya"
    :kyselytyyppi "tyopaikkajakson_suorittaneet"
    :voimassa-loppupvm (LocalDate/of 2024 2 14)
    :suorituskieli "fi"
    :heratepvm (LocalDate/of 2024 1 6)
    :herate-source "ehoks_update"
    :toimipiste-oid "1.2.246.562.10.12312312312"
    :jakson-yksiloiva-tunniste "4"
    :koulutustoimija "1.2.246.562.10.346830761110"
    :tutkintotunnus 351407
    :hoks-id 12345
    :tutkintonimike "(\"12345\",\"23456\")"
    :voimassa-alkupvm (LocalDate/of 2024 1 16)}
   {:tila "odottaa_kasittelya"
    :kyselytyyppi "tyopaikkajakson_suorittaneet"
    :voimassa-loppupvm (LocalDate/of 2024 3 1)
    :suorituskieli "fi"
    :heratepvm (LocalDate/of 2024 1 25)
    :herate-source "ehoks_update"
    :toimipiste-oid "1.2.246.562.10.12312312312"
    :jakson-yksiloiva-tunniste "7"
    :koulutustoimija "1.2.246.562.10.346830761110"
    :tutkintotunnus 351407
    :hoks-id 12345
    :tutkintonimike "(\"12345\",\"23456\")"
    :voimassa-alkupvm (LocalDate/of 2024 2 1)}
   {:tila "odottaa_kasittelya"
    :kyselytyyppi "tyopaikkajakson_suorittaneet"
    :voimassa-loppupvm (LocalDate/of 2024 5 15)
    :suorituskieli "fi"
    :heratepvm (LocalDate/of 2024 4 5)
    :herate-source "ehoks_update"
    :toimipiste-oid "1.2.246.562.10.12312312312"
    :jakson-yksiloiva-tunniste "9"
    :koulutustoimija "1.2.246.562.10.346830761110"
    :tutkintotunnus 351407
    :hoks-id 12345
    :tutkintonimike "(\"12345\",\"23456\")"
    :voimassa-alkupvm (LocalDate/of 2024 4 16)}])

(deftest test-initiate-all!
  (with-redefs [date/now (constantly (LocalDate/of 2023 10 18))
                organisaatio/get-organisaatio!
                organisaatio-test/mock-get-organisaatio!]
    (db-hoks/insert-hoks!
      {:id                 (:id hoks-test/hoks-1)
       :oppija-oid         (:oppija-oid hoks-test/hoks-1)
       :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks-test/hoks-1)})
    (testing "The function successfully initiates all palautteet."
      (palaute/initiate-all! {:hoks hoks-test/hoks-1
                              :opiskeluoikeus oo-test/opiskeluoikeus-1
                              ::tapahtuma/type :hoks-tallennus})
      (let [real (map #(remove-vals nil? (dissoc % :id :created-at :updated-at))
                      (palaute/get-by-hoks-id-and-kyselytyypit!
                        db/spec
                        {:hoks-id            (:id hoks-test/hoks-1)
                         :kyselytyypit      ["aloittaneet" "valmistuneet"
                                             "tyopaikkajakson_suorittaneet"]}))
            expected expected-for-initiate-all]
        (is (= real expected)
            ["diff: " (d/diff real expected)])))))
