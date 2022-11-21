(ns oph.ehoks.hoks.hoks-parts.parts-test-data)

(def oto-data {:nimi "Nimi"
               :kuvaus "Kuvaus"
               :alku "2018-12-12"
               :loppu "2018-12-20"})

(def hao-data {:tutkinnon-osa-koodi-uri "tutkinnonosat_300268"
               :tutkinnon-osa-koodi-versio 1
               :vaatimuksista-tai-tavoitteista-poikkeaminen
               "Ei poikkeamia."
               :opetus-ja-ohjaus-maara 10.1
               :osaamisen-hankkimistavat
               [{:alku "2022-12-12"
                 :loppu "2022-12-20"
                 :yksiloiva-tunniste "asdfasdf"
                 :ajanjakson-tarkenne "Tarkenne tässä"
                 :osa-aikaisuustieto 25
                 :osaamisen-hankkimistapa-koodi-uri
                 "osaamisenhankkimistapa_koulutussopimus"
                 :osaamisen-hankkimistapa-koodi-versio 1
                 :muut-oppimisymparistot
                 [{:oppimisymparisto-koodi-uri "oppimisymparistot_0002"
                   :oppimisymparisto-koodi-versio 1
                   :alku "2019-03-10"
                   :loppu "2019-03-19"}]
                 :keskeytymisajanjaksot []
                 :hankkijan-edustaja
                 {:nimi "Heikki Hankkija"
                  :rooli "Opettaja"
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
                 :alku "2023-03-10"
                 :loppu "2023-03-19"
                 :yksilolliset-kriteerit ["Yksi kriteeri"]}]})

(def hao-data-wo-osa-aikaisuus
  {:tutkinnon-osa-koodi-uri "tutkinnonosat_300268"
   :tutkinnon-osa-koodi-versio               1
   :vaatimuksista-tai-tavoitteista-poikkeaminen
   "Ei poikkeamia."
   :opetus-ja-ohjaus-maara                   10.1
   :osaamisen-hankkimistavat
   [{:alku "2022-12-12"
     :loppu "2022-12-20"
     :yksiloiva-tunniste "asdfasdf"
     :ajanjakson-tarkenne "Tarkenne tässä"
     :osa-aikaisuustieto 0
     :osaamisen-hankkimistapa-koodi-uri
     "osaamisenhankkimistapa_koulutussopimus"
     :osaamisen-hankkimistapa-koodi-versio 1
     :muut-oppimisymparistot
     [{:oppimisymparisto-koodi-uri "oppimisymparistot_0002"
       :oppimisymparisto-koodi-versio 1
       :alku "2019-03-10"
       :loppu "2019-03-19"}]
     :keskeytymisajanjaksot []
     :hankkijan-edustaja
     {:nimi "Heikki Hankkija"
      :rooli "Opettaja"
      :oppilaitos-oid "1.2.246.562.10.54452422420"}
     :tyopaikalla-jarjestettava-koulutus
     {:vastuullinen-tyopaikka-ohjaaja
      {:nimi "Oiva Ohjaaja"
       :sahkoposti "oiva.ohjaaja@esimerkki2.com"}
      :tyopaikan-nimi "Ohjaus Oyk"
      :tyopaikan-y-tunnus "12345222-4"
      :keskeiset-tyotehtavat ["Testitehtävä2"]}}]
   :koulutuksen-jarjestaja-oid               "1.2.246.562.10.00000000005"
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
     :alku "2023-03-10"
     :loppu "2023-03-19"
     :yksilolliset-kriteerit ["Yksi kriteeri"]}]})

