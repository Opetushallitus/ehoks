(ns oph.ehoks.palaute.tyoelama-test
  (:require [clojure.test :refer [are deftest is testing use-fixtures]]
            [clojure.tools.logging.test :refer [logged? with-log]]
            [medley.core :refer [find-first remove-vals]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.external.organisaatio-test :as organisaatio-test]
            [oph.ehoks.hoks-test :as hoks-test]
            [oph.ehoks.opiskeluoikeus-test :as oo-test]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.tyoelama :as tep]
            [oph.ehoks.test-utils :as test-utils]
            [oph.ehoks.utils.date :as date])
  (:import (java.time LocalDate)))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-database-after-test)

(def test-jakso
  {:hoks-id (:id hoks-test/hoks-1)
   :yksiloiva-tunniste "1"
   :tyopaikan-nimi "Testityöpaikka"
   :tyopaikan-ytunnus "1234567-8"
   :tyopaikkaohjaaja-nimi "Testi Ohjaaja"
   :alku (LocalDate/of 2023 9 9)
   :loppu (LocalDate/of 2023 12 15)
   :osa-aikaisuustieto 100
   :oppija-oid "123.456.789"
   :tyyppi "test-tyyppi"
   :tutkinnonosa-id "test-tutkinnonosa-id"
   :keskeytymisajanjaksot [{:alku  (LocalDate/of 2023 9 28)
                            :loppu (LocalDate/of 2023 9 29)}]
   :hankkimistapa-id 2
   :hankkimistapa-tyyppi "koulutussopimus_01"})

(deftest test-next-niputus-date
  (testing "The function returns the correct niputus date when given `pvm-str`."
    (are [pvm-str expected] (= (tep/next-niputus-date (LocalDate/parse pvm-str))
                               (LocalDate/parse expected))
      "2021-12-03" "2021-12-16"
      "2021-12-27" "2022-01-01"
      "2021-04-25" "2021-05-01"
      "2022-06-24" "2022-07-01")))

(deftest test-osa-aikaisuus-missing?
  (testing "The function returns"
    (testing "`true` when osa-aikaisuus is missing."
      (are [jakso] (true? (tep/osa-aikaisuus-missing? jakso))
        {:osa-aikaisuustieto nil :loppu (LocalDate/of 2023 8 1)}
        {:loppu (LocalDate/of 2023 8 1)}))
    (testing "falsey value when osa-aikaisuus is not missing."
      (are [jakso] (not (tep/osa-aikaisuus-missing? jakso))
        {:osa-aikaisuustieto nil :loppu (LocalDate/of 2023 6 30)}
        {:loppu (LocalDate/of 2023 6 30)}
        {:osa-aikaisuustieto 30 :loppu (LocalDate/of 2023 8 1)}))))

(deftest test-fully-keskeytynyt?
  (testing "fully-keskeytynyt?"
    (let [herate1 {:keskeytymisajanjaksot [{:alku  (LocalDate/of 2021 8 8)
                                            :loppu (LocalDate/of 2021 8 10)}
                                           {:alku  (LocalDate/of 2021 8 1)
                                            :loppu (LocalDate/of 2021 8 4)}]
                   :loppu (LocalDate/of 2021 8 9)}
          herate2 {:keskeytymisajanjaksot [{:alku  (LocalDate/of 2021 8 8)
                                            :loppu (LocalDate/of 2021 8 10)}
                                           {:alku  (LocalDate/of 2021 8 1)
                                            :loppu (LocalDate/of 2021 8 4)}]
                   :loppu (LocalDate/of 2021 8 11)}
          herate3 {}
          herate4 {:keskeytymisajanjaksot [{:alku (LocalDate/of 2021 8 8)}]}]
      (is (tep/fully-keskeytynyt? herate1))
      (is (not (tep/fully-keskeytynyt? herate2)))
      (is (not (tep/fully-keskeytynyt? herate3)))
      (is (not (tep/fully-keskeytynyt? herate4))))))

