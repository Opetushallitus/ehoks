(ns oph.ehoks.oppijaindex-test
  (:require [clojure.test :as t]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-opiskeluoikeus]
            [oph.ehoks.db.db-operations.oppija :as db-oppija]
            [oph.ehoks.opiskeluoikeus :as opiskeluoikeus]
            [oph.ehoks.oppijaindex :as sut]
            [oph.ehoks.test-utils :as test-utils])
  (:import (clojure.lang ExceptionInfo)
           (java.time LocalDate)))

(t/use-fixtures :once test-utils/migrate-database)
(t/use-fixtures :each test-utils/empty-database-after-test)

(defn mock-get-hankintakoulutus-oids-by-master-oid
  [oid]
  (case oid
    "1.2.246.562.15.10000000009" ["1.2.246.562.15.10000000009"]
    "1.2.246.562.15.12345678903" []
    "1.2.246.562.15.23456789017" ["1.2.246.562.15.12345678903"
                                  "1.2.246.562.15.23456789017"]
    "1.2.246.562.15.34567890123" ["1.2.246.562.15.34567890123"]))

(def opiskeluoikeus-data
  {:oppilaitos {:oid "1.2.246.562.10.22222222220"}
   :tila {:opiskeluoikeusjaksot
          [{:alku "2023-07-03"
            :tila {:koodiarvo "lasna"
                   :nimi {:fi "Läsnä"}
                   :koodistoUri "koskiopiskeluoikeudentila"
                   :koodistoVersio 1}}]}
   :suoritukset
   [{:koulutusmoduuli
     {:tunniste
      {:koodiarvo "351407"
       :nimi {:fi "Testialan perustutkinto"
              :sv "Grundexamen inom testsbranschen"
              :en "Testing"}}}}]
   :tyyppi {:koodiarvo "ammatillinenkoulutus"}})

(def onr-data
  {:status 200
   :body {:oidHenkilo "1.2.246.562.24.11111111110"
          :hetu "250103-5360"
          :etunimet "Tero"
          :kutsumanimi "Tero"
          :sukunimi "Testaaja"}})

(def onr-data-master
  {:status 200
   :body {:duplicate false
          :oidHenkilo "1.2.246.562.24.11111111123"
          :hetu "250103-5360"
          :etunimet "Matti"
          :kutsumanimi "Masterinho"
          :sukunimi "Masteri"}})

(def onr-data-master-name-changed
  {:status 200
   :body {:duplicate false
          :oidHenkilo "1.2.246.562.24.11111111123"
          :hetu "250103-5360"
          :etunimet "Matti2"
          :kutsumanimi "Masterinho"
          :sukunimi "Masteri"}})

(def onr-data-slave1
  {:status 200
   :body {:oidHenkilo "1.2.246.562.24.30738063716"
          :hetu "250103-5360"
          :etunimet "Laura"
          :kutsumanimi "L"
          :sukunimi "Testaa"}})

(def onr-data-slave2
  {:status 200
   :body {:oidHenkilo "1.2.246.562.24.20043052079"
          :hetu "250103-5360"
          :etunimet "Eero"
          :kutsumanimi "E"
          :sukunimi "Testaa"}})

(def onr-data-slave3
  {:status 200
   :body {:oidHenkilo "1.2.246.562.24.46525423540"
          :hetu "250103-5360"
          :etunimet "Sami"
          :kutsumanimi "S"
          :sukunimi "Testaa"}})

(def onr-slaves-data
  {:status 200
   :body [{:duplicate true,
           :sukupuoli nil,
           :yhteystiedotRyhma [],
           :oppijanumero nil,
           :etunimet "Laura",
           :yksilointiYritetty false,
           :turvakielto false,
           :asiointiKieli nil,
           :syntymaaika nil,
           :kielisyys [],
           :created 1557819277015,
           :henkiloTyyppi "OPPIJA",
           :modified 1652780439987,
           :aidinkieli {:id 1, :kieliKoodi "fi", :kieliTyyppi "suomi"},
           :sukunimi "Testaa",
           :eiSuomalaistaHetua false,
           :kasittelijaOid "1.2.246.562.24.87715614791",
           :kuolinpaiva nil,
           :yksiloity false,
           :id 77449524,
           :kansalaisuus [],
           :hetu nil,
           :oidHenkilo "1.2.246.562.24.30738063716",
           :passivoitu true,
           :kaikkiHetut [],
           :kutsumanimi "Laura",
           :kotikunta nil,
           :vtjsynced nil,
           :yksiloityVTJ false}
          {:duplicate true,
           :sukupuoli "2",
           :yhteystiedotRyhma
           [{:id 742955,
             :ryhmaKuvaus "yhteystietotyyppi1",
             :ryhmaAlkuperaTieto "alkupera2",
             :readOnly false,
             :yhteystieto []}],
           :oppijanumero nil,
           :etunimet "Eero",
           :yksilointiYritetty false,
           :turvakielto false,
           :asiointiKieli {:id 1, :kieliKoodi "fi", :kieliTyyppi "suomi"},
           :syntymaaika "2015-01-01",
           :kielisyys [],
           :created 1421310786739,
           :henkiloTyyppi "OPPIJA",
           :modified 1652780439987,
           :aidinkieli {:id 1, :kieliKoodi "fi", :kieliTyyppi "suomi"},
           :sukunimi "Testaa",
           :eiSuomalaistaHetua false,
           :kasittelijaOid "1.2.246.562.24.87715614791",
           :kuolinpaiva nil,
           :yksiloity false,
           :id 742943,
           :kansalaisuus [{:id 1, :kansalaisuusKoodi "246"}],
           :hetu nil,
           :oidHenkilo "1.2.246.562.24.20043052079",
           :passivoitu false,
           :kaikkiHetut [],
           :kutsumanimi "Laura",
           :kotikunta nil,
           :vtjsynced nil,
           :yksiloityVTJ false}
          {:duplicate true,
           :sukupuoli "2",
           :yhteystiedotRyhma
           [{:id 742961,
             :ryhmaKuvaus "yhteystietotyyppi2",
             :ryhmaAlkuperaTieto "alkupera2",
             :readOnly false,
             :yhteystieto []}],
           :oppijanumero nil,
           :etunimet "Sami",
           :yksilointiYritetty false,
           :turvakielto false,
           :asiointiKieli {:id 1, :kieliKoodi "fi", :kieliTyyppi "suomi"},
           :syntymaaika "2015-01-01",
           :kielisyys [],
           :created 1421310851143,
           :henkiloTyyppi "OPPIJA",
           :modified 1652780439987,
           :aidinkieli {:id 1, :kieliKoodi "fi", :kieliTyyppi "suomi"},
           :sukunimi "Testaa",
           :eiSuomalaistaHetua false,
           :kasittelijaOid "1.2.246.562.24.87715614791",
           :kuolinpaiva nil,
           :yksiloity false,
           :id 742957,
           :kansalaisuus [{:id 1, :kansalaisuusKoodi "246"}],
           :hetu nil,
           :oidHenkilo "1.2.246.562.24.46525423540",
           :passivoitu true,
           :kaikkiHetut [],
           :kutsumanimi "Laura",
           :kotikunta nil,
           :vtjsynced nil,
           :yksiloityVTJ false}]})

