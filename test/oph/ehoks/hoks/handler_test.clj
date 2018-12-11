(ns oph.ehoks.hoks.handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.handler :refer [app]]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils]
            [clj-time.core :as t]))

(def url "/ehoks-backend/api/v1/hoks")

; TODO Change to use OHJ auth
; TODO Test also role access
; TODO update tests to use real-like data

(deftest post-ppto
  (testing "GET puuttuva paikallinen tutkinnon osa"
    (let [response
          (utils/with-authentication
            app
            (mock/request
                 :post
                 (format
                   "%s/1/puuttuva-paikallinen-tutkinnon-osa"
                   url)))]
      (is (= (:status response) 200))
      (is (= (utils/parse-body
               (:body response))
             {:data [] :meta {}})))))

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
      (is (= (utils/parse-body
               (:body response))
             {:data {:uri ""} :meta {}})))))

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

(deftest put-all-ppto
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

(deftest put-one-ppto
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
