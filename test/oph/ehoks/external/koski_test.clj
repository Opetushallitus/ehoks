(ns oph.ehoks.external.koski-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.koski :as k]))

(deftest test-filter-oppija
  (testing "Filtering Oppija values"
    (is (= (k/filter-oppija {}) {:henkilö {}}))
    (is (= (k/filter-oppija
             {:henkilö {:oid "1.2.246.562.24.44651722625"
                        :hetu "250103-5360"
                        :syntymäaika "1903-01-25"
                        :etunimet "Aarto Maurits"
                        :kutsumanimi "Aarto"
                        :sukunimi "Väisänen-perftest"
                        :turvakielto false}
              :opiskeluoikeudet [{}]})
           {:henkilö {:oid "1.2.246.562.24.44651722625"
                      :hetu "250103-5360"
                      :syntymäaika "1903-01-25"
                      :etunimet "Aarto Maurits"
                      :kutsumanimi "Aarto"
                      :sukunimi "Väisänen-perftest"}
            :opiskeluoikeudet [{}]}))))
