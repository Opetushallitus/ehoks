(ns oph.ehoks.utils-test
  (:require [clojure.test :refer [are deftest is testing]]
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

(deftest test-distinct-vals
  (testing "Returns distinct values for a key"
    (is (= (set (utils/distinct-vals :a [{:a 1} {:a 2} {:a 1}]))
           #{1 2})))

  (testing "Ignores nil values"
    (is (= (set (utils/distinct-vals :a [{:a nil} {:a 1} {:a nil} {:a 2}]))
           #{1 2})))

  (testing "Handles missing keys"
    (is (= (set (utils/distinct-vals :a [{:a 1} {:b 2} {:a 3}]))
           #{1 3})))

  (testing "Returns empty collection if no values"
    (is (= (utils/distinct-vals :a [{:b 2} {:b 3}])
           '())))

  (testing "Handles empty input"
    (is (= (utils/distinct-vals :a [])
           '())))

  (testing "Handles all nils"
    (is (= (utils/distinct-vals :a [{:a nil} {:a nil}])
           '()))))
