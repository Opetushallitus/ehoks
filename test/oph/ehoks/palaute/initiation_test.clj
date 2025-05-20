(ns oph.ehoks.palaute.initiation-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.koski-test :as koski-test]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.hoks-test :as hoks-test]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.initiation :as init]
            [oph.ehoks.palaute.tapahtuma :as tapahtuma]
            [oph.ehoks.test-utils :as test-utils]
            [oph.ehoks.utils.date :as date])
  (:import (java.time LocalDate)))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-both-dbs-after-test)

(deftest test-missing-opiskeluoikeus-reinit-palautteet-for-uninitiated-hokses!
  (hoks/save! hoks-test/hoks-1)
  (testing "palaute reinitiation succeeds with missing opiskeluoikeus"
    (with-redefs [koski/get-opiskeluoikeus! (fn [oid] nil)
                  date/now (constantly (LocalDate/of 2021 7 1))]
      (init/reinit-palautteet-for-uninitiated-hokses! 2)
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
      (init/reinit-palautteet-for-uninitiated-hokses! 0)
      (is (= (palaute/get-by-hoks-id-and-kyselytyypit!
               db/spec {:hoks-id (:id hoks-test/hoks-1)
                        :kyselytyypit ["aloittaneet" "valmistuneet"
                                       "tyopaikkajakson_suorittaneet"]})
             [])))
    (testing (str "reinit-palautteet-for-uninitiated-hokses! makes palautteet "
                  "and sets `palaute-handled-at` timestamp.")
      (init/reinit-palautteet-for-uninitiated-hokses! 7)
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
      (is (-> (:id hoks-test/hoks-1)
              (db-hoks/select-hoks-by-id #{:palaute_handled_at})
              :palaute-handled-at
              (inst-ms)
              (> (inst-ms #inst "2025-05-20")))))
    (testing "reinit-palautteet-for-uninitiated-hokses! is idemponent"
      (init/reinit-palautteet-for-uninitiated-hokses! 7)
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