(def onr-data-name-changed
  {:status 200
   :body {:oidHenkilo "1.2.246.562.24.11111111110"
          :hetu "250103-5360"
          :etunimet "Tero"
          :kutsumanimi "Tero"
          :sukunimi "Testaaja-Paivitetty"}})

(def tila-data
  {:opiskeluoikeusjaksot
   [{:alku "2018-01-01"
     :tila {:koodiarvo "eronnut"
            :nimi {:fi "Eronnut"}
            :koodistoUri "koskiopiskeluoikeudentila"
            :koodistoVersio 1}}
    {:alku "2019-01-01"
     :tila {:koodiarvo "lasna"
            :nimi {:fi "Läsnä"}
            :koodistoUri "koskiopiskeluoikeudentila"
            :koodistoVersio 1}}
    {:alku "2020-01-01"
     :tila {:koodiarvo "lasna"
            :nimi {:fi "Läsnä"}
            :koodistoUri "koskiopiskeluoikeudentila"
            :koodistoVersio 1}}]})

(t/deftest get-oppijat-without-index
  (t/testing "Get oppijat without index"
    (db-hoks/insert-hoks! {:oppija-oid "1.2.246.562.24.11111111110"
                           :opiskeluoikeus-oid "1.2.246.562.15.22222222220"})
    (db-hoks/insert-hoks! {:oppija-oid "1.2.246.562.24.11111111110"
                           :opiskeluoikeus-oid "1.2.246.562.15.23222222228"})
    (db-hoks/insert-hoks! {:oppija-oid "1.2.246.562.24.12111111119"
                           :opiskeluoikeus-oid "1.2.246.562.15.24222222226"})
    (t/is
      (= (sut/get-oppijat-without-index)
         [{:oppija_oid "1.2.246.562.24.11111111110"}
          {:oppija_oid "1.2.246.562.24.12111111119"}]))
    (t/is
      (= (sut/get-oppijat-without-index-count)
         2))))

(t/deftest get-opiskeluoikeudet-without-index
  (t/testing "Get opiskeluoikeudet without index"
    (db-hoks/insert-hoks! {:oppija-oid "1.2.246.562.24.11111111110"
                           :opiskeluoikeus-oid "1.2.246.562.15.22222222220"})
    (db-hoks/insert-hoks! {:oppija-oid "1.2.246.562.24.11111111110"
                           :opiskeluoikeus-oid "1.2.246.562.15.23222222228"})
    (db-hoks/insert-hoks! {:oppija-oid "1.2.246.562.24.12111111119"
                           :opiskeluoikeus-oid "1.2.246.562.15.24222222226"})
    (t/is
      (= (sut/get-opiskeluoikeudet-without-index)
         [{:oppija_oid "1.2.246.562.24.11111111110"
           :opiskeluoikeus_oid "1.2.246.562.15.22222222220"}
          {:oppija_oid "1.2.246.562.24.11111111110"
           :opiskeluoikeus_oid "1.2.246.562.15.23222222228"}
          {:oppija_oid "1.2.246.562.24.12111111119"
           :opiskeluoikeus_oid "1.2.246.562.15.24222222226"}]))
    (t/is
      (= (sut/get-opiskeluoikeudet-without-index-count)
         3))))

(t/deftest get-opiskeluoikeudet-without-tutkinto-nimi
  (t/testing "Get opiskeluoikeudet without tutkinto_nimi"
    (db-oppija/insert-oppija!
      {:oid "1.2.246.562.24.11111111110"
       :nimi "Testi Oppija"})
    (db-opiskeluoikeus/insert-opiskeluoikeus!
      {:oid "1.2.246.562.15.76000000000"
       :oppija_oid "1.2.246.562.24.11111111110"})
    (db-opiskeluoikeus/insert-opiskeluoikeus!
      {:oid "1.2.246.562.15.76300000007"
       :oppija_oid "1.2.246.562.24.11111111110"})
    (db-opiskeluoikeus/insert-opiskeluoikeus!
      {:oid "1.2.246.562.15.76400000006"
       :oppija_oid "1.2.246.562.24.11111111110"
       :tutkinto-nimi {:fi "Testitutkinto" :sv "test"}})
    (let [results (vec (sort-by :oid
                                (sut/get-opiskeluoikeudet-without-tutkinto)))]
      (t/is (= (get-in results [0 :oid])
               "1.2.246.562.15.76000000000"))
      (t/is (= (get-in results [1 :oid])
               "1.2.246.562.15.76300000007")))))

(t/deftest get-opiskeluoikeudet-without-tutkinto-count
  (t/testing "Get count of opiskeluoikeudet without tutkinto_nimi"
    (db-oppija/insert-oppija!
      {:oid "1.2.246.562.24.11111111110"
       :nimi "Testi Oppija"})
    (db-opiskeluoikeus/insert-opiskeluoikeus!
      {:oid "1.2.246.562.15.76000000000"
       :oppija_oid "1.2.246.562.24.11111111110"})
    (db-opiskeluoikeus/insert-opiskeluoikeus!
      {:oid "1.2.246.562.15.76300000007"
       :oppija_oid "1.2.246.562.24.11111111110"})
    (db-opiskeluoikeus/insert-opiskeluoikeus!
      {:oid "1.2.246.562.15.76400000006"
       :oppija_oid "1.2.246.562.24.11111111110"
       :tutkinto-nimi {:fi "Testitutkinto" :sv "test"}})
    (t/is (= (sut/get-opiskeluoikeudet-without-tutkinto-count) 2))))

