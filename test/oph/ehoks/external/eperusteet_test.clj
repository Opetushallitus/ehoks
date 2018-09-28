(ns oph.ehoks.external.eperusteet-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.eperusteet :as ep]))

(deftest test-map-perusteet
  (testing "Mapping perusteet"
    (is (= (ep/map-perusteet []) []))
    (is (= (ep/map-perusteet nil) '()))
    (is (= (ep/map-perusteet
             [{:id 123
               :globalVersion {:aikaleima 1234567}
               :nimi {:fi "Testi"
                      :en "Test"}
               :tutkintonimikkeet [{:nimi {:fi "Testinimike"
                                           :en "Test qualification title"}}]
               :osaamisalat [{:arvo "1636"
                              :nimi {:fi "Testiala"
                                     :sv "Test kunskapsområde"}}]}])
           (vector
             {:id 123
              :nimi {:fi "Testi"
                     :en "Test"}
              :osaamisalat (vector {:nimi {:fi "Testiala"
                                           :sv "Test kunskapsområde"}})
              :tutkintonimikkeet (vector {:nimi {:fi "Testinimike"
                                                 :en "Test qualification title"}})})))))
