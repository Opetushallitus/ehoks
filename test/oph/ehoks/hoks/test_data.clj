(ns oph.ehoks.hoks.test-data
  (:require [oph.ehoks.hoks.hoks-parts.parts-test-data :as parts-test-data]))

(def hao-data-oht-matching-tunniste
  [{:tutkinnon-osa-koodi-uri "tutkinnonosat_300268"
    :tutkinnon-osa-koodi-versio 1
    :vaatimuksista-tai-tavoitteista-poikkeaminen
    "Ei poikkeamia."
    :osaamisen-hankkimistavat
    [{:alku "2018-12-12"
      :loppu "2018-12-20"
      :yksiloiva-tunniste "asdfasdf"
      :ajanjakson-tarkenne "Tarkenne tässä"
      :osa-aikaisuustieto 50
      :osaamisen-hankkimistapa-koodi-uri
      "osaamisenhankkimistapa_koulutussopimus"
      :osaamisen-hankkimistapa-koodi-versio 1
      :muut-oppimisymparistot
      [{:oppimisymparisto-koodi-uri "oppimisymparistot_0002"
        :oppimisymparisto-koodi-versio 1
        :alku "2019-03-10"
        :loppu "2025-03-19"}]
      :keskeytymisajanjaksot []
      :hankkijan-edustaja
      {:nimi "Heikki Hankkija"
       :rooli "Rehtori"
       :oppilaitos-oid "1.2.246.562.10.54452422420"}}]
    :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000005"
    :osaamisen-osoittaminen
    [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453924330"}
      :nayttoymparisto {:nimi "Testiympäristö 2"
                        :y-tunnus "12345671-2"
                        :kuvaus "Testi test"}
      :sisallon-kuvaus ["Testaus"]
      :koulutuksen-jarjestaja-osaamisen-arvioijat
      [{:nimi "Timo Testaaja"
        :organisaatio {:oppilaitos-oid
                       "1.2.246.562.10.54452521332"}}]
      :tyoelama-osaamisen-arvioijat
      [{:nimi "Taneli Työmies"
        :organisaatio {:nimi "Tanelin Paja Ky"
                       :y-tunnus "12345622-2"}}]
      :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_kl"
                    :koodi-versio 3}]
      :alku "2019-03-10"
      :loppu "2019-03-19"
      :yksilolliset-kriteerit ["Yksi kriteeri"]}]}])

(def hyto-data-oht-matching-and-new-tunniste
  [{:tutkinnon-osa-koodi-uri "tutkinnonosat_3002683"
    :tutkinnon-osa-koodi-versio 1
    :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000007"
    :osa-alueet
    [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ku"
      :osa-alue-koodi-versio 1
      :vaatimuksista-tai-tavoitteista-poikkeaminen "joku poikkeaminen"
      :olennainen-seikka false
      :osaamisen-hankkimistavat
      [{:alku "2018-12-15"
        :loppu "2020-12-23"
        :yksiloiva-tunniste "uusi-tunniste"
        :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_oppisopimus"
        :osaamisen-hankkimistapa-koodi-versio 1
        :oppisopimuksen-perusta-koodi-uri "oppisopimuksenperusta_01"
        :oppisopimuksen-perusta-koodi-versio 1
        :muut-oppimisymparistot
        [{:oppimisymparisto-koodi-uri "oppimisymparistot_0222"
          :oppimisymparisto-koodi-versio 3
          :alku "2015-03-10"
          :loppu "2018-03-19"}]
        :keskeytymisajanjaksot []},
       {:alku "2018-12-15"
        :loppu "2022-12-23"
        :yksiloiva-tunniste "qiuewyroqiwuer"
        :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_oppisopimus"
        :osaamisen-hankkimistapa-koodi-versio 1
        :oppisopimuksen-perusta-koodi-uri "oppisopimuksenperusta_01"
        :oppisopimuksen-perusta-koodi-versio 1
        :muut-oppimisymparistot
        [{:oppimisymparisto-koodi-uri "oppimisymparistot_0222"
          :oppimisymparisto-koodi-versio 3
          :alku "2015-03-10"
          :loppu "2021-03-19"}]
        :keskeytymisajanjaksot []}]
      :osaamisen-osoittaminen
      [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.00000000002"}
        :nayttoymparisto {:nimi "aaa"}
        :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_en"
                      :koodi-versio 4}]
        :koulutuksen-jarjestaja-osaamisen-arvioijat
        [{:nimi "Erkki Esimerkkitetsaaja"
          :organisaatio {:oppilaitos-oid
                         "1.2.246.562.10.13490579333"}}]
        :alku "2018-12-12"
        :loppu "2018-12-20"
        :sisallon-kuvaus ["Kuvaus"]
        :tyoelama-osaamisen-arvioijat [{:nimi "Nimi" :organisaatio
                                        {:nimi "Organisaation nimi"}}]
        :yksilolliset-kriteerit ["Ensimmäinen kriteeri"]}]}]}])

