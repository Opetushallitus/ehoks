(ns oph.ehoks.logging.audit-test
  (:require [clojure.data.json :as json]
            [clojure.test :refer [are deftest is testing use-fixtures]]
            [clojure.tools.logging.test :refer [matches with-log]]
            [oph.ehoks.hoks.hoks-parts.parts-test-data
             :refer [ahato-data multiple-ahato-values-patched]]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils]
            [oph.ehoks.logging.audit :as a]
            [oph.ehoks.test-utils :as test-utils]))

(use-fixtures :once test-utils/migrate-database)

(def ahato-path "aiemmin-hankittu-ammat-tutkinnon-osa")

(declare expected-log-entry-on-hoks-creation)

(deftest test-handle-audit-logging
  (with-log
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/create-mock-post-request ahato-path ahato-data app hoks)
      (let [[hoks-create _]
            (map #(-> % :message json/read-str (dissoc "bootTime"))
                 (matches "audit" :info #""))
            logseq (get hoks-create "logSeq")]
        (is (= hoks-create (expected-log-entry-on-hoks-creation  logseq))))
      (test-utils/clear-db))))

(deftest test-changes
  (testing "Single element replaced in map"
    (is (= (#'a/changes {:a 1 :b 2 :c 3} {:a 1 :b -2 :c 3})
           '({"path" ":b" "oldValue" 2 "newValue" -2}))))

  (testing "Single element added to map"
    (is (= (#'a/changes {:a 1 :b 2} {:a 1 :b 2 :c 3})
           '({"path" ":c" "newValue" 3}))))

  (testing "Single element removed from map"
    (is (= (#'a/changes {:a 1 :b 2 :c 3} {:a 1 :c 3})
           '({"path" ":b" "oldValue" 2}))))

  (testing "Changes in nested data structures (within map) are detected"
    (is (= (#'a/changes {:a 1 :b {:c 2 :d  3} :e 4 :f [0  1 2]}
                        {:a 1 :b {:c 2 :d -3} :e 4 :f [0 -1 2]})
           '({"path" ":b.:d" "oldValue" 3 "newValue" -3}
              {"path" ":f.1"  "oldValue" 1 "newValue" -1}))))

  (testing "Single element replaced in vector"
    (is (= (#'a/changes [0 1 2] [0 3 2])
           '({"path" "1" "oldValue" 1 "newValue" 3}))))

  (testing "Single element added to the end of the vector"
    (is (= (#'a/changes [0 1 2] [0 1 2 3]) '({"path" "3" "newValue" 3}))))

  (testing "Single element removed from the end of the vector"
    (is (= (#'a/changes [0 1 2 3] [0 1 2]) '({"path" "3" "oldValue" 3}))))

  (testing "Single removal from the middle of the vector"
    (is (= (#'a/changes [0 1 2 3 4] [0 1 3 4])
           '({"path" "2" "oldValue" 2 "newValue" 3}
              {"path" "3" "oldValue" 3 "newValue" 4}
              {"path" "4" "oldValue" 4}))))

  (testing "Single addition to the middle of the vector"
    (is (= (#'a/changes [0 1 3 4] [0 1 2 3 4])
           '({"path" "2" "oldValue" 3 "newValue" 2}
              {"path" "3" "oldValue" 4 "newValue" 3}
              {"path" "4" "newValue" 4}))))

  (testing "Changes in nested data structures (within vector) are detected"
    (is (= (#'a/changes [0 {:a 1 :b 2} 3 [4 5 6] 7]
                        [0 {:a 1 :b -2} 3 [4 -5 6] 7])
           '({"path" "1.:b" "oldValue" 2 "newValue" -2}
              {"path" "3.1" "oldValue" 5 "newValue" -5}))))

  (testing "Value is added when old value is `nil`"
    (are [new] (get (first (#'a/changes nil new)) "newValue")
      1 "a" true {:a 1} [0 1]))
  (testing "Value is removed when new value is `nil`"
    (are [old] (get (first (#'a/changes old nil)) "oldValue")
      1 "a" true {:a 1} [0 1]))
  (testing (str "Value is replaced when the old and the new value are unequal
                but both are not maps or vectors.")
    (are [old new] (as-> (#'a/changes old new) c
                     (first c)
                     (and (= (get c "oldValue") old)
                          (= (get c "newValue") new)))
      "kissa"     "koira"
      1           2
      1           "1"
      true        1
      false       0
      :a          "a"
      \a          "a"
      1.0         1
      1/2         0.5
      {:a 1 :b 2} [1 2])))

(deftest test-make-json-serializable
  (testing "The function returns (expected) values that are JSON serializable"
    (are [obj processed] (= (#'a/make-json-serializable obj) processed)
      nil               nil
      true              true
      "cat"             "cat"
      23                23
      1.2               1.2
      1/5               1/5
      :key              ":key"
      'symbol           "symbol"
      {:key 'symbol}    {:key "symbol"}
      [:key 'symbol 23] [":key" "symbol" 23])))

(defn- expected-log-entry-on-hoks-creation
  [logseq]
  {"hostname" ""
   "logSeq" logseq
   "user"
   {"oid" "1.2.246.562.24.11474338834"
    "ipAddress" "127.0.0.1"
    "session" "ST-testitiketti"
    "userAgent" "no user agent"}
   "status" "succeeded"
   "applicationType" "backend"
   "operation" "create"
   "serviceName" "both"
   "changes"
   [{"path" ""
     "newValue"
     {"id" 1
      "ensikertainen-hyvaksyminen" "2019-03-18"
      "osaamisen-hankkimisen-tarve" false
      "opiskeluoikeus-oid" "1.2.246.562.15.10000000009"
      "oppija-oid" "1.2.246.562.24.12312312319"}}]
   "type" "log"
   "version" 1
   "target" {"hoksId" 1
             "oppijaOid" "1.2.246.562.24.12312312319"
             "opiskeluoikeusOid" "1.2.246.562.15.10000000009"}})
