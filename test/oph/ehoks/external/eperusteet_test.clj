(ns oph.ehoks.external.eperusteet-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.external.eperusteet :as ep]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.external.cache :as c]
            [clojure.java.io :as io]
            [cheshire.core :as cheshire]
            [com.rpl.specter :as spc :refer [ALL]]))

(defn with-clear-cache [t]
  (c/clear-cache!)
  (t)
  (c/clear-cache!))

(use-fixtures :each with-clear-cache)

(defn get-mock-eperusteet-value [file]
  (-> (io/resource file)
      slurp
      (cheshire/parse-string true)))

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
                           {:status 400
                            :body {:koodi 400
                                   :syy "tutkinnon-osaa-ei-ole"}})))]
      (is (thrown? clojure.lang.ExceptionInfo
                   (ep/get-tutkinnon-osa-viitteet 100000))))))

(deftest get-koulutuksenOsa-by-koodiUri-not-found
  (testing "Not getting koulutuksenOsa by koodiUri items"
    (client/with-mock-responses
      [(fn [_ __] {:status 200
                   :body {:data []
                          :sivuja 0
                          :kokonaismäärä 0
                          :sivukoko 25
                          :sivu 0}})]
      (is (thrown? clojure.lang.ExceptionInfo
                   (ep/get-koulutuksenOsa-by-koodiUri
                     "koulutuksenosattuva_404"))))))

(deftest uses-nimi-from-nimiKoodi
  (testing "Uses nimi object from nimiKoodi"
    (client/with-mock-responses
      [(fn [url options]
         (cond
           (and (.endsWith url "/external/perusteet")
                (= "koulutuksenosattuva_104"
                   (get-in options [:query-params :koodi])))
           {:status 200
            :body {:data [{:id 7534950}]}}
           (.endsWith url "/external/peruste/7534950")
           {:status 200
            :body {:id 7534950
                   :koulutuksenOsat
                   [{:id 7535567
                     :nimi {:_id "8332155"
                            :fi "Valinnaiset koulutuksen osat"
                            :sv "Valbara utbildningsdelar"}
                     :nimiKoodi {:nimi {:fi "Valinnaiset opinnot"
                                        :sv "Valbara utbildningsdelar"}
                                 :uri "koulutuksenosattuva_104"}}]}}))]
     (is (= [{:id 7535567
              :nimi {:fi "Valinnaiset opinnot"
                     :sv "Valbara utbildningsdelar"}
              :osaamisalat []
              :koulutuksenOsaId "12345"}]
            (ep/get-koulutuksenOsa-by-koodiUri "koulutuksenosattuva_104"))))))

(deftest find-tutkinto-not-found
  (testing "Not finding any tutkinto items"
    (client/with-mock-responses
      [(fn [_ __] {:status 200
                   :body {:data []
                          :sivuja 0
                          :kokonaismäärä 0
                          :sivukoko 25
                          :sivu 0}})]
      (is (thrown? clojure.lang.ExceptionInfo (ep/find-tutkinto "no-found"))))))

(deftest get-suoritustavat-not-found
  (testing "Not finding any suoritustavat items"
    (client/with-mock-responses
      [(fn [_ __] (throw (ex-info
                           "HTTP Exception"
                           {:status 404 :body {:syy "Tilaa ei asetettu"
                                               :koodi "404"}})))]
      (is (thrown? clojure.lang.ExceptionInfo
                   (ep/get-rakenne 100000 "reformi"))))))

(deftest transform-arviointikriteerit
  (testing "Osaamistasot for arviointiAsteikko 3 are transformed and
            empty kriteerit are dropped"
    (client/with-mock-responses
      [(fn [_ __] {:status 200
                   :body {:data (get-mock-eperusteet-value
                                  "mock/eperusteet-tutkinnonosa-asteikko3.json")
                          :sivuja 0
                          :kokonaismäärä 0
                          :sivukoko 25
                          :sivu 0}})]
      (let [response (ep/find-tutkinnon-osat "tutkinnonosat_asteikko3")
            adjusted (ep/adjust-tutkinnonosa-arviointi response)
            osaamistasot (spc/select [ALL :arviointi :arvioinninKohdealueet
                                      ALL :arvioinninKohteet ALL
                                      :osaamistasonKriteerit ALL :_osaamistaso]
                                     adjusted)
            kriteerit (spc/select [ALL :arviointi :arvioinninKohdealueet
                                   ALL :arvioinninKohteet ALL
                                   :osaamistasonKriteerit ALL :kriteerit]
                                  adjusted)]
        (is (not (some #(= 7 %) osaamistasot)))
        (is (not (some #(= 9 %) osaamistasot)))
        (is (not (some #(empty? %) kriteerit))))))

  (testing "Osaamistason kriteerit for arviointiAsteikko 2 are transformed and
            empty cells are dropped"
    (client/with-mock-responses
      [(fn [_ __] {:status 200
                   :body {:data (get-mock-eperusteet-value
                                  "mock/eperusteet-tutkinnonosa-asteikko2.json")
                          :sivuja 0
                          :kokonaismäärä 0
                          :sivukoko 25
                          :sivu 0}})]
      (let [response (ep/find-tutkinnon-osat "tutkinnonosat_asteikko2")
            adjusted (ep/adjust-tutkinnonosa-arviointi response)
            osaamistasot (spc/select [ALL :arviointi :arvioinninKohdealueet
                                      ALL :arvioinninKohteet ALL
                                      :osaamistasonKriteerit ALL :_osaamistaso]
                                     adjusted)
            kriteerit (spc/select [ALL :arviointi :arvioinninKohdealueet
                                   ALL :arvioinninKohteet ALL
                                   :osaamistasonKriteerit ALL :kriteerit]
                                  adjusted)]
        (is (not (some #(= 2 %) osaamistasot)))
        (is (not (some #(= 4 %) osaamistasot)))
        (is (not (some #(empty? %) kriteerit))))))

  (testing "Osaamistason kriteerit for arviointiAsteikko 1 are transformed and
            empty cells are dropped"
    (client/with-mock-responses
      [(fn [_ __] {:status 200
                   :body {:data (get-mock-eperusteet-value
                                  "mock/eperusteet-tutkinnonosa-asteikko1.json")
                          :sivuja 0
                          :kokonaismäärä 0
                          :sivukoko 25
                          :sivu 0}})]
      (let [response (ep/find-tutkinnon-osat "tutkinnonosat_asteikko1")
            adjusted (ep/adjust-tutkinnonosa-arviointi response)
            osaamistasot (spc/select [ALL :arviointi :arvioinninKohdealueet
                                      ALL :arvioinninKohteet ALL
                                      :osaamistasonKriteerit ALL :_osaamistaso]
                                     adjusted)
            kriteerit (spc/select [ALL :arviointi :arvioinninKohdealueet
                                   ALL :arvioinninKohteet ALL
                                   :osaamistasonKriteerit ALL :kriteerit]
                                  adjusted)]
        (is (not (some #(not (clojure.string/blank? %)) osaamistasot)))
        (is (not (some #(empty? %) kriteerit)))))))
