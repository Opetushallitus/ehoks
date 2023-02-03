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
      :keskeytymisajanjaksot []}]
    :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000005"
    :osaamisen-osoittaminen
    [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453924330"}
      :nayttoymparisto {:nimi "Testiympäristö 2"
                        :y-tunnus "1234567-1"
                        :kuvaus "Testi test"}
      :sisallon-kuvaus ["Testaus"]
      :koulutuksen-jarjestaja-osaamisen-arvioijat
      [{:nimi "Timo Testaaja"
        :organisaatio {:oppilaitos-oid
                       "1.2.246.562.10.54452521332"}}]
      :tyoelama-osaamisen-arvioijat
      [{:nimi "Taneli Työmies"
        :organisaatio {:nimi "Tanelin Paja Ky"
                       :y-tunnus "1234561-2"}}]
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
   :hankittavat-koulutuksen-osat
   [parts-test-data/hankittavat-koulutuksen-osat-data]
   :aiemmin-hankitut-ammat-tutkinnon-osat [parts-test-data/ahato-data]
   :aiemmin-hankitut-paikalliset-tutkinnon-osat [parts-test-data/ahpto-data]
   :aiemmin-hankitut-yhteiset-tutkinnon-osat [parts-test-data/ahyto-data]})

(def hoks-data-without-osa-aikaisuus
  {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
   :oppija-oid "1.2.246.562.24.12312312312"
   :ensikertainen-hyvaksyminen "2018-12-15"
   :osaamisen-hankkimisen-tarve false
   :osaamisen-saavuttamisen-pvm "2020-10-22"
   :urasuunnitelma-koodi-uri "urasuunnitelma_0002"
   :versio 4
   :sahkoposti "testi@gmail.com"
   :opiskeluvalmiuksia-tukevat-opinnot [parts-test-data/oto-data]
   :hankittavat-ammat-tutkinnon-osat [parts-test-data/hao-data-wo-osa-aikaisuus]
   :hankittavat-paikalliset-tutkinnon-osat [parts-test-data/hpto-data]
   :hankittavat-yhteiset-tutkinnon-osat
   [parts-test-data/hyto-data-wo-osa-aikaisuus]
   :hankittavat-koulutuksen-osat
   [parts-test-data/hankittavat-koulutuksen-osat-data]
   :aiemmin-hankitut-ammat-tutkinnon-osat [parts-test-data/ahato-data]
   :aiemmin-hankitut-paikalliset-tutkinnon-osat [parts-test-data/ahpto-data]
   :aiemmin-hankitut-yhteiset-tutkinnon-osat [parts-test-data/ahyto-data]})

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
                         :y-tunnus "1234567-1"
                         :kuvaus "Testi test"}
       :sisallon-kuvaus ["Testaus"]
       :koulutuksen-jarjestaja-osaamisen-arvioijat
       [{:nimi "Timo Testaaja2"
         :organisaatio {:oppilaitos-oid
                        "1.2.246.562.10.54452521332"}}]
       :tyoelama-osaamisen-arvioijat
       [{:nimi "Taneli Työmies2"
         :organisaatio {:nimi "Tanelin Paja Oy"
                        :y-tunnus "1234561-2"}}]
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

(def hoks-with-updated-hankittavat-koulutuksen-osat
  {:id 1
   :ensikertainen-hyvaksyminen "2018-12-15"
   :osaamisen-hankkimisen-tarve false
   :hankittavat-koulutuksen-osat
   [{:koulutuksen-osa-koodi-uri "koulutuksenosattuva_104"
     :koulutuksen-osa-koodi-versio 1
     :alku "2022-09-01"
     :loppu "2022-09-21"
     :laajuus 10.4}]})
