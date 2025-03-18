(ns oph.ehoks.db.dynamodb-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.koski-test :as koski-test]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.hoks.handler :as hoks-handler]
            [oph.ehoks.oppijaindex :as oi]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.opiskelija :as opiskelija]
            [oph.ehoks.palaute.vastaajatunnus :as vt]
            [oph.ehoks.test-utils :as test-utils]
            [taoensso.faraday :as far])
  (:import (java.time LocalDate)))

(use-fixtures :once test-utils/migrate-database)
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
            herate (first (palaute/get-by-hoks-id-and-kyselytyypit!
                            db/spec {:hoks-id (:id saved-hoks)
                                     :kyselytyypit ["aloittaneet"]}))
            ctx (vt/build-ctx herate)
            amis-herate
            (opiskelija/build-amisherate-record-for-heratepalvelu ctx)]
        (is (= (:sahkoposti (:hoks ctx)) "irma.isomerkki@esimerkki.com"))
        (ddb/sync-amis-herate! amis-herate)
        (let [ddb-key {:tyyppi_kausi
                       (str "aloittaneet/"
                            (palaute/rahoituskausi (LocalDate/now)))
                       :toimija_oppija
                       "1.2.246.562.10.10000000009/1.2.246.562.24.12312312319"}
              ddb-item (ddb/get-item! :amis ddb-key)]
          (is (= (:sahkoposti ddb-item) "irma.isomerkki@esimerkki.com"))
          (is (= (:herate-source ddb-item) "sqs_viesti_ehoksista"))
          (far/update-item @ddb/faraday-opts @(ddb/tables :amis) ddb-key
                           {:update-expr "SET lahetystila = :1, #2 = :2"
                            :expr-attr-names {"#2" "viestintapalvelu-id"}
                            :expr-attr-vals {":1" "lahetetty"
                                             ":2" "2027-05-06"}})
          (ddb/sync-amis-herate! (assoc amis-herate :sahkoposti "foo@bar.com"))
          ; fields that are owned by herätepalvelu are not overwritten
          (let [new-ddb-item (ddb/get-item! :amis ddb-key)]
            (is (= (:sahkoposti new-ddb-item) "foo@bar.com"))
            (is (= (:viestintapalvelu-id new-ddb-item) "2027-05-06"))
            (is (= (:lahetystila new-ddb-item) "ei_lahetetty"))))))))
