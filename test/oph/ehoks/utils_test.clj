(ns oph.ehoks.utils-test
  (:require [clojure.test :refer [are deftest testing]]
            [oph.ehoks.utils :as utils]))

(deftest test-replace-in
  (let [test-map {:a 1 :b {:c 2} :d 3}]
    (testing "The function works as expected in normal cases."
      (are [sks dks expected] (= (utils/replace-in test-map sks dks)
                                 expected)
        :a :c         {:b {:c 2} :c 1 :d 3}
        :a [:b :c]    {:b {:c 1} :d 3}
        :a [:b :e]    {:b {:c 2 :e 1} :d 3}
        [:b :c] :e    {:a 1 :d 3 :e 2}
        [:b :c] :a    {:a 2 :d 3}
        :b :e         {:a 1 :d 3 :e {:c 2}}))
    (testing (str "The function returns the map unchanged if no value is found "
                  "with given source keys.")
      (are [sks dks] (= (utils/replace-in test-map sks dks)
                        test-map)
        :f      :a
        [:b :a] :a))))
