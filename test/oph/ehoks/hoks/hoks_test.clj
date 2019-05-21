hankittavat-(ns oph.ehoks.hoks.hoks-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.utils :as utils :refer [eq]]
            [oph.ehoks.db.postgresql :as db]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.db.migrations :as m]))

(defn with-database [f]
  (m/clean!)
  (m/migrate!)
  (f)
  (m/clean!))

(defn clean-db [f]
  (m/clean!)
  (m/migrate!)
  (f))

(use-fixtures :each with-database)

(use-fixtures :once clean-db)

(def ooato-data
  [{:valittu-todentamisen-prosessi-koodi-versio 1
    :tutkinnon-osa-koodi-versio 2
    :valittu-todentamisen-prosessi-koodi-uri
    "osaamisentodentamisenprosessi_3"
    :tutkinnon-osa-koodi-uri "tutkinnonosat_100022"
    :tarkentavat-tiedot-arvioija
    {:lahetetty-arvioitavaksi (java.time.LocalDate/of 2019 3 18)
     :aiemmin-hankitun-osaamisen-arvioijat
     [{:nimi "Erkki Esimerkki"
       :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921623"}}]}
    :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921419"
    :tarkentavat-tiedot-naytto
    [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_fy"
                    :koodi-versio 1}]
      :koulutuksen-jarjestaja-arvioijat
      [{:nimi "Aapo Arvioija"
        :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921674"}}]
      :jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921685"}
      :nayttoymparisto {:nimi "Toinen Esimerkki Oyj"
                        :y-tunnus "12345699-2"
                        :kuvaus "Testiyrityksen testiosasostalla"}
      :tyoelama-arvioijat [{:nimi "Teppo Työmies"
                            :organisaatio
                            {:nimi "Testiyrityksen Sisar Oy"
                             :y-tunnus "12345689-3"}}]
      :keskeiset-tyotehtavat-naytto ["Tutkimustyö"
                                     "Raportointi"]
      :alku (java.time.LocalDate/of 2019 2 9)
      :loppu (java.time.LocalDate/of 2019 1 10)}]}])

(def oopto-data
  [{:valittu-todentamisen-prosessi-koodi-versio 2
    :laajuus 30
    :nimi "Testiopintojakso"
    :tavoitteet-ja-sisallot "Tavoitteena on testioppiminen."
    :valittu-todentamisen-prosessi-koodi-uri
    "osaamisentodentamisenprosessi_0001"
    :amosaa-tunniste "12345"
    :tarkentavat-tiedot-arvioija
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
      :koulutuksen-jarjestaja-arvioijat
      [{:nimi "Teuvo Testaaja"
        :organisaatio {:oppilaitos-oid
                       "1.2.246.562.10.12346234690"}}]
      :jarjestaja {:oppilaitos-oid
                   "1.2.246.562.10.93270534262"}

      :nayttoymparisto {:nimi "Testi Oyj"
                        :y-tunnus "1289211-2"
                        :kuvaus "Testiyhtiö"}
      :tyoelama-arvioijat
      [{:nimi "Terttu Testihenkilö"
        :organisaatio {:nimi "Testi Oyj"
                       :y-tunnus "1289211-2"}}]
      :keskeiset-tyotehtavat-naytto ["Testauksen suunnittelu"
                                     "Jokin toinen testi"]
      :alku (java.time.LocalDate/of 2019 2 1)
      :loppu (java.time.LocalDate/of 2019 2 1)}]}])

(def pao-data
  [{:tutkinnon-osa-koodi-uri "tutkinnonosat_102499"
    :tutkinnon-osa-koodi-versio 4
    :vaatimuksista-tai-tavoitteista-poikkeaminen
    "Ei poikkeamia."
    :hankitun-osaamisen-naytto
    [{:jarjestaja
      {:oppilaitos-oid "1.2.246.562.10.54453924330"}
      :nayttoymparisto {:nimi "Testiympäristö 2"
                        :y-tunnus "12345671-2"
                        :kuvaus "Testi test"}
      :keskeiset-tyotehtavat-naytto ["Testaus"]
      :koulutuksen-jarjestaja-arvioijat
      [{:nimi "Timo Testaaja"
        :organisaatio
        {:oppilaitos-oid "1.2.246.562.10.54452521332"}}]
      :tyoelama-arvioijat
      [{:nimi "Taneli Työmies"
        :organisaatio {:nimi "Tanelin Paja Ky"
                       :y-tunnus "12345622-2"}}]
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
      :tyopaikalla-hankittava-osaaminen
      {:vastuullinen-ohjaaja
       {:nimi "Aimo Ohjaaja"
        :sahkoposti "aimo.ohjaaja@esimerkki2.com"}
       :tyopaikan-nimi "Ohjausyhtiö Oy"
       :tyopaikan-y-tunnus "12345212-4"
       :muut-osallistujat [{:organisaatio
                            {:nimi "Kolmas Esimerkki Oy"
                             :y-tunnus "12345233-5"}
                            :nimi "Kalle Kirjuri"
                            :rooli "Kirjuri"}]
       :keskeiset-tyotehtavat ["Testitehtävä"]
       :lisatiedot false}
      :muut-oppimisymparisto
      [{:oppimisymparisto-koodi-uri "oppimisymparistot_0002"
        :oppimisymparisto-koodi-versio 1
        :selite "Testioppilaitos 2"
        :lisatiedot false}]
      :ajanjakson-tarkenne "Ei tarkennettavia asioita"
      :hankkijan-edustaja
      {:nimi "Heikki Hankkija"
       :rooli "Opettaja"
       :oppilaitos-oid "1.2.246.562.10.54452422420"}
      :alku (java.time.LocalDate/of 2019 1 11)
      :loppu (java.time.LocalDate/of 2019 3 14)}]
    :koulutuksen-jarjestaja-oid "1.2.246.562.10.54411232222"}])

