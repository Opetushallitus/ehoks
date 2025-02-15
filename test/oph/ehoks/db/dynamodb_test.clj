(ns oph.ehoks.db.dynamodb-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.koski-test :as koski-test]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.hoks.handler :as hoks-handler]
            [oph.ehoks.oppijaindex :as oi]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.test-utils :as test-utils]
            [taoensso.faraday :as far])
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
    (is (thrown-with-msg?
          clojure.lang.ExceptionInfo
          #"palaute not found"
          (ddb/sync-amis-herate!
            {:existing-palaute {:hoks-id 54343
                                :kyselytyyppi "aloittaneet"}})))))

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
      (let [opiskeluoikeus (koski-test/mock-get-opiskeluoikeus-raw
                             (:opiskeluoikeus-oid hoks-data))
            saved-hoks (hoks-handler/save-hoks-and-initiate-all-palautteet!
                         {:hoks           hoks-data
                          :opiskeluoikeus opiskeluoikeus})
            hoks (hoks/get-by-id (:id saved-hoks))]
        (is (= (:sahkoposti hoks) "irma.isomerkki@esimerkki.com"))
        (ddb/sync-amis-herate!
          {:existing-palaute {:hoks-id      (:id saved-hoks)
                              :kyselytyyppi "aloittaneet"}})
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
                           {:update-expr "SET lahetystila = :1, #2 = :2"
                            :expr-attr-names {"#2" "viestintapalvelu-id"}
                            :expr-attr-vals {":1" "lahetetty"
                                             ":2" "2027-05-06"}})
          (hoks-handler/change-hoks-and-initiate-all-palautteet!
            {:hoks           (assoc hoks-data
                                    :id (:id saved-hoks)
                                    :sahkoposti "foo@bar.com")
             :opiskeluoikeus opiskeluoikeus}
            hoks/update!)
          (ddb/sync-amis-herate!
            {:existing-palaute {:hoks-id      (:id saved-hoks)
                                :kyselytyyppi "aloittaneet"}})
          ; fields that are owned by herätepalvelu are not overwritten
          (let [new-ddb-item
                (far/get-item @ddb/faraday-opts @(ddb/tables :amis) ddb-key)]
            (is (= (:sahkoposti new-ddb-item) "foo@bar.com"))
            (is (= (:viestintapalvelu-id new-ddb-item) "2027-05-06"))
            (is (= (:lahetystila new-ddb-item) "ei_lahetetty"))))))))
