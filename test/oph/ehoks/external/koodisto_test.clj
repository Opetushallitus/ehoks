(ns oph.ehoks.external.koodisto-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.koodisto :as k]
            [clojure.java.io :as io]
            [cheshire.core :as cheshire]))

(defn get-mock-koodisto-value [file]
  (-> (io/resource file)
      slurp
      (cheshire/parse-string true)))

(deftest test-filter-koodisto-values
  (testing "Filtering Koodisto values"
    (is (= (k/filter-koodisto-values {}) {}))
    (is (= (k/filter-koodisto-values nil) {}))
    (let [filtered (k/filter-koodisto-values
                     (get-mock-koodisto-value "mock/koodisto-value.json"))]
      (is (= (:tila filtered) "HYVAKSYTTY"))
      (is (= (:koodiArvo filtered) "4"))
      (is (nil? (:voimassaLoppuPvm filtered)))
      (is (string? (:voimassaAlkuPvm filtered)))
      (is (string? (:resourceUri filtered)))
      (is (map? (:koodisto filtered)))
      (is (string? (get-in filtered [:koodisto :koodistoUri])))
      (is (string? (get-in filtered [:koodisto :organisaatioOid])))
      (is (coll? (get-in filtered [:koodisto :koodistoVersios])))
      (is (= (get-in filtered [:koodisto :koodistoVersios 0]) 2))
      (is (int? (:versio filtered)))
      (is (string? (:koodiUri filtered)))
      (is (int? (:paivitysPvm filtered)))
      (is (int? (:version filtered)))
      (is (coll? (:metadata filtered)))
      (let [metadata (first (:metadata filtered))]
        (is (string? (:nimi metadata)))
        (is (string? (:kuvaus metadata)))
        (is (string? (:lyhytNimi metadata)))
        (is (string? (:kayttoohje metadata)))
        (is (string? (:kasite metadata)))
        (is (string? (:sisaltaaMerkityksen metadata)))
        (is (string? (:eiSisallaMerkitysta metadata)))
        (is (string? (:huomioitavaKoodi metadata)))
        (is (string? (:sisaltaaKoodiston metadata)))
        (is (= (:kieli metadata) "FI"))))))
