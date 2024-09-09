(ns oph.ehoks.db.hoks-test
  (:require [clojure.test :as t]
            [oph.ehoks.utils :as utils]))

(t/deftest replace-with-in-test
  (t/testing "Empty replace"
    (t/is (empty? (utils/replace-with-in
                    {:hello {}}
                    [:hello :world]
                    :hello-world)))))