(t/deftest get-oppija-opiskeluoikeudet
  (t/testing "Get oppija opiskeluoikeudet"
    (db-oppija/insert-oppija! {:oid "1.2.246.562.24.11111111110"})
    (db-opiskeluoikeus/insert-opiskeluoikeus!
      {:oppija-oid "1.2.246.562.24.11111111110"
       :oid "1.2.246.562.15.22222222220"})
    (db-opiskeluoikeus/insert-opiskeluoikeus!
      {:oppija-oid "1.2.246.562.24.11111111110"
       :oid "1.2.246.562.15.24222222226"})
    (db-oppija/insert-oppija! {:oid "1.2.246.562.24.12111111119"})
    (db-opiskeluoikeus/insert-opiskeluoikeus!
      {:oppija-oid "1.2.246.562.24.12111111119"
       :oid "1.2.246.562.15.23222222228"})
    (t/is
      (= (sut/get-oppija-opiskeluoikeudet "1.2.246.562.24.11111111110")
         [{:oid "1.2.246.562.15.22222222220"
           :oppija-oid "1.2.246.562.24.11111111110"}
          {:oid "1.2.246.562.15.24222222226"
           :oppija-oid "1.2.246.562.24.11111111110"}]))
    (t/is
      (= (sut/get-oppija-opiskeluoikeudet "1.2.246.562.24.12111111119")
         [{:oid "1.2.246.562.15.23222222228"
           :oppija-oid "1.2.246.562.24.12111111119"}]))))

(t/deftest get-oppija-by-oid
  (t/testing "Get oppija by oid"
    (db-oppija/insert-oppija!
      {:oid "1.2.246.562.24.11111111110" :nimi "Test 1"})
    (db-oppija/insert-oppija!
      {:oid "1.2.246.562.24.12111111119" :nimi "Test 2"})
    (t/is (= (sut/get-oppija-by-oid "1.2.246.562.24.11111111110")
             {:oid "1.2.246.562.24.11111111110" :nimi "Test 1"}))
    (t/is (= (sut/get-oppija-by-oid "1.2.246.562.24.12111111119")
             {:oid "1.2.246.562.24.12111111119" :nimi "Test 2"}))))

(t/deftest get-opiskeluoikeus-by-oid!
  (db-oppija/insert-oppija! {:oid "1.2.246.562.24.11111111110" :nimi "Test 1"})
  (db-opiskeluoikeus/insert-opiskeluoikeus!
    {:oid        "1.2.246.562.15.22222222220"
     :oppija_oid "1.2.246.562.24.11111111110"})
  (db-opiskeluoikeus/insert-opiskeluoikeus!
    {:oid        "1.2.246.562.15.23222222228"
     :oppija_oid "1.2.246.562.24.11111111110"})
  (t/testing "Get opiskeluoikeus by oid"
    (t/is (= (sut/get-opiskeluoikeus-by-oid! "1.2.246.562.15.22222222220")
             {:oid        "1.2.246.562.15.22222222220"
              :oppija-oid "1.2.246.562.24.11111111110"}))
    (t/is (= (sut/get-opiskeluoikeus-by-oid! "1.2.246.562.15.23222222228")
             {:oid        "1.2.246.562.15.23222222228"
              :oppija-oid "1.2.246.562.24.11111111110"})))
  (t/testing "Returns nil if no opiskeluoikeus is found in index"
    (t/is (nil? (sut/get-opiskeluoikeus-by-oid!
                  "1.2.246.562.15.22222222221")))))

(t/deftest get-existing-opiskeluoikeus-by-oid!
  (db-oppija/insert-oppija! {:oid "1.2.246.562.24.11111111110" :nimi "Test 1"})
  (db-opiskeluoikeus/insert-opiskeluoikeus!
    {:oid        "1.2.246.562.15.22222222220"
     :oppija_oid "1.2.246.562.24.11111111110"})
  (t/testing "Get existing opiskeluoikeus by oid"
    (t/is (= (sut/get-existing-opiskeluoikeus-by-oid!
               "1.2.246.562.15.22222222220")
             {:oid        "1.2.246.562.15.22222222220"
              :oppija-oid "1.2.246.562.24.11111111110"})))
  (t/testing "Throws if opiskeluoikeus is not found in index"
    (t/is (thrown-with-msg? ExceptionInfo
                            #"`1.2.246.562.15.23222222228` not in index"
                            (sut/get-existing-opiskeluoikeus-by-oid!
                              "1.2.246.562.15.23222222228")))))

(t/deftest add-oppija-opiskeluoikeus
  (t/testing "Add oppija and opiskeluoikeus"
    (test-utils/with-ticket-auth
      ["1.2.246.562.10.22222222220"
       (fn [_ ^String url __]
         (cond
           (> (.indexOf url "oppijanumerorekisteri-service") -1)
           onr-data
           (> (.indexOf url "/koski/api/opiskeluoikeus") -1)
           {:status 200
            :body (assoc
                    opiskeluoikeus-data
                    :alkamispäivä "2023-07-03"
                    :arvioituPäättymispäivä "2025-12-01"
                    :oid "1.2.246.562.15.10000000009")}))]
      (sut/add-oppija! "1.2.246.562.24.11111111110")
      (sut/add-opiskeluoikeus!
        "1.2.246.562.15.10000000009" "1.2.246.562.24.11111111110")
      (test-utils/eq
        (sut/get-oppija-by-oid "1.2.246.562.24.11111111110")
        {:oid "1.2.246.562.24.11111111110"
         :nimi "Tero Testaaja"})
      (test-utils/eq
        (sut/get-opiskeluoikeus-by-oid! "1.2.246.562.15.10000000009")
        {:oid "1.2.246.562.15.10000000009"
         :alkamispaiva (LocalDate/of 2023 7 3)
         :arvioitu-paattymispaiva (LocalDate/of 2025 12 1)
         :oppija-oid "1.2.246.562.24.11111111110"
         :oppilaitos-oid "1.2.246.562.10.22222222220"
         :tutkinto-nimi {:fi "Testialan perustutkinto"
                         :sv "Grundexamen inom testsbranschen"
                         :en "Testing"}
         :osaamisala-nimi {:fi "" :sv ""}}))))

