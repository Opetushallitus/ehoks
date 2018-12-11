(ns oph.ehoks.hoks.handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.handler :refer [app]]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils :refer [eq]]
            [clj-time.core :as t]))

(def url "/ehoks-backend/api/v1/hoks")

; TODO Change to use OHJ auth
; TODO Test also role access
; TODO update tests to use real-like data

(deftest get-ppto
  (testing "GET puuttuva paikallinen tutkinnon osa"
    (let [response
          (utils/with-authentication
            app
            (mock/request
              :get
              (format
                "%s/1/puuttuva-paikallinen-tutkinnon-osa/1"
                url)))]
      (is (= (:status response) 200))
      (eq (utils/parse-body
            (:body response))
          {:data {:eid 1
                  :amosaa-tunniste ""
                  :nimi ""
                  :laajuus 0
                  :kuvaus ""
                  :osaamisen-hankkimistavat []
                  :koulutuksen-jarjestaja-oid ""
                  :hankitun-osaamisen-naytto
                  {:jarjestaja {:nimi ""}
                   :nayttoymparisto {:nimi ""}
                   :kuvaus ""
                   :ajankohta {:alku "2018-12-12"
                               :loppu "2018-12-20"}
                   :sisalto ""
                   :ammattitaitovaatimukset []
                   :arvioijat []}
                  :tarvittava-opetus ""}
           :meta {}}))))

(deftest post-ppto
  (testing "POST puuttuva paikallinen tutkinnon osa"
    (let [response
          (utils/with-authentication
            app
            (-> (mock/request
                  :post
                  (format
                    "%s/1/puuttuva-paikallinen-tutkinnon-osa"
                    url))
                (mock/json-body
                  {:amosaa-tunniste ""
                   :nimi ""
                   :laajuus 0
                   :kuvaus ""
                   :osaamisen-hankkimistavat []
                   :koulutuksen-jarjestaja-oid ""
                   :hankitun-osaamisen-naytto
                   {:jarjestaja {:nimi ""}
                    :nayttoymparisto {:nimi ""}
                    :kuvaus ""
                    :ajankohta {:alku (t/local-date 2018 12 12)
                                :loppu (t/local-date 2018 12 20)}
                    :sisalto ""
                    :ammattitaitovaatimukset []
                    :arvioijat []}
                   :tarvittava-opetus ""})))]
      (is (= (:status response) 200))
      (eq (utils/parse-body
            (:body response))
          {:data {:uri ""} :meta {}}))))

(deftest put-ppto
  (testing "PUT puuttuva paikallinen tutkinnon osa"
    (let [response
          (utils/with-authentication
            app
            (-> (mock/request
                  :put
                  (format
                    "%s/1/puuttuva-paikallinen-tutkinnon-osa/1"
                    url))
                (mock/json-body
                  {:eid 1
                   :amosaa-tunniste ""
                   :nimi ""
                   :laajuus 0
                   :kuvaus ""
                   :osaamisen-hankkimistavat []
                   :koulutuksen-jarjestaja-oid ""
                   :hankitun-osaamisen-naytto
                   {:jarjestaja {:nimi ""}
                    :nayttoymparisto {:nimi ""}
                    :kuvaus ""
                    :ajankohta {:alku (t/local-date 2018 12 12)
                                :loppu (t/local-date 2018 12 20)}
                    :sisalto ""
                    :ammattitaitovaatimukset []
                    :arvioijat []}
                   :tarvittava-opetus ""})))]
      (is (= (:status response) 204)))))

(deftest patch-all-ppto
  (testing "PATCH all puuttuva paikallinen tutkinnon osa"
    (let [response
          (utils/with-authentication
            app
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/puuttuva-paikallinen-tutkinnon-osa/1"
                    url))
                (mock/json-body
                  {:eid 1
                   :amosaa-tunniste ""
                   :nimi ""
                   :laajuus 0
                   :kuvaus ""
                   :osaamisen-hankkimistavat []
                   :koulutuksen-jarjestaja-oid ""
                   :hankitun-osaamisen-naytto
                   {:jarjestaja {:nimi ""}
                    :nayttoymparisto {:nimi ""}
                    :kuvaus ""
                    :ajankohta {:alku (t/local-date 2018 12 12)
                                :loppu (t/local-date 2018 12 20)}
                    :sisalto ""
                    :ammattitaitovaatimukset []
                    :arvioijat []}
                   :tarvittava-opetus ""})))]
      (is (= (:status response) 204)))))

