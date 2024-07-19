(ns oph.ehoks.db.dynamodb-test
  (:require [clojure.test :refer :all]
            [taoensso.faraday :as far]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.koski-test :as koski-test]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.oppijaindex :as oi]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.palaute.opiskelija :refer
             [get-for-heratepalvelu-by-hoks-id-and-kyselytyypit!]]
            [oph.ehoks.test-utils :as test-utils])
  (:import (java.time LocalDate)))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-database-after-test)

(deftest amis-field-mapping
  (testing "map-keys-to-ddb maps correctly"
    (is (= (ddb/map-keys-to-ddb :foo) :foo))
    (is (= (ddb/map-keys-to-ddb :toimija-oppija) :toimija_oppija))
    (is (= (ddb/map-keys-to-ddb :sahkoposti) :sahkoposti))))

(deftest missing-sync-test
  (testing "sync-item! fails when not enough information is available
  for identifying the item"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"item key missing"
                          (ddb/sync-item! :amis {}))))
  (testing "sync-amis-herate! fails when there is no information in db"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"palaute not found"
                          (ddb/sync-amis-herate! 54343 "aloittaneet")))))

(def hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                :oppija-oid "1.2.246.562.24.12312312319"
                :ensikertainen-hyvaksyminen (LocalDate/now)
                :osaamisen-hankkimisen-tarve true
                :sahkoposti "irma.isomerkki@esimerkki.com"})

(deftest sync-herate-to-dynamodb
  (with-redefs [koski/get-opiskeluoikeus-info-raw
                koski-test/mock-get-opiskeluoikeus-raw]

    (testing "When HOKS is saved, appropriate aloituspalaute is saved
    into database.  If this palaute is synced into herätepalvelu,
    the same values can be found there."
      (db-ops/insert-one! :oppijat {:oid (:oppija-oid hoks-data)})
      (oi/add-opiskeluoikeus!
        (:opiskeluoikeus-oid hoks-data)
        (:oppija-oid hoks-data))
      (let [saved-hoks (hoks/save! hoks-data)
            hoks (hoks/get-by-id (:id saved-hoks))]
        (is (= (:sahkoposti hoks) "irma.isomerkki@esimerkki.com"))
        (ddb/sync-amis-herate! (:id saved-hoks) "aloittaneet")
        (let [ddb-key {:tyyppi_kausi
                       (str "aloittaneet/"
                            (palaute/rahoituskausi (LocalDate/now)))
                       :toimija_oppija
                       "1.2.246.562.10.10000000009/1.2.246.562.24.12312312319"}
              ddb-item (far/get-item
                         @ddb/faraday-opts @(ddb/tables :amis) ddb-key)]
          (is (= (:sahkoposti ddb-item) "irma.isomerkki@esimerkki.com"))
          (is (= (:herate-source ddb-item) "sqs_viesti_ehoksista"))
          (far/update-item @ddb/faraday-opts @(ddb/tables :amis) ddb-key
                           {:update-expr "SET lahetystila = :1"
                            :expr-attr-vals {":1" "lahetetty"}})
          (hoks/update! (:id saved-hoks)
                        (assoc hoks-data :sahkoposti "foo@bar.com"))
          (ddb/sync-amis-herate! (:id saved-hoks) "aloittaneet")
          ; fields that are owned by herätepalvelu are not overwritten
          (let [new-ddb-item
                (far/get-item @ddb/faraday-opts @(ddb/tables :amis) ddb-key)]
            (is (= (:sahkoposti new-ddb-item) "foo@bar.com"))
            (is (= (:lahetystila new-ddb-item) "lahetetty"))))))))
