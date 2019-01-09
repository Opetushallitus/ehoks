(ns oph.ehoks.hoks.handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.handler :refer [app]]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils :refer [eq]]
            [clj-time.core :as t])
  (:import (java.time LocalDate)))

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

(def pyto-path "puuttuvat-yhteisen-tutkinnon-osat")

(deftest get-pyto
  (testing "GET puuttuvat yhteisen tutkinnon osat"
    (let [response
          (utils/with-authentication
            app
            (mock/request
              :get
              (format
                "%s/1/%s/1"
                url pyto-path)))]
      (is (= (:status response) 200))
      (eq (utils/parse-body
            (:body response))
          {:data {:eid 1
                  :eperusteet-id 1
                  :tutkinnon-osat []
                  :koulutuksen-jarjestaja-oid "1"}
           :meta {}}))))

(deftest post-pyto
  (testing "POST puuttuvat yhteisen tutkinnon osat"
    (let [response
          (utils/with-authentication
            app
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s/"
                    url pyto-path))
                (mock/json-body
                  {:eperusteet-id 1
                   :tutkinnon-osat []
                   :koulutuksen-jarjestaja-oid "1"})))]
      (is (= (:status response) 200))
      (eq (utils/parse-body
            (:body response))
          {:data {:uri ""} :meta {}}))))

(deftest put-pyto
  (testing "PUT puuttuvat yhteisen tutkinnon osat"
    (let [response
          (utils/with-authentication
            app
            (-> (mock/request
                  :put
                  (format
                    "%s/1/%s/1"
                    url pyto-path))
                (mock/json-body
                  {:eid 1
                   :eperusteet-id 1
                   :tutkinnon-osat []
                   :koulutuksen-jarjestaja-oid "1"})))]
      (is (= (:status response) 204)))))

(deftest patch-one-pyto
  (testing "PATCH one value puuttuvat yhteisen tutkinnon osat"
    (let [response
          (utils/with-authentication
            app
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/%s/1"
                    url pyto-path))
                (mock/json-body
                  {:eid 1
                   :koulutuksen-jarjestaja-oid "123"})))]
      (is (= (:status response) 204)))))

(deftest patch-all-pyto
  (testing "PATCH all puuttuvat yhteisen tutkinnon osat"
    (let [response
          (utils/with-authentication
            app
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/%s/1"
                    url pyto-path))
                (mock/json-body
                  {:eid 1
                   :eperusteet-id 1
                   :tutkinnon-osat []
                   :koulutuksen-jarjestaja-oid "1"})))]
      (is (= (:status response) 204)))))

(def ovatu-path "opiskeluvalmiuksia-tukevat-opinnot")

(deftest get-ovatu
  (testing "GET opiskeluvalmiuksia tukevat opinnot"
    (let [response
          (utils/with-authentication
            app
            (mock/request
              :get
              (format
                "%s/1/%s/1"
                url ovatu-path)))]
      (is (= (:status response) 200))
      (eq (utils/parse-body
            (:body response))
          {:data {:eid 1
                  :nimi ""
                  :kuvaus ""
                  :kesto 1
                  :ajankohta {:alku "2018-12-12"
                              :loppu "2018-12-20"}}
           :meta {}}))))

(deftest post-ovatu
  (testing "POST opiskeluvalmiuksia tukevat opinnot"
    (let [response
          (utils/with-authentication
            app
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s/"
                    url ovatu-path))
                (mock/json-body
                  {:nimi ""
                   :kuvaus ""
                   :kesto 1
                   :ajankohta {:alku "2018-12-12"
                               :loppu "2018-12-20"}})))]
      (is (= (:status response) 200))
      (eq (utils/parse-body
            (:body response))
          {:data {:uri ""} :meta {}}))))

(deftest put-ovatu
  (testing "PUT opiskeluvalmiuksia tukevat opinnot"
    (let [response
          (utils/with-authentication
            app
            (-> (mock/request
                  :put
                  (format
                    "%s/1/%s/1"
                    url ovatu-path))
                (mock/json-body
                  {:eid 1
                   :nimi ""
                   :kuvaus ""
                   :kesto 1
                   :ajankohta {:alku "2018-12-12"
                               :loppu "2018-12-20"}})))]
      (is (= (:status response) 204)))))

(deftest patch-one-ovatu
  (testing "PATCH one value opiskeluvalmiuksia tukevat opinnot"
    (let [response
          (utils/with-authentication
            app
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/%s/1"
                    url ovatu-path))
                (mock/json-body
                  {:eid 1
                   :nimi ""})))]
      (is (= (:status response) 204)))))

(deftest patch-all-ovatu
  (testing "PATCH all opiskeluvalmiuksia tukevat opinnot"
    (let [response
          (utils/with-authentication
            app
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/%s/1"
                    url ovatu-path))
                (mock/json-body
                  {:eid 1
                   :nimi ""
                   :kuvaus ""
                   :kesto 1
                   :ajankohta {:alku "2018-12-12"
                               :loppu "2018-12-20"}})))]
      (is (= (:status response) 204)))))
