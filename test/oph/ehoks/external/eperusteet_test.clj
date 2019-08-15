(ns oph.ehoks.external.eperusteet-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.external.eperusteet :as ep]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.external.cache :as c]))

(defn with-clear-cache [t]
  (c/clear-cache!)
  (t)
  (c/clear-cache!))

(use-fixtures :each with-clear-cache)

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
              :tutkintonimikkeet
              (vector
                {:nimi {:fi "Testinimike"
                        :en "Test qualification title"}})})))))

(deftest find-perusteet-not-found
  (testing "Not finding any perusteet items"
    (client/with-mock-responses
      [(fn [_ __] {:status 200
                   :body {:data []
                          :sivuja 0
                          :kokonaismäärä 0
                          :sivukoko 25
                          :sivu 0}})]
      (is (= (ep/search-perusteet-info "no-found") [])))))

(deftest get-perusteet-not-found
  (testing "Not getting any perusteet items"
    (client/with-mock-responses
      [(fn [_ __] (throw (ex-info "HTTP Exception" {:status 404})))]
      (is (thrown? clojure.lang.ExceptionInfo (ep/get-perusteet 100000))))))

(deftest find-tutkinnon-osat-not-found
  (testing "Not findind any tutkinnon osat items"
    (client/with-mock-responses
      [(fn [_ __] {:status 200
                   :body {:data []
                          :sivuja 0
                          :kokonaismäärä 0
                          :sivukoko 25
                          :sivu 0}})]
      (is (= (ep/find-tutkinnon-osat "tutkinnonosat_404") [])))))

(deftest get-tutkinnon-osa-vitteet-not-found
  (testing "Not getting any tutkinnon osa viitteet items"
    (client/with-mock-responses
      [(fn [_ __] (throw (ex-info
                           "HTTP Exception"
                           {:status 400 :body {:koodi 400
                                               :syy "tutkinnon-osaa-ei-ole"}})))]
      (is (thrown? clojure.lang.ExceptionInfo (ep/get-tutkinnon-osa-viitteet 100000))))))

(deftest find-tutkinto-not-found
  (testing "Not finding any tutkinto items"
    (client/with-mock-responses
      [(fn [_ __] (throw (ex-info "HTTP Exception" {:status 404})))]
      (is (thrown? clojure.lang.ExceptionInfo (ep/find-tutkinto "no-found"))))))

(deftest get-suoritustavat-not-found
  (testing "Not finding any suoritustavat items"
    (client/with-mock-responses
      [(fn [_ __] (throw (ex-info
                           "HTTP Exception"
                           {:status 404 :body {:syy "Tilaa ei asetettu"
                                               :koodi "404"}})))]
      (is (thrown? clojure.lang.ExceptionInfo (ep/get-suoritustavat 100000))))))