(t/deftest update-oppija-opiskeluoikeus
  (t/testing "Update oppija and opiskeluoikeus"
    (test-utils/with-ticket-auth
      ["1.2.246.562.10.22222222220"
       (fn [_ ^String url __]
         (cond
           (> (.indexOf url "oppijanumerorekisteri-service") -1)
           onr-data
           (> (.indexOf url "/koski/api/opiskeluoikeus") -1)
           {:status 200
            :body (assoc
                    opiskeluoikeus-data
                    :oid "1.2.246.562.15.10000000009")}))]
      (sut/add-oppija! "1.2.246.562.24.11111111110")
      (sut/add-opiskeluoikeus!
        "1.2.246.562.15.10000000009" "1.2.246.562.24.11111111110")
      (test-utils/eq
        (sut/get-oppija-by-oid "1.2.246.562.24.11111111110")
        {:oid "1.2.246.562.24.11111111110"
         :nimi "Tero Testaaja"})
      (test-utils/eq
        (sut/get-opiskeluoikeus-by-oid! "1.2.246.562.15.10000000009")
        {:oid "1.2.246.562.15.10000000009"
         :oppija-oid "1.2.246.562.24.11111111110"
         :oppilaitos-oid "1.2.246.562.10.22222222220"
         :tutkinto-nimi {:fi "Testialan perustutkinto"
                         :sv "Grundexamen inom testsbranschen"
                         :en "Testing"}
         :osaamisala-nimi {:fi "" :sv ""}}))

    ; testataan että jos ei ole alkamispäivää korvataan aina uudella
    (test-utils/with-ticket-auth
      ["1.2.246.562.10.22222222220"
       (fn [_ ___ __]
         {:status 200
          :body (assoc
                  opiskeluoikeus-data
                  :alkamispäivä "2023-07-01"
                  :oid "1.2.246.562.15.10000000009")})]
      (t/is (sut/opiskeluoikeus-information-outdated?!
              "1.2.246.562.15.10000000009"))
      (sut/add-opiskeluoikeus!
        "1.2.246.562.15.10000000009" "1.2.246.562.24.11111111110")
      (test-utils/eq
        (sut/get-opiskeluoikeus-by-oid! "1.2.246.562.15.10000000009")
        {:oid "1.2.246.562.15.10000000009"
         :alkamispaiva (LocalDate/of 2023 7 1)
         :oppija-oid "1.2.246.562.24.11111111110"
         :oppilaitos-oid "1.2.246.562.10.22222222220"
         :tutkinto-nimi {:fi "Testialan perustutkinto"
                         :sv "Grundexamen inom testsbranschen"
                         :en "Testing"}
         :osaamisala-nimi {:fi "" :sv ""}}))

    ; testataan että muuten pidetään indeksissä jo oleva
    (test-utils/with-ticket-auth
      ["1.2.246.562.10.22222222220"
       (fn [_ ___ __]
         {:status 200
          :body (assoc
                  opiskeluoikeus-data
                  :alkamispäivä "2023-07-03"
                  :arvioituPäättymispäivä "2025-10-01"
                  :oid "1.2.246.562.15.10000000009")})]
      (t/is (not (sut/opiskeluoikeus-information-outdated?!
                   "1.2.246.562.15.10000000009")))
      (sut/add-opiskeluoikeus!
        "1.2.246.562.15.10000000009" "1.2.246.562.24.11111111110")
      (test-utils/eq
        (sut/get-opiskeluoikeus-by-oid! "1.2.246.562.15.10000000009")
        {:oid "1.2.246.562.15.10000000009"
         :alkamispaiva (LocalDate/of 2023 7 1)  ;; i.e. no change
         :oppija-oid "1.2.246.562.24.11111111110"
         :oppilaitos-oid "1.2.246.562.10.22222222220"
         :tutkinto-nimi {:fi "Testialan perustutkinto"
                         :sv "Grundexamen inom testsbranschen"
                         :en "Testing"}
         :osaamisala-nimi {:fi "" :sv ""}})
      (db-ops/update! :opiskeluoikeudet
                      {:updated_at (java.time.LocalDate/of 2022 9 1)}
                      ["oid = ?" "1.2.246.562.15.10000000009"])
      (t/is (sut/opiskeluoikeus-information-outdated?!
              "1.2.246.562.15.10000000009")))

    ; odota cachen vanhenemista
    (Thread/sleep 500)

    (test-utils/with-ticket-auth
      ["1.2.246.562.10.22222222220"
       (fn [_ ^String url __]
         (cond
           (> (.indexOf url "oppijanumerorekisteri-service") -1)
           {:status 200
            :body {:oidHenkilo "1.2.246.562.24.11111111110"
                   :hetu "250103-5360"
                   :etunimet "Tero"
                   :kutsumanimi "Tero"
                   :sukunimi "Testinen"}}
           (> (.indexOf url "/koski/api/opiskeluoikeus") -1)
           {:status 200
            :body {:oppilaitos {:oid "1.2.246.562.10.222222222223"}}}))]
      (sut/update-oppija! "1.2.246.562.24.11111111110")
      (sut/update-opiskeluoikeus-without-error-forwarding!
        "1.2.246.562.15.10000000009" "1.2.246.562.24.11111111110")
      (test-utils/eq
        (sut/get-oppija-by-oid "1.2.246.562.24.11111111110")
        {:oid "1.2.246.562.24.11111111110"
         :nimi "Tero Testinen"})
      (test-utils/eq
        (sut/get-opiskeluoikeus-by-oid! "1.2.246.562.15.10000000009")
        {:oid "1.2.246.562.15.10000000009"
         :oppija-oid "1.2.246.562.24.11111111110"
         :oppilaitos-oid "1.2.246.562.10.222222222223"
         :tutkinto-nimi {:fi "" :sv ""}
         :osaamisala-nimi {:fi "" :sv ""}}))))

