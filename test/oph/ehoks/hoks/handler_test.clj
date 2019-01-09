(ns oph.ehoks.hoks.handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.handler :refer [app]]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils :refer [eq]]
            [clj-time.core :as t]
            [oph.ehoks.db.memory :as db]))

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

(defn get-authenticated [url]
  (-> (utils/with-authentication
        app
        (mock/request :get url))
      :body
      utils/parse-body))

(deftest get-created-hoks
  (testing "GET newly created HOKS"
    (db/clear)
    (let [hoks-data {:opiskeluoikeus {:oid "1.3.444.555.66.77777777777"
                                      :tutkinto {:laajuus 5 :nimi "Test"}}
                     :oppijan-oid "1.2.333.444.55.66666666666"
                     :luonut "Teppo Tekijä"
                     :opiskeluoikeus-oid "1.3.444.555.66.77777777777"
                     :paivittanyt "Pekka Päivittäjä"
                     :hyvaksynyt "Heikki Hyväksyjä"}
          response
          (utils/with-authentication
            app
            (-> (mock/request :post url)
                (mock/json-body hoks-data)))
          body (utils/parse-body (:body response))]
      (is (= (:status response) 200))
      (eq body {:data {:uri (format "%s/1" url)} :meta {}})
      (let [hoks (-> (get-in body [:data :uri]) get-authenticated :data)]
        (eq
          hoks
          (assoc
            hoks-data
            :eid 1
            :luotu (:luotu hoks)
            :hyvaksytty (:hyvaksytty hoks)
            :paivitetty (:paivitetty hoks)
            :versio 1))))))

(deftest get-last-version-of-hoks
  (testing "GET latest (second) version of HOKS"
    (db/clear)
    (let [hoks-data {:opiskeluoikeus {:oid "1.3.444.555.66.77777777777"
                                      :tutkinto {:laajuus 5 :nimi "Test"}}
                     :oppijan-oid "1.2.333.444.55.66666666666"
                     :luonut "Teppo Tekijä"
                     :opiskeluoikeus-oid "1.3.444.555.66.77777777777"
                     :paivittanyt "Pekka Päivittäjä"
                     :hyvaksynyt "Heikki Hyväksyjä"}]
      (utils/with-authentication
        app
        (-> (mock/request :post url)
            (mock/json-body hoks-data)))
      (let [response
            (utils/with-authentication
              app
              (-> (mock/request :post url)
                  (mock/json-body hoks-data)))
            body (utils/parse-body (:body response))]
        (is (= (:status response) 200))
        (eq body {:data {:uri (format "%s/1" url)} :meta {}})
        (let [hoks (-> (get-in body [:data :uri]) get-authenticated :data)]
          (eq
            hoks
            (assoc
              hoks-data
              :eid 1
              :luotu (:luotu hoks)
              :hyvaksytty (:hyvaksytty hoks)
              :paivitetty (:paivitetty hoks)
              :versio 2)))))))

(deftest put-created-hoks
  (testing "PUT updates created HOKS"
    (db/clear)
    (let [hoks-data {:opiskeluoikeus {:oid "1.3.444.555.66.77777777777"
                                      :tutkinto {:laajuus 5 :nimi "Test"}}
                     :oppijan-oid "1.2.333.444.55.66666666666"
                     :luonut "Teppo Tekijä"
                     :opiskeluoikeus-oid "1.3.444.555.66.77777777777"
                     :paivittanyt "Pekka Päivittäjä"
                     :hyvaksynyt "Heikki Hyväksyjä"}
          response
          (utils/with-authentication
            app
            (-> (mock/request :post url)
                (mock/json-body hoks-data)))
          body (utils/parse-body (:body response))]
      (let [hoks (-> (get-in body [:data :uri]) get-authenticated :data)
            put-response
            (utils/with-authentication
              app
              (-> (mock/request :put (get-in body [:data :uri]))
                  (mock/json-body
                    (-> hoks
                        (assoc :paivittanyt "Teuvo Testaaja")
                        (dissoc :luonut :luotu :versio :paivitetty)))))]
        (is (= (:status put-response) 204))
        (let [updated-hoks
              (-> (get-in body [:data :uri]) get-authenticated :data)]
          (eq
            updated-hoks
            (assoc
              hoks
              :paivitetty (:paivitetty updated-hoks)
              :versio 2
              :paivittanyt "Teuvo Testaaja")))))))
