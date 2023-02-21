(ns oph.ehoks.hoks.hoks-save-test
  (:require [clojure.test :refer :all]
            [oph.ehoks.utils :as utils :refer [eq empty-database-after-test
                                               migrate-database]]
            [oph.ehoks.db.postgresql.aiemmin-hankitut :as db-ah]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.hoks.hankittavat :as ha]
            [oph.ehoks.hoks.aiemmin-hankitut :as ah]
            [oph.ehoks.hoks.opiskeluvalmiuksia-tukevat :as ot]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]))

(use-fixtures :once migrate-database)
(use-fixtures :each empty-database-after-test)

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
       :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921623"}}]}
    :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921419"
    :tarkentavat-tiedot-naytto
    [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_fy"
                    :koodi-versio 1}]
      :koulutuksen-jarjestaja-osaamisen-arvioijat
      [{:nimi "Aapo Arvioija"
        :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921674"}}]
      :jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921685"}
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
                      "1.2.246.562.10.54453923411"}}]}
    :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453945322"
    :vaatimuksista-tai-tavoitteista-poikkeaminen
    "Ei poikkeamaa."
    :tarkentavat-tiedot-naytto
    [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_li"
                    :koodi-versio 6}]
      :koulutuksen-jarjestaja-osaamisen-arvioijat
      [{:nimi "Teuvo Testaaja"
        :organisaatio {:oppilaitos-oid
                       "1.2.246.562.10.12346234690"}}]
      :jarjestaja {:oppilaitos-oid
                   "1.2.246.562.10.93270534262"}

      :nayttoymparisto {:nimi "Testi Oyj"
                        :y-tunnus "1289211-2"
                        :kuvaus "Testiyhtiö"}
      :tyoelama-osaamisen-arvioijat
      [{:nimi "Terttu Testihenkilö"
        :organisaatio {:nimi "Testi Oyj"
                       :y-tunnus "1289211-2"}}]
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
      {:oppilaitos-oid "1.2.246.562.10.54453924330"}
      :nayttoymparisto {:nimi "Testiympäristö 2"
                        :y-tunnus "1234567-1"
                        :kuvaus "Testi test"}
      :sisallon-kuvaus ["Testaus"]
      :yksilolliset-kriteerit ["kriteeri 1" "kriteeri2"]
      :koulutuksen-jarjestaja-osaamisen-arvioijat
      [{:nimi "Timo Testaaja"
        :organisaatio
        {:oppilaitos-oid "1.2.246.562.10.54452521332"}}]
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
       :oppilaitos-oid "1.2.246.562.10.54451211340"}
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
       :oppilaitos-oid "1.2.246.562.10.54452422420"}
      :alku (java.time.LocalDate/of 2019 1 11)
      :loppu (java.time.LocalDate/of 2019 3 14)
      :yksiloiva-tunniste "1234567890"}]
    :koulutuksen-jarjestaja-oid "1.2.246.562.10.54411232222"}])

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
                      "1.2.246.562.10.54453931311"}}]}
    :osa-alueet
    [{:osa-alue-koodi-uri "ammatillisenoppiaineet_bi"
      :osa-alue-koodi-versio 4
      :koulutuksen-jarjestaja-oid
      "1.2.246.562.10.54453923578"
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
                         "1.2.246.562.10.54539267901"}}]
        :jarjestaja {:oppilaitos-oid
                     "1.2.246.562.10.55890967901"}

        :nayttoymparisto {:nimi "Ab Yhtiö Oy"
                          :y-tunnus "1234128-1"
                          :kuvaus "Testi"}
        :tyoelama-osaamisen-arvioijat
        [{:nimi "Tellervo Työntekijä"
          :organisaatio {:nimi "Ab Yhtiö Oy"
                         :y-tunnus "1234128-1"}}]
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
                       "1.2.246.562.10.13490579090"}}]
      :jarjestaja {:oppilaitos-oid
                   "1.2.246.562.10.93270579090"}
      :nayttoymparisto {:nimi "Testi Oy"
                        :y-tunnus "1289235-2"
                        :kuvaus "Testiyhtiö"}
      :tyoelama-osaamisen-arvioijat
      [{:nimi "Tapio Testihenkilö"
        :organisaatio {:nimi "Testi Oy"
                       :y-tunnus "1289235-2"}}]
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

(def hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                :oppija-oid "1.2.246.562.24.12312312312"
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

(def min-hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"})

(deftest get-aiemmin-hankitut-ammat-tutkinnon-osat-test
  (testing "Set HOKS aiemmin hankitut tutkinnon osat"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)]
      (ah/save-aiemmin-hankitut-ammat-tutkinnon-osat!
        (:id hoks)
        ahato-data)
      (eq
        (utils/dissoc-module-ids
          (ah/get-aiemmin-hankitut-ammat-tutkinnon-osat
            (:id hoks)))
        ahato-data))))