(t/deftest insert-and-update-oppija-opiskeluoikeus-with-hankintakoulutus
  (t/testing "Insert and update oppija and opiskeluoikeus with hankintakoulutus"
    (test-utils/with-ticket-auth
      ["1.2.246.562.10.22222222220"
       (fn [_ ^String url __]
         (cond
           (> (.indexOf url "oppijanumerorekisteri-service") -1)
           onr-data
           (> (.indexOf url "/koski/api/opiskeluoikeus") -1)
           {:status 200
            :body   (assoc
                      opiskeluoikeus-data
                      :oid "1.2.246.562.15.10000000009")}))]
      (sut/add-oppija! "1.2.246.562.24.11111111110")
      (sut/add-opiskeluoikeus!
        "1.2.246.562.15.10000000009" "1.2.246.562.24.11111111110"))

    ; odota cachen vanhenemista
    (Thread/sleep 500)

    (test-utils/with-ticket-auth
      ["1.2.246.562.10.22222222220"]
      (sut/insert-hankintakoulutus-opiskeluoikeus!
        "1.2.246.562.15.10000000009"
        "1.2.246.562.24.11111111110"
        (assoc
          opiskeluoikeus-data
          :oid "1.2.246.562.15.20000000008"
          :sisältyyOpiskeluoikeuteen
          {:oppilaitos {:oid "1.2.246.562.15.99999123"
                        :oppilaitosnumero
                        {:koodiarvo "10076"}
                        :nimi
                        {:fi "Testi-yliopisto"
                         :sv "Testi-universitet"
                         :en "Testi University"}}
           :oid        "1.2.246.562.15.10000000009"}))
      (test-utils/eq
        (sut/get-opiskeluoikeus-by-oid!
          "1.2.246.562.15.20000000008")
        {:osaamisala-nimi {:fi "", :sv ""},
         :oid "1.2.246.562.15.20000000008",
         :hankintakoulutus-opiskeluoikeus-oid
         "1.2.246.562.15.10000000009",
         :oppilaitos-oid "1.2.246.562.10.22222222220",
         :tutkinto-nimi
         {:en "Testing",
          :fi "Testialan perustutkinto",
          :sv "Grundexamen inom testsbranschen"},
         :oppija-oid "1.2.246.562.24.11111111110",
         :hankintakoulutus-jarjestaja-oid
         "1.2.246.562.15.99999123"})
      (sut/insert-hankintakoulutus-opiskeluoikeus!
        "1.2.246.562.15.10000000009"
        "1.2.246.562.24.11111111110"
        (assoc
          opiskeluoikeus-data
          :oid "1.2.246.562.15.20000000008"
          :sisältyyOpiskeluoikeuteen
          {:oppilaitos {:oid "1.2.246.562.10.99999125000"
                        :oppilaitosnumero
                        {:koodiarvo "10076"}
                        :nimi
                        {:fi "Katujen-yliopisto"
                         :sv "Gatan-universitet"
                         :en "Street University"}}
           :oid        "1.2.246.562.15.10000000009"}))
      (test-utils/eq
        (sut/get-opiskeluoikeus-by-oid!
          "1.2.246.562.15.20000000008")
        {:osaamisala-nimi {:fi "", :sv ""},
         :oid "1.2.246.562.15.20000000008",
         :hankintakoulutus-opiskeluoikeus-oid
         "1.2.246.562.15.10000000009",
         :oppilaitos-oid "1.2.246.562.10.22222222220",
         :tutkinto-nimi
         {:en "Testing",
          :fi "Testialan perustutkinto",
          :sv "Grundexamen inom testsbranschen"},
         :oppija-oid "1.2.246.562.24.11111111110",
         :hankintakoulutus-jarjestaja-oid
         "1.2.246.562.10.99999125000"}))))

(t/deftest set-paattynyt-test
  (t/testing "Setting paattynyt timestamp"
    (db-oppija/insert-oppija! {:oid "1.2.246.562.24.12111111119"})
    (db-opiskeluoikeus/insert-opiskeluoikeus!
      {:oppija-oid "1.2.246.562.24.12111111119"
       :oid "1.2.246.562.15.23222222228"})
    (let [timestamp (java.sql.Timestamp. 1568367627293)]
      (sut/set-opiskeluoikeus-paattynyt! "1.2.246.562.15.23222222228" timestamp)
      (t/is
        (= (.compareTo
             timestamp
             ^java.sql.Timestamp
             (get
               (sut/get-opiskeluoikeus-by-oid! "1.2.246.562.15.23222222228")
               :paattynyt))
           0)))))

(t/deftest oppija-opiskeluoikeus-match-test
  (with-redefs [oph.ehoks.config/config {:enforce-opiskeluoikeus-match? true}]
    (let [opiskeluoikeudet [{:oid "1.2.246.562.15.55003456345"
                             :oppilaitos {:oid "1.2.246.562.10.12000000005"
                                          :nimi {:fi "TestiFi"
                                                 :sv "TestiSv"
                                                 :en "TestiEn"}}
                             :alkamispäivä "2020-03-12"}]]

      (t/testing "Opintooikeus belonging to oppija return true"
        (t/is
          (sut/oppija-opiskeluoikeus-match?
            opiskeluoikeudet
            "1.2.246.562.15.55003456345")))

      (t/testing "Opintooikeus not belonging to oppija return false"
        (t/is
          (not
            (sut/oppija-opiskeluoikeus-match?
              opiskeluoikeudet
              "1.2.246.562.15.55003456347")))))))

(t/deftest hankintakoulutus-opiskeluoikeus-test
  (t/testing "Save opiskeluoikeus with sisältyyOpiskeluoikeuteen information"
    (test-utils/with-ticket-auth
      ["1.2.246.562.10.22222222220"
       (fn [_ ^String url __]
         (cond
           (> (.indexOf url "oppijanumerorekisteri-service") -1)
           onr-data
           (> (.indexOf url "/koski/api/opiskeluoikeus") -1)
           {:status 200
            :body (assoc
                    opiskeluoikeus-data
                    :sisältyyOpiskeluoikeuteen
                    {:oppilaitos {:oppilaitosnumero
                                  {:koodiarvo "10076"}
                                  :nimi
                                  {:fi "Testi-yliopisto"
                                   :sv "Testi-universitetet"
                                   :en "Testi University"}}
                     :oid "1.2.246.562.15.99999123"})}))]
      (t/is
        (thrown-with-msg?
          ExceptionInfo
          (re-pattern (str "Opiskeluoikeus `1.2.246.562.15.50000000005` "
                           "sisältyy toiseen opiskeluoikeuteen."))
          (sut/add-opiskeluoikeus!
            "1.2.246.562.15.50000000005" "1.2.246.562.24.11111111110")))
      (t/is
        (nil? (sut/get-opiskeluoikeus-by-oid! "1.2.246.562.15.10000000009"))))))

