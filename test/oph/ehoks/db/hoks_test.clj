(ns oph.ehoks.db.hoks-test
  (:require [oph.ehoks.db.hoks :as h]
            [clojure.test :as t]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(t/deftest replace-with-in-test
  (t/testing "Empty replace"
    (t/is (empty? (db-ops/replace-with-in
                    {:hello {}}
                    [:hello :world]
                    :hello-world)))))
