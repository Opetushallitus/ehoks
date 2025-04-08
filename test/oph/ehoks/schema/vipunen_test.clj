(ns oph.ehoks.schema.vipunen-test
  (:require [clojure.test :refer [deftest testing is]]
            [schema.utils :as s-utils]
            [oph.ehoks.hoks-test :as hoks-test]
            [oph.ehoks.hoks.schema :refer [vipunen-hoks-coercer]]
            [oph.ehoks.test-utils :refer [eq]])
  (:import (java.time LocalDate)))

(def test-hoks
  {:eid "kerran-mulla-oli-makkara-mut-ei-enaa"
   :id 3
   :ensikertainen-hyvaksyminen (LocalDate/of 2022 2 2)
   :sahkoposti "joo@oppilaitos.fi"
   :hankittavat-ammat-tutkinnon-osat
   [{:tutkinnon-osa-koodi-uri "tutkinnonosat_300268"
     :tutkinnon-osa-koodi-versio 1
     :osaamisen-osoittaminen
     [{:nayttoymparisto {:nimi "Osman Onnela"}
       :sisallon-kuvaus ["syömistä" "juomista"]
       :alku (LocalDate/of 2023 3 15)
       :loppu (LocalDate/of 2023 3 17)
       :tyoelama-osaamisen-arvioijat
       [{:nimi "Spuupaduu Mattila"
         :organisaatio {:nimi "jee" :y-tunnus "1234567-1"}}]}]
     :osaamisen-hankkimistavat
     [{:osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_koulutussopimus"
       :osaamisen-hankkimistapa-koodi-versio 1
       :alku (LocalDate/of 2023 1 1)
       :loppu (LocalDate/of 2023 4 1)
       :yksiloiva-tunniste "kaalepin-kaappi"
       :hankkijan-edustaja {:nimi "Oon possu" :puhelinnumero "4390"}
       :jarjestajan-edustaja {:nimi "Oon kiva" :sahkoposti "abc@foo"}}]}]})

(def test-hoks-redacted
  {:eid "kerran-mulla-oli-makkara-mut-ei-enaa"
   :id 3
   :ensikertainen-hyvaksyminen (LocalDate/of 2022 2 2)
   :hankittavat-ammat-tutkinnon-osat
   [{:tutkinnon-osa-koodi-uri "tutkinnonosat_300268"
     :tutkinnon-osa-koodi-versio 1
     :osaamisen-osoittaminen
     [{:nayttoymparisto {:nimi "Osman Onnela"}
       :sisallon-kuvaus ["syömistä" "juomista"]
       :alku (LocalDate/of 2023 3 15)
       :loppu (LocalDate/of 2023 3 17)
       :tyoelama-osaamisen-arvioijat
       [{:nimi "<REDACTED>"
         :organisaatio {:nimi "jee" :y-tunnus "1234567-1"}}]}]
     :osaamisen-hankkimistavat
     [{:osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_koulutussopimus"
       :osaamisen-hankkimistapa-koodi-versio 1
       :alku (LocalDate/of 2023 1 1)
       :loppu (LocalDate/of 2023 4 1)
       :yksiloiva-tunniste "kaalepin-kaappi"}]}]})

(deftest test-vipunen-hoks-coercer
  (testing "redacts correct fields"
    (eq (-> hoks-test/hoks-1
            (assoc :eid "abc")
            (vipunen-hoks-coercer)
            (get-in [:hankittavat-paikalliset-tutkinnon-osat 0
                     :osaamisen-hankkimistavat 0
                     :tyopaikalla-jarjestettava-koulutus]))
        {:vastuullinen-tyopaikka-ohjaaja true
         :tyopaikan-nimi "Ohjaus Oy"
         :tyopaikan-y-tunnus "5523718-7"
         :keskeiset-tyotehtavat ["Hälytysten valvonta"
                                 "Vuoronvaihdon tarkistukset"]})
    (eq (vipunen-hoks-coercer test-hoks)
        test-hoks-redacted)
    (eq (vipunen-hoks-coercer (assoc hoks-test/hoks-3 :eid "xyz"))
        (-> hoks-test/hoks-3
            (assoc :eid "xyz")
            (dissoc :sahkoposti :puhelinnumero)))
    (eq (vipunen-hoks-coercer hoks-test/hoks-3)
        (s-utils/error {:eid 'missing-required-key}))))
