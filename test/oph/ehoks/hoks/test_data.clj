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
