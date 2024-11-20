(ns oph.ehoks.hoks-test
  (:import [java.time LocalDate]))

(def hoks-1
  {:id                          12345
   :oppija-oid                  "1.2.246.562.24.12312312319"
   :opiskeluoikeus-oid          "1.2.246.562.15.10000000009"
   :osaamisen-hankkimisen-tarve true
   :ensikertainen-hyvaksyminen  (LocalDate/of 2023 04 16)
   :osaamisen-saavuttamisen-pvm (LocalDate/of 2024 02 05)
   :sahkoposti                  "testi.testaaja@testidomain.testi"
   :puhelinnumero               "0123456789"
   :hankittavat-ammat-tutkinnon-osat
   [{:tutkinnon-osa-koodi-uri "tutkinnonosat_300268"
     :tutkinnon-osa-koodi-versio 1
     :osaamisen-hankkimistavat
     [{:yksiloiva-tunniste "1"
       :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_oppisopimus"
       :osaamisen-hankkimistapa-koodi-versio 1
       :oppisopimuksen-perusta-koodi-uri "oppisopimuksenperusta_01"
       :oppisopimuksen-perusta-koodi-versio 1
       :alku (LocalDate/of 2023 12 1)
       :loppu (LocalDate/of 2023 12 5)
       :osa-aikaisuustieto 100
       :tyopaikalla-jarjestettava-koulutus
       {:vastuullinen-tyopaikka-ohjaaja
        {:nimi "Olli Ohjaaja"
         :sahkoposti "olli.ohjaaja@esimerkki.com"
         :puhelinnumero "0401111111"}
        :tyopaikan-nimi "Ohjaus Oy"
        :tyopaikan-y-tunnus "5523718-7"
        :keskeiset-tyotehtavat ["Hälytysten valvonta"
                                "Vuoronvaihdon tarkistukset"]}}
      {:yksiloiva-tunniste "2"
       :osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_oppilaitosmuotoinenkoulutus"
       :osaamisen-hankkimistapa-koodi-versio 1
       :alku (LocalDate/of 2023 10 1)
       :loppu (LocalDate/of 2023 10 10)
       :osa-aikaisuustieto 50}]}
    {:tutkinnon-osa-koodi-uri "tutkinnonosat_300269"
     :tutkinnon-osa-koodi-versio 1
     :osaamisen-hankkimistavat
     [{:yksiloiva-tunniste "3"
       :osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_koulutussopimus"
       :osaamisen-hankkimistapa-koodi-versio 1
       :alku (LocalDate/of 2023 11 1)
       :loppu (LocalDate/of 2023 11 25)
       :keskeytymisajanjaksot [{:alku (LocalDate/of 2023 11 5)
                                :loppu (LocalDate/of 2023 11 8)}
                               {:alku (LocalDate/of 2023 11 16)
                                :loppu (LocalDate/of 2023 11 16)}]
       :osa-aikaisuustieto 80
       :tyopaikalla-jarjestettava-koulutus
       {:vastuullinen-tyopaikka-ohjaaja
        {:nimi "Matti Meikäläinen"
         :sahkoposti "matti.meikalainen@esimerkki.com"
         :puhelinnumero "0402222222"}
        :tyopaikan-nimi "Ohjaus Oy"
        :tyopaikan-y-tunnus "5523718-7"
        :keskeiset-tyotehtavat ["Hälytysten valvonta"
                                "Vuoronvaihdon tarkistukset"]}}]}]
   :hankittavat-paikalliset-tutkinnon-osat
   [{:nimi "Testiosa"
     :osaamisen-hankkimistavat
     [{:yksiloiva-tunniste "4"
       :osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_koulutussopimus"
       :osaamisen-hankkimistapa-koodi-versio 1
       :alku (LocalDate/of 2024 1 1)
       :loppu (LocalDate/of 2024 1 6)
       :osa-aikaisuustieto 100
       :tyopaikalla-jarjestettava-koulutus
       {:vastuullinen-tyopaikka-ohjaaja
        {:nimi "Olli Ohjaaja"
         :sahkoposti "olli.ohjaaja@esimerkki.com"
         :puhelinnumero "0401111111"}
        :tyopaikan-nimi "Ohjaus Oy"
        :tyopaikan-y-tunnus "5523718-7"
        :keskeiset-tyotehtavat ["Hälytysten valvonta"
                                "Vuoronvaihdon tarkistukset"]}}]}
    {:nimi "Testiosa 2"
     :osaamisen-hankkimistavat
     [{:yksiloiva-tunniste "5"
       :osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_oppilaitosmuotoinenkoulutus"
       :osaamisen-hankkimistapa-koodi-versio 1
       :alku (LocalDate/of 2024 2 1)
       :loppu (LocalDate/of 2024 2 3)
       :osa-aikaisuustieto 100}]}]
   :hankittavat-yhteiset-tutkinnon-osat
   [{:tutkinnon-osa-koodi-uri "tutkinnonosat_300270"
     :tutkinnon-osa-koodi-versio 1
     :osa-alueet
     [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ku"
       :osa-alue-koodi-versio 1
       :osaamisen-hankkimistavat
       [{:yksiloiva-tunniste "6"
         :osaamisen-hankkimistapa-koodi-uri
         "osaamisenhankkimistapa_oppilaitosmuotoinenkoulutus"
         :osaamisen-hankkimistapa-koodi-versio 1
         :alku (LocalDate/of 2023 8 1)
         :loppu (LocalDate/of 2023 8 12)
         :osa-aikaisuustieto 70}
        {:yksiloiva-tunniste "7"
         :osaamisen-hankkimistapa-koodi-uri
         "osaamisenhankkimistapa_koulutussopimus"
         :osaamisen-hankkimistapa-koodi-versio 1
         :alku (LocalDate/of 2024 1 1)
         :loppu (LocalDate/of 2024 1 25)
         :osa-aikaisuustieto 60
         :tyopaikalla-jarjestettava-koulutus
         {:vastuullinen-tyopaikka-ohjaaja
          {:nimi "Matti Meikäläinen"
           :sahkoposti "matti.meikalainen@esimerkki.com"
           :puhelinnumero "0402222222"}
          :tyopaikan-nimi "Ohjaus Oy"
          :tyopaikan-y-tunnus "5523718-7"
          :keskeiset-tyotehtavat ["Hälytysten valvonta"
                                  "Vuoronvaihdon tarkistukset"]}}]}
      {:osa-alue-koodi-uri "ammatillisenoppiaineet_ma"
       :osa-alue-koodi-versio 1
       :osaamisen-hankkimistavat
       [{:yksiloiva-tunniste "8"
         :osaamisen-hankkimistapa-koodi-uri
         "osaamisenhankkimistapa_oppilaitosmuotoinenkoulutus"
         :osaamisen-hankkimistapa-koodi-versio 1
         :alku (LocalDate/of 2024 2 1)
         :loppu (LocalDate/of 2024 3 1)
         :osa-aikaisuustieto 100}]}]}
    {:tutkinnon-osa-koodi-uri "tutkinnonosat_300271"
     :tutkinnon-osa-koodi-versio 1
     :osa-alueet
     [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ai"
       :osa-alue-koodi-versio 1
       :osaamisen-hankkimistavat
       [{:yksiloiva-tunniste "9"
         :osaamisen-hankkimistapa-koodi-uri
         "osaamisenhankkimistapa_oppisopimus"
         :osaamisen-hankkimistapa-koodi-versio 1
         :oppisopimuksen-perusta-koodi-uri "oppisopimuksenperusta_01"
         :oppisopimuksen-perusta-koodi-versio 1
         :alku (LocalDate/of 2024 4 1)
         :loppu (LocalDate/of 2024 4 5)
         :osa-aikaisuustieto 80
         :tyopaikalla-jarjestettava-koulutus
         {:vastuullinen-tyopaikka-ohjaaja
          {:nimi "Olli Ohjaaja"
           :sahkoposti "olli.ohjaaja@esimerkki.com"
           :puhelinnumero "0401111111"}
          :tyopaikan-nimi "Ohjaus Oy"
          :tyopaikan-y-tunnus "5523718-7"
          :keskeiset-tyotehtavat ["Hälytysten valvonta"
                                  "Vuoronvaihdon tarkistukset"]}}]}]}]})

(def hoks-2
  (dissoc hoks-1 :osaamisen-hankkimisen-tarve :osaamisen-saavuttamisen-pvm))

(def hoks-3
  {:id                          12346
   :oppija-oid                  "1.2.246.562.24.12312312319"
   :opiskeluoikeus-oid          "1.2.246.562.15.20000000008"
   :osaamisen-hankkimisen-tarve true
   :ensikertainen-hyvaksyminen  (LocalDate/of 2023 06 16)
   :osaamisen-saavuttamisen-pvm (LocalDate/of 2024 04 05)
   :sahkoposti                  "testi.testaaja@testidomain.testi"
   :puhelinnumero               "0123456789"})

(def hoks-4
  {:id                          12347
   :oppija-oid                  "1.2.246.562.24.12312312319"
   :opiskeluoikeus-oid          "1.2.246.562.15.30000000007"
   :osaamisen-hankkimisen-tarve true
   :ensikertainen-hyvaksyminen  (LocalDate/of 2024 8 16)
   :osaamisen-saavuttamisen-pvm (LocalDate/of 2024 11 05)
   :sahkoposti                  "testi.testaaja@testidomain.testi"
   :puhelinnumero               "0123456789"})