(t/deftest hankintakoulutus-filter-test
  (t/testing "Existing hankintakoulutus is filtered from opiskeluoikeudet"
    (let [opiskeluoikeudet [(assoc
                              opiskeluoikeus-data
                              :sisältyyOpiskeluoikeuteen
                              {:oppilaitos {:oppilaitosnumero
                                            {:koodiarvo "10076"}
                                            :nimi
                                            {:fi "Testi-yliopisto"
                                             :sv "Testi-universitetet"
                                             :en "Testi University"}}
                               :oid "1.2.246.562.15.99999123"})]]
      (t/is
        (= (count (sut/filter-hankintakoulutukset-for-current-opiskeluoikeus
                    opiskeluoikeudet "1.2.246.562.15.99999123")) 1))))

  (t/testing "Hankintakoulutus is filtered from opiskeluoikeudet
              if does not match to opiskeluoikeus-oid"
    (let [opiskeluoikeudet [(assoc
                              opiskeluoikeus-data
                              :sisältyyOpiskeluoikeuteen
                              {:oppilaitos {:oppilaitosnumero
                                            {:koodiarvo "10076"}
                                            :nimi
                                            {:fi "Testi-yliopisto"
                                             :sv "Testi-universitetet"
                                             :en "Testi University"}}
                               :oid "1.2.246.562.15.99999123"})]]
      (t/is
        (= (count (sut/filter-hankintakoulutukset-for-current-opiskeluoikeus
                    opiskeluoikeudet "1.2.246.562.15.99999124")) 0))))

  (t/testing "Empty list returned if no hankintakoulutus in opiskeluoikeudet"
    (let [opiskeluoikeudet [opiskeluoikeus-data]]
      (t/is
        (= (count (sut/filter-hankintakoulutukset-for-current-opiskeluoikeus
                    opiskeluoikeudet "1.2.246.562.15.99999123")) 0)))))

(t/deftest validate-opiskeluoikeus-status-test
  (with-redefs [oph.ehoks.config/config
                {:prevent-finished-opiskeluoikeus-updates? true}]

    (t/testing "Active opiskeluoikeus returns true"
      (test-utils/with-ticket-auth
        ["1.2.246.562.10.22222222220"
         (fn [_ ^String url __]
           (cond
             (> (.indexOf url "oppijanumerorekisteri-service") -1)
             onr-data
             (> (.indexOf url "/koski/api/opiskeluoikeus") -1)
             {:status 200
              :body (assoc
                      opiskeluoikeus-data
                      :oid "1.2.246.562.15.55003456344"
                      :tila {:opiskeluoikeusjaksot
                             [{:alku "2018-01-01"
                               :tila {:koodiarvo "lasna"
                                      :nimi {:fi "Läsnä"}
                                      :koodistoUri "koskiopiskeluoikeudentila"
                                      :koodistoVersio 1}}]})}))]

        (t/is (opiskeluoikeus/still-active? "1.2.246.562.15.55003456344"))))

    (t/testing "Finished opiskeluoikeus returns false"
      (test-utils/with-ticket-auth
        ["1.2.246.562.10.22222222220"
         (fn [_ ^String url __]
           (cond
             (> (.indexOf url "oppijanumerorekisteri-service") -1)
             onr-data
             (> (.indexOf url "/koski/api/opiskeluoikeus") -1)
             {:status 200
              :body (assoc
                      opiskeluoikeus-data
                      :oid "1.2.246.562.15.55003456345"
                      :tila {:opiskeluoikeusjaksot
                             [{:alku "2018-01-01"
                               :tila {:koodiarvo "eronnut"
                                      :nimi {:fi "Eronnut"}
                                      :koodistoUri "koskiopiskeluoikeudentila"
                                      :koodistoVersio 1}}]})}))]

        (t/is
          (not (opiskeluoikeus/still-active? "1.2.246.562.15.55003456345")))))

    (t/testing "Active opiskeluoikeus matching hoks is filtered from multiple"
      (test-utils/with-ticket-auth
        ["1.2.246.562.10.22222222220"
         (fn [_ ^String url __]
           (cond
             (> (.indexOf url "oppijanumerorekisteri-service") -1)
             onr-data
             (> (.indexOf url "/koski/api/opiskeluoikeus") -1)
             {:status 200
              :body (assoc
                      opiskeluoikeus-data
                      :oid "1.2.246.562.15.55003456346"
                      :tila tila-data)}))]

        (t/is (opiskeluoikeus/still-active? "1.2.246.562.15.55003456346"))))

    (t/testing "Active opiskeluoikeus is parsed from hoks and opiskeluoikeudet"
      (test-utils/with-ticket-auth
        ["1.2.246.562.10.22222222220"
         (fn [_ ^String url __]
           (cond
             (> (.indexOf url "oppijanumerorekisteri-service") -1)
             onr-data
             (> (.indexOf url "/koski/api/opiskeluoikeus") -1)
             {:status 200
              :body (assoc
                      opiskeluoikeus-data
                      :oid "1.2.246.562.15.55003456346"
                      :tila tila-data)}))]

        (t/is
          (opiskeluoikeus/still-active?
            {:id 1234
             :opiskeluoikeus-oid "1.2.246.562.15.55003456346"}
            (list
              (assoc
                opiskeluoikeus-data
                :oid "1.2.246.562.15.55003456346"
                :tila tila-data)))))))

  (t/testing "Without feature flag enabled always returns true"
    (t/is (opiskeluoikeus/still-active? "not an opiskeluoikeus-oid"))
    (t/is (opiskeluoikeus/still-active? "not a hoks"
                                        "not a list of opiskeluoikeus"))))

