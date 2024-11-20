(ns oph.ehoks.palaute.tyoelama.nippu-test
  (:require [clojure.test :refer [deftest is testing]]
            [oph.ehoks.palaute.tyoelama.nippu :as nippu])
  (:import [java.time LocalDate]))
;
(def expected-tpo-nippu-data
  {:ohjaaja_ytunnus_kj_tutkinto (str "Matti Meikäläinen/1234567-1/"
                                     "1.2.246.562.10.346830761110/12345")
   :ohjaaja                     "Matti Meikäläinen"
   :tyopaikka                   "Meikäläisen Murkinat Oy"
   :ytunnus                     "1234567-1"
   :koulutuksenjarjestaja       "1.2.246.562.10.346830761110"
   :tutkinto                    "12345"
   :kasittelytila               "ei_niputettu"
   :sms_kasittelytila           "ei_lahetetty"
   :niputuspvm                  (LocalDate/of 2024 12 16)})

(deftest test-build-tpo-nippu-for-heratepalvelu
  (let [ctx {:koulutustoimija "1.2.246.562.10.346830761110"
             :suoritus        {:koulutusmoduuli
                               {:tunniste {:koodiarvo "12345"}}}
             :niputuspvm      (LocalDate/of 2024 12 16)}
        tep-palaute
        {:vastuullinen-tyopaikka-ohjaaja-nimi "Matti Meikäläinen"
         :tyopaikan-y-tunnus                  "1234567-1"
         :tyopaikan-nimi                      "Meikäläisen Murkinat Oy"}]
    (testing "Kasittelytila for nippu will be"
      (testing "\"ei_niputettu\" when"
        (testing "there are no keskeytymisajanjaksos"
          (is (= (nippu/build-tpo-nippu-for-heratepalvelu ctx tep-palaute {})
                 expected-tpo-nippu-data)))
        (testing "there are keskeytymisajanjaksos but they're all closed"
          (is (= (nippu/build-tpo-nippu-for-heratepalvelu
                   ctx tep-palaute [{:alku  (LocalDate/of 2023 11 1)
                                     :loppu (LocalDate/of 2023 11 16)}
                                    {:alku  (LocalDate/of 2024 02 5)
                                     :loppu (LocalDate/of 2024 02 8)}])
                 expected-tpo-nippu-data))))
      (testing
       "\"ei_niputeta\" when there are one or more open keskeytymisajanjakso"
        (is (= (nippu/build-tpo-nippu-for-heratepalvelu
                 ctx tep-palaute [{:alku  (LocalDate/of 2023 11 1)
                                   :loppu (LocalDate/of 2023 11 16)}
                                  {:alku  (LocalDate/of 2024 02 5)}])
               (assoc expected-tpo-nippu-data
                      :kasittelytila     "ei_niputeta"
                      :sms_kasittelytila "ei_niputeta")))))))
