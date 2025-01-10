(ns oph.ehoks.hoks.hoks-save-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.postgresql.aiemmin-hankitut :as db-ah]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.external.organisaatio-test :as organisaatio-test]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.hoks.aiemmin-hankitut :as ah]
            [oph.ehoks.hoks.handler :as hoks-handler]
            [oph.ehoks.hoks.hankittavat :as ha]
            [oph.ehoks.hoks.opiskeluvalmiuksia-tukevat :as ot]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.opiskeluoikeus-test :as oo-test]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.opiskelija :as opalaute]
            [oph.ehoks.palaute.tapahtuma :as tapahtumat]
            [oph.ehoks.test-utils :as test-utils :refer [eq]]
            [oph.ehoks.utils.date :as date]
            [schema.core :as s])
  (:import [java.time LocalDate]))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-database-after-test)

(def ahato-data
  [{:valittu-todentamisen-prosessi-koodi-versio 1
    :tutkinnon-osa-koodi-versio 2
    :valittu-todentamisen-prosessi-koodi-uri
    "osaamisentodentamisenprosessi_3"
    :tutkinnon-osa-koodi-uri "tutkinnonosat_100022"
    :tarkentavat-tiedot-osaamisen-arvioija
    {:lahetetty-arvioitavaksi (java.time.LocalDate/of 2019 3 18)
     :aiemmin-hankitun-osaamisen-arvioijat
     [{:nimi "Erkki Esimerkki"
       :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921626"}}]}
    :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921410"
    :tarkentavat-tiedot-naytto
    [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_fy"
                    :koodi-versio 1}]
      :koulutuksen-jarjestaja-osaamisen-arvioijat
      [{:nimi "Aapo Arvioija"
        :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921675"}}]
      :jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921683"}
      :nayttoymparisto {:nimi "Toinen Esimerkki Oyj"
                        :y-tunnus "1234562-0"
                        :kuvaus "Testiyrityksen testiosasostalla"}
      :tyoelama-osaamisen-arvioijat [{:nimi "Teppo Työmies"
                                      :organisaatio
                                      {:nimi "Testiyrityksen Sisar Oy"
                                       :y-tunnus "1234563-9"}}]
      :sisallon-kuvaus ["Tutkimustyö"
                        "Raportointi"]
      :yksilolliset-kriteerit ["kriteeri 1" "kriteeri2"]
      :alku (java.time.LocalDate/of 2019 2 9)
      :loppu (java.time.LocalDate/of 2019 1 10)}]}])

(def ahpto-data
  [{:valittu-todentamisen-prosessi-koodi-versio 2
    :laajuus 30
    :nimi "Testiopintojakso"
    :tavoitteet-ja-sisallot "Tavoitteena on testioppiminen."
    :valittu-todentamisen-prosessi-koodi-uri
    "osaamisentodentamisenprosessi_0001"
    :amosaa-tunniste "12345"
    :tarkentavat-tiedot-osaamisen-arvioija
    {:lahetetty-arvioitavaksi
     (java.time.LocalDate/of 2019 1 20)
     :aiemmin-hankitun-osaamisen-arvioijat
     [{:nimi "Aarne Arvioija"
       :organisaatio {:oppilaitos-oid
                      "1.2.246.562.10.54453923416"}}]}
    :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453945328"
    :vaatimuksista-tai-tavoitteista-poikkeaminen
    "Ei poikkeamaa."
    :tarkentavat-tiedot-naytto
    [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_li"
                    :koodi-versio 6}]
      :koulutuksen-jarjestaja-osaamisen-arvioijat
      [{:nimi "Teuvo Testaaja"
        :organisaatio {:oppilaitos-oid
                       "1.2.246.562.10.12346234698"}}]
      :jarjestaja {:oppilaitos-oid
                   "1.2.246.562.10.93270534261"}

      :nayttoymparisto {:nimi "Testi Oyj"
                        :y-tunnus "1289211-4"
                        :kuvaus "Testiyhtiö"}
      :tyoelama-osaamisen-arvioijat
      [{:nimi "Terttu Testihenkilö"
        :organisaatio {:nimi "Testi Oyj"
                       :y-tunnus "1289211-4"}}]
      :sisallon-kuvaus ["Testauksen suunnittelu"
                        "Jokin toinen testi"]
      :yksilolliset-kriteerit ["kriteeri 1" "kriteeri2"]
      :alku (java.time.LocalDate/of 2019 2 1)
      :loppu (java.time.LocalDate/of 2019 2 1)}]}])

