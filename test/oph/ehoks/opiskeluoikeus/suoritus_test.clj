(ns oph.ehoks.opiskeluoikeus.suoritus-test
  (:require [clojure.test :refer [are deftest is testing use-fixtures]]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus])
  (:import (java.time LocalDate)))

(deftest test-get-osaamisalat
  (testing "Get osaamisalat"
    (let [suoritus {:osaamisala [{:alku "2021-12-12"
                                  :loppu "2022-03-03"
                                  :koodiarvo "asdfasfdads"
                                  :osaamisala {:koodiarvo "test1"}}
                                 {:alku "2021-12-15"
                                  :koodiarvo "test2"}
                                 {:alku "2022-03-01"
                                  :loppu "2022-03-15"
                                  :koodiarvo "qrqrew"}
                                 {:alku "2021-12-31"
                                  :loppu "2022-01-25"
                                  :koodiarvo "lkhlkhjl"}]}
                   expected ["test1" "test2"]]
      (is (= (suoritus/get-osaamisalat
               suoritus "1.2.3.4" (LocalDate/of 2022 2 2)) expected))
      (is (empty? (suoritus/get-osaamisalat
                    {:osaamisala []} "1.2.3.4" (LocalDate/of 2022 2 2)))))))