(deftest patch-one-ppto
  (testing "PATCH one value puuttuva paikallinen tutkinnon osa"
    (let [response
          (utils/with-authentication
            app
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/puuttuva-paikallinen-tutkinnon-osa/1"
                    url))
                (mock/json-body
                  {:eid 1
                   :amosaa-tunniste "1"})))]
      (is (= (:status response) 204)))))

(def pao-path "puuttuva-ammatillinen-osaaminen")

(deftest get-pao
  (testing "GET puuttuva ammatillinen osaaminen"
    (let [response
          (utils/with-authentication
            app
            (mock/request
              :get
              (format
                "%s/1/%s/1"
                url pao-path)))]
      (is (= (:status response) 200))
      (eq (utils/parse-body
            (:body response))
          {:data {:eid 1
                  :tutkinnon-osa
                  {:tunniste
                   {:koodi-arvo "1"
                    :koodi-uri "esimerkki_uri"
                    :versio 1}
                   :eperusteet-id ""}
                  :vaatimuksista-tai-tavoitteista-poikkeaminen ""
                  :osaamisen-hankkimistavat []
                  :koulutuksen-jarjestaja-oid ""
                  :tarvittava-opetus ""}
           :meta {}}))))

(deftest post-pao
  (testing "POST puuttuva ammatillinen osaaminen"
    (let [response
          (utils/with-authentication
            app
            (-> (mock/request
                  :post
                  (format
                    "%s/1/puuttuva-ammatillinen-osaaminen/"
                    url))
                (mock/json-body
                  {:tutkinnon-osa {:tunniste {:koodi-arvo "1"
                                              :koodi-uri "esimerkki_uri"
                                              :versio 1}
                                   :eperusteet-id ""}
                   :vaatimuksista-tai-tavoitteista-poikkeaminen ""
                   :osaamisen-hankkimistavat []
                   :koulutuksen-jarjestaja-oid ""
                   :tarvittava-opetus ""})))]
      (is (= (:status response) 200))
      (eq (utils/parse-body
            (:body response))
          {:data {:uri ""} :meta {}}))))

(deftest put-pao
  (testing "PUT puuttuva ammatillinen osaaminen"
    (let [response
          (utils/with-authentication
            app
            (-> (mock/request
                  :put
                  (format
                    "%s/1/%s/1"
                    url pao-path))
                (mock/json-body
                  {:eid 1
                   :tutkinnon-osa {:tunniste {:koodi-arvo "1"
                                              :koodi-uri "esimerkki_uri"
                                              :versio 1}
                                   :eperusteet-id ""}
                   :vaatimuksista-tai-tavoitteista-poikkeaminen ""
                   :osaamisen-hankkimistavat []
                   :koulutuksen-jarjestaja-oid ""
                   :tarvittava-opetus ""})))]
      (is (= (:status response) 204)))))

(deftest patch-all-pao
  (testing "PATCH all puuttuva ammatillinen osaaminen"
    (let [response
          (utils/with-authentication
            app
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/%s/1"
                    url pao-path))
                (mock/json-body
                  {:eid 1
                   :tutkinnon-osa {:tunniste {:koodi-arvo "1"
                                              :koodi-uri "esimerkki_uri"
                                              :versio 1}
                                   :eperusteet-id ""}
                   :vaatimuksista-tai-tavoitteista-poikkeaminen ""
                   :osaamisen-hankkimistavat []
                   :koulutuksen-jarjestaja-oid ""
                   :tarvittava-opetus ""})))]
      (is (= (:status response) 204)))))

(deftest patch-one-pao
  (testing "PATCH one value puuttuva ammatillinen osaaminen"
    (let [response
          (utils/with-authentication
            app
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/%s/1"
                    url pao-path))
                (mock/json-body
                  {:eid 1
                   :vaatimuksista-tai-tavoitteista-poikkeaminen "Test"})))]
      (is (= (:status response) 204)))))
