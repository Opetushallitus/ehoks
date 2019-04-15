(ns oph.ehoks.db.hoks-test
  (:require [oph.ehoks.db.hoks :as h]
            [clojure.test :as t]))

(t/deftest replace-with-in-test
  (t/testing "Empty replace"
    (t/is (empty? (h/replace-with-in
                    {:hello {}}
                    [:hello :world]
                    :hello-world)))))