(def hao-data
  [{:tutkinnon-osa-koodi-uri "tutkinnonosat_102499"
    :tutkinnon-osa-koodi-versio 4
    :vaatimuksista-tai-tavoitteista-poikkeaminen
    "Ei poikkeamia."
    :osaamisen-osoittaminen
    [{:jarjestaja
      {:oppilaitos-oid "1.2.246.562.10.54453924331"}
      :nayttoymparisto {:nimi "Testiympäristö 2"
                        :y-tunnus "1234567-1"
                        :kuvaus "Testi test"}
      :sisallon-kuvaus ["Testaus"]
      :yksilolliset-kriteerit ["kriteeri 1" "kriteeri2"]
      :koulutuksen-jarjestaja-osaamisen-arvioijat
      [{:nimi "Timo Testaaja"
        :organisaatio
        {:oppilaitos-oid "1.2.246.562.10.54452521336"}}]
      :tyoelama-osaamisen-arvioijat
      [{:nimi "Taneli Työmies"
        :organisaatio {:nimi "Tanelin Paja Ky"
                       :y-tunnus "1234561-2"}}]
      :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_kl"
                    :koodi-versio 3}]
      :alku (java.time.LocalDate/of 2019 3 10)
      :loppu (java.time.LocalDate/of 2019 3 19)}]
    :osaamisen-hankkimistavat
    [{:jarjestajan-edustaja
      {:nimi "Ville Valvoja"
       :rooli "Valvojan apulainen"
       :oppilaitos-oid "1.2.246.562.10.54451211343"}
      :osaamisen-hankkimistapa-koodi-uri
      "osaamisenhankkimistapa_oppisopimus"
      :osaamisen-hankkimistapa-koodi-versio 2
      :tyopaikalla-jarjestettava-koulutus
      {:vastuullinen-tyopaikka-ohjaaja
       {:nimi "Aimo Ohjaaja"
        :sahkoposti "aimo.ohjaaja@esimerkki2.com"}
       :tyopaikan-nimi "Ohjausyhtiö Oy"
       :tyopaikan-y-tunnus "1234569-8"
       :keskeiset-tyotehtavat ["Testitehtävä"]}
      :muut-oppimisymparistot
      [{:oppimisymparisto-koodi-uri "oppimisymparistot_0002"
        :oppimisymparisto-koodi-versio 1
        :alku (java.time.LocalDate/of 2019 1 13)
        :loppu (java.time.LocalDate/of 2019 2 19)}]
      :keskeytymisajanjaksot
      [{:alku (java.time.LocalDate/of 2021 9 20)
        :loppu (java.time.LocalDate/of 2021 9 28)}
       {:alku (java.time.LocalDate/of 2021 9 29)}]
      :ajanjakson-tarkenne "Ei tarkennettavia asioita"
      :osa-aikaisuustieto 50
      :hankkijan-edustaja
      {:nimi "Heikki Hankkija"
       :rooli "Opettaja"
       :oppilaitos-oid "1.2.246.562.10.54452422428"}
      :alku (java.time.LocalDate/of 2019 1 11)
      :loppu (java.time.LocalDate/of 2019 3 14)
      :yksiloiva-tunniste "1234567890"}]
    :koulutuksen-jarjestaja-oid "1.2.246.562.10.54411232223"}])

(def oto-data
  [{:nimi "Testiopintojakso"
    :kuvaus "Testi"
    :alku (java.time.LocalDate/of 2018 06 01)
    :loppu (java.time.LocalDate/of 2018 07 31)}])

(def ahyto-data
  [{:valittu-todentamisen-prosessi-koodi-uri
    "osaamisentodentamisenprosessi_0001"
    :valittu-todentamisen-prosessi-koodi-versio 3
    :tutkinnon-osa-koodi-versio 2
    :tutkinnon-osa-koodi-uri "tutkinnonosat_10203"
    :tarkentavat-tiedot-osaamisen-arvioija
    {:lahetetty-arvioitavaksi
     (java.time.LocalDate/of 2016 2 29)
     :aiemmin-hankitun-osaamisen-arvioijat
     [{:nimi "Arttu Arvioija"
       :organisaatio {:oppilaitos-oid
                      "1.2.246.562.10.54453931310"}}]}
    :osa-alueet
    [{:osa-alue-koodi-uri "ammatillisenoppiaineet_bi"
      :osa-alue-koodi-versio 4
      :koulutuksen-jarjestaja-oid
      "1.2.246.562.10.54453923572"
      :vaatimuksista-tai-tavoitteista-poikkeaminen
      "Testaus ei kuulu vaatimuksiin."
      :valittu-todentamisen-prosessi-koodi-uri
      "osaamisentodentamisenprosessi_0003"
      :valittu-todentamisen-prosessi-koodi-versio 4
      :tarkentavat-tiedot-osaamisen-arvioija
      {:lahetetty-arvioitavaksi (java.time.LocalDate/of 2020 5 25)
       :aiemmin-hankitun-osaamisen-arvioijat
       [{:nimi "Tama tyyppi"
         :organisaatio {:oppilaitos-oid
                        "1.2.246.562.10.54453931444"}}]}
      :tarkentavat-tiedot-naytto
      [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_bi"
                      :koodi-versio 3}]
        :koulutuksen-jarjestaja-osaamisen-arvioijat
        [{:nimi "Teppo Testaaja"
          :organisaatio {:oppilaitos-oid
                         "1.2.246.562.10.54539267903"}}]
        :jarjestaja {:oppilaitos-oid
                     "1.2.246.562.10.55890967908"}

        :nayttoymparisto {:nimi "Ab Yhtiö Oy"
                          :y-tunnus "1234128-3"
                          :kuvaus "Testi"}
        :tyoelama-osaamisen-arvioijat
        [{:nimi "Tellervo Työntekijä"
          :organisaatio {:nimi "Ab Yhtiö Oy"
                         :y-tunnus "1234128-3"}}]
        :sisallon-kuvaus ["Testaus" "Kirjoitus"]
        :yksilolliset-kriteerit ["kriteeri 1" "kriteeri2"]
        :alku (java.time.LocalDate/of 2019 1 4)
        :loppu (java.time.LocalDate/of 2019 3 1)}]}]
    :koulutuksen-jarjestaja-oid "1.2.246.562.10.13490590901"
    :tarkentavat-tiedot-naytto
    [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_ma"
                    :koodi-versio 6}]
      :koulutuksen-jarjestaja-osaamisen-arvioijat
      [{:nimi "Erkki Esimerkkitestaaja"
        :organisaatio {:oppilaitos-oid
                       "1.2.246.562.10.13490579094"}}]
      :jarjestaja {:oppilaitos-oid
                   "1.2.246.562.10.93270579092"}
      :nayttoymparisto {:nimi "Testi Oy"
                        :y-tunnus "1289234-1"
                        :kuvaus "Testiyhtiö"}
      :tyoelama-osaamisen-arvioijat
      [{:nimi "Tapio Testihenkilö"
        :organisaatio {:nimi "Testi Oy"
                       :y-tunnus "1289234-1"}}]
      :sisallon-kuvaus ["Testauksen suunnittelu"
                        "Jokin toinen testi"]
      :yksilolliset-kriteerit ["kriteeri 1" "kriteeri2"]
      :alku (java.time.LocalDate/of 2019 3 1)
      :loppu (java.time.LocalDate/of 2019 3 1)}]}])

