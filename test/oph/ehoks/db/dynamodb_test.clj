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
        (let [ddb-item
              (->> {:tyyppi_kausi
                    (str "aloittaneet/" (palaute/rahoituskausi (LocalDate/now)))
                    :toimija_oppija
                    "1.2.246.562.10.10000000009/1.2.246.562.24.12312312319"}
                   (far/get-item @ddb/faraday-opts @(ddb/tables :amis)))]
          (is (= (:sahkoposti ddb-item) "irma.isomerkki@esimerkki.com"))
          (is (= (:herate-source ddb-item) "sqs_viesti_ehoksista")))))))