(deftest test-tyopaikkajaksot
  (testing (str "The function returns osaamisen hankkimistavat with koodi-uri"
                "\"osaamisenhankkimistapa_koulutussopimus\" or "
                "\"osaamisenhankkimistapa_oppisopimus\"")
    (is (= (map :yksiloiva-tunniste (tep/tyopaikkajaksot hoks-test/hoks-1))
           '("1" "3" "4" "7" "9")))))

(deftest test-initial-palaute-and-reason
  (testing "On HOKS creation or update"
    (testing "don't initiate kysely if"
      (testing "opiskeluoikeus is in terminal state."
        (is (= (tep/initial-palaute-state-and-reason
                 test-jakso oo-test/opiskeluoikeus-5)
               [:ei-laheteta
                :opiskeluoikeus-oid
                :opiskeluoikeus-terminaalitilassa])))
      (testing "osa-aikaisuus is missing from työpaikkajakso"
        (is (= (tep/initial-palaute-state-and-reason
                 (dissoc test-jakso :osa-aikaisuustieto)
                 oo-test/opiskeluoikeus-1)
               [:ei-laheteta nil :osa-aikaisuus-puuttuu])))
      (testing "työpaikkajakso is interrupted on it's end date"
        (is (= (tep/initial-palaute-state-and-reason
                 (assoc-in test-jakso
                           [:keskeytymisajanjaksot 1]
                           {:alku  (LocalDate/of 2023 12 1)
                            :loppu (LocalDate/of 2023 12 15)})
                 oo-test/opiskeluoikeus-1)
               [:ei-laheteta nil :tyopaikkajakso-keskeytynyt])))
      (testing "opiskeluoikeus doesn't have any ammatillinen suoritus"
        (is (= (tep/initial-palaute-state-and-reason
                 test-jakso
                 oo-test/opiskeluoikeus-2)
               [:ei-laheteta :opiskeluoikeus-oid :ei-ammatillinen])))
      (testing "there is a feedback preventing code in opiskeluoikeusjakso."
        (is (= (tep/initial-palaute-state-and-reason
                 test-jakso
                 oo-test/opiskeluoikeus-4)
               [:ei-laheteta :opiskeluoikeus-oid :rahoitusperuste])))
      (testing "opiskeluoikeus is linked to another opiskeluoikeus"
        (is (= (tep/initial-palaute-state-and-reason
                 test-jakso
                 oo-test/opiskeluoikeus-3)
               [:ei-laheteta :opiskeluoikeus-oid :liittyva-opiskeluoikeus]))))
    (testing "initiate kysely if when all of the checks are OK."
        (is (= (tep/initial-palaute-state-and-reason
                 test-jakso
                 oo-test/opiskeluoikeus-1)
               [:odottaa-kasittelya nil :hoks-tallennettu])))))

(defn- build-expected-herate
  [jakso hoks]
  (let [voimassa-alkupvm (tep/next-niputus-date (:loppu jakso))]
    {:tila                           "odottaa_kasittelya"
     :kyselytyyppi                   "tyopaikkajakson_suorittaneet"
     :hoks-id                        (:id hoks)
     :jakson-yksiloiva-tunniste      (:yksiloiva-tunniste jakso)
     :heratepvm                      (:loppu jakso)
     :tutkintotunnus                 351407
     :tutkintonimike                 "(\"12345\",\"23456\")"
     :voimassa-alkupvm               voimassa-alkupvm
     :voimassa-loppupvm              (tep/voimassa-loppupvm voimassa-alkupvm)
     :koulutustoimija                "1.2.246.562.10.346830761110"
     :toimipiste-oid                 "1.2.246.562.10.12312312312"
     :herate-source                  "ehoks_update"}))

(deftest test-initiate!
  (db-hoks/insert-hoks!
    {:id                 (:id hoks-test/hoks-1)
     :oppija-oid         (:oppija-oid hoks-test/hoks-1)
     :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks-test/hoks-1)})
  (let [suoritus        (find-first suoritus/ammatillinen?
                                    (:suoritukset oo-test/opiskeluoikeus-1))
        koulutustoimija (get-in oo-test/opiskeluoikeus-1
                                [:koulutustoimija :oid])
        toimipiste-oid  (get-in suoritus [:toimipiste :oid])]
    (testing "Testing that function `initiate!`"
      (testing (str "stores kysely info to `palautteet` DB table and "
                    "successfully sends aloituskysely and paattokysely "
                    "herate to SQS queue")
        (tep/initiate!
          test-jakso hoks-test/hoks-1 suoritus koulutustoimija toimipiste-oid)
        (is (= (-> (palaute/get-by-hoks-id-and-yksiloiva-tunniste!
                     db/spec
                     {:hoks-id            (:id hoks-test/hoks-1)
                      :yksiloiva-tunniste (:yksiloiva-tunniste test-jakso)})
                   (dissoc :id :created-at :updated-at)
                   (->> (remove-vals nil?)))
               (build-expected-herate test-jakso hoks-test/hoks-1))))
      (testing
       "doesn't initiate tyoelamapalaute if it has already been initiated"
        (with-log
          (tep/initiate!
            test-jakso hoks-test/hoks-1 suoritus koulutustoimija toimipiste-oid)
          (is (logged? 'oph.ehoks.palaute.tyoelama
                       :warn
                       #"Palaute has already been initiated")))))))

(deftest test-initiate-all-uninitiated!
  (with-redefs [date/now (constantly (LocalDate/of 2023 10 18))
                organisaatio/get-organisaatio!
                organisaatio-test/mock-get-organisaatio!]
    (db-hoks/insert-hoks!
      {:id                 (:id hoks-test/hoks-1)
       :oppija-oid         (:oppija-oid hoks-test/hoks-1)
       :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks-test/hoks-1)})
    (testing (str "The function successfully initiates kyselys for every "
                  "tyopaikkajakso in HOKS.")
      (tep/initiate-all-uninitiated! hoks-test/hoks-1 oo-test/opiskeluoikeus-1)
      (doseq [jakso (tep/tyopaikkajaksot hoks-test/hoks-1)]
        (is (= (-> (palaute/get-by-hoks-id-and-yksiloiva-tunniste!
                     db/spec
                     {:hoks-id            (:id hoks-test/hoks-1)
                      :yksiloiva-tunniste (:yksiloiva-tunniste jakso)})
                   (dissoc :id :created-at :updated-at)
                   (->> (remove-vals nil?)))
               (build-expected-herate jakso hoks-test/hoks-1)))))))
