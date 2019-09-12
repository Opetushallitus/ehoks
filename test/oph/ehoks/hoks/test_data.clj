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