(def hpto-data
  [{:koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
    :tavoitteet-ja-sisallot "Testitavoite"
    :nimi "Orientaatio alaan"
    :osaamisen-hankkimistavat
    [{:jarjestajan-edustaja
      {:nimi "Erkki Edustaja"
       :rooli "Valvoja"
       :oppilaitos-oid "1.2.246.562.10.54453921340"}
      :osaamisen-hankkimistapa-koodi-uri
      "osaamisenhankkimistapa_oppisopimus"
      :osaamisen-hankkimistapa-koodi-versio 1
      :tyopaikalla-jarjestettava-koulutus
      {:vastuullinen-tyopaikka-ohjaaja
       {:nimi "Olli Ohjaaja"
        :sahkoposti "olli.ohjaaja@esimerkki.com"}
       :tyopaikan-nimi "Ohjaus Oy"
       :tyopaikan-y-tunnus "1234556-7"
       :keskeiset-tyotehtavat ["Hälytysten valvonta"
                               "Vuoronvaihdon tarkistukset"]}
      :muut-oppimisymparistot
      [{:oppimisymparisto-koodi-uri "oppimisymparistot_0001"
        :oppimisymparisto-koodi-versio 1
        :alku (java.time.LocalDate/of 2019 1 13)
        :loppu (java.time.LocalDate/of 2019 2 19)}]
      :keskeytymisajanjaksot
      [{:alku (java.time.LocalDate/of 2021 9 20)
        :loppu (java.time.LocalDate/of 2021 9 28)}]
      :ajanjakson-tarkenne "Ei tarkennettavaa"
      :hankkijan-edustaja
      {:nimi "Harri Hankkija"
       :rooli "Opettaja"
       :oppilaitos-oid "1.2.246.562.10.54453921350"}
      :alku (java.time.LocalDate/of 2019 2 10)
      :loppu (java.time.LocalDate/of 2019 2 15)
      :yksiloiva-tunniste "abcd"}]
    :osaamisen-osoittaminen
    [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921330"}
      :nayttoymparisto {:nimi "Testiympäristö"
                        :y-tunnus "1234567-1"
                        :kuvaus "Test"}
      :sisallon-kuvaus ["Renkaanvaihto"
                        "Tuulilasin vaihto"]
      :yksilolliset-kriteerit ["kriteeri 1" "kriteeri2"]
      :koulutuksen-jarjestaja-osaamisen-arvioijat
      [{:nimi "Terttu Testaaja"
        :organisaatio
        {:oppilaitos-oid "1.2.246.562.10.54453921332"}}]
      :tyoelama-osaamisen-arvioijat [{:nimi "Teppo Työmies"
                                      :organisaatio {:nimi "Kallen Paja Ky"
                                                     :y-tunnus "1234456-4"}}]
      :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_fk"
                    :koodi-versio 2}]
      :alku (java.time.LocalDate/of 2019 3 11)
      :loppu (java.time.LocalDate/of 2019 3 13)}]}])

(def hyto-data
  [{:tutkinnon-osa-koodi-uri "tutkinnonosat_121123"
    :tutkinnon-osa-koodi-versio 3
    :osa-alueet
    [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ke"
      :osa-alue-koodi-versio 4
      :osaamisen-hankkimistavat
      [{:osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_oppisopimus"
        :osaamisen-hankkimistapa-koodi-versio 2
        :yksiloiva-tunniste "1"
        :alku (java.time.LocalDate/of 2019 1 13)
        :loppu (java.time.LocalDate/of 2019 2 19)
        :muut-oppimisymparistot [{:oppimisymparisto-koodi-uri
                                  "oppimisymparistot_0001"
                                  :oppimisymparisto-koodi-versio 1
                                  :alku (java.time.LocalDate/of 2019 1 13)
                                  :loppu (java.time.LocalDate/of 2019 2 19)}]
        :keskeytymisajanjaksot
        [{:alku (java.time.LocalDate/of 2021 9 20)
          :loppu (java.time.LocalDate/of 2021 9 28)}]}]
      :osaamisen-osoittaminen
      [{:nayttoymparisto {:nimi "Nimi"}
        :sisallon-kuvaus ["eka"]
        :alku (java.time.LocalDate/of 2019 1 13)
        :loppu (java.time.LocalDate/of 2019 2 19)
        :osa-alueet []
        :tyoelama-osaamisen-arvioijat []
        :koulutuksen-jarjestaja-osaamisen-arvioijat []
        :yksilolliset-kriteerit []}]}]}])

(def koulutuksen-osa-data
  [{:koulutuksen-osa-koodi-uri "koulutuksenosattuva_102"
    :koulutuksen-osa-koodi-versio 1
    :alku (java.time.LocalDate/of 2022 1 13)
    :loppu (java.time.LocalDate/of 2022 2 19)
    :laajuus 5.5M}])

