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
  (let [tep-palaute
        {:vastuullinen-tyopaikka-ohjaaja-nimi "Matti Meikäläinen"
         :tyopaikan-y-tunnus                  "1234567-1"
         :tyopaikan-nimi                      "Meikäläisen Murkinat Oy"}
        ctx {:existing-palaute tep-palaute
             :koulutustoimija "1.2.246.562.10.346830761110"
             :suoritus        {:koulutusmoduuli
                               {:tunniste {:koodiarvo "12345"}}}
             :niputuspvm      (LocalDate/of 2024 12 16)}]
    (is (= (nippu/build-tpo-nippu-for-heratepalvelu ctx)
           expected-tpo-nippu-data))))