(def patch-all-hao-data
  (merge
    hao-data
    {:tutkinnon-osa-koodi-uri "tutkinnonosat_3002681"
     :tutkinnon-osa-koodi-versio 1
     :osaamisen-osoittaminen []
     :osaamisen-hankkimistavat
     [{:jarjestajan-edustaja
       {:nimi "Veikko Valvoja"
        :rooli "Valvoja"
        :oppilaitos-oid "1.2.246.562.10.54451211340"}
       :osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_oppisopimus"
       :osaamisen-hankkimistapa-koodi-versio 2
       :oppisopimuksen-perusta-koodi-uri
       "oppisopimuksenperusta_01"
       :oppisopimuksen-perusta-koodi-versio 1
       :tyopaikalla-jarjestettava-koulutus
       {:vastuullinen-tyopaikka-ohjaaja
        {:nimi "Oiva Ohjaaja"
         :sahkoposti "oiva.ohjaaja@esimerkki2.com"}
        :tyopaikan-nimi "Ohjaus Oyk"
        :tyopaikan-y-tunnus "12345222-4"
        :keskeiset-tyotehtavat ["Testitehtävä2"]}
       :muut-oppimisymparistot []
       :keskeytymisajanjaksot []
       :ajanjakson-tarkenne "Ei ole"
       :hankkijan-edustaja
       {:nimi "Harri Hankkija"
        :rooli "Opettajan sijainen"
        :oppilaitos-oid "1.2.246.562.10.55552422420"}
       :alku "2019-01-12"
       :loppu "2019-02-11"
       :yksiloiva-tunniste "jk;l"}]
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000116"}))

(def hpto-data {:nimi "222"
                :osaamisen-hankkimistavat []
                :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000001"
                :olennainen-seikka true
                :opetus-ja-ohjaus-maara 16.0
                :osaamisen-osoittaminen
                [{:jarjestaja {:oppilaitos-oid
                               "1.2.246.562.10.00000000002"}
                  :koulutuksen-jarjestaja-osaamisen-arvioijat []
                  :osa-alueet []
                  :sisallon-kuvaus ["ensimmäinen sisältö" "toinenkin"]
                  :nayttoymparisto {:nimi "aaa"}
                  :alku "2018-12-12"
                  :loppu "2018-12-20"
                  :tyoelama-osaamisen-arvioijat [{:nimi "Nimi" :organisaatio
                                                  {:nimi "Organisaation nimi"}}]
                  :vaatimuksista-tai-tavoitteista-poikkeaminen
                  "Poikkeama onpi tämä."
                  :yksilolliset-kriteerit ["kriteeri 1" "kriteeri2"]}]})

(def hyto-data
  {:tutkinnon-osa-koodi-uri "tutkinnonosat_3002683"
   :tutkinnon-osa-koodi-versio 1
   :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000007"
   :osa-alueet
   [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ku"
     :osa-alue-koodi-versio 1
     :vaatimuksista-tai-tavoitteista-poikkeaminen "joku poikkeaminen"
     :olennainen-seikka false
     :opetus-ja-ohjaus-maara 10.0
     :osaamisen-hankkimistavat
     [{:alku "2018-12-15"
       :loppu "2018-12-23"
       :osa-aikaisuustieto 50
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
       :yksilolliset-kriteerit ["Ensimmäinen kriteeri"]}]}]})

(def hyto-data-wo-osa-aikaisuus
  {:tutkinnon-osa-koodi-uri "tutkinnonosat_3002683"
   :tutkinnon-osa-koodi-versio 1
   :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000007"
   :osa-alueet
   [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ku"
     :osa-alue-koodi-versio 1
     :vaatimuksista-tai-tavoitteista-poikkeaminen "joku poikkeaminen"
     :olennainen-seikka false
     :opetus-ja-ohjaus-maara 10.0
     :osaamisen-hankkimistavat
     [{:alku "2022-12-15"
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
       :keskeytymisajanjaksot []
       :tyopaikalla-jarjestettava-koulutus
       {:tyopaikan-nimi "joku nimi"
        :tyopaikan-y-tunnus "5403241-1"
        :keskeiset-tyotehtavat ["tehtava" "toinen"]
        :vastuullinen-tyopaikka-ohjaaja
        {:nimi "ohjaaja o"}}}]
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
       :yksilolliset-kriteerit ["Ensimmäinen kriteeri"]}]}]})