(t/deftest delete-opiskeluoikeus-from-index
  (t/testing "Delete opiskeluoikeus from index"
    (test-utils/with-ticket-auth
      ["1.2.246.562.10.22222222220"
       (fn [_ ^String url __]
         (cond
           (> (.indexOf url (str "oppijanumerorekisteri-service/henkilo/"
                                 "1.2.246.562.24.11111111110")) -1)
           onr-data
           (> (.indexOf url "/koski/api/opiskeluoikeus") -1)
           {:status 200
            :body (assoc
                    opiskeluoikeus-data
                    :oid "1.2.246.562.15.10000000009")}))]
      (sut/add-oppija! "1.2.246.562.24.11111111110")
      (sut/add-opiskeluoikeus!
        "1.2.246.562.15.10000000009" "1.2.246.562.24.11111111110")
      (test-utils/eq
        (sut/get-oppija-by-oid "1.2.246.562.24.11111111110")
        {:oid "1.2.246.562.24.11111111110"
         :nimi "Tero Testaaja"})
      (test-utils/eq
        (sut/get-opiskeluoikeus-by-oid! "1.2.246.562.15.10000000009")
        {:oid "1.2.246.562.15.10000000009"
         :oppija-oid "1.2.246.562.24.11111111110"
         :oppilaitos-oid "1.2.246.562.10.22222222220"
         :tutkinto-nimi {:fi "Testialan perustutkinto"
                         :sv "Grundexamen inom testsbranschen"
                         :en "Testing"}
         :osaamisala-nimi {:fi "" :sv ""}})

      (db-opiskeluoikeus/delete-opiskeluoikeus-from-index!
        "1.2.246.562.15.10000000009")
      (test-utils/eq
        (sut/get-opiskeluoikeus-by-oid! "1.2.246.562.15.10000000009")
        nil))))

(t/deftest onr-modify-name-change
  (t/testing "onr-modified call has different name compared to oppijaindex"
    (test-utils/with-ticket-auth
      ["1.2.246.562.10.22222222220"
       (fn [_ ^String url __]
         (when
          (> (.indexOf url "oppijanumerorekisteri-service") -1)
           onr-data))]
      (sut/add-oppija! "1.2.246.562.24.11111111110")
      (test-utils/eq
        (sut/get-oppija-by-oid "1.2.246.562.24.11111111110")
        {:oid "1.2.246.562.24.11111111110"
         :nimi "Tero Testaaja"}))
    (test-utils/with-ticket-auth
      ["1.2.246.562.10.22222222220"
       (fn [_ ^String url __]
         (when
          (> (.indexOf url "oppijanumerorekisteri-service") -1)
           onr-data-name-changed))]
      (sut/handle-onrmodified "1.2.246.562.24.11111111110")
      (test-utils/eq
        (sut/get-oppija-by-oid "1.2.246.562.24.11111111110")
        {:oid "1.2.246.562.24.11111111110"
         :nimi "Tero Testaaja-Paivitetty"}))))

