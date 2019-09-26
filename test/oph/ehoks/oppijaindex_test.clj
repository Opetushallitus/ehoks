(ns oph.ehoks.oppijaindex-test
  (:require [oph.ehoks.oppijaindex :as sut]
            [clojure.test :as t]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-opiskeluoikeus]
            [oph.ehoks.db.db-operations.oppija :as db-oppija]))

(t/use-fixtures :each utils/with-database)

(def opiskeluoikeus-data
  {:oppilaitos {:oid "1.2.246.562.10.222222222222"}
   :suoritukset
   [{:koulutusmoduuli
     {:tunniste
      {:koodiarvo "351407"
       :nimi {:fi "Testialan perustutkinto"
              :sv "Grundexamen inom testsbranschen"
              :en "Testing"}}}}]})

(t/deftest get-oppijat-without-index
  (t/testing "Get oppijat without index"
    (db-hoks/insert-hoks! {:oppija-oid "1.2.246.562.24.11111111111"
                           :opiskeluoikeus-oid "1.2.246.562.15.22222222222"})
    (db-hoks/insert-hoks! {:oppija-oid "1.2.246.562.24.11111111111"
                           :opiskeluoikeus-oid "1.2.246.562.15.22222222223"})
    (db-hoks/insert-hoks! {:oppija-oid "1.2.246.562.24.11111111112"
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
    (db-hoks/insert-hoks! {:oppija-oid "1.2.246.562.24.11111111111"
                           :opiskeluoikeus-oid "1.2.246.562.15.22222222222"})
    (db-hoks/insert-hoks! {:oppija-oid "1.2.246.562.24.11111111111"
                           :opiskeluoikeus-oid "1.2.246.562.15.22222222223"})
    (db-hoks/insert-hoks! {:oppija-oid "1.2.246.562.24.11111111112"
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
    (db-oppija/insert-oppija {:oid "1.2.246.562.24.11111111111"})
    (db-opiskeluoikeus/insert-opiskeluoikeus
      {:oppija-oid "1.2.246.562.24.11111111111"
       :oid "1.2.246.562.15.22222222222"})
    (db-opiskeluoikeus/insert-opiskeluoikeus
      {:oppija-oid "1.2.246.562.24.11111111111"
       :oid "1.2.246.562.15.22222222224"})
    (db-oppija/insert-oppija {:oid "1.2.246.562.24.11111111112"})
    (db-opiskeluoikeus/insert-opiskeluoikeus
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
    (db-oppija/insert-oppija {:oid "1.2.246.562.24.11111111111" :nimi "Test 1"})
    (db-oppija/insert-oppija {:oid "1.2.246.562.24.11111111112" :nimi "Test 2"})
    (t/is (= (sut/get-oppija-by-oid "1.2.246.562.24.11111111111")
             {:oid "1.2.246.562.24.11111111111" :nimi "Test 1"}))
    (t/is (= (sut/get-oppija-by-oid "1.2.246.562.24.11111111112")
             {:oid "1.2.246.562.24.11111111112" :nimi "Test 2"}))))

(t/deftest get-opiskeluoikeus-by-oid
  (t/testing "Get opiskeluoikeus by oid"
    (db-oppija/insert-oppija {:oid "1.2.246.562.24.11111111111" :nimi "Test 1"})
    (db-opiskeluoikeus/insert-opiskeluoikeus
      {:oid "1.2.246.562.15.22222222222"
       :oppija_oid "1.2.246.562.24.11111111111"})
    (db-opiskeluoikeus/insert-opiskeluoikeus
      {:oid "1.2.246.562.15.22222222223"
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

(t/deftest add-oppija-opiskeluoikeus
  (t/testing "Add oppija and opiskeluoikeus"
    (utils/with-ticket-auth
      ["1.2.246.562.10.222222222222"
       (fn [_ url __]
         (cond
           (> (.indexOf url "oppijanumerorekisteri-service") -1)
           {:status 200
            :body {:oidHenkilo "1.2.246.562.24.111111111111"
                   :hetu "250103-5360"
                   :etunimet "Tero"
                   :kutsumanimi "Tero"
                   :sukunimi "Testaaja"}}
           (> (.indexOf url "/koski/api/opiskeluoikeus") -1)
           {:status 200
            :body opiskeluoikeus-data}))]
      (sut/add-oppija! "1.2.246.562.24.111111111111")
      (sut/add-opiskeluoikeus!
        "1.2.246.562.15.00000000001" "1.2.246.562.24.111111111111")
      (utils/eq
        (sut/get-oppija-by-oid "1.2.246.562.24.111111111111")
        {:oid "1.2.246.562.24.111111111111"
         :nimi "Tero Testaaja"})
      (utils/eq
        (sut/get-opiskeluoikeus-by-oid "1.2.246.562.15.00000000001")
        {:oid "1.2.246.562.15.00000000001"
         :oppija-oid "1.2.246.562.24.111111111111"
         :oppilaitos-oid "1.2.246.562.10.222222222222"
         :tutkinto "Testialan perustutkinto"
         :tutkinto-nimi {:fi "Testialan perustutkinto"
                         :sv "Grundexamen inom testsbranschen"
                         :en "Testing"}
         :osaamisala ""
         :osaamisala-nimi {:fi "" :sv ""}}))))

(t/deftest update-oppija-opiskeluoikeus
  (t/testing "Update oppija and opiskeluoikeus"
    (utils/with-ticket-auth
      ["1.2.246.562.10.222222222222"
       (fn [_ url __]
         (cond
           (> (.indexOf url "oppijanumerorekisteri-service") -1)
           {:status 200
            :body {:oidHenkilo "1.2.246.562.24.111111111111"
                   :hetu "250103-5360"
                   :etunimet "Tero"
                   :kutsumanimi "Tero"
                   :sukunimi "Testaaja"}}
           (> (.indexOf url "/koski/api/opiskeluoikeus") -1)
           {:status 200
            :body opiskeluoikeus-data}))]
      (sut/add-oppija! "1.2.246.562.24.111111111111")
      (sut/add-opiskeluoikeus!
        "1.2.246.562.15.00000000001" "1.2.246.562.24.111111111111")
      (utils/eq
        (sut/get-oppija-by-oid "1.2.246.562.24.111111111111")
        {:oid "1.2.246.562.24.111111111111"
         :nimi "Tero Testaaja"})
      (utils/eq
        (sut/get-opiskeluoikeus-by-oid "1.2.246.562.15.00000000001")
        {:oid "1.2.246.562.15.00000000001"
         :oppija-oid "1.2.246.562.24.111111111111"
         :oppilaitos-oid "1.2.246.562.10.222222222222"
         :tutkinto "Testialan perustutkinto"
         :tutkinto-nimi {:fi "Testialan perustutkinto"
                         :sv "Grundexamen inom testsbranschen"
                         :en "Testing"}
         :osaamisala ""
         :osaamisala-nimi {:fi "" :sv ""}}))

    (utils/with-ticket-auth
      ["1.2.246.562.10.222222222222"
       (fn [_ url __]
         (cond
           (> (.indexOf url "oppijanumerorekisteri-service") -1)
           {:status 200
            :body {:oidHenkilo "1.2.246.562.24.111111111111"
                   :hetu "250103-5360"
                   :etunimet "Tero"
                   :kutsumanimi "Tero"
                   :sukunimi "Testinen"}}
           (> (.indexOf url "/koski/api/opiskeluoikeus") -1)
           {:status 200
            :body {:oppilaitos {:oid "1.2.246.562.10.222222222223"}}}))]
      (sut/update-oppija! "1.2.246.562.24.111111111111")
      (sut/update-opiskeluoikeus!
        "1.2.246.562.15.00000000001" "1.2.246.562.24.111111111111")
      (utils/eq
        (sut/get-oppija-by-oid "1.2.246.562.24.111111111111")
        {:oid "1.2.246.562.24.111111111111"
         :nimi "Tero Testinen"})
      (utils/eq
        (sut/get-opiskeluoikeus-by-oid "1.2.246.562.15.00000000001")
        {:oid "1.2.246.562.15.00000000001"
         :oppija-oid "1.2.246.562.24.111111111111"
         :oppilaitos-oid "1.2.246.562.10.222222222223"
         :tutkinto ""
         :tutkinto-nimi {:fi "" :sv ""}
         :osaamisala ""
         :osaamisala-nimi {:fi "" :sv ""}}))))

(t/deftest set-paattynyt-test
  (t/testing "Setting paattynyt timestamp"
    (db-oppija/insert-oppija {:oid "1.2.246.562.24.11111111112"})
    (db-opiskeluoikeus/insert-opiskeluoikeus
      {:oppija-oid "1.2.246.562.24.11111111112"
       :oid "1.2.246.562.15.22222222223"})
    (let [timestamp (java.sql.Timestamp. 1568367627293)]
      (sut/set-opiskeluoikeus-paattynyt! "1.2.246.562.15.22222222223" timestamp)
      (t/is
        (= (.compareTo
             timestamp
             (get
               (sut/get-opiskeluoikeus-by-oid "1.2.246.562.15.22222222223")
               :paattynyt))
           0)))))