(def oto-data
  [{:nimi "Testiopintojakso"
    :kuvaus "Testi"
    :alku (java.time.LocalDate/of 2018 06 01)
    :loppu (java.time.LocalDate/of 2018 07 31)}])

(def ooyto-data
  [{:valittu-todentamisen-prosessi-koodi-uri
    "osaamisentodentamisenprosessi_0001"
    :valittu-todentamisen-prosessi-koodi-versio 3
    :tutkinnon-osa-koodi-versio 2
    :tutkinnon-osa-koodi-uri "tutkinnonosat_10203"
    :tarkentavat-tiedot-arvioija
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
      :tarkentavat-tiedot
      [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_bi"
                      :koodi-versio 3}]
        :koulutuksen-jarjestaja-arvioijat
        [{:nimi "Teppo Testaaja"
          :organisaatio {:oppilaitos-oid
                         "1.2.246.562.10.54539267901"}}]
        :jarjestaja {:oppilaitos-oid
                     "1.2.246.562.10.55890967901"}

        :nayttoymparisto {:nimi "Ab Yhtiö Oy"
                          :y-tunnus "1234128-1"
                          :kuvaus "Testi"}
        :tyoelama-arvioijat
        [{:nimi "Tellervo Työntekijä"
          :organisaatio {:nimi "Ab Yhtiö Oy"
                         :y-tunnus "1234128-1"}}]
        :keskeiset-tyotehtavat-naytto ["Testaus" "Kirjoitus"]
        :alku (java.time.LocalDate/of 2019 1 4)
        :loppu (java.time.LocalDate/of 2019 3 1)}]}]
    :koulutuksen-jarjestaja-oid "1.2.246.562.10.13490590901"
    :tarkentavat-tiedot-naytto
    [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_ma"
                    :koodi-versio 6}]
      :koulutuksen-jarjestaja-arvioijat
      [{:nimi "Erkki Esimerkkitestaaja"
        :organisaatio {:oppilaitos-oid
                       "1.2.246.562.10.13490579090"}}]
      :jarjestaja {:oppilaitos-oid
                   "1.2.246.562.10.93270579090"}
      :nayttoymparisto {:nimi "Testi Oy"
                        :y-tunnus "1289235-2"
                        :kuvaus "Testiyhtiö"}
      :tyoelama-arvioijat
      [{:nimi "Tapio Testihenkilö"
        :organisaatio {:nimi "Testi Oy"
                       :y-tunnus "1289235-2"}}]
      :keskeiset-tyotehtavat-naytto ["Testauksen suunnittelu"
                                     "Jokin toinen testi"]
      :alku (java.time.LocalDate/of 2019 3 1)
      :loppu (java.time.LocalDate/of 2019 3 1)}]}])