(def hoks-data
  {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
   :oppija-oid "1.2.246.562.24.12312312312"
   :ensikertainen-hyvaksyminen "2018-12-15"
   :osaamisen-hankkimisen-tarve false
   :osaamisen-saavuttamisen-pvm "2020-10-22"
   :urasuunnitelma-koodi-uri "urasuunnitelma_0002"
   :versio 4
   :sahkoposti "testi@gmail.com"
   :opiskeluvalmiuksia-tukevat-opinnot [parts-test-data/oto-data]
   :hankittavat-ammat-tutkinnon-osat [parts-test-data/hao-data]
   :hankittavat-paikalliset-tutkinnon-osat [parts-test-data/hpto-data]
   :hankittavat-yhteiset-tutkinnon-osat [parts-test-data/hyto-data]
   :aiemmin-hankitut-ammat-tutkinnon-osat [parts-test-data/ahato-data]
   :aiemmin-hankitut-paikalliset-tutkinnon-osat [parts-test-data/ahpto-data]
   :aiemmin-hankitut-yhteiset-tutkinnon-osat [parts-test-data/ahyto-data]})

(def ahpto-of-hoks-updated
  {:id 1
   :ensikertainen-hyvaksyminen "2018-12-15"
   :osaamisen-hankkimisen-tarve false
   :aiemmin-hankitut-paikalliset-tutkinnon-osat
   [{:valittu-todentamisen-prosessi-koodi-versio 3
     :laajuus 40
     :nimi "Testiopintojaksoo"
     :tavoitteet-ja-sisallot "Tavoitteena on oppiminen."
     :valittu-todentamisen-prosessi-koodi-uri
     "osaamisentodentamisenprosessi_0003"
     :amosaa-tunniste "12345"
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453945325"
     :vaatimuksista-tai-tavoitteista-poikkeaminen "Ei poikkeamaa"
     :tarkentavat-tiedot-osaamisen-arvioija
     {:lahetetty-arvioitavaksi "2021-01-01"
      :aiemmin-hankitun-osaamisen-arvioijat
      [{:nimi "Aarne Arvioija toinen"
        :organisaatio {:oppilaitos-oid
                       "1.2.246.562.10.54453923421"}}]}
     :tarkentavat-tiedot-naytto
     [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_bi"
                     :koodi-versio 6}]
       :koulutuksen-jarjestaja-osaamisen-arvioijat
       [{:nimi "Teuvo Test"
         :organisaatio {:oppilaitos-oid
                        "1.2.246.562.10.12346234691"}}]
       :jarjestaja {:oppilaitos-oid
                    "1.2.246.562.10.93270534263"}
       :nayttoymparisto {:nimi "Testi Oy"
                         :y-tunnus "1289212-2"
                         :kuvaus "Testiyhtiöö"}
       :tyoelama-osaamisen-arvioijat
       [{:nimi "Terttu Test"
         :organisaatio {:nimi "Testi Oy"
                        :y-tunnus "1289211-4"}}]
       :sisallon-kuvaus ["Testauksen suunnittelu"
                         "Jokin toinen testi"]
       :alku "2018-02-01"
       :loppu "2021-03-01"
       :yksilolliset-kriteerit ["Ensimmäinen kriteeri" "Toinen"]}]}]})