(def hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                :oppija-oid "1.2.246.562.24.12312312319"
                :ensikertainen-hyvaksyminen
                (java.time.LocalDate/of 2019 3 18)
                :osaamisen-hankkimisen-tarve true
                :sahkoposti "erkki.esimerkki@esimerkki.com"
                :aiemmin-hankitut-yhteiset-tutkinnon-osat ahyto-data
                :hankittavat-paikalliset-tutkinnon-osat hpto-data
                :aiemmin-hankitut-paikalliset-tutkinnon-osat ahpto-data
                :aiemmin-hankitut-ammat-tutkinnon-osat ahato-data
                :hankittavat-yhteiset-tutkinnon-osat hyto-data
                :hankittavat-ammat-tutkinnon-osat hao-data
                :hankittavat-koulutuksen-osat []
                :opiskeluvalmiuksia-tukevat-opinnot oto-data})

(def min-hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"})

(deftest hoks-schema-test
  (testing "Example HOKS passes schema checks"
    (is (= hoks-data (s/validate hoks-schema/HOKSLuonti hoks-data)))))

(deftest get-aiemmin-hankitut-ammat-tutkinnon-osat-test
  (testing "Set HOKS aiemmin hankitut tutkinnon osat"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)]
      (ah/save-aiemmin-hankitut-ammat-tutkinnon-osat!
        (:id hoks)
        ahato-data)
      (eq (test-utils/dissoc-module-ids
            (ah/get-aiemmin-hankitut-ammat-tutkinnon-osat
              (:id hoks)))
          (test-utils/dissoc-module-ids ahato-data)))))

(deftest get-aiemmin-hankitut-paikalliset-tutkinnon-osat-test
  (testing "Get HOKS aiemmin hankitut paikalliset tutkinnon osat"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)]
      (ah/save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
        (:id hoks) ahpto-data)
      (eq (test-utils/dissoc-module-ids
            (ah/get-aiemmin-hankitut-paikalliset-tutkinnon-osat (:id hoks)))
          (test-utils/dissoc-module-ids ahpto-data)))))

(deftest get-hankittava-ammat-tutkinnon-osa-test
  (testing "Get HOKS hankittava ammatillinen osaaminen"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)]
      (ha/save-hankittavat-ammat-tutkinnon-osat! (:id hoks) hao-data)
      (eq (test-utils/dissoc-module-ids
            (ha/get-hankittavat-ammat-tutkinnon-osat (:id hoks)))
          (test-utils/dissoc-module-ids hao-data)))))

(deftest get-opiskeluvalmiuksia-tukevat-opinnot-test
  (testing "Get HOKS opiskeluvalmiuksia tukevat opinnot"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)]
      (ot/save-opiskeluvalmiuksia-tukevat-opinnot! (:id hoks) oto-data)
      (eq (test-utils/dissoc-module-ids
            (ot/get-opiskeluvalmiuksia-tukevat-opinnot (:id hoks)))
          (test-utils/dissoc-module-ids oto-data)))))

(deftest get-aiemmin-hankitut-yhteiset-tutkinnon-osat-test
  (testing "Get HOKS aiemmin hankitut yhteiset tutkinnon osat"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)]
      (ah/save-aiemmin-hankitut-yhteiset-tutkinnon-osat! (:id hoks) ahyto-data)
      (eq (test-utils/dissoc-module-ids
            (ah/get-aiemmin-hankitut-yhteiset-tutkinnon-osat (:id hoks)))
          (test-utils/dissoc-module-ids ahyto-data)))))

(deftest get-hankittavat-paikalliset-tutkinnon-osat-test
  (testing "Set HOKS hankittavat paikalliset tutkinnon osat"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)
          ppto-col
          (ha/save-hankittavat-paikalliset-tutkinnon-osat!
            (:id hoks) hpto-data)]
      (eq (test-utils/dissoc-module-ids
            (ha/get-hankittavat-paikalliset-tutkinnon-osat (:id hoks)))
          (test-utils/dissoc-module-ids hpto-data)))))

(deftest get-hankittavat-yhteiset-tutkinnon-osat-test
  (testing "Get HOKS hankittavat yhteiset tutkinnon osat"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)]
      (ha/save-hankittavat-yhteiset-tutkinnon-osat! (:id hoks) hyto-data)
      (eq (test-utils/dissoc-module-ids
            (ha/get-hankittavat-yhteiset-tutkinnon-osat (:id hoks)))
          (test-utils/dissoc-module-ids hyto-data)))))

(deftest get-hankittavat-koulutuksen-osat
  (testing "GET TUVA hankittavat koulutuksen osat"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)]
      (ha/save-hankittavat-koulutuksen-osat! (:id hoks) koulutuksen-osa-data)
      (eq (ha/get-hankittavat-koulutuksen-osat (:id hoks))
          koulutuksen-osa-data))))

(deftest get-hoks-test
  (testing "Save and get full HOKS"
    (let [hoks (with-redefs [k/get-opiskeluoikeus-info-raw
                             test-utils/mock-get-opiskeluoikeus-info-raw]
                 (hoks/save! hoks-data))
          hoks-from-database (hoks/get-by-id (:id hoks))]
      (is (= hoks-from-database
             (s/validate hoks-schema/HOKS hoks-from-database)))
      (eq (test-utils/dissoc-module-ids hoks-from-database)
          (assoc
            (test-utils/dissoc-module-ids hoks-data)
            :id 1
            :eid (:eid hoks)
            :manuaalisyotto false)))))

