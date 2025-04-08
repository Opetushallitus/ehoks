(ns oph.ehoks.schema.vipunen-test
  (:require [clojure.test :refer [deftest testing is]]
            [schema.utils :as s-utils]
            [oph.ehoks.hoks-test :as hoks-test]
            [oph.ehoks.hoks.schema :refer [vipunen-hoks-coercer]]
            [oph.ehoks.test-utils :refer [eq]]))

(deftest test-vipunen-hoks-coercer
  (testing "redacts correct fields"
    (eq (-> hoks-test/hoks-1
            (assoc :eid "abc")
            (vipunen-hoks-coercer)
            (get-in [:hankittavat-paikalliset-tutkinnon-osat 0
                     :osaamisen-hankkimistavat 0
                     :tyopaikalla-jarjestettava-koulutus]))
        {:tyopaikan-nimi "Ohjaus Oy"
         :tyopaikan-y-tunnus "5523718-7"
         :keskeiset-tyotehtavat ["Hälytysten valvonta"
                                 "Vuoronvaihdon tarkistukset"]})
    (eq (vipunen-hoks-coercer (assoc hoks-test/hoks-3 :eid "xyz"))
        (-> hoks-test/hoks-3
            (assoc :eid "xyz")
            (dissoc :sahkoposti :puhelinnumero)))
    (eq (vipunen-hoks-coercer hoks-test/hoks-3)
        (s-utils/error {:eid 'missing-required-key}))))