(t/deftest onr-modify-slaves-found
  (t/testing "onr-modified call with master oid that is not found in
  oppijat-table. /slaves call from ONR returns three oppijas and they all have
  opiskeluoikeus and hoks."
    (test-utils/with-ticket-auth
      ["1.2.246.562.10.22222222220"
       (fn [_ ^String url __]
         (cond
           (> (.indexOf url (str "oppijanumerorekisteri-service/henkilo/"
                                 "1.2.246.562.24.30738063716")) -1)
           onr-data-slave1
           (> (.indexOf url (str "oppijanumerorekisteri-service/henkilo/"
                                 "1.2.246.562.24.20043052079")) -1)
           onr-data-slave2
           (> (.indexOf url (str "oppijanumerorekisteri-service/henkilo/"
                                 "1.2.246.562.24.46525423540")) -1)
           onr-data-slave3
           (> (.indexOf url (str "oppijanumerorekisteri-service/henkilo/"
                                 "1.2.246.562.24.11111111123")) -1)
           onr-data-master
           (> (.indexOf url (str "/koski/api/opiskeluoikeus/"
                                 "1.2.246.562.15.40000000006")) -1)
           {:status 200
            :body (assoc
                    opiskeluoikeus-data
                    :oid "1.2.246.562.15.40000000006")}
           (> (.indexOf url (str "/koski/api/opiskeluoikeus/"
                                 "1.2.246.562.15.10000000009")) -1)
           {:status 200
            :body (assoc
                    opiskeluoikeus-data
                    :oid "1.2.246.562.15.10000000009")}
           (> (.indexOf url (str "/koski/api/opiskeluoikeus/"
                                 "1.2.246.562.15.20000000008")) -1)
           {:status 200
            :body (assoc
                    opiskeluoikeus-data
                    :oid "1.2.246.562.15.20000000008")}
           (> (.indexOf url (str "/koski/api/opiskeluoikeus/"
                                 "1.2.246.562.15.30000000007")) -1)
           {:status 200
            :body (assoc
                    opiskeluoikeus-data
                    :oid "1.2.246.562.15.30000000007")}))]
      (sut/add-oppija! "1.2.246.562.24.30738063716")
      (sut/add-oppija! "1.2.246.562.24.20043052079")
      (sut/add-oppija! "1.2.246.562.24.11111111123")
      (sut/add-oppija! "1.2.246.562.24.46525423540")
      (sut/add-opiskeluoikeus!
        "1.2.246.562.15.10000000009" "1.2.246.562.24.30738063716")
      (sut/add-opiskeluoikeus!
        "1.2.246.562.15.20000000008" "1.2.246.562.24.20043052079")
      (sut/add-opiskeluoikeus!
        "1.2.246.562.15.40000000006" "1.2.246.562.24.11111111123")
      (sut/add-opiskeluoikeus!
        "1.2.246.562.15.30000000007" "1.2.246.562.24.46525423540")
      (db-hoks/insert-hoks! {:oppija-oid "1.2.246.562.24.30738063716"
                             :opiskeluoikeus-oid "1.2.246.562.15.10000000009"})
      (db-hoks/insert-hoks! {:oppija-oid "1.2.246.562.24.20043052079"
                             :opiskeluoikeus-oid "1.2.246.562.15.20000000008"})
      (db-hoks/insert-hoks! {:opiskeluoikeus-oid "1.2.246.562.15.40000000006"
                             :oppija-oid "1.2.246.562.24.11111111123"})
      (db-hoks/insert-hoks! {:oppija-oid "1.2.246.562.24.46525423540"
                             :opiskeluoikeus-oid "1.2.246.562.15.30000000007"})
      (test-utils/eq
        (sut/get-oppija-by-oid "1.2.246.562.24.30738063716")
        {:oid "1.2.246.562.24.30738063716"
         :nimi "Laura Testaa"})
      (test-utils/eq
        (sut/get-oppija-by-oid "1.2.246.562.24.20043052079")
        {:oid "1.2.246.562.24.20043052079"
         :nimi "Eero Testaa"})
      (test-utils/eq
        (sut/get-oppija-by-oid "1.2.246.562.24.46525423540")
        {:oid "1.2.246.562.24.46525423540"
         :nimi "Sami Testaa"})
      (test-utils/eq
        (sut/get-oppija-by-oid "1.2.246.562.24.11111111123")
        {:oid "1.2.246.562.24.11111111123"
         :nimi "Matti Masteri"})
      (test-utils/eq
        (sut/get-opiskeluoikeus-by-oid! "1.2.246.562.15.10000000009")
        {:oid "1.2.246.562.15.10000000009"
         :oppija-oid "1.2.246.562.24.30738063716"
         :oppilaitos-oid "1.2.246.562.10.22222222220"
         :tutkinto-nimi {:fi "Testialan perustutkinto"
                         :sv "Grundexamen inom testsbranschen"
                         :en "Testing"}
         :osaamisala-nimi {:fi "" :sv ""}})
      (test-utils/eq
        (sut/get-opiskeluoikeus-by-oid! "1.2.246.562.15.20000000008")
        {:oid "1.2.246.562.15.20000000008"
         :oppija-oid "1.2.246.562.24.20043052079"
         :oppilaitos-oid "1.2.246.562.10.22222222220"
         :tutkinto-nimi {:fi "Testialan perustutkinto"
                         :sv "Grundexamen inom testsbranschen"
                         :en "Testing"}
         :osaamisala-nimi {:fi "" :sv ""}})
      (test-utils/eq
        (sut/get-opiskeluoikeus-by-oid! "1.2.246.562.15.30000000007")
        {:oid "1.2.246.562.15.30000000007"
         :oppija-oid "1.2.246.562.24.46525423540"
         :oppilaitos-oid "1.2.246.562.10.22222222220"
         :tutkinto-nimi {:fi "Testialan perustutkinto"
                         :sv "Grundexamen inom testsbranschen"
                         :en "Testing"}
         :osaamisala-nimi {:fi "" :sv ""}}))
    (test-utils/with-ticket-auth
      ["1.2.246.562.10.22222222220"
       (fn [_ ^String url __]
         (cond
           (> (.indexOf url (str "slaves")) -1)
           onr-slaves-data
           (> (.indexOf url (str "oppijanumerorekisteri-service"
                                 "/henkilo"
                                 "/1.2.246.562.24.11111111123")) -1)
           onr-data-master-name-changed))]
      (sut/handle-onrmodified "1.2.246.562.24.11111111123")
      (test-utils/eq
        (sut/get-oppija-by-oid "1.2.246.562.24.11111111123")
        {:oid "1.2.246.562.24.11111111123"
         :nimi "Matti2 Masteri"})
      (test-utils/eq
        (sut/get-opiskeluoikeus-by-oid! "1.2.246.562.15.10000000009")
        {:oid "1.2.246.562.15.10000000009"
         :oppija-oid "1.2.246.562.24.11111111123"
         :oppilaitos-oid "1.2.246.562.10.22222222220"
         :tutkinto-nimi {:fi "Testialan perustutkinto"
                         :sv "Grundexamen inom testsbranschen"
                         :en "Testing"}
         :osaamisala-nimi {:fi "" :sv ""}})
      (test-utils/eq
        (sut/get-opiskeluoikeus-by-oid! "1.2.246.562.15.20000000008")
        {:oid "1.2.246.562.15.20000000008"
         :oppija-oid "1.2.246.562.24.11111111123"
         :oppilaitos-oid "1.2.246.562.10.22222222220"
         :tutkinto-nimi {:fi "Testialan perustutkinto"
                         :sv "Grundexamen inom testsbranschen"
                         :en "Testing"}
         :osaamisala-nimi {:fi "" :sv ""}})
      (test-utils/eq
        (sut/get-opiskeluoikeus-by-oid! "1.2.246.562.15.30000000007")
        {:oid "1.2.246.562.15.30000000007"
         :oppija-oid "1.2.246.562.24.11111111123"
         :oppilaitos-oid "1.2.246.562.10.22222222220"
         :tutkinto-nimi {:fi "Testialan perustutkinto"
                         :sv "Grundexamen inom testsbranschen"
                         :en "Testing"}
         :osaamisala-nimi {:fi "" :sv ""}})
      (test-utils/eq
        (sut/get-oppija-by-oid "1.2.246.562.24.30738063716")
        nil)
      (test-utils/eq
        (sut/get-oppija-by-oid "1.2.246.562.24.20043052079")
        nil)
      (test-utils/eq
        (sut/get-oppija-by-oid "1.2.246.562.24.46525423540")
        nil)
      (test-utils/eq
        (empty?
          (db-hoks/select-hoks-by-oppija-oid "1.2.246.562.24.30738063716"))
        true)
      (test-utils/eq
        (empty?
          (db-hoks/select-hoks-by-oppija-oid "1.2.246.562.24.20043052079"))
        true)
      (test-utils/eq
        (empty?
          (db-hoks/select-hoks-by-oppija-oid "1.2.246.562.24.46525423540"))
        true)
      (test-utils/eq
        (sort-by :id (map #(select-keys % [:id :oppija-oid])
                          (db-hoks/select-hoks-by-oppija-oid
                            "1.2.246.562.24.11111111123")))
        [{:id 1, :oppija-oid "1.2.246.562.24.11111111123"}
         {:id 2, :oppija-oid "1.2.246.562.24.11111111123"}
         {:id 3, :oppija-oid "1.2.246.562.24.11111111123"}
         {:id 4, :oppija-oid "1.2.246.562.24.11111111123"}]))))
