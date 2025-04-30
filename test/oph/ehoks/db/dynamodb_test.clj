(ns oph.ehoks.db.dynamodb-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.koski-test :as koski-test]
            [oph.ehoks.hoks.handler :as hoks-handler]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.opiskelija :as opiskelija]
            [oph.ehoks.palaute.vastaajatunnus :as vt]
            [oph.ehoks.test-utils :as test-utils])
  (:import (java.time LocalDate)))

(use-fixtures :once test-utils/with-clean-database-and-clean-dynamodb)
(use-fixtures :each test-utils/empty-database-after-test)

(deftest missing-sync-test
  (testing "sync-item! fails when not enough information is available
  for identifying the item"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"item key missing"
                          (ddb/sync-item! :amis {})))))

(def hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                :oppija-oid "1.2.246.562.24.12312312319"
                :ensikertainen-hyvaksyminen (LocalDate/now)
                :osaamisen-hankkimisen-tarve true
                :sahkoposti "irma.isomerkki@esimerkki.com"})

(deftest test-sync-herate-to-dynamodb
  (with-redefs [koski/get-opiskeluoikeus-info-raw
                koski-test/mock-get-opiskeluoikeus-raw]
    (let [opiskeluoikeus (koski-test/mock-get-opiskeluoikeus-raw
                           (:opiskeluoikeus-oid hoks-data))
          saved-hoks (hoks-handler/save-hoks-and-initiate-all-palautteet!
                       {:hoks           hoks-data
                        :opiskeluoikeus opiskeluoikeus})
          herate (first (palaute/get-by-hoks-id-and-kyselytyypit!
                          db/spec {:hoks-id (:id saved-hoks)
                                   :kyselytyypit ["aloittaneet"]}))
          ctx (vt/build-ctx herate)
          amis-herate
          (opiskelija/build-amisherate-record-for-heratepalvelu ctx)
          ddb-key {:tyyppi_kausi (str "aloittaneet/"
                                      (palaute/rahoituskausi (LocalDate/now)))
                   :toimija_oppija
                   "1.2.246.562.10.10000000009/1.2.246.562.24.12312312319"}]
      (testing "When HOKS is saved, appropriate aloituspalaute is saved
                into database.  If this palaute is to synced to herätepalvelu,
                the same values will be found there."
        (is (= (:sahkoposti (:hoks ctx)) "irma.isomerkki@esimerkki.com"))
        (ddb/sync-amis-herate-if-not-exists! amis-herate)
        (let [ddb-item (ddb/get-item! :amis ddb-key)]
          (is (= (:sahkoposti ddb-item) "irma.isomerkki@esimerkki.com"))
          (is (= (:herate-source ddb-item) "sqs_viesti_ehoksista"))))
      (testing
       "If palaute is already found in Herätepalvelu, do not sync anything."
        (ddb/sync-amis-herate-if-not-exists!
          (assoc amis-herate :sahkoposti "foo@bar.com"))
        ; fields that are owned by herätepalvelu are not overwritten
        (let [new-ddb-item (ddb/get-item! :amis ddb-key)]
          (is (= (:sahkoposti new-ddb-item)
                 "irma.isomerkki@esimerkki.com")))))))
