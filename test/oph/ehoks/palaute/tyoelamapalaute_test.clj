(ns oph.ehoks.palaute.tyoelamapalaute-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.tools.logging.test :refer [logged? the-log with-log]]
            [medley.core :refer [remove-vals]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.palaute.opiskelija-test :as op-test]
            [oph.ehoks.palaute.tyoelamapalaute :as tep]
            [oph.ehoks.test-utils :as test-utils])
  (:import (java.time LocalDate)))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-database-after-test)

(def test-jakso
  {:hoks-id (:id op-test/test-hoks)
   :yksiloiva-tunniste "1"
   :tyopaikan-nimi "Testityöpaikka"
   :tyopaikan-ytunnus "1234567-8"
   :tyopaikkaohjaaja-nimi "Testi Ojaaja"
   :alku "2021-09-09"
   :loppu "2021-12-15"
   :oppija-oid "123.456.789"
   :tyyppi "test-tyyppi"
   :tutkinnonosa-id "test-tutkinnonosa-id"
   :hankkimistapa-id 2
   :hankkimistapa-tyyppi "koulutussopimus_01"})

(deftest test-initiate!
  (db-hoks/insert-hoks!
    {:id                 (:id op-test/test-hoks)
     :oppija-oid         (:oppija-oid op-test/test-hoks)
     :opiskeluoikeus-oid (:opiskeluoikeus-oid op-test/test-hoks)})
  (testing "Testing that function `initiate!`"
    (testing (str "stores kysely info to `palautteet` DB table and "
                  "successfully sends aloituskysely and paattokysely "
                  "herate to SQS queue")
      (tep/initiate! test-jakso op-test/opiskeluoikeus-1)
      (is (= (-> (tep/get-jakso-by-hoks-id-and-yksiloiva-tunniste!
                   db/spec
                   {:hoks-id            (:id op-test/test-hoks)
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
              :voimassa-alkupvm               (LocalDate/parse "2021-12-16")
              :voimassa-loppupvm              (LocalDate/parse "2022-02-14")
              :koulutustoimija                "1.2.246.562.10.346830761110"
              :herate-source                  "ehoks_update"})))
    (testing
     "doesn't initiate tyoelamapalaute if it has already been initiated"
      (with-log
        (tep/initiate! test-jakso op-test/opiskeluoikeus-1)
        (is (logged? 'oph.ehoks.palaute.tyoelamapalaute
                     :warn
                     #"Palaute has already been initiated"))))))
