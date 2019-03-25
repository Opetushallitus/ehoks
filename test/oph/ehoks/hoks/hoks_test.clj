(ns oph.ehoks.hoks.hoks-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.utils :as utils :refer [eq]]
            [oph.ehoks.db.postgresql :as db]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.db.migrations :as m]))

(defn with-database [f]
  (f)
  (m/clean!)
  (m/migrate!))

(defn create-db [f]
  (m/migrate!)
  (f)
  (m/clean!))

(use-fixtures :each with-database)

(use-fixtures :once create-db)

(deftest get-olemassa-olevat-ammatilliset-tutkinnon-osat-test
  (testing "Set HOKS olemassa olevat tutkinnon osat"
    (let [hoks (db/insert-hoks! {})]
      (h/save-olemassa-olevat-ammatilliset-tutkinnon-osat
        hoks
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
          [{:osa-alue-koodi-uri "ammatillisenoppiaineet_fy"
            :osa-alue-koodi-versio 1
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
      (eq (h/get-olemassa-olevat-ammatilliset-tutkinnon-osat
            (:id hoks))
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
            [{:osa-alue-koodi-uri "ammatillisenoppiaineet_fy"
              :osa-alue-koodi-versio 1
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
              :loppu (java.time.LocalDate/of 2019 1 10)}]}]))))

(deftest get-olemassa-olevat-paikalliset-tutkinnon-osat-test
  (testing "Get HOKS olemassa olevat paikalliset tutkinnon osat"
    (let [hoks (db/insert-hoks! {})
          oopto-data [{:laajuus 30
                       :nimi "Testiopintojakso"
                       :tavoitteet-ja-sisallot "Tavoitteena on testioppiminen."
                       :amosaa-tunniste "12345"
                       :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453945322"
                       :vaatimuksista-tai-tavoitteista-poikkeaminen
                       "Ei poikkeamaa."}]]
      (h/save-olemassa-olevat-paikalliset-tutkinnon-osat! hoks oopto-data)
      (eq
        (h/get-olemassa-olevat-paikalliset-tutkinnon-osat (:id hoks))
        oopto-data))))

(deftest get-olemassa-olevat-yhteiset-tutkinnon-osat-test
  (testing "Get HOKS olemassa olevat yhteiset tutkinnon osat"
    (let [hoks (db/insert-hoks! {})
          ooyto-data [{:valittu-todentamisen-prosessi-koodi-uri
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
                       [:osa-alue-koodi-uri "ammatillisenoppiaineet_bi"
                        :osa-alue-koodi-versio 4
                        :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453923578"
                        :vaatimuksista-tai-tavoitteista-poikkeaminen
                        "Testaus ei kuulu vaatimuksiin."
                        :valittu-todentamisen-prosessi-koodi-uri
                        "osaamisentodentamisenprosessi_0003"
                        :valittu-todentamisen-prosessi-koodi-versio 4
                        :tarkentavat-tiedot
                        [{:osa-alue-koodi-uri "ammatillisenoppiaineet_bi"
                          :koulutuksen-jarjestaja-arvioijat
                          [:nimi "Teppo Testaaja"
                           :organisaatio {:oppilaitos-oid
                                          "1.2.246.562.10.544539267901"}]
                          :jarjestaja {:oppilaitos-oid
                                       "1.2.246.562.10.558909267901"}
                          :tyoelama-arvioijat
                          [{:nimi "Tellervo Työntekijä"
                            :organisaatio {:nimi "Ab Yhtiö Oy"
                                           :y-tunnus "1234128-1"}}]
                          :keskeiset-tyotehtavat-naytto ["Testaus" "Kirjoitus"]
                          :alku (java.time.LocalDate/of 2019 1 4)
                          :loppu (java.time.LocalDate/of 2019 3 1)}]]
                       :koulutuksen-jarjestaja-oid "1.2.246.562.10.13490590901"
                       :tarkentavat-tiedot-naytto
                       [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ma"
                         :koulutuksen-jarjestaja-arvioijat
                         [{:nimi "Erkki Esimerkkitestaaja"
                           :organisaatio {:oppilaitos-oid
                                          "1.2.246.562.10.13490579090"}}]
                         :jarjestaja {:oppilaitos-oid
                                      "1.2.246.562.10.93270579090"}
                         :osa-alue-koodi-versio 6
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
                         :loppu (java.time.LocalDate/of 2019 3 1)}]}]]
      (h/save-olemassa-olevat-yhteiset-tutkinnon-osat! hoks ooyto-data)
      (eq
        (h/get-olemassa-olevat-yhteiset-tutkinnon-osat (:id hoks))
        ooyto-data))))

(deftest get-puuttuvat-paikalliset-tutkinnon-osat-test
  (testing "Set HOKS puuttuvat paikalliset tutkinnon osat"
    (let [hoks (db/insert-hoks! {})
          ppto-col
          (h/save-puuttuvat-paikalliset-tutkinnon-osat!
            hoks
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
                :osa-alue-koodi-uri "ammatillisenoppiaineet_fk"
                :osa-alue-koodi-versio 2
                :alku (java.time.LocalDate/of 2019 3 11)
                :loppu (java.time.LocalDate/of 2019 3 13)}]}])]
      (eq
        (h/get-puuttuvat-paikalliset-tutkinnon-osat (:id hoks))
        [{:koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
          :tavoitteet-ja-sisallot "Testitavoite"
          :nimi "Orientaatio alaan"
          :id 1
          :osaamisen-hankkimistavat
          [{:jarjestajan-edustaja
            {:nimi "Erkki Edustaja"
             :rooli "Valvoja"
             :oppilaitos-oid "1.2.246.562.10.54453921340"}
            :osaamisen-hankkimistapa-koodi-uri
            "osaamisenhankkimistapa_oppisopimus"
            :osaamisen-hankkimistapa-koodi-versio 1
            :muut-oppimisymparisto
            [{:oppimisymparisto-koodi-uri "oppimisymparistot_0001"
              :oppimisymparisto-koodi-versio 1
              :selite "Testioppilaitos"
              :lisatiedot false}]
            :ajanjakson-tarkenne "Ei tarkennettavaa"
            :tyopaikalla-hankittava-osaaminen
            {:vastuullinen-ohjaaja {:nimi "Olli Ohjaaja"
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
            :hankkijan-edustaja {:nimi "Harri Hankkija"
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
              :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921332"}}]
            :tyoelama-arvioijat [{:nimi "Teppo Työmies"
                                  :organisaatio {:nimi "Kallen Paja Ky"
                                                 :y-tunnus "12345679-2"}}]
            :id 1
            :osa-alue-koodi-uri "ammatillisenoppiaineet_fk"
            :osa-alue-koodi-versio 2
            :alku (java.time.LocalDate/of 2019 3 11)
            :loppu (java.time.LocalDate/of 2019 3 13)}]}]))))