(deftest get-hoks-with-tuva-oo
  (testing "Save and get full HOKS with TUVA opiskeluoikeus oid"
    (let [hoks (with-redefs [k/get-opiskeluoikeus-info-raw
                             test-utils/mock-get-opiskeluoikeus-info-raw]
                 (hoks/save!
                   (assoc hoks-data
                          :tuva-opiskeluoikeus-oid
                          "1.2.246.562.15.20000000008")))
          hoks-from-database (hoks/get-by-id (:id hoks))]
      (is (= hoks-from-database
             (s/validate hoks-schema/HOKS hoks-from-database)))
      (eq (-> (test-utils/dissoc-module-ids hoks-from-database)
              (select-keys [:id :tuva-opiskeluoikeus-oid]))
          {:id 1
           :tuva-opiskeluoikeus-oid "1.2.246.562.15.20000000008"}))))

(deftest tarkentavat-tiedot-osaamisen-arvioija-save
  (testing "If tarkentavat-tiedot-osaamisen-arvioija is missing
  lahetetty-arvioitavaksi, save should still succeed"
    (let [arvioija-without-lahetetty-date {:aiemmin-hankitun-osaamisen-arvioijat
                                           [{:nimi "Paulanen Testi",
                                             :organisaatio
                                             {:oppilaitos-oid
                                              "1.2.246.562.10.63885480000"}}]}
          tta (ah/save-tarkentavat-tiedot-osaamisen-arvioija!
                arvioija-without-lahetetty-date)
          stored-arvioija (ah/get-tarkentavat-tiedot-osaamisen-arvioija
                            (:id tta))]
      (eq stored-arvioija
          arvioija-without-lahetetty-date))))

(deftest empty-values-test
  (testing "DB handling of empty values"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)
          ahato (db-ah/insert-aiemmin-hankittu-ammat-tutkinnon-osa!
                  {:hoks-id (:id hoks)})
          data {}
          tta (ah/save-tarkentavat-tiedot-osaamisen-arvioija! data)]
      (eq (test-utils/dissoc-module-ids
            (ah/get-tarkentavat-tiedot-osaamisen-arvioija (:id tta)))
          (assoc data :aiemmin-hankitun-osaamisen-arvioijat [])))))

(deftest get-hoks-test-send-msg-fail
  (testing (str "Save HOKS but fail in sending msg, "
                "test that HOKS saving is not rolled back")
    (with-redefs [oph.ehoks.external.aws-sqs/send-amis-palaute-message
                  #(throw (Exception. "fail"))]
      (eq (hoks/get-by-id 1) {:aiemmin-hankitut-ammat-tutkinnon-osat []
                              :aiemmin-hankitut-paikalliset-tutkinnon-osat []
                              :hankittavat-paikalliset-tutkinnon-osat []
                              :aiemmin-hankitut-yhteiset-tutkinnon-osat []
                              :hankittavat-ammat-tutkinnon-osat []
                              :opiskeluvalmiuksia-tukevat-opinnot []
                              :hankittavat-yhteiset-tutkinnon-osat []
                              :hankittavat-koulutuksen-osat []}))))

(deftest set-tep-kasitelty-test
  (testing
   "If loppu is before today set tep_kasitelty to true"
    (let [oh-data
          {:jarjestajan-edustaja {:nimi "Ville Valvoja"
                                  :rooli "Valvojan apulainen"
                                  :oppilaitos-oid "1.2.246.562.10.54451211343"}
           :osaamisen-hankkimistapa-koodi-uri
           "osaamisenhankkimistapa_oppisopimus"
           :osaamisen-hankkimistapa-koodi-versio 2
           :tyopaikalla-jarjestettava-koulutus
           {:vastuullinen-tyopaikka-ohjaaja
            {:nimi "Aimo Ohjaaja"
             :sahkoposti "aimo.ohjaaja@esimerkki2.com"}
            :tyopaikan-nimi "Ohjausyhtiö Oy"
            :tyopaikan-y-tunnus "1233456-9"
            :keskeiset-tyotehtavat ["Testitehtävä"]}
           :ajanjakson-tarkenne "Ei tarkennettavia asioita"
           :hankkijan-edustaja {:nimi "Heikki Hankkija"
                                :rooli "Opettaja"
                                :oppilaitos-oid "1.2.246.562.10.54452422428"}
           :alku (java.time.LocalDate/of 2019 1 11)}
          oh1 (ha/save-osaamisen-hankkimistapa!
                (assoc
                  oh-data
                  :loppu (.plusDays (java.time.LocalDate/now) 1))
                :hato
                12345)
          oh2 (ha/save-osaamisen-hankkimistapa!
                (assoc
                  oh-data
                  :loppu (.minusDays (java.time.LocalDate/now) 1))
                :hato
                12345)
          oh3 (ha/save-osaamisen-hankkimistapa!
                (assoc
                  oh-data
                  :loppu (java.time.LocalDate/now))
                :hato
                12345)]
      (is (= false (:tep_kasitelty oh1)))
      (is (= true (:tep_kasitelty oh2)))
      (is (= true (:tep_kasitelty oh3))))))

(defn mock-call
  [counter]
  (fn [_] (swap! counter inc)))

(def hoks-osaaminen-saavutettu
  (assoc hoks-data
         :osaamisen-saavuttamisen-pvm (java.time.LocalDate/of 2022 12 15)))

(def hoks-ei-osaamisen-hankkimisen-tarvetta
  (assoc hoks-data
         :osaamisen-hankkimisen-tarve
         false))

(def hoks-osaaminen-saavutettu-ei-osaamisen-hankkimisen-tarvetta
  (assoc hoks-data
         :osaamisen-hankkimisen-tarve false
         :osaamisen-saavuttamisen-pvm (java.time.LocalDate/of 2022 12 15)))

(def ^:private amis-palautteet-after-creation
  #{["odottaa_kasittelya" "aloittaneet"
     (LocalDate/of 2019 3 18) (LocalDate/of 2019 4 16)]
    ["odottaa_kasittelya" "valmistuneet"
     (LocalDate/of 2022 12 15) (LocalDate/of 2023 1 13)]})

