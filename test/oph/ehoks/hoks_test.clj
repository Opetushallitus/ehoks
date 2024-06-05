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
   [{:osaamisen-hankkimistavat
     [{:yksiloiva-tunniste "1"
       :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_oppisopimus"
       :loppu (LocalDate/of 2023 12 5)
       :osa-aikaisuustieto 100}
      {:yksiloiva-tunniste "2"
       :osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_oppilaitosmuotoinenkoulutus"
       :loppu (LocalDate/of 2023 10 10)
       :osa-aikaisuustieto 50}]}
    {:osaamisen-hankkimistavat
     [{:yksiloiva-tunniste "3"
       :osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_koulutussopimus"
       :loppu (LocalDate/of 2023 11 25)
       :osa-aikaisuustieto 80}]}]
   :hankittavat-paikalliset-tutkinnon-osat
   [{:osaamisen-hankkimistavat
     [{:yksiloiva-tunniste "4"
       :osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_koulutussopimus"
       :loppu (LocalDate/of 2024 1 6)
       :osa-aikaisuustieto 100}]}
    {:osaamisen-hankkimistavat
     [{:yksiloiva-tunniste "5"
       :osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_oppilaitosmuotoinenkoulutus"
       :loppu (LocalDate/of 2024 2 3)
       :osa-aikaisuustieto 100}]}]
   :hankittavat-yhteiset-tutkinnon-osat
   [{:osa-alueet
     [{:osaamisen-hankkimistavat
       [{:yksiloiva-tunniste "6"
         :osaamisen-hankkimistapa-koodi-uri
         "osaamisenhankkimistapa_oppilaitosmuotoinenkoulutus"
         :loppu (LocalDate/of 2023 8 12)
         :osa-aikaisuustieto 70}
        {:yksiloiva-tunniste "7"
         :osaamisen-hankkimistapa-koodi-uri
         "osaamisenhankkimistapa_koulutussopimus"
         :loppu (LocalDate/of 2024 1 25)
         :osa-aikaisuustieto 60}]}
      {:osaamisen-hankkimistavat
       [{:yksiloiva-tunniste "8"
         :osaamisen-hankkimistapa-koodi-uri
         "osaamisenhankkimistapa_oppilaitosmuotoinenkoulutus"
         :loppu (LocalDate/of 2024 3 1)
         :osa-aikaisuustieto 100}]}]}
    {:osa-alueet
     [{:osaamisen-hankkimistavat
       [{:yksiloiva-tunniste "9"
         :osaamisen-hankkimistapa-koodi-uri
         "osaamisenhankkimistapa_oppisopimus"
         :loppu (LocalDate/of 2024 4 5)
         :osa-aikaisuustieto 80}]}]}]})

(def hoks-2
  (dissoc hoks-1 :osaamisen-hankkimisen-tarve :osaamisen-saavuttamisen-pvm))
