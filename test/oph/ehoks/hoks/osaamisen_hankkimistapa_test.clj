(ns oph.ehoks.hoks.osaamisen-hankkimistapa-test
  (:require [clojure.test :refer [deftest is testing]]
            [oph.ehoks.hoks-test :as hoks-test]
            [oph.ehoks.hoks.osaamisen-hankkimistapa :as oht])
  (:import [java.time LocalDate]))

(deftest test-fully-keskeytynyt?
  (testing "fully-keskeytynyt?"
    (let [herate1 {:keskeytymisajanjaksot [{:alku  (LocalDate/of 2021 8 8)
                                            :loppu (LocalDate/of 2021 8 10)}
                                           {:alku  (LocalDate/of 2021 8 1)
                                            :loppu (LocalDate/of 2021 8 4)}]
                   :loppu (LocalDate/of 2021 8 9)}
          herate2 {:keskeytymisajanjaksot [{:alku  (LocalDate/of 2021 8 8)
                                            :loppu (LocalDate/of 2021 8 10)}
                                           {:alku  (LocalDate/of 2021 8 1)
                                            :loppu (LocalDate/of 2021 8 4)}]
                   :loppu (LocalDate/of 2021 8 11)}
          herate3 {}
          herate4 {:keskeytymisajanjaksot [{:alku (LocalDate/of 2021 8 8)}]
                   :loppu (LocalDate/of 2021 8 11)}
          herate5 {:keskeytymisajanjaksot [{:alku (LocalDate/of 2021 8 8)}]}]
      (is (oht/fully-keskeytynyt? herate1))
      (is (not (oht/fully-keskeytynyt? herate2)))
      (is (not (oht/fully-keskeytynyt? herate3)))
      (is (oht/fully-keskeytynyt? herate4))
      (is (not (oht/fully-keskeytynyt? herate5))))))

(deftest test-tyopaikkajaksot
  (testing (str "The function returns osaamisen hankkimistavat with koodi-uri"
                "\"osaamisenhankkimistapa_koulutussopimus\" or "
                "\"osaamisenhankkimistapa_oppisopimus\"")
    (is (= (map :yksiloiva-tunniste (oht/tyopaikkajaksot hoks-test/hoks-1))
           '("1" "3" "4" "7" "9")))))