(def ^:private tep-palautteet-after-creation
  #{["ei_laheteta" "1"
     (LocalDate/of 2019 3 1) (LocalDate/of 2019 3 30)]
    ["odottaa_kasittelya" "1234567890"
     (LocalDate/of 2019 3 16) (LocalDate/of 2019 4 14)]
    ["odottaa_kasittelya" "abcd"
     (LocalDate/of 2019 2 16) (LocalDate/of 2019 3 17)]})

(def ^:private palaute-tapahtumat-after-creation
  #{["aloittaneet" (LocalDate/of 2019 3 18)
     "odottaa_kasittelya" "hoks_tallennettu"]
    ["valmistuneet" (LocalDate/of 2022 12 15)
     "odottaa_kasittelya" "hoks_tallennettu"]
    ["tyopaikkajakson_suorittaneet" (LocalDate/of 2019 3 14)
     "odottaa_kasittelya" "hoks_tallennettu"]
    ["tyopaikkajakson_suorittaneet" (LocalDate/of 2019 2 15)
     "odottaa_kasittelya" "hoks_tallennettu"]
    ["tyopaikkajakson_suorittaneet" (LocalDate/of 2019 2 19)
     "ei_laheteta" "puuttuva_yhteystieto"]})

(def ^:private palautteet-after-kohderyhma-exclusion
  #{["ei_laheteta" "aloittaneet"] ["ei_laheteta" "valmistuneet"]})

(def ^:private tapahtumat-after-kohderyhma-exclusion
  #{["valmistuneet" "hoks_tallennettu"] ["valmistuneet" "ei_ole"]
    ["aloittaneet" "hoks_tallennettu"] ["aloittaneet" "ei_ole"]})

(def ^:private palautteet-after-updated-hoks
  #{["ei_laheteta" "tyopaikkajakson_suorittaneet"
     "abcd" (LocalDate/parse "2019-02-16")]
    ["odottaa_kasittelya" "tyopaikkajakson_suorittaneet"
     "1234567890" (LocalDate/parse "2019-07-16")]
    ["ei_laheteta" "tyopaikkajakson_suorittaneet"
     "1" (LocalDate/parse "2019-03-01")]
    ["odottaa_kasittelya" "valmistuneet"
     nil (LocalDate/parse "2022-11-01")]
    ["odottaa_kasittelya" "aloittaneet"
     nil (LocalDate/parse "2019-03-18")]})

(deftest form-opiskelijapalaute-in-hoks-creation-and-change
  (testing "save: forms opiskelijapalaute when has osaamisen-hankkimisen-tarve"
    (let [sqs-call-counter (atom 0)]
      (with-redefs [sqs/send-amis-palaute-message (mock-call sqs-call-counter)
                    organisaatio/get-organisaatio!
                    organisaatio-test/mock-get-organisaatio!
                    date/now (constantly (LocalDate/of 2018 7 1))]
        (let [opiskeluoikeus oo-test/opiskeluoikeus-1
              hoks-db (hoks-handler/save-hoks-and-initiate-all-palautteet!
                        {:hoks           hoks-osaaminen-saavutettu
                         :opiskeluoikeus opiskeluoikeus})
              amis (palaute/get-by-hoks-id-and-kyselytyypit!
                     db/spec {:hoks-id (:id hoks-db)
                              :kyselytyypit ["aloittaneet" "valmistuneet"]})
              tep (palaute/get-by-hoks-id-and-kyselytyypit!
                    db/spec {:hoks-id (:id hoks-db)
                             :kyselytyypit ["tyopaikkajakson_suorittaneet"]})
              tap (tapahtumat/get-all-by-hoks-id-and-kyselytyypit!
                    db/spec {:hoks-id (:id hoks-db)
                             :kyselytyypit ["aloittaneet" "valmistuneet"
                                            "tyopaikkajakson_suorittaneet"]})]
          (eq (set (map (juxt :tila :kyselytyyppi
                              :voimassa-alkupvm :voimassa-loppupvm) amis))
              amis-palautteet-after-creation)
          (eq (set (map (juxt :tila :jakson-yksiloiva-tunniste
                              :voimassa-alkupvm :voimassa-loppupvm) tep))
              tep-palautteet-after-creation)
          (eq (set (map (juxt :kyselytyyppi :heratepvm :uusi-tila :syy) tap))
              palaute-tapahtumat-after-creation)
          (is (true? (test-utils/wait-for
                       (fn [_] (= @sqs-call-counter 2)) 5000)))
          ;; with changed HOKS
          (hoks-handler/change-hoks-and-initiate-all-palautteet!
            {:hoks           {:id (:id hoks-db)
                              :osaamisen-hankkimisen-tarve false}
             :opiskeluoikeus opiskeluoikeus}
            hoks/update!)
          (eq (set (map (juxt :tila :kyselytyyppi)
                        (palaute/get-by-hoks-id-and-kyselytyypit!
                          db/spec
                          {:hoks-id (:id hoks-db)
                           :kyselytyypit ["aloittaneet" "valmistuneet"]})))
              palautteet-after-kohderyhma-exclusion)
          (eq (set (map (juxt :kyselytyyppi :syy)
                        (tapahtumat/get-all-by-hoks-id-and-kyselytyypit!
                          db/spec
                          {:hoks-id (:id hoks-db)
                           :kyselytyypit ["aloittaneet" "valmistuneet"]})))
              tapahtumat-after-kohderyhma-exclusion)
          ;; with changed dates in HOKS
          (hoks-handler/change-hoks-and-initiate-all-palautteet!
            {:hoks
             (-> (:id hoks-db)
                 (hoks/get-by-id)
                 (test-utils/dissoc-module-ids)
                 (assoc :osaamisen-saavuttamisen-pvm (LocalDate/of 2022 11 1))
                 (assoc :osaamisen-hankkimisen-tarve true)
                 (update
                   :hankittavat-paikalliset-tutkinnon-osat
                   (fn [hptos]
                     (map (fn [hpto]
                            (update
                              hpto :osaamisen-hankkimistavat
                              (fn [ohts]
                                (map #(assoc % :keskeytymisajanjaksot
                                             [{:alku (LocalDate/of 2019 2 15)}])
                                     ohts))))
                          hptos)))
                 (update
                   :hankittavat-ammat-tutkinnon-osat
                   (fn [hatos]
                     (map (fn [hato]
                            (update
                              hato :osaamisen-hankkimistavat
                              (fn [ohts]
                                (map #(assoc % :alku (LocalDate/of 2019 05 15)
                                             :loppu (LocalDate/of 2019 07 15))
                                     ohts))))
                          hatos))))
             :opiskeluoikeus opiskeluoikeus}
            hoks/replace!)
          (eq (set (map (juxt :tila :kyselytyyppi :jakson-yksiloiva-tunniste
                              :voimassa-alkupvm)
                        (palaute/get-by-hoks-id-and-kyselytyypit!
                          db/spec
                          {:hoks-id (:id hoks-db)
                           :kyselytyypit ["aloittaneet" "valmistuneet"
                                          "tyopaikkajakson_suorittaneet"]})))
              palautteet-after-updated-hoks))))))

