(ns oph.ehoks.palaute.opiskelija.kyselylinkki-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.tools.logging.test :refer [logged? with-log]]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.palaute.opiskelija.kyselylinkki :as kyselylinkki])
  (:import [java.time LocalDate]))

(deftest test-update-status!
  (testing "returns updated kyselylinkki when status is found"
    (with-redefs [arvo/get-kyselylinkki-status!
                  (fn [_] {:vastattu          true
                           :voimassa-loppupvm "2025-12-31T00:00:00"})
                  oph.ehoks.palaute.opiskelija.kyselylinkki/update! identity]
      (let [input {:kyselylinkki "https://testidomain.testi/ABC123"}
            result (kyselylinkki/update-status! input)]
        (is (= result
               (assoc input
                      :vastattu          true
                      :voimassa-loppupvm (LocalDate/of 2025 12 31)))))))

  (testing "returns original kyselylinkki when linkki is not found from Arvo"
    (with-redefs [arvo/get-kyselylinkki-status! (fn [_] nil)
                  oph.ehoks.palaute.opiskelija.kyselylinkki/update! identity]
      (with-log
        (let [input {:kyselylinkki "ABC123"}
              result (kyselylinkki/update-status! input)]
          (is (= input result))
          (is (logged? 'oph.ehoks.palaute.opiskelija.kyselylinkki
                       :error
                       #"kyselylinkki `ABC123` was not found from Arvo")))))))
