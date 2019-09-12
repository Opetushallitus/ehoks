(ns oph.ehoks.hoks.test-data)

(def oto-data {:nimi "Nimi"
               :kuvaus "Kuvaus"
               :alku "2018-12-12"
               :loppu "2018-12-20"})

(def hao-data {:tutkinnon-osa-koodi-uri "tutkinnonosat_300268"
               :tutkinnon-osa-koodi-versio 1
               :vaatimuksista-tai-tavoitteista-poikkeaminen
               "Ei poikkeamia."
               :osaamisen-hankkimistavat
               [{:alku "2018-12-12"
                 :loppu "2018-12-20"
                 :ajanjakson-tarkenne "Tarkenne tässä"
                 :osaamisen-hankkimistapa-koodi-uri
                 "osaamisenhankkimistapa_koulutussopimus"
                 :osaamisen-hankkimistapa-koodi-versio 1
                 :muut-oppimisymparistot
                 [{:oppimisymparisto-koodi-uri "oppimisymparistot_0002"
                   :oppimisymparisto-koodi-versio 1
                   :alku "2019-03-10"
                   :loppu "2019-03-19"}]
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
                 :alku "2019-03-10"
                 :loppu "2019-03-19"
                 :yksilolliset-kriteerit ["Yksi kriteeri"]}]})

(def hpto-data {:nimi "222"
                :osaamisen-hankkimistavat []
                :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000001"
                :olennainen-seikka true
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
     :osaamisen-hankkimistavat
     [{:alku "2018-12-15"
       :loppu "2018-12-23"
       :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_oppisopimus"
       :osaamisen-hankkimistapa-koodi-versio 1
       :muut-oppimisymparistot
       [{:oppimisymparisto-koodi-uri "oppimisymparistot_0222"
         :oppimisymparisto-koodi-versio 3
         :alku "2015-03-10"
         :loppu "2021-03-19"}]}]
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

(def hoks-data
  {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
   :oppija-oid "1.2.246.562.24.12312312312"
   :ensikertainen-hyvaksyminen "2018-12-15"
   :osaamisen-hankkimisen-tarve false
   :urasuunnitelma-koodi-uri "urasuunnitelma_0002"
   :versio 4
   :sahkoposti "testi@gmail.com"
   :opiskeluvalmiuksia-tukevat-opinnot [oto-data]
   :hankittavat-ammat-tutkinnon-osat [hao-data]
   :hankittavat-paikalliset-tutkinnon-osat [hpto-data]
   :hankittavat-yhteiset-tutkinnon-osat [hyto-data]
   :aiemmin-hankitut-ammat-tutkinnon-osat [ahato-data]
   :aiemmin-hankitut-paikalliset-tutkinnon-osat [ahpto-data]
   :aiemmin-hankitut-yhteiset-tutkinnon-osat [ahyto-data]})

(def ahyto-of-hoks-updated
  {:id 1
   :ensikertainen-hyvaksyminen "2018-12-15"
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