(def hankittavat-koulutuksen-osat-data
  {:koulutuksen-osa-koodi-uri "koulutuksenosattuva_102"
   :koulutuksen-osa-koodi-versio 1
   :alku "2022-12-12"
   :loppu "2022-12-20"
   :laajuus 5.5})

(def ahato-data
  {:valittu-todentamisen-prosessi-koodi-versio 3
   :tutkinnon-osa-koodi-versio 100022
   :valittu-todentamisen-prosessi-koodi-uri "osaamisentodentamisenprosessi_3"
   :tutkinnon-osa-koodi-uri "tutkinnonosat_100022"
   :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921419"
   :tarkentavat-tiedot-osaamisen-arvioija
   {:lahetetty-arvioitavaksi "2019-03-18"
    :aiemmin-hankitun-osaamisen-arvioijat
    [{:nimi "Erkki Esimerkki"
      :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921623"}}
     {:nimi "Joku Tyyppi"
      :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921000"}}]}
   :tarkentavat-tiedot-naytto
   [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_fy"
                   :koodi-versio 1}]
     :koulutuksen-jarjestaja-osaamisen-arvioijat
     [{:nimi "Aapo Arvioija"
       :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921674"}}]
     :jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921685"}
     :nayttoymparisto {:nimi "Toinen Esimerkki Oyj"
                       :y-tunnus "12345699-2"
                       :kuvaus "Testiyrityksen testiosasostalla"}
     :tyoelama-osaamisen-arvioijat [{:nimi "Teppo Työmies"
                                     :organisaatio
                                     {:nimi "Testiyrityksen Sisar Oy"
                                      :y-tunnus "12345689-3"}}]
     :sisallon-kuvaus ["Tutkimustyö"
                       "Raportointi"]
     :yksilolliset-kriteerit ["Ensimmäinen kriteeri"]
     :alku "2019-02-09"
     :loppu "2019-01-12"}]})

(def ahpto-data
  {:valittu-todentamisen-prosessi-koodi-versio 2
   :laajuus 30
   :nimi "Testiopintojakso"
   :tavoitteet-ja-sisallot "Tavoitteena on testioppiminen."
   :valittu-todentamisen-prosessi-koodi-uri
   "osaamisentodentamisenprosessi_0001"
   :amosaa-tunniste "12345"
   :tarkentavat-tiedot-osaamisen-arvioija
   {:lahetetty-arvioitavaksi "2020-01-01"
    :aiemmin-hankitun-osaamisen-arvioijat
    [{:nimi "Aarne Arvioija"
      :organisaatio {:oppilaitos-oid
                     "1.2.246.562.10.54453923411"}}]}
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
     :alku "2019-02-01"
     :loppu "2019-03-01"
     :yksilolliset-kriteerit ["Ensimmäinen kriteeri"]}]
   :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453945322"
   :vaatimuksista-tai-tavoitteista-poikkeaminen "Ei poikkeamaa."})

(def tarkentavat-tiedot-osaamisen-arvioija
  {:lahetetty-arvioitavaksi "2020-01-24"
   :aiemmin-hankitun-osaamisen-arvioijat
   [{:nimi "Uusi Ominaisuus"
     :organisaatio {:oppilaitos-oid
                    "1.2.246.562.10.54453931322"}}]})

(def ahyto-data
  {:valittu-todentamisen-prosessi-koodi-uri
   "osaamisentodentamisenprosessi_0001"
   :valittu-todentamisen-prosessi-koodi-versio 3
   :tutkinnon-osa-koodi-versio 2
   :tutkinnon-osa-koodi-uri "tutkinnonosat_10203"
   :tarkentavat-tiedot-osaamisen-arvioija
   {:lahetetty-arvioitavaksi "2016-02-29"
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
     tarkentavat-tiedot-osaamisen-arvioija
     :tarkentavat-tiedot-naytto
     [{:sisallon-kuvaus ["kuvaus1"]
       :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_bi"
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
       :yksilolliset-kriteerit ["Joku kriteeri"]
       :alku "2019-01-04"
       :loppu "2019-03-01"}]}]
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
     :yksilolliset-kriteerit ["Ensimmäinen kriteeri"]
     :alku "2019-03-01"
     :loppu "2019-06-01"}]})

