(ns oph.ehoks.palaute.initiation-test
  (:require [clojure.test :refer [use-fixtures deftest testing is]]
            [oph.ehoks.test-utils :as test-utils]
            [oph.ehoks.db :as db]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.hoks-test :as hoks-test]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.koski-test :as koski-test]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.initiation :as init]))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-both-dbs-after-test)

(deftest test-reinit-palautteet-for-uninitiated-hokses!
  (with-redefs [koski/get-opiskeluoikeus-info-raw
                koski-test/mock-get-opiskeluoikeus-raw]
    (testing "saved HOKS doesn't have palautteet"
      (hoks/save! hoks-test/hoks-1)
      (is (= (palaute/get-by-hoks-id-and-kyselytyypit!
               db/spec {:hoks-id (:id hoks-test/hoks-1)
                        :kyselytyypit ["aloittaneet" "valmistuneet"
                                       "tyopaikkajakson_suorittaneet"]})
             [])))
    (testing "batchsize is honored"
      (init/reinit-palautteet-for-uninitiated-hokses! 0)
      (is (= (palaute/get-by-hoks-id-and-kyselytyypit!
               db/spec {:hoks-id (:id hoks-test/hoks-1)
                        :kyselytyypit ["aloittaneet" "valmistuneet"
                                       "tyopaikkajakson_suorittaneet"]})
             [])))
    (testing "reinit-palautteet-for-uninitiated-hokses! makes palautteet"
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
              ["ei_laheteta" "tyopaikkajakson_suorittaneet"]])))
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