(def ahato-of-hoks-updated
  {:id 1
   :ensikertainen-hyvaksyminen "2018-12-15"
   :osaamisen-hankkimisen-tarve true
   :aiemmin-hankitut-ammat-tutkinnon-osat
   [{:valittu-todentamisen-prosessi-koodi-versio 5
     :tutkinnon-osa-koodi-versio 100033
     :valittu-todentamisen-prosessi-koodi-uri "osaamisentodentamisenprosessi_2"
     :tutkinnon-osa-koodi-uri "tutkinnonosat_100022"
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921429"
     :tarkentavat-tiedot-osaamisen-arvioija
     {:lahetetty-arvioitavaksi "2012-03-18"
      :aiemmin-hankitun-osaamisen-arvioijat
      [{:nimi "Erkki Esimerk"
        :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921633"}}
       {:nimi "Joku Tyyp"
        :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921001"}}]}
     :tarkentavat-tiedot-naytto
     [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_en"
                     :koodi-versio 3}]
       :koulutuksen-jarjestaja-osaamisen-arvioijat
       [{:nimi "Aapo Arvo"
         :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921684"}}]
       :jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921785"}
       :nayttoymparisto {:nimi "Esimerkki Oyj"
                         :y-tunnus "12345699-3"
                         :kuvaus "Testiyrityksen testiosa"}
       :tyoelama-osaamisen-arvioijat [{:nimi "Teppo Työm"
                                       :organisaatio
                                       {:nimi "Testiyrityksen Sisar"
                                        :y-tunnus "12345689-5"}}]
       :sisallon-kuvaus ["Tutkimustyö" "Raportointi" "joku"]
       :yksilolliset-kriteerit ["Ensimmäinen kriteeri" "toinen"]
       :alku "2018-02-09"
       :loppu "2021-01-12"}]}]})

(def multiple-ahyto-values-patched
  {:valittu-todentamisen-prosessi-koodi-uri
   "osaamisentodentamisenprosessi_2000"

   :tarkentavat-tiedot-osaamisen-arvioija
   {:lahetetty-arvioitavaksi "2020-04-01"
    :aiemmin-hankitun-osaamisen-arvioijat
    [{:nimi "Muutettu Arvioija"
      :organisaatio {:oppilaitos-oid
                     "1.2.246.562.10.54453932222"}}
     {:nimi "Toinen Arvioija"
      :organisaatio {:oppilaitos-oid
                     "1.2.246.562.10.54453933333"}}]}

   :osa-alueet
   [{:osa-alue-koodi-uri "ammatillisenoppiaineet_bi"
     :osa-alue-koodi-versio 4
     :valittu-todentamisen-prosessi-koodi-uri
     "osaamisentodentamisenprosessi_0003"
     :valittu-todentamisen-prosessi-koodi-versio 4
     :tarkentavat-tiedot-osaamisen-arvioija
     parts-test-data/tarkentavat-tiedot-osaamisen-arvioija
     :tarkentavat-tiedot-naytto
     [{:sisallon-kuvaus ["kuvaus1"]
       :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_en"
                     :koodi-versio 5}]
       :koulutuksen-jarjestaja-osaamisen-arvioijat
       [{:nimi "Teppo Testaaja2"
         :organisaatio {:oppilaitos-oid
                        "1.2.246.562.10.54539267000"}}]
       :jarjestaja {:oppilaitos-oid
                    "1.2.246.562.10.55890967000"}

       :nayttoymparisto {:nimi "Ab Betoni Oy"
                         :y-tunnus "1234128-1"
                         :kuvaus "Testi"}
       :tyoelama-osaamisen-arvioijat
       [{:nimi "Tellervo Työntekijä"
         :organisaatio {:nimi "Ab Betoni Oy"
                        :y-tunnus "1234128-1"}}]
       :yksilolliset-kriteerit ["testi"]
       :alku "2029-01-04"
       :loppu "2030-03-01"}]}]

   :tarkentavat-tiedot-naytto
   [{:nayttoymparisto {:nimi "Testi Oy"
                       :y-tunnus "1289235-2"
                       :kuvaus "Testiyhtiö"}
     :koulutuksen-jarjestaja-osaamisen-arvioijat
     [{:nimi "Joku Arvioija"
       :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453911333"}}]
     :sisallon-kuvaus ["Testauksen suunnittelu"
                       "Jokin toinen testi"]
     :yksilolliset-kriteerit ["kriteeri"]
     :alku "2015-03-31"
     :loppu "2021-06-01"}
    {:nayttoymparisto {:nimi "Toka Oy"}
     :koulutuksen-jarjestaja-osaamisen-arvioijat
     [{:nimi "Joku Toinen Arvioija"
       :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453911555"}}]
     :sisallon-kuvaus ["Jotakin sisaltoa"]
     :yksilolliset-kriteerit ["testi" "toinen"]
     :alku "2014-05-05"
     :loppu "2022-09-12"}]})

(def multiple-ahato-values-patched
  {:tutkinnon-osa-koodi-versio 3000
   :tarkentavat-tiedot-osaamisen-arvioija
   {:lahetetty-arvioitavaksi "2020-01-01"
    :aiemmin-hankitun-osaamisen-arvioijat
    [{:nimi "Nimi Muutettu"
      :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453555555"}}
     {:nimi "Joku Tyyppi"
      :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921000"}}]}
   :tarkentavat-tiedot-naytto
   [{:koulutuksen-jarjestaja-osaamisen-arvioijat
     [{:nimi "Muutettu Arvioija"
       :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921674"}}]
     :jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921685"}
     :nayttoymparisto {:nimi "Testi Oy"
                       :y-tunnus "12345699-2"
                       :kuvaus "Testiyrityksen testiosasostalla"}
     :sisallon-kuvaus ["Tutkimustyö"
                       "Raportointi"]
     :yksilolliset-kriteerit ["testikriteeri"]
     :alku "2019-02-09"
     :loppu "2019-01-12"}]})

(def multiple-ahpto-values-patched
  {:tavoitteet-ja-sisallot "Muutettu tavoite."
   :tarkentavat-tiedot-osaamisen-arvioija
   {:lahetetty-arvioitavaksi "2020-01-01"
    :aiemmin-hankitun-osaamisen-arvioijat
    [{:nimi "Aarne Arvioija"
      :organisaatio {:oppilaitos-oid
                     "1.2.246.562.10.54453923411"}}]}
   :tarkentavat-tiedot-naytto
   [{:koulutuksen-jarjestaja-osaamisen-arvioijat
     [{:nimi "Muutettu Arvioija"
       :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921674"}}]
     :jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921685"}
     :nayttoymparisto {:nimi "Testi Oy"
                       :y-tunnus "12345699-2"
                       :kuvaus "Testiyrityksen testiosasostalla"}
     :sisallon-kuvaus ["Tutkimustyö"
                       "Raportointi"]
     :alku "2019-02-09"
     :loppu "2019-01-12"
     :yksilolliset-kriteerit ["Toinen kriteeri"]
     :osa-alueet []
     :tyoelama-osaamisen-arvioijat []}]})

(def osa-alueet-of-hyto
  [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ru"
    :osa-alue-koodi-versio 4
    :vaatimuksista-tai-tavoitteista-poikkeaminen "uusi poikkeaminen"
    :olennainen-seikka true
    :osaamisen-hankkimistavat
    [{:alku "2019-01-15"
      :loppu "2020-02-23"
      :yksiloiva-tunniste "09807987"
      :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_muutettu"
      :osaamisen-hankkimistapa-koodi-versio 3
      :ajanjakson-tarkenne "tarkenne"
      :muut-oppimisymparistot
      [{:oppimisymparisto-koodi-uri "oppimisymparistot_0222"
        :oppimisymparisto-koodi-versio 3
        :alku "2016-03-12"
        :loppu "2025-06-19"}]
      :keskeytymisajanjaksot
      [{:alku "2021-03-03"
        :loppu "2021-05-05"}]
      :jarjestajan-edustaja
      {:nimi "testi testaaja"
       :oppilaitos-oid "1.2.246.562.10.00000000421"}
      :hankkijan-edustaja
      {:nimi "testi edustaja"
       :oppilaitos-oid "1.2.246.562.10.00000000321"}
      :tyopaikalla-jarjestettava-koulutus
      {:tyopaikan-nimi "joku nimi"
       :keskeiset-tyotehtavat ["tehtava" "toinen"]
       :vastuullinen-tyopaikka-ohjaaja
       {:nimi "ohjaaja o"}}}]
    :osaamisen-osoittaminen
    [{:alku "2018-12-12"
      :loppu "2018-12-20"
      :sisallon-kuvaus ["Kuvaus"]
      :yksilolliset-kriteerit ["Ensimmäinen kriteeri"]
      :vaatimuksista-tai-tavoitteista-poikkeaminen "nyt poikettiin"
      :jarjestaja {:oppilaitos-oid "1.2.246.562.10.00000000002"}
      :nayttoymparisto {:nimi "aaa"}
      :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_en"
                    :koodi-versio 4}]
      :koulutuksen-jarjestaja-osaamisen-arvioijat
      [{:nimi "Erkki Esimerkkitetsaaja"
        :organisaatio {:oppilaitos-oid
                       "1.2.246.562.10.13490579333"}}]
      :tyoelama-osaamisen-arvioijat [{:nimi "Nimi" :organisaatio
                                      {:nimi "Organisaation nimi"}}]}]}])

(def multiple-hyto-values-patched
  {:tutkinnon-osa-koodi-uri "tutkinnonosat_3002683"
   :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000009"
   :osa-alueet osa-alueet-of-hyto})

(def hato-of-hoks-updated
  {:id 1
   :ensikertainen-hyvaksyminen "2018-12-15"
   :osaamisen-hankkimisen-tarve true
   :hankittavat-ammat-tutkinnon-osat
   [{:tutkinnon-osa-koodi-uri "tutkinnonosat_300222"
     :tutkinnon-osa-koodi-versio 2
     :vaatimuksista-tai-tavoitteista-poikkeaminen
     "Ei poikkeamia."
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000005"
     :osaamisen-hankkimistavat
     [{:alku "2018-12-12"
       :loppu "2018-12-20"
       :yksiloiva-tunniste "Täysin randomi string"
       :ajanjakson-tarkenne "Tarkenne muuttunut"
       :osa-aikaisuustieto 50
       :osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_koulutussopimus"
       :osaamisen-hankkimistapa-koodi-versio 1
       :muut-oppimisymparistot
       [{:oppimisymparisto-koodi-uri "oppimisymparistot_0003"
         :oppimisymparisto-koodi-versio 1
         :alku "2019-03-10"
         :loppu "2019-03-19"}]
       :keskeytymisajanjaksot
       [{:alku "2021-09-01"
         :loppu "2021-09-21"}]
       :hankkijan-edustaja
       {:nimi "Heikki Hank"
        :rooli "Opettaja"
        :oppilaitos-oid "1.2.246.562.10.54452422420"}}]
     :osaamisen-osoittaminen
     [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453924330"}
       :nayttoymparisto {:nimi "Testiympäristö 2"
                         :y-tunnus "12345671-2"
                         :kuvaus "Testi test"}
       :sisallon-kuvaus ["Testaus"]
       :koulutuksen-jarjestaja-osaamisen-arvioijat
       [{:nimi "Timo Testaaja2"
         :organisaatio {:oppilaitos-oid
                        "1.2.246.562.10.54452521332"}}]
       :tyoelama-osaamisen-arvioijat
       [{:nimi "Taneli Työmies2"
         :organisaatio {:nimi "Tanelin Paja Oy"
                        :y-tunnus "12345622-2"}}]
       :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_kl"
                     :koodi-versio 3}]
       :alku "2019-03-10"
       :loppu "2019-03-19"
       :yksilolliset-kriteerit ["Yksi kriteeri" "toinen kriteeri"]}]}]})

(def hpto-of-hoks-updated
  {:id 1
   :ensikertainen-hyvaksyminen "2018-12-15"
   :osaamisen-hankkimisen-tarve true
   :hankittavat-paikalliset-tutkinnon-osat
   [{:nimi "testinimi"
     :koulutuksen-jarjestaja-oid
     "1.2.246.562.10.00000000001"
     :olennainen-seikka false
     :osaamisen-hankkimistavat
     [{:alku "2019-12-12"
       :loppu "2020-12-20"
       :yksiloiva-tunniste "hjhkjgh"
       :ajanjakson-tarkenne "Tarkenne muuttunut"
       :osa-aikaisuustieto 50
       :osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_koulutussopimus"
       :osaamisen-hankkimistapa-koodi-versio 1
       :muut-oppimisymparistot
       [{:oppimisymparisto-koodi-uri
         "oppimisymparistot_0004"
         :oppimisymparisto-koodi-versio 2
         :alku "2019-03-10"
         :loppu "2021-03-19"}]
       :keskeytymisajanjaksot
       [{:alku "2021-09-01"
         :loppu "2021-09-21"}]
       :hankkijan-edustaja
       {:nimi "Heikki Hankk"
        :rooli "Opettaja"
        :oppilaitos-oid "1.2.246.562.10.54452422420"}}]
     :osaamisen-osoittaminen
     [{:jarjestaja {:oppilaitos-oid
                    "1.2.246.562.10.00000000022"}
       :koulutuksen-jarjestaja-osaamisen-arvioijat []
       :osa-alueet []
       :sisallon-kuvaus
       ["ensimmäinen sisältö" "toinenkin" "kolkki"]
       :nayttoymparisto {:nimi "aaab"}
       :alku "2018-12-12"
       :loppu "2018-12-20"
       :tyoelama-osaamisen-arvioijat
       [{:nimi "Nimi2"
         :organisaatio {:nimi "Organisaation nimi"}}]
       :vaatimuksista-tai-tavoitteista-poikkeaminen
       "Poikkeama tämä."
       :yksilolliset-kriteerit
       ["kriteeri 1"]}]}]})

(def hyto-of-hoks-updated
  {:id 1
   :ensikertainen-hyvaksyminen "2018-12-15"
   :osaamisen-hankkimisen-tarve false
   :hankittavat-yhteiset-tutkinnon-osat
   [{:tutkinnon-osa-koodi-uri "tutkinnonosat_3002690"
     :tutkinnon-osa-koodi-versio 3
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000007"
     :osa-alueet
     [{:osa-alue-koodi-uri "ammatillisenoppiaineet_bi"
       :osa-alue-koodi-versio 1
       :vaatimuksista-tai-tavoitteista-poikkeaminen "poikkeaminen"
       :olennainen-seikka true
       :osaamisen-hankkimistavat
       [{:alku "2018-12-15"
         :loppu "2018-12-23"
         :yksiloiva-tunniste "1.2.3.4.5.6.7.8"
         :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_joku"
         :osaamisen-hankkimistapa-koodi-versio 3
         :muut-oppimisymparistot
         [{:oppimisymparisto-koodi-uri "oppimisymparistot_0232"
           :oppimisymparisto-koodi-versio 3
           :alku "2016-03-10"
           :loppu "2021-03-19"}]
         :keskeytymisajanjaksot
         [{:alku "2021-09-01"
           :loppu "2021-09-21"}]}]
       :osaamisen-osoittaminen
       [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.00000000032"}
         :nayttoymparisto {:nimi "aaab"}
         :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_ru"
                       :koodi-versio 4}]
         :koulutuksen-jarjestaja-osaamisen-arvioijat
         [{:nimi "Erkki Esimerkkitest"
           :organisaatio {:oppilaitos-oid
                          "1.2.246.562.10.13490579322"}}]
         :alku "2018-12-12"
         :loppu "2019-12-20"
         :sisallon-kuvaus ["Kuvaus" "toinen"]
         :tyoelama-osaamisen-arvioijat [{:nimi "Nimi" :organisaatio
                                         {:nimi "Organisaation name"}}]
         :yksilolliset-kriteerit ["Ensimmäinen kriteeri" "toka"]}]}]}]})