(def ahyto-of-hoks-updated
  {:id 1
   :ensikertainen-hyvaksyminen "2018-12-15"
   :osaamisen-hankkimisen-tarve true
   :aiemmin-hankitut-yhteiset-tutkinnon-osat
   [{:valittu-todentamisen-prosessi-koodi-uri
     "osaamisentodentamisenprosessi_0002"
     :valittu-todentamisen-prosessi-koodi-versio 4
     :tutkinnon-osa-koodi-versio 2
     :tutkinnon-osa-koodi-uri "tutkinnonosat_10203"
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.13490590921"
     :tarkentavat-tiedot-osaamisen-arvioija
     {:lahetetty-arvioitavaksi "2017-03-29"
      :aiemmin-hankitun-osaamisen-arvioijat
      [{:nimi "Arttu Arvioi"
        :organisaatio {:oppilaitos-oid
                       "1.2.246.562.10.54453931312"}}]}
     :osa-alueet
     [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ru"
       :osa-alue-koodi-versio 4
       :koulutuksen-jarjestaja-oid
       "1.2.246.562.10.54453923577"
       :vaatimuksista-tai-tavoitteista-poikkeaminen
       "Testaus ei kuulu."
       :valittu-todentamisen-prosessi-koodi-uri
       "osaamisentodentamisenprosessi_0004"
       :valittu-todentamisen-prosessi-koodi-versio 4
       :tarkentavat-tiedot-osaamisen-arvioija
       tarkentavat-tiedot-osaamisen-arvioija
       :tarkentavat-tiedot-naytto
       [{:sisallon-kuvaus ["kuvaus1" "kuvaus2"]
         :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_ai"
                       :koodi-versio 5}]
         :koulutuksen-jarjestaja-osaamisen-arvioijat
         [{:nimi "Teppo Test"
           :organisaatio {:oppilaitos-oid
                          "1.2.246.562.10.54539267911"}}]
         :jarjestaja {:oppilaitos-oid
                      "1.2.246.562.10.55890967911"}
         :nayttoymparisto {:nimi "Ab Yhtiö"
                           :y-tunnus "1234128-2"
                           :kuvaus "Testi1"}
         :tyoelama-osaamisen-arvioijat
         [{:nimi "Tellervo Tekijä"
           :organisaatio {:nimi "Ab Yhtiö"
                          :y-tunnus "1234128-1"}}]
         :yksilolliset-kriteerit ["Joku kriteeri" "Toinen"]
         :alku "2019-01-04"
         :loppu "2021-03-01"}]}]
     :tarkentavat-tiedot-naytto
     [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_ru"
                     :koodi-versio 7}]
       :koulutuksen-jarjestaja-osaamisen-arvioijat
       [{:nimi "Erkki Esimerkkitest"
         :organisaatio {:oppilaitos-oid
                        "1.2.246.562.10.13490579091"}}]
       :jarjestaja {:oppilaitos-oid
                    "1.2.246.562.10.93270579093"}
       :nayttoymparisto {:nimi "Testi"
                         :y-tunnus "1289235-3"
                         :kuvaus "Testiyht"}
       :tyoelama-osaamisen-arvioijat
       [{:nimi "Tapio Testi"
         :organisaatio {:nimi "Testi Oyj"
                        :y-tunnus "1289235-3"}}]
       :sisallon-kuvaus
       ["Testauksen suunnittelu" "Jokin toinen testi" "kolmas"]
       :yksilolliset-kriteerit ["Ensimmäinen kriteeri"]
       :alku "2019-03-01"
       :loppu "2019-06-01"}]}]})

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
     tarkentavat-tiedot-osaamisen-arvioija
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
