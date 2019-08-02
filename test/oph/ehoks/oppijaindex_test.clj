(ns oph.ehoks.oppijaindex-test
  (:require [oph.ehoks.oppijaindex :as sut]
            [clojure.test :as t]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.db.postgresql :as db]))

(t/use-fixtures :each utils/with-database)

(t/use-fixtures :once utils/clean-db)

(t/deftest get-oppijat-without-index
  (t/testing "Get oppijat without index"
    (db/insert-hoks! {:oppija-oid "1.2.246.562.24.11111111111"
                      :opiskeluoikeus-oid "1.2.246.562.15.22222222222"})
    (db/insert-hoks! {:oppija-oid "1.2.246.562.24.11111111111"
                      :opiskeluoikeus-oid "1.2.246.562.15.22222222223"})
    (db/insert-hoks! {:oppija-oid "1.2.246.562.24.11111111112"
                      :opiskeluoikeus-oid "1.2.246.562.15.22222222224"})
    (t/is
      (= (sut/get-oppijat-without-index)
         [{:oppija_oid "1.2.246.562.24.11111111111"}
          {:oppija_oid "1.2.246.562.24.11111111112"}]))
    (t/is
      (= (sut/get-oppijat-without-index-count)
         2))))

(t/deftest get-opiskeluoikeudet-without-index
  (t/testing "Get opiskeluoikeudet without index"
    (db/insert-hoks! {:oppija-oid "1.2.246.562.24.11111111111"
                      :opiskeluoikeus-oid "1.2.246.562.15.22222222222"})
    (db/insert-hoks! {:oppija-oid "1.2.246.562.24.11111111111"
                      :opiskeluoikeus-oid "1.2.246.562.15.22222222223"})
    (db/insert-hoks! {:oppija-oid "1.2.246.562.24.11111111112"
                      :opiskeluoikeus-oid "1.2.246.562.15.22222222224"})
    (t/is
      (= (sut/get-opiskeluoikeudet-without-index)
         [{:oppija_oid "1.2.246.562.24.11111111111"
           :opiskeluoikeus_oid "1.2.246.562.15.22222222222"}
          {:oppija_oid "1.2.246.562.24.11111111111"
           :opiskeluoikeus_oid "1.2.246.562.15.22222222223"}
          {:oppija_oid "1.2.246.562.24.11111111112"
           :opiskeluoikeus_oid "1.2.246.562.15.22222222224"}]))
    (t/is
      (= (sut/get-opiskeluoikeudet-without-index-count)
         3))))

(t/deftest get-oppija-opiskeluoikeudet
  (t/testing "Get oppija opiskeluoikeudet"
    (db/insert-oppija {:oid "1.2.246.562.24.11111111111"})
    (db/insert-opiskeluoikeus
      {:oppija-oid "1.2.246.562.24.11111111111"
       :oid "1.2.246.562.15.22222222222"})
    (db/insert-opiskeluoikeus
      {:oppija-oid "1.2.246.562.24.11111111111"
       :oid "1.2.246.562.15.22222222224"})
    (db/insert-oppija {:oid "1.2.246.562.24.11111111112"})
    (db/insert-opiskeluoikeus
      {:oppija-oid "1.2.246.562.24.11111111112"
       :oid "1.2.246.562.15.22222222223"})
    (t/is
      (= (sut/get-oppija-opiskeluoikeudet "1.2.246.562.24.11111111111")
         [{:oid "1.2.246.562.15.22222222222"
           :oppija-oid "1.2.246.562.24.11111111111"
           :tutkinto ""
           :osaamisala ""}
          {:oid "1.2.246.562.15.22222222224"
           :oppija-oid "1.2.246.562.24.11111111111"
           :tutkinto ""
           :osaamisala ""}]))
    (t/is
      (= (sut/get-oppija-opiskeluoikeudet "1.2.246.562.24.11111111112")
         [{:oid "1.2.246.562.15.22222222223"
           :oppija-oid "1.2.246.562.24.11111111112"
           :tutkinto ""
           :osaamisala ""}]))))

(t/deftest get-oppija-by-oid
  (t/testing "Get oppija by oid"
    (db/insert-oppija {:oid "1.2.246.562.24.11111111111" :nimi "Test 1"})
    (db/insert-oppija {:oid "1.2.246.562.24.11111111112" :nimi "Test 2"})
    (t/is (= (sut/get-oppija-by-oid "1.2.246.562.24.11111111111")
             {:oid "1.2.246.562.24.11111111111" :nimi "Test 1"}))
    (t/is (= (sut/get-oppija-by-oid "1.2.246.562.24.11111111112")
             {:oid "1.2.246.562.24.11111111112" :nimi "Test 2"}))))

(t/deftest get-opiskeluoikeus-by-oid
  (t/testing "Get opiskeluoikeus by oid"
    (db/insert-oppija {:oid "1.2.246.562.24.11111111111" :nimi "Test 1"})
    (db/insert-opiskeluoikeus {:oid "1.2.246.562.15.22222222222"
                               :oppija_oid "1.2.246.562.24.11111111111"})
    (db/insert-opiskeluoikeus {:oid "1.2.246.562.15.22222222223"
                               :oppija_oid "1.2.246.562.24.11111111111"})
    (t/is (= (sut/get-opiskeluoikeus-by-oid "1.2.246.562.15.22222222222")
             {:oid "1.2.246.562.15.22222222222"
              :oppija-oid "1.2.246.562.24.11111111111"
              :tutkinto ""
              :osaamisala ""}))
    (t/is (= (sut/get-opiskeluoikeus-by-oid "1.2.246.562.15.22222222223")
             {:oid "1.2.246.562.15.22222222223"
              :oppija-oid "1.2.246.562.24.11111111111"
              :tutkinto ""
              :osaamisala ""}))))