(deftest do-not-form-opiskelijapalaute-in-hoks-save
  (testing (str "save: does not form opiskelijapalaute when "
                "does not have osaamisen-hankkimisen-tarve")
    (let [sqs-call-counter (atom 0)]
      (with-redefs [sqs/send-amis-palaute-message (mock-call sqs-call-counter)
                    organisaatio/get-organisaatio!
                    organisaatio-test/mock-get-organisaatio!]
        (hoks-handler/save-hoks-and-initiate-all-palautteet!
          {:hoks hoks-osaaminen-saavutettu-ei-osaamisen-hankkimisen-tarvetta
           :opiskeluoikeus oo-test/opiskeluoikeus-1})
        (is (= @sqs-call-counter 0))))))

(deftest form-opiskelijapalaute-in-hoks-update
  (testing (str "update: forms opiskelijapalaute when has "
                "osaamisen-hankkimisen-tarve")
    (let [sqs-call-counter (atom 0)]
      (with-redefs [sqs/send-amis-palaute-message (mock-call sqs-call-counter)
                    k/get-opiskeluoikeus! (fn [oid] oo-test/opiskeluoikeus-1)
                    organisaatio/get-organisaatio!
                    organisaatio-test/mock-get-organisaatio!
                    date/now (constantly (LocalDate/of 2018 7 1))]
        (let [opiskeluoikeus oo-test/opiskeluoikeus-1
              saved-hoks (hoks-handler/save-hoks-and-initiate-all-palautteet!
                           {:hoks           hoks-data
                            :opiskeluoikeus opiskeluoikeus})
              aloitus-herate (palaute/get-by-hoks-id-and-kyselytyypit!
                               db/spec {:hoks-id (:id saved-hoks)
                                        :kyselytyypit ["aloittaneet"]})]
          (palaute/update! db/spec (assoc (first aloitus-herate)
                                          :tila "lahetetty"))
          (hoks-handler/change-hoks-and-initiate-all-palautteet!
            {:hoks (assoc hoks-osaaminen-saavutettu
                          :id (:id saved-hoks)
                          :ensikertainen-hyvaksyminen (LocalDate/of 2021 1 1))
             :opiskeluoikeus opiskeluoikeus}
            hoks/update!)
          (eq (set (map (juxt :tila :kyselytyyppi :heratepvm)
                        (palaute/get-by-hoks-id-and-kyselytyypit!
                          db/spec
                          {:hoks-id (:id saved-hoks)
                           :kyselytyypit ["aloittaneet" "valmistuneet"]})))
              #{["odottaa_kasittelya" "aloittaneet" (LocalDate/of 2021 1 1)]
                ["odottaa_kasittelya" "valmistuneet" (LocalDate/of 2022 12 15)]
                ["lahetetty" "aloittaneet" (LocalDate/of 2019 3 18)]})
          (is (= @sqs-call-counter 3))
          (is (= 1 (opalaute/reinitiate-hoksit-between!
                     :aloituskysely
                     (LocalDate/of 2021 1 1)
                     (LocalDate/of 2021 6 1))))
          (is (= @sqs-call-counter 4))
          (is (= 1 (opalaute/reinitiate-hoksit-between!
                     :paattokysely
                     (LocalDate/of 2022 12 1)
                     (LocalDate/of 2022 12 30))))
          (is (= @sqs-call-counter 5))
          (eq (set (map (juxt :tila :kyselytyyppi :heratepvm)
                        (palaute/get-by-hoks-id-and-kyselytyypit!
                          db/spec
                          {:hoks-id (:id saved-hoks)
                           :kyselytyypit ["aloittaneet" "valmistuneet"]})))
              #{["odottaa_kasittelya" "aloittaneet" (LocalDate/of 2021 1 1)]
                ["odottaa_kasittelya" "valmistuneet" (LocalDate/of 2022 12 15)]
                ["lahetetty" "aloittaneet" (LocalDate/of 2019 3 18)]}))))))

