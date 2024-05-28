(ns oph.ehoks.palaute.tyoelamapalaute-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.tools.logging.test :refer [logged? with-log]]
            [medley.core :refer [remove-vals]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.hoks-test :as hoks-test]
            [oph.ehoks.opiskeluoikeus-test :as oo-test]
            [oph.ehoks.palaute.tyoelamapalaute :as tep]
            [oph.ehoks.test-utils :as test-utils])
  (:import (java.time LocalDate)))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-database-after-test)

(def test-jakso
  {:hoks-id (:id hoks-test/hoks-1)
   :yksiloiva-tunniste "1"
   :tyopaikan-nimi "Testityöpaikka"
   :tyopaikan-ytunnus "1234567-8"
   :tyopaikkaohjaaja-nimi "Testi Ohjaaja"
   :alku "2023-09-09"
   :loppu "2023-12-15"
   :osa-aikaisuustieto 100
   :oppija-oid "123.456.789"
   :tyyppi "test-tyyppi"
   :tutkinnonosa-id "test-tutkinnonosa-id"
   :keskeytymisajanjaksot [{:alku "2023-09-28" :loppu "2023-09-29"}]
   :hankkimistapa-id 2
   :hankkimistapa-tyyppi "koulutussopimus_01"})

(deftest test-initiate?
  (testing "On HOKS creation or update"
    (letfn [(test-not-initiated [jakso opiskeluoikeus log-msg]
              (with-log
                (is (not (tep/initiate? jakso opiskeluoikeus)))
                (is (logged? 'oph.ehoks.palaute.tyoelamapalaute
                             :info
                             log-msg))))]
      (testing "don't initiate kysely if"
        (testing "opiskeluoikeus is in terminal state."
          (test-not-initiated
            test-jakso oo-test/opiskeluoikeus-5 #"terminal state"))
        (testing "osa-aikaisuus is missing from työpaikkajakso"
          (test-not-initiated
            (dissoc test-jakso :osa-aikaisuustieto)
            oo-test/opiskeluoikeus-1
            #"Osa-aikaisuus missing"))
        (testing "työpaikkajakso is interrupted on it's end date"
          (test-not-initiated
            (assoc-in test-jakso
                      [:keskeytymisajanjaksot 1]
                      {:alku "2023-12-01" :loppu "2023-12-15"})
            oo-test/opiskeluoikeus-1
            #"interrupted"))
        (testing "opiskeluoikeus doesn't have any ammatillinen suoritus"
          (test-not-initiated
            test-jakso oo-test/opiskeluoikeus-2 #"No ammatillinen suoritus"))
        (testing "there is a feedback preventing code in opiskeluoikeusjakso."
          (test-not-initiated
            test-jakso oo-test/opiskeluoikeus-4 #"funding basis"))
        (testing "opiskeluoikeus is linked to another opiskeluoikeus"
          (test-not-initiated
            test-jakso oo-test/opiskeluoikeus-3 #"linked to another")))
      (testing "initiate kysely if when all of the checks are OK."
        (is (tep/initiate? test-jakso oo-test/opiskeluoikeus-1))))))

(deftest test-initiate!
  (db-hoks/insert-hoks!
    {:id                 (:id hoks-test/hoks-1)
     :oppija-oid         (:oppija-oid hoks-test/hoks-1)
     :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks-test/hoks-1)})
  (testing "Testing that function `initiate!`"
    (testing (str "stores kysely info to `palautteet` DB table and "
                  "successfully sends aloituskysely and paattokysely "
                  "herate to SQS queue")
      (tep/initiate! test-jakso oo-test/opiskeluoikeus-1)
      (is (= (-> (tep/get-jakso-by-hoks-id-and-yksiloiva-tunniste!
                   db/spec
                   {:hoks-id            (:id hoks-test/hoks-1)
                    :yksiloiva-tunniste (:yksiloiva-tunniste test-jakso)})
                 (dissoc :id :created-at :updated-at)
                 (->> (remove-vals nil?)))
             {:tila                           "odottaa_kasittelya"
              :kyselytyyppi                   "tyopaikkajakson_suorittaneet"
              :hoks-id                        12345
              :jakson-yksiloiva-tunniste      (:yksiloiva-tunniste test-jakso)
              :heratepvm                      (LocalDate/parse
                                                (:loppu test-jakso))
              :tutkintotunnus                 351407
              :tutkintonimike                 "(\"12345\",\"23456\")"
              :voimassa-alkupvm               (LocalDate/parse "2023-12-16")
              :voimassa-loppupvm              (LocalDate/parse "2024-02-14")
              :koulutustoimija                "1.2.246.562.10.346830761110"
              :herate-source                  "ehoks_update"})))
    (testing
     "doesn't initiate tyoelamapalaute if it has already been initiated"
      (with-log
        (tep/initiate! test-jakso oo-test/opiskeluoikeus-1)
        (is (logged? 'oph.ehoks.palaute.tyoelamapalaute
                     :warn
                     #"Palaute has already been initiated"))))))