(deftest get-aiemmin-hankitut-paikalliset-tutkinnon-osat-test
  (testing "Get HOKS aiemmin hankitut paikalliset tutkinnon osat"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)]
      (ah/save-aiemmin-hankitut-paikalliset-tutkinnon-osat!
        (:id hoks) ahpto-data)
      (eq
        (utils/dissoc-module-ids
          (ah/get-aiemmin-hankitut-paikalliset-tutkinnon-osat (:id hoks)))
        ahpto-data))))

(deftest get-hankittava-ammat-tutkinnon-osa-test
  (testing "Get HOKS hankittava ammatillinen osaaminen"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)]
      (ha/save-hankittavat-ammat-tutkinnon-osat! (:id hoks) hao-data)
      (eq
        (utils/dissoc-module-ids
          (ha/get-hankittavat-ammat-tutkinnon-osat (:id hoks)))
        hao-data))))

(deftest get-opiskeluvalmiuksia-tukevat-opinnot-test
  (testing "Get HOKS opiskeluvalmiuksia tukevat opinnot"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)]
      (ot/save-opiskeluvalmiuksia-tukevat-opinnot! (:id hoks) oto-data)
      (eq
        (utils/dissoc-module-ids
          (ot/get-opiskeluvalmiuksia-tukevat-opinnot (:id hoks)))
        oto-data))))

(deftest get-aiemmin-hankitut-yhteiset-tutkinnon-osat-test
  (testing "Get HOKS aiemmin hankitut yhteiset tutkinnon osat"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)]
      (ah/save-aiemmin-hankitut-yhteiset-tutkinnon-osat! (:id hoks) ahyto-data)
      (eq
        (utils/dissoc-module-ids
          (ah/get-aiemmin-hankitut-yhteiset-tutkinnon-osat (:id hoks)))
        ahyto-data))))

(deftest get-hankittavat-paikalliset-tutkinnon-osat-test
  (testing "Set HOKS hankittavat paikalliset tutkinnon osat"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)
          ppto-col
          (ha/save-hankittavat-paikalliset-tutkinnon-osat!
            (:id hoks) hpto-data)]
      (eq
        (utils/dissoc-module-ids
          (ha/get-hankittavat-paikalliset-tutkinnon-osat (:id hoks)))
        hpto-data))))

(deftest get-hankittavat-yhteiset-tutkinnon-osat-test
  (testing "Get HOKS hankittavat yhteiset tutkinnon osat"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)]
      (ha/save-hankittavat-yhteiset-tutkinnon-osat! (:id hoks) hyto-data)
      (eq
        (utils/dissoc-module-ids
          (ha/get-hankittavat-yhteiset-tutkinnon-osat (:id hoks)))
        hyto-data))))

(deftest get-hankittavat-koulutuksen-osat
  (testing "GET TUVA hankittavat koulutuksen osat"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)]
      (ha/save-hankittavat-koulutuksen-osat! (:id hoks) koulutuksen-osa-data)
      (eq
        (ha/get-hankittavat-koulutuksen-osat (:id hoks))
        koulutuksen-osa-data))))

(deftest get-hoks-test
  (testing "Save and get full HOKS"
    (let [hoks (h/save-hoks! hoks-data)]
      (eq
        (utils/dissoc-module-ids (h/get-hoks-by-id (:id hoks)))
        (assoc
          hoks-data
          :id 1
          :eid (:eid hoks)
          :manuaalisyotto false)))))

(deftest get-hoks-with-tuva-oo
  (testing "Save and get full HOKS with TUVA opiskeluoikeus oid"
    (let [hoks (h/save-hoks!
                 (assoc hoks-data
                        :tuva-opiskeluoikeus-oid "1.2.246.562.15.00000000002"))]
      (eq
        (-> (utils/dissoc-module-ids (h/get-hoks-by-id (:id hoks)))
            (select-keys [:id :tuva-opiskeluoikeus-oid]))
        {:id 1
         :tuva-opiskeluoikeus-oid "1.2.246.562.15.00000000002"}))))

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
      (eq (utils/dissoc-module-ids
            (ah/get-tarkentavat-tiedot-osaamisen-arvioija (:id tta)))
          (assoc data :aiemmin-hankitun-osaamisen-arvioijat [])))))

(deftest get-hoks-test-send-msg-fail
  (testing (str "Save HOKS but fail in sending msg, "
                "test that HOKS saving is not rolled back")
    (with-redefs [oph.ehoks.external.aws-sqs/send-amis-palaute-message
                  #(throw (Exception. "fail"))]
      (eq (h/get-hoks-by-id 1) {:aiemmin-hankitut-ammat-tutkinnon-osat []
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
                                  :oppilaitos-oid "1.2.246.562.10.54451211340"}
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
                                :oppilaitos-oid "1.2.246.562.10.54452422420"}
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