(def ppto-data
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
      :tyopaikalla-hankittava-osaaminen
      {:vastuullinen-ohjaaja
       {:nimi "Olli Ohjaaja"
        :sahkoposti "olli.ohjaaja@esimerkki.com"}
       :tyopaikan-nimi "Ohjaus Oy"
       :tyopaikan-y-tunnus "12345689-4"
       :muut-osallistujat [{:organisaatio {:nimi "Esimerkki Oy"
                                           :y-tunnus "12345688-5"}
                            :nimi "Kiira Kirjaaja"
                            :rooli "Avustaja"}]
       :keskeiset-tyotehtavat ["Hälytysten valvonta"
                               "Vuoronvaihdon tarkistukset"]
       :lisatiedot false}
      :muut-oppimisymparisto
      [{:oppimisymparisto-koodi-uri "oppimisymparistot_0001"
        :oppimisymparisto-koodi-versio 1
        :selite "Testioppilaitos"
        :lisatiedot false}]
      :ajanjakson-tarkenne "Ei tarkennettavaa"
      :hankkijan-edustaja
      {:nimi "Harri Hankkija"
       :rooli "Opettaja"
       :oppilaitos-oid "1.2.246.562.10.54453921350"}
      :alku (java.time.LocalDate/of 2019 2 10)
      :loppu (java.time.LocalDate/of 2019 2 15)}]
    :hankitun-osaamisen-naytto
    [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921330"}
      :nayttoymparisto {:nimi "Testiympäristö"
                        :y-tunnus "12345678-1"
                        :kuvaus "Test"}
      :keskeiset-tyotehtavat-naytto ["Renkaanvaihto"
                                     "Tuulilasin vaihto"]
      :koulutuksen-jarjestaja-arvioijat
      [{:nimi "Terttu Testaaja"
        :organisaatio
        {:oppilaitos-oid "1.2.246.562.10.54453921332"}}]
      :tyoelama-arvioijat [{:nimi "Teppo Työmies"
                            :organisaatio {:nimi "Kallen Paja Ky"
                                           :y-tunnus "12345679-2"}}]
      :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_fk"
                    :koodi-versio 2}]
      :alku (java.time.LocalDate/of 2019 3 11)
      :loppu (java.time.LocalDate/of 2019 3 13)}]}])

(def pyto-data
  [{:tutkinnon-osa-koodi-uri "tutkinnonosat_121123"
    :tutkinnon-osa-koodi-versio 3
    :koulutuksen-jarjestaja-oid "1.2.246.562.10.54452345789"
    :osa-alueet
    [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ke"
      :osa-alue-koodi-versio 4
      :osaamisen-hankkimistavat
      [{:jarjestajan-edustaja
        {:nimi "Valmari Valvoja"
         :rooli "Apulainen"
         :oppilaitos-oid "1.2.246.562.10.54452355620"}
        :osaamisen-hankkimistapa-koodi-uri
        "osaamisenhankkimistapa_oppisopimus"
        :osaamisen-hankkimistapa-koodi-versio 2
        :tyopaikalla-hankittava-osaaminen
        {:vastuullinen-ohjaaja
         {:nimi "Olli Ohjaaja"
          :sahkoposti "olli.ohjaaja@esimerkki3.com"}
         :tyopaikan-nimi "Ohjausyhtiö Oy"
         :tyopaikan-y-tunnus "12345212-4"
         :muut-osallistujat [{:organisaatio
                              {:nimi "Uusi Esimerkki Oy"
                               :y-tunnus "12322233-5"}
                              :nimi "Keijo Kirjuri"
                              :rooli "Assistentti"}]
         :keskeiset-tyotehtavat ["Testitehtävä 2"]
         :lisatiedot false}
        :muut-oppimisymparisto
        [{:oppimisymparisto-koodi-uri "oppimisymparistot_0001"
          :oppimisymparisto-koodi-versio 1
          :selite "Testioppilaitos 2"
          :lisatiedot false}]
        :ajanjakson-tarkenne "Ei tarkennettavia asioita"
        :hankkijan-edustaja
        {:nimi "Heikki Hankkija"
         :rooli "Opettaja"
         :oppilaitos-oid "1.2.246.562.10.52224422420"}
        :alku (java.time.LocalDate/of 2019 1 13)
        :loppu (java.time.LocalDate/of 2019 2 19)}]
      :vaatimuksista-tai-tavoitteista-poikkeaminen
      "Testaus ei kuulu vaatimuksiin tällä kertaa."
      :hankitun-osaamisen-naytto
      [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.54423523330"}
        :osaamistavoitteet ["Tavoite 1"
                            "Tavoite 2"]
        :nayttoymparisto {:nimi "Testiympäristö"
                          :y-tunnus "12345258-1"
                          :kuvaus "Test"}
        :keskeiset-tyotehtavat-naytto ["Testitehtävä 1"
                                       "Testitehtävä 2"]
        :koulutuksen-jarjestaja-arvioijat
        [{:nimi "Toivo Testaaja"
          :organisaatio
          {:oppilaitos-oid "1.2.246.562.10.54452535232"}}]
        :tyoelama-arvioijat [{:nimi "Taneli Työmies"
                              :organisaatio {:nimi "Tanelin Paja Ky"
                                             :y-tunnus "12323459-2"}}]
        :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_cu"
                      :koodi-versio 2}]
        :alku (java.time.LocalDate/of 2019 3 1)
        :loppu (java.time.LocalDate/of 2019 3 18)}]}]}])

