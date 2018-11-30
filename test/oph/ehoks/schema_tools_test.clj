(ns oph.ehoks.schema-tools-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.schema-tools :as st]
            [schema.core :as s]))

(s/defschema
  TestSchema
  "Original Test Schema"
  {:key1 s/Str
   :key2 s/Int
   (s/optional-key :key3) s/Bool})

(s/defschema
  ModifiedTestSchema
  "Modified Test Schema"
  {:key1 s/Str
   :key2 s/Int
   (s/optional-key :key3) s/Bool})

(s/defschema
  ExtraOptional
  "Schema with one extra optional and first removed"
  {(s/optional-key :key2) s/Int
   (s/optional-key :key3) s/Bool})

(deftest test-modify
  (testing "Modify schema"
    (is (= ModifiedTestSchema
           (st/modify TestSchema "Modified Test Schema")))
    (is (= ExtraOptional
           (st/modify
             TestSchema
             "Schema with one extra optional and first removed"
             {:removed [:key1]
              :optionals [:key2 :key3]})))))
