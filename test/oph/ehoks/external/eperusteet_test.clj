(ns oph.ehoks.external.eperusteet-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.external.eperusteet :as ep]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.external.cache :as c]
            [clojure.java.io :as io]
            [clojure.string]
            [cheshire.core :as cheshire]))

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
      [(fn [^String url options]
         (cond
           (and (.endsWith url "/external/perusteet")
                (= "koulutuksenosattuva_104"
                   (get-in options [:query-params :koodi])))
           {:status 200
            :body {:data [{:id 7534950}]}}
           (.endsWith url "/external/peruste/7534950/koulutuksenOsat")
           {:status 200
            :body [{:id 7535567
                    :viiteId 7654321
                    :nimi {:_id "8332155"
                           :fi "Valinnaiset koulutuksen osat"
                           :sv "Valbara utbildningsdelar"}
                    :nimiKoodi {:nimi {:fi "Valinnaiset opinnot"
                                       :sv "Valbara utbildningsdelar"}
                                :uri "koulutuksenosattuva_104"}}]}))]
      (is (= [{:id 7535567
               :nimi {:fi "Valinnaiset opinnot"
                      :sv "Valbara utbildningsdelar"}
               :osaamisalat []
               :koulutuksenOsaViiteId 7654321}]
             (ep/get-koulutuksenOsa-by-koodiUri "koulutuksenosattuva_104"))))))

(deftest peruste->koulutuksenOsa-with-different-codes
  (testing "Transform correctly a peruste that has the searched-for code"
    (let [peruste-test-data
          (get-mock-eperusteet-value "mock/eperusteet-peruste-7534950.json")]
      (is (= (ep/peruste->koulutuksenOsa
               peruste-test-data
               "koulutuksenosattuva_102")
             [{:id 7535564,
               :nimi {:fi "Työelämätaidot ja työelämässä tapahtuva oppiminen",
                      :sv "Arbetslivsfärdigheter och lärande i arbetslivet"},
               :osaamisalat (),
               :koulutuksenOsaViiteId 7535295}]))
      (is (= (ep/peruste->koulutuksenOsa peruste-test-data "tataeiole") nil)))))

(deftest caches-eperusteet-external-api-requests
  (testing "Caches eperusteet external API requests"
    (let [call-count (atom 0)]
      (client/with-mock-responses
        [(fn [^String url options]
           (swap! call-count inc)
           (cond
             (and (.endsWith url "/external/perusteet")
                  (= "koulutuksenosattuva_104"
                     (get-in options [:query-params :koodi])))
             {:status 200
              :body {:data [{:id 7534950}]}}
             (.endsWith url "/external/peruste/7534950/koulutuksenOsat")
             {:status 200
              :body [{:id 7535567
                      :nimi {:_id "8332155"
                             :fi "Valinnaiset koulutuksen osat"
                             :sv "Valbara utbildningsdelar"}
                      :nimiKoodi {:nimi {:fi "Valinnaiset opinnot"
                                         :sv "Valbara utbildningsdelar"}
                                  :uri "koulutuksenosattuva_104"}}]}))]
        (is (every? true?
                    (repeatedly
                      5
                      #(= [{:id 7535567
                            :nimi {:fi "Valinnaiset opinnot"
                                   :sv "Valbara utbildningsdelar"}
                            :osaamisalat []
                            :koulutuksenOsaViiteId nil}]
                          (ep/get-koulutuksenOsa-by-koodiUri
                            "koulutuksenosattuva_104")))))
        (is (= 2 @call-count))))))

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
      [(fn [_ __]
         ;; will use the same response as both find-perusteet-external
         ;; and get-peruste-by-id response
         {:status 200
          :body {:data [{:id "foo1"}]  ; for find-perusteet-external
                 :tutkinnonOsat        ; for get-peruste-by-id
                 (get-mock-eperusteet-value
                   "mock/eperusteet-tutkinnonosa-asteikko3.json")}})]
      (let [response (ep/find-tutkinnon-osat "tutkinnonosat_106003")
            adjusted (ep/adjust-tutkinnonosa-arviointi response)]
        (is (= [["9" nil] ["6" nil] ["5" nil] ["7" nil] ["8" nil]]
               (->> response first :arviointi :arvioinninKohdealueet
                    first :arvioinninKohteet first :osaamistasonKriteerit
                    (map (juxt :_osaamistaso :osaamistaso)))))
        (is (= [["9" "5"] ["5" "1"] ["7" "3"]]
               (->> adjusted first :arviointi :arvioinninKohdealueet
                    first :arvioinninKohteet first :osaamistasonKriteerit
                    (map (juxt :_osaamistaso :osaamistaso))))))))

  (testing "Osaamistason kriteerit for arviointiAsteikko 2 are transformed and
            empty cells are dropped"
    (client/with-mock-responses
      [(fn [_ __]
         {:status 200
          :body {:data [{:id "foo2"}]  ; for find-perusteet-external
                 :tutkinnonOsat        ; for get-peruste-by-id
                 (get-mock-eperusteet-value
                   "mock/eperusteet-tutkinnonosa-asteikko2.json")}})]
      (let [response (ep/find-tutkinnon-osat "tutkinnonosat_100054")
            adjusted (ep/adjust-tutkinnonosa-arviointi response)]
        (is (= [["2" nil] ["3" nil] ["4" nil]]
               (->> response first :arviointi :arvioinninKohdealueet
                    first :arvioinninKohteet first :osaamistasonKriteerit
                    (map (juxt :_osaamistaso :osaamistaso)))))
        (is (= [["2" "1"] ["3" "3"] ["4" "5"]]
               (->> adjusted first :arviointi :arvioinninKohdealueet
                    first :arvioinninKohteet first :osaamistasonKriteerit
                    (map (juxt :_osaamistaso :osaamistaso))))))))

  (testing "Osaamistason kriteerit for arviointiAsteikko 1 are transformed and
            osaamistaso values already there are preserved"
    (client/with-mock-responses
      [(fn [_ __]
         {:status 200
          :body {:data [{:id "foo3"}]  ; for find-perusteet-external
                 :tutkinnonOsat        ; for get-peruste-by-id
                 (get-mock-eperusteet-value
                   "mock/eperusteet-tutkinnonosa-asteikko1.json")}})]
      (let [response (ep/find-tutkinnon-osat "tutkinnonosat_300186")
            adjusted (ep/adjust-tutkinnonosa-arviointi response)]
        (is (= [["1" "Hyväksytty"]]
               (->> adjusted first :arviointi :arvioinninKohdealueet
                    second :arvioinninKohteet first :osaamistasonKriteerit
                    (map (juxt :_osaamistaso
                               (comp :arvo :koodi :osaamistaso))))))
        (is (= [["1" nil]]
               (->> response first :arviointi :arvioinninKohdealueet
                    first :arvioinninKohteet first :osaamistasonKriteerit
                    (map (juxt :_osaamistaso :osaamistaso)))))
        (is (= [["1" ""]]
               (->> adjusted first :arviointi :arvioinninKohdealueet
                    first :arvioinninKohteet first :osaamistasonKriteerit
                    (map (juxt :_osaamistaso :osaamistaso)))))))))