(def hoks-data
  {:paivittaja {:nimi "Pekka Päivittäjä"}
   :olemassa-olevat-ammatilliset-tutkinnon-osat ooato-data
   :ensikertainen-hyvaksyminen
   (java.time.LocalDate/of 2019 1 20)
   :olemassa-olevat-paikalliset-tutkinnon-osat oopto-data
   :sahkoposti "erkki.esimerkki@esimerkki.com"
   :hankittavat-paikalliset-tutkinnon-osat ppto-data
   :urasuunnitelma-koodi-uri "urasuunnitelma_0001"
   :olemassa-olevat-yhteiset-tutkinnon-osat ooyto-data
   :opiskeluoikeus-oid "1.2.246.562.15.00000012345"
   :laatija {:nimi "Lasse Laatija"}
   :hankittavat-ammatilliset-tutkinnon-osat pao-data
   :urasuunnitelma-koodi-versio 2
   :opiskeluvalmiuksia-tukevat-opinnot oto-data
   :oppija-oid "1.2.246.562.24.29790141661"
   :hyvaksyja {:nimi "Heikki Hyväksyjä"}
   :hankittavat-yhteiset-tutkinnon-osat pyto-data})

(deftest get-olemassa-olevat-ammatilliset-tutkinnon-osat-test
  (testing "Set HOKS olemassa olevat tutkinnon osat"
    (let [hoks (db/insert-hoks! {})]
      (h/save-olemassa-olevat-ammatilliset-tutkinnon-osat!
        hoks
        ooato-data)
      (eq (h/get-olemassa-olevat-ammatilliset-tutkinnon-osat
            (:id hoks))
          ooato-data))))

(deftest get-olemassa-olevat-paikalliset-tutkinnon-osat-test
  (testing "Get HOKS olemassa olevat paikalliset tutkinnon osat"
    (let [hoks (db/insert-hoks! {})]
      (h/save-olemassa-olevat-paikalliset-tutkinnon-osat! hoks oopto-data)
      (eq
        (h/get-olemassa-olevat-paikalliset-tutkinnon-osat (:id hoks))
        oopto-data))))

(deftest get-puuttuva-ammatillinen-osaaminen-test
  (testing "Get HOKS puuttuva ammatillinen osaaminen"
    (let [hoks (db/insert-hoks! {})]
      (h/save-hankittavat-ammatilliset-tutkinnon-osat! hoks pao-data)
      (eq
        (h/get-hankittavat-ammatilliset-tutkinnon-osat (:id hoks))
        pao-data))))

(deftest get-opiskeluvalmiuksia-tukevat-opinnot-test
  (testing "Get HOKS opiskeluvalmiuksia tukevat opinnot"
    (let [hoks (db/insert-hoks! {})]
      (h/save-opiskeluvalmiuksia-tukevat-opinnot! hoks oto-data)
      (eq
        (h/get-opiskeluvalmiuksia-tukevat-opinnot (:id hoks))
        oto-data))))

(deftest get-olemassa-olevat-yhteiset-tutkinnon-osat-test
  (testing "Get HOKS olemassa olevat yhteiset tutkinnon osat"
    (let [hoks (db/insert-hoks! {})]
      (h/save-olemassa-olevat-yhteiset-tutkinnon-osat! hoks ooyto-data)
      (eq
        (h/get-olemassa-olevat-yhteiset-tutkinnon-osat (:id hoks))
        ooyto-data))))

(deftest get-hankittavat-paikalliset-tutkinnon-osat-test
  (testing "Set HOKS puuttuvat paikalliset tutkinnon osat"
    (let [hoks (db/insert-hoks! {})
          ppto-col
          (h/save-hankittavat-paikalliset-tutkinnon-osat! hoks ppto-data)]
      (eq
        (h/get-hankittavat-paikalliset-tutkinnon-osat (:id hoks))
        ppto-data))))

(deftest get-hankittavat-yhteiset-tutkinnon-osat-test
  (testing "Get HOKS puuttuvat yhteiset tutkinnon osat"
    (let [hoks (db/insert-hoks! {})]
      (h/save-hankittavat-yhteiset-tutkinnon-osat! hoks pyto-data)
      (eq
        (h/get-hankittavat-yhteiset-tutkinnon-osat (:id hoks))
        pyto-data))))

(deftest get-hoks-test
  (testing "Save and get full HOKS"
    (let [hoks (h/save-hoks! hoks-data)]
      (eq
        (h/get-hoks-by-id (:id hoks))
        (assoc
          hoks-data
          :id 1
          :eid (:eid hoks))))))

(deftest empty-values-test
  (testing "DB handling of empty values"
    (let [hoks (db/insert-hoks! {})
          ooato (db/insert-olemassa-oleva-ammatillinen-tutkinnon-osa!
                  {:hoks-id (:id hoks)})
          data {}
          tta (h/save-ooato-tarkentavat-tiedot-arvioija! data)]
      (eq (h/get-tarkentavat-tiedot-arvioija (:id tta))
          (assoc data :aiemmin-hankitun-osaamisen-arvioijat [])))))