(deftest do-not-form-opiskelijapalaute-in-hoks-update
  (testing (str "update: does not form opiskelijapalaute when "
                "does not have osaamisen-hankkimisen-tarve")
    (let [sqs-call-counter (atom 0)]
      (with-redefs [sqs/send-amis-palaute-message (mock-call sqs-call-counter)
                    organisaatio/get-organisaatio!
                    organisaatio-test/mock-get-organisaatio!]
        (let [opiskeluoikeus oo-test/opiskeluoikeus-1
              saved-hoks
              (hoks-handler/save-hoks-and-initiate-all-palautteet!
                {:hoks           hoks-ei-osaamisen-hankkimisen-tarvetta
                 :opiskeluoikeus opiskeluoikeus})]
          (hoks-handler/change-hoks-and-initiate-all-palautteet!
            {:hoks
             (assoc hoks-osaaminen-saavutettu-ei-osaamisen-hankkimisen-tarvetta
                    :id (:id saved-hoks))
             :opiskeluoikeus opiskeluoikeus}
            hoks/update!)
          (eq (set (map (juxt :tila :kyselytyyppi)
                        (palaute/get-by-hoks-id-and-kyselytyypit!
                          db/spec
                          {:hoks-id (:id saved-hoks)
                           :kyselytyypit ["aloittaneet" "valmistuneet"]})))
              #{["ei_laheteta" "aloittaneet"]
                ["ei_laheteta" "valmistuneet"]})
          (is (= @sqs-call-counter 0)))))))

(deftest form-opiskelijapalaute-in-hoks-replace
  (let [sqs-call-counter (atom 0)]
    (with-redefs [sqs/send-amis-palaute-message (mock-call sqs-call-counter)
                  organisaatio/get-organisaatio!
                  organisaatio-test/mock-get-organisaatio!
                  date/now (constantly (LocalDate/of 2018 7 1))]
      (testing "When existing HOKS is replaced with a new one, "
        (testing
         "form opiskelijapalaute if `osaamisen-hankkimisen-tarve` is `true`"
          (let [saved-hoks (hoks-handler/save-hoks-and-initiate-all-palautteet!
                             {:hoks           hoks-data
                              :opiskeluoikeus oo-test/opiskeluoikeus-1})]
            (Thread/sleep 15) ; in ms, workaround to make the test pass
            (is (= @sqs-call-counter 1)) ; sent herate for aloituskysely
            (hoks-handler/change-hoks-and-initiate-all-palautteet!
              {:hoks           (assoc hoks-osaaminen-saavutettu
                                      :id (:id saved-hoks))
               :opiskeluoikeus oo-test/opiskeluoikeus-1}
              hoks/replace!)
            (is (= @sqs-call-counter 3))) ; herate sent for both kyselyt

          (reset! sqs-call-counter 0)
          (test-utils/clear-db)

          ; FIXME: Currently `opiskeluoikeus-oid` is not required in
          ; `HOKSKorvaus` schema, even though it probably should be. The
          ; following assertions can be removed once that is fixed.
          (testing ", even when `opiskeluoikeus-oid` is missing from new HOKS"
            (let [opiskeluoikeus oo-test/opiskeluoikeus-1
                  saved-hoks
                  (hoks-handler/save-hoks-and-initiate-all-palautteet!
                    {:hoks           hoks-data
                     :opiskeluoikeus opiskeluoikeus})]
              (Thread/sleep 15) ; in ms, workaround to make the test pass
              (is (= @sqs-call-counter 1)) ; herate sent for aloituskysely
              (hoks-handler/change-hoks-and-initiate-all-palautteet!
                {:hoks
                 (assoc (dissoc hoks-osaaminen-saavutettu :opiskeluoikeus-oid)
                        :id (:id saved-hoks))
                 :opiskeluoikeus opiskeluoikeus}
                hoks/replace!)
              (is (= @sqs-call-counter 3))))))))) ; herate sent for both kyselys

(deftest do-not-form-opiskelijapalaute-in-hoks-replace
  (testing (str "replace: does not form opiskelijapalaute when "
                "does not have osaamisen-hankkimisen-tarve")
    (let [sqs-call-counter (atom 0)]
      (with-redefs [sqs/send-amis-palaute-message (mock-call sqs-call-counter)
                    organisaatio/get-organisaatio!
                    organisaatio-test/mock-get-organisaatio!]
        (let [opiskeluoikeus oo-test/opiskeluoikeus-1
              saved-hoks
              (hoks-handler/save-hoks-and-initiate-all-palautteet!
                {:hoks           hoks-ei-osaamisen-hankkimisen-tarvetta
                 :opiskeluoikeus opiskeluoikeus})]
          (hoks-handler/change-hoks-and-initiate-all-palautteet!
            {:hoks (assoc
                     hoks-osaaminen-saavutettu-ei-osaamisen-hankkimisen-tarvetta
                     :id (:id saved-hoks))
             :opiskeluoikeus opiskeluoikeus}
            hoks/replace!)
          (is (= @sqs-call-counter 0)))))))
