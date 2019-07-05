(ns oph.ehoks.external.koski-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.external.http-client :as client]
            [cheshire.core :as json]))

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

(deftest test-get-oppija-opiskeluoikeudet
  (testing "Get opiskeluoikeudet for oppija"
    (client/set-post!
      (fn [url options]
        (cond
          (.endsWith
            url "/koski/api/sure/oids")
          (let [oids (json/parse-string (:body options))]
            {:status 200
             :body (map
                     (fn [oid]
                       {:henkilö {:oid oid}
                        :opiskeluoikeudet
                                 [{:oid "1.2.246.562.15.55003456345"
                                   :oppilaitos
                                        {:oid "1.2.246.562.10.12944436166"}}
                                  {:oid "1.2.246.562.15.55003456345"
                                   :oppilaitos
                                        {:oid "1.2.246.562.10.12944436166"}}]})
                        oids)}))))

    (is (= (k/get-oppija-opiskeluoikeudet "1.2.246.562.24.51659804532")
           [{:oid "1.2.246.562.15.55003456345"
             :oppilaitos
                  {:oid "1.2.246.562.10.12944436166"}}
            {:oid "1.2.246.562.15.55003456345"
             :oppilaitos
                  {:oid "1.2.246.562.10.12944436166"}}]))
    (client/reset-functions!)))
