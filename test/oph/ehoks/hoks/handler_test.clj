(ns oph.ehoks.hoks.handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.handler :refer [app]]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils :refer [eq]]
            [clj-time.core :as t]
            [oph.ehoks.db.memory :as db]
            [oph.ehoks.external.http-client :as client]))

(def url "/ehoks-backend/api/v1/hoks")

; TODO Change to use OHJ auth
; TODO Test also role access
; TODO update tests to use real-like data

(defn get-authenticated [url]
  (-> (utils/with-authentication
        app
        (mock/request :get url))
      :body
      utils/parse-body))

(deftest post-and-get-ppto
  (testing "GET newly created puuttuva paikallinen tutkinnon osa"
    (db/clear)
    (let [ppto-data   {:nimi "222"
                       :laajuus 0
                       :kuvaus "fef"
                       :osaamisen-hankkimistavat []
                       :koulutuksen-jarjestaja-oid "124"
                       :hankitun-osaamisen-naytto
                       {:jarjestaja {:nimi "abc"}
                        :nayttoymparisto {:nimi "aaa"}
                        :kuvaus "fff"
                        :ajankohta {:alku "2018-12-12"
                                    :loppu "2018-12-20"}
                        :sisalto "sisalto"
                        :ammattitaitovaatimukset []
                        :arvioijat []}}
          ppto-response
          (utils/with-authentication
            app
            (-> (mock/request :post (format
                                      "%s/1/puuttuva-paikallinen-tutkinnon-osa"
                                      url))
                (mock/json-body ppto-data)))
          body (utils/parse-body (:body ppto-response))]
      (is (= (:status ppto-response) 200))
      (eq body {:data {:uri (format "%s/1/puuttuva-paikallinen-tutkinnon-osa/1"
                                    url)} :meta {}})
      (let [ppto-new (utils/with-authentication
                       app
                       (mock/request
                         :get (format
                                "%s/1/puuttuva-paikallinen-tutkinnon-osa/1"
                                url)))]
        (eq
          (:data (utils/parse-body (:body ppto-new)))
          (assoc
            ppto-data
            :id 1))))))

(deftest put-ppto
  (testing "PUT puuttuva paikallinen tutkinnon osa"
    (db/clear)
    (let [ppto-data   {:nimi "22992"
                       :laajuus 0
                       :kuvaus "fef"
                       :osaamisen-hankkimistavat []
                       :koulutuksen-jarjestaja-oid "124"
                       :hankitun-osaamisen-naytto
                       {:jarjestaja {:nimi "abc"}
                        :nayttoymparisto {:nimi "aaa"}
                        :kuvaus "ppp"
                        :ajankohta {:alku "2018-12-12"
                                    :loppu "2018-12-20"}
                        :sisalto "sisalto"
                        :ammattitaitovaatimukset []
                        :arvioijat []}}
          ppto-response
          (utils/with-authentication
            app
            (-> (mock/request :post (format
                                      "%s/1/puuttuva-paikallinen-tutkinnon-osa"
                                      url))
                (mock/json-body ppto-data)))
          put-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :put
                  (format
                    "%s/1/puuttuva-paikallinen-tutkinnon-osa/1"
                    url))
                (mock/json-body
                  {:id 1
                   :amosaa-tunniste 11
                   :nimi "333"
                   :laajuus 3
                   :kuvaus "fef"
                   :osaamisen-hankkimistavat []
                   :koulutuksen-jarjestaja-oid "333"
                   :hankitun-osaamisen-naytto
                   {:jarjestaja {:nimi "abc"}
                    :nayttoymparisto {:nimi "aaa"}
                    :kuvaus "fff"
                    :ajankohta {:alku "2018-12-12"
                                :loppu "2018-12-20"}
                    :sisalto "sisalto"
                    :ammattitaitovaatimukset []
                    :arvioijat []}})))]
      (is (= (:status put-response) 204)))))

(deftest patch-all-ppto
  (testing "PATCH all puuttuva paikallinen tutkinnon osa"
    (db/clear)
    (let [ppto-data   {:nimi "22992"
                       :laajuus 0
                       :kuvaus "fef"
                       :osaamisen-hankkimistavat []
                       :koulutuksen-jarjestaja-oid "124"
                       :hankitun-osaamisen-naytto
                       {:jarjestaja {:nimi "abc"}
                        :nayttoymparisto {:nimi "aaa"}
                        :kuvaus "ppp"
                        :ajankohta {:alku "2018-12-12"
                                    :loppu "2018-12-20"}
                        :sisalto "sisalto"
                        :ammattitaitovaatimukset []
                        :arvioijat []}}
          ppto-response
          (utils/with-authentication
            app
            (-> (mock/request :post (format
                                      "%s/1/puuttuva-paikallinen-tutkinnon-osa"
                                      url))
                (mock/json-body ppto-data)))
          patch-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/puuttuva-paikallinen-tutkinnon-osa/1"
                    url))
                (mock/json-body
                  {:id 1
                   :amosaa-tunniste 1
                   :nimi ""
                   :laajuus 0
                   :kuvaus ""
                   :osaamisen-hankkimistavat []
                   :koulutuksen-jarjestaja-oid "1214"
                   :hankitun-osaamisen-naytto
                   {:jarjestaja {:nimi "a1bc"}
                    :nayttoymparisto {:nimi "a1aa"}
                    :kuvaus "ppp"
                    :ajankohta {:alku "2018-12-12"
                                :loppu "2018-12-20"}
                    :sisalto "sisalto"
                    :ammattitaitovaatimukset []
                    :arvioijat []}})))]
      (is (= (:status patch-response) 204)))))

(deftest patch-one-ppto
  (testing "PATCH one value puuttuva paikallinen tutkinnon osa"
    (db/clear)
    (let [ppto-data   {:nimi "222"
                       :laajuus 0
                       :kuvaus "fef"
                       :osaamisen-hankkimistavat []
                       :koulutuksen-jarjestaja-oid "124"
                       :hankitun-osaamisen-naytto
                       {:jarjestaja {:nimi "abc"}
                        :nayttoymparisto {:nimi "aaa"}
                        :kuvaus "fff"
                        :ajankohta {:alku "2018-12-12"
                                    :loppu "2018-12-20"}
                        :sisalto "sisalto"
                        :ammattitaitovaatimukset []
                        :arvioijat []}}
          ppto-response
          (utils/with-authentication
            app
            (-> (mock/request :post (format
                                      "%s/1/puuttuva-paikallinen-tutkinnon-osa"
                                      url))
                (mock/json-body ppto-data)))
          ppto-body (utils/parse-body
                      (:body ppto-response))
          patch-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/puuttuva-paikallinen-tutkinnon-osa/1"
                    url))
                (mock/json-body
                  {:id 1 :nimi "2223"})))
          get-response (-> (get-in ppto-body [:data :uri])
                           get-authenticated :data)]
      (is (= (:status patch-response) 204))
      (eq get-response
          (assoc ppto-data
                 :id 1
                 :nimi "2223")))))

(def pao-path "puuttuva-ammatillinen-osaaminen")
(def pao-data     {:tutkinnon-osa
                   {:koodi-uri "tutkinnonosat_300268"}
                   :osaamisen-hankkimistavat
                   [{:ajankohta {:alku "2018-12-12"
                                 :loppu "2018-12-20"}
                     :osaamisen-hankkimistavan-tunniste
                     {:koodi-arvo "1"
                      :koodi-uri "esimerkki_uri"
                      :versio 1}}]
                   :koulutuksen-jarjestaja-oid "123"})

(deftest post-and-get-pao
  (testing "POST puuttuva ammatillinen osaaminen and then get the created ppao"
    (db/clear)
    (let [post-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :post
                  (format
                    "%s/1/puuttuva-ammatillinen-osaaminen"
                    url))
                (mock/json-body
                  pao-data)))
          get-response  (utils/with-authentication
                          app
                          (mock/request
                            :get
                            (format
                              "%s/1/%s/1"
                              url pao-path)))]
      (is (= (:status post-response) 200))
      (eq (utils/parse-body
            (:body post-response))
          {:meta {} :data {:uri   (format
                                    "%s/1/puuttuva-ammatillinen-osaaminen/1"
                                    url)}})
      (is (= (:status get-response) 200))
      (eq (utils/parse-body
            (:body get-response))
          {:meta {} :data (assoc pao-data :id 1)}))))

(deftest put-pao
  (testing "PUT puuttuva ammatillinen osaaminen"
    (db/clear)
    (let [post-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :post
                  (format
                    "%s/1/puuttuva-ammatillinen-osaaminen"
                    url))
                (mock/json-body
                  pao-data)))
          put-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :put
                  (format
                    "%s/1/%s/1"
                    url pao-path))
                (mock/json-body
                  (assoc pao-data :id 1 :koulutuksen-jarjestaja-oid "124"))))
          get-response  (utils/with-authentication
                          app
                          (mock/request
                            :get
                            (format
                              "%s/1/%s/1"
                              url pao-path)))]
      (is (= (:status put-response) 204))
      (eq (utils/parse-body
            (:body get-response))
          {:meta {} :data
           (assoc pao-data :id 1 :koulutuksen-jarjestaja-oid "124")}))))

(def patch-all-pao-data
  {:tutkinnon-osa
   {:koodi-uri "tutkinnonosat_300268"}
   :osaamisen-hankkimistavat
   [{:ajankohta {:alku "2018-12-12"
                 :loppu "2018-12-22"}
     :osaamisen-hankkimistavan-tunniste
     {:koodi-arvo "22"
      :koodi-uri "esimerkki_uri32"
      :versio 1}}]
   :koulutuksen-jarjestaja-oid "12432"})

(deftest patch-all-pao
  (testing "PATCH ALL puuttuva ammatillinen osaaminen"
    (db/clear)
    (let [post-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :post
                  (format
                    "%s/1/puuttuva-ammatillinen-osaaminen"
                    url))
                (mock/json-body
                  pao-data)))
          patch-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/%s/1"
                    url pao-path))
                (mock/json-body
                  (assoc patch-all-pao-data :id 1))))
          get-response  (utils/with-authentication
                          app
                          (mock/request
                            :get
                            (format
                              "%s/1/%s/1"
                              url pao-path)))]
      (is (= (:status patch-response) 204))
      (eq (utils/parse-body
            (:body get-response))
          {:meta {} :data  (assoc patch-all-pao-data :id 1)}))))

(deftest patch-one-pao
  (testing "PATCH one value puuttuva ammatillinen osaaminen"
    (db/clear)
    (let [post-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :post
                  (format
                    "%s/1/puuttuva-ammatillinen-osaaminen"
                    url))
                (mock/json-body
                  pao-data)))
          response
          (utils/with-authentication
            app
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/%s/1"
                    url pao-path))
                (mock/json-body
                  {:id 1
                   :vaatimuksista-tai-tavoitteista-poikkeaminen "Test"})))]
      (is (= (:status response) 204)))))

(def pyto-path "puuttuvat-yhteisen-tutkinnon-osat")

(def pyto-data
  {:koodi-uri "tutkinnonosat_300268"
   :osa-alueet
   [{:koodi-uri "tutkinnonosat_300268"
     :osaamisen-hankkimistavat [{:ajankohta {:alku "2018-12-15"
                                             :loppu "2018-12-23"}
                                 :osaamisen-hankkimistavan-tunniste
                                 {:koodi-arvo "31"
                                  :koodi-uri "esimerkki_uri3"
                                  :versio 3}}]
     :hankitun-osaamisen-naytto
     {:jarjestaja {:nimi "ddd"}
      :nayttoymparisto {:nimi "aaddda"}
      :kuvaus "fff"
      :ajankohta {:alku "2018-12-16"
                  :loppu "2018-12-26"}
      :sisalto "sisalto uusi"
      :arvioijat [{:nimi "Nimi2"
                   :rooli {:koodi-arvo "2"
                           :koodi-uri "esimerkki_uri2"
                           :versio 1}
                   :organisaatio {:nimi "aaa2"}}]}}]
   :koulutuksen-jarjestaja-oid "1234"})

(def pyto-patch-data
  {:koodi-uri "tutkinnonosat_300268"
   :osa-alueet
   [{:koodi-uri "tutkinnonosat_300268"
     :osaamisen-hankkimistavat [{:ajankohta {:alku "2018-12-15"
                                             :loppu "2018-12-23"}
                                 :osaamisen-hankkimistavan-tunniste
                                 {:koodi-arvo "31"
                                  :koodi-uri "esimerkki_uri3"
                                  :versio 3}}]
     :hankitun-osaamisen-naytto
     {:jarjestaja {:nimi "ddd"}
      :nayttoymparisto {:nimi "aaddda"}
      :kuvaus "fff"
      :ajankohta {:alku "2018-12-16"
                  :loppu "2018-12-26"}
      :sisalto "sisalto uusi"
      :arvioijat [{:nimi "Nimi2"
                   :rooli {:koodi-arvo "2"
                           :koodi-uri "esimerkki_uri2"
                           :versio 1}
                   :organisaatio {:nimi "aaa2"}}]}}]
   :koulutuksen-jarjestaja-oid "1234"})

(deftest post-and-get-pyto
  (testing "POST puuttuvat yhteisen tutkinnon osat"
    (db/clear)
    (let [post-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s"
                    url pyto-path))
                (mock/json-body pyto-data)))
          get-response
          (utils/with-authentication
            app
            (mock/request
              :get
              (format
                "%s/1/%s/1"
                url pyto-path)))]
      (is (= (:status post-response) 200))
      (eq (utils/parse-body
            (:body post-response))
          {:data {:uri   (format
                           "%s/1/%s/1"
                           url pyto-path)} :meta {}})
      (is (= (:status get-response) 200))
      (eq (:id (:data (utils/parse-body
                        (:body get-response))))
          1))))

(deftest put-pyto
  (testing "PUT puuttuvat yhteisen tutkinnon osat"
    (db/clear)
    (let [post-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s"
                    url pyto-path))
                (mock/json-body pyto-data)))
          response
          (utils/with-authentication
            app
            (-> (mock/request
                  :put
                  (format
                    "%s/1/%s/1"
                    url pyto-path))
                (mock/json-body
                  (assoc pyto-data :id 1))))]
      (is (= (:status response) 204)))))

(deftest patch-one-pyto
  (testing "PATCH one value puuttuvat yhteisen tutkinnon osat"
    (db/clear)
    (let [post-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s"
                    url pyto-path))
                (mock/json-body pyto-data)))
          response
          (utils/with-authentication
            app
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/%s/1"
                    url pyto-path))
                (mock/json-body
                  {:id 1
                   :koulutuksen-jarjestaja-oid "123"})))]
      (is (= (:status response) 204)))))

(deftest patch-all-pyto
  (testing "PATCH all puuttuvat yhteisen tutkinnon osat"
    (db/clear)
    (let [post-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s"
                    url pyto-path))
                (mock/json-body pyto-data)))
          response
          (utils/with-authentication
            app
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/%s/1"
                    url pyto-path))
                (mock/json-body
                  pyto-patch-data)))]
      (is (= (:status response) 204)))))

(def ovatu-path "opiskeluvalmiuksia-tukevat-opinnot")
(def ovatu-data {:nimi "Nimi"
                 :kuvaus "Kuvaus"
                 :kesto 10
                 :ajankohta {:alku "2018-12-12"
                             :loppu "2018-12-20"}})

(deftest post-and-get-ovatu
  (testing "GET opiskeluvalmiuksia tukevat opinnot"
    (db/clear)
    (let [post-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s"
                    url ovatu-path))
                (mock/json-body ovatu-data)))
          get-response
          (utils/with-authentication
            app
            (mock/request
              :get
              (format
                "%s/1/%s/1"
                url ovatu-path)))]
      (is (= (:status post-response) 200))
      (eq (utils/parse-body
            (:body post-response))
          {:data {:uri (format
                         "%s/1/%s/1"
                         url ovatu-path)} :meta {}})
      (is (= (:status get-response) 200))
      (eq (utils/parse-body
            (:body get-response))
          {:data {:id 1
                  :nimi "Nimi"
                  :kuvaus "Kuvaus"
                  :kesto 10
                  :ajankohta {:alku "2018-12-12"
                              :loppu "2018-12-20"}}
           :meta {}}))))

(deftest put-ovatu
  (testing "PUT opiskeluvalmiuksia tukevat opinnot"
    (db/clear)
    (let [post-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s"
                    url ovatu-path))
                (mock/json-body ovatu-data)))
          put-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :put
                  (format
                    "%s/1/%s/1"
                    url ovatu-path))
                (mock/json-body
                  {:id 1
                   :nimi "Uusi nimi"
                   :kuvaus "Uusi kuvaus"
                   :kesto 2
                   :ajankohta {:alku "2018-12-15"
                               :loppu "2018-12-25"}})))]
      (is (= (:status put-response) 204)))))

(deftest patch-one-ovatu
  (testing "PATCH one value opiskeluvalmiuksia tukevat opinnot"
    (db/clear)
    (let [post-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s"
                    url ovatu-path))
                (mock/json-body ovatu-data)))
          patch-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/%s/1"
                    url ovatu-path))
                (mock/json-body
                  {:id 1
                   :nimi "Uusi nimi"})))]
      (is (= (:status patch-response) 204)))))

(deftest patch-all-ovatu
  (testing "PATCH all opiskeluvalmiuksia tukevat opinnot"
    (db/clear)
    (let [post-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s"
                    url ovatu-path))
                (mock/json-body ovatu-data)))
          patch-response
          (utils/with-authentication
            app
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/%s/1"
                    url ovatu-path))
                (mock/json-body
                  {:id 1
                   :nimi "Uusi nimi"
                   :kuvaus "Uusi kuvaus"
                   :kesto 10
                   :ajankohta {:alku "2018-12-11"
                               :loppu "2018-12-21"}})))]
      (is (= (:status patch-response) 204)))))

(deftest get-created-hoks
  (testing "GET newly created HOKS"
    (db/clear)
    (let [hoks-data {:opiskeluoikeus {:oid "1.3.444.555.66.77777777777"
                                      :tutkinto {:laajuus 5 :nimi "Test"}}
                     :oppija-oid "1.2.333.444.55.66666666666"
                     :luonut "Teppo Tekijä"
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
            :id 1
            :luotu (:luotu hoks)
            :hyvaksytty (:hyvaksytty hoks)
            :paivitetty (:paivitetty hoks)
            :versio 1))))))

(deftest get-last-version-of-hoks
  (testing "GET latest (second) version of HOKS"
    (db/clear)
    (let [hoks-data {:opiskeluoikeus {:oid "1.3.444.555.66.77777777777"
                                      :tutkinto {:laajuus 5 :nimi "Test"}}
                     :oppija-oid "1.2.333.444.55.66666666666"
                     :luonut "Teppo Tekijä"
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
              :id 1
              :luotu (:luotu hoks)
              :hyvaksytty (:hyvaksytty hoks)
              :paivitetty (:paivitetty hoks)
              :versio 2)))))))

(deftest put-created-hoks
  (testing "PUT updates created HOKS"
    (db/clear)
    (let [hoks-data {:opiskeluoikeus {:oid "1.3.444.555.66.77777777777"
                                      :tutkinto {:laajuus 5 :nimi "Test"}}
                     :oppija-oid "1.2.333.444.55.66666666666"
                     :luonut "Teppo Tekijä"
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

(deftest put-non-existing-hoks
  (testing "PUT prevents updating non existing HOKS"
    (db/clear)
    (let [hoks-data {:opiskeluoikeus {:oid "1.3.444.555.66.77777777777"
                                      :tutkinto {:laajuus 5 :nimi "Test"}}
                     :oppija-oid "1.2.333.444.55.66666666666"
                     :paivittanyt "Teuvo Testaaja"
                     :hyvaksytty (java.util.Date.)
                     :id 1
                     :hyvaksynyt "Heikki Hyväksyjä"}
          response
          (utils/with-authentication
            app
            (-> (mock/request :put (format "%s/1" url))
                (mock/json-body hoks-data)))]
      (is (= (:status response) 404)))))

(deftest patch-created-hoks
  (testing "PATCH updates value of created HOKS"
    (db/clear)
    (let [hoks-data {:opiskeluoikeus {:oid "1.3.444.555.66.77777777777"
                                      :tutkinto {:laajuus 5 :nimi "Test"}}
                     :oppija-oid "1.2.333.444.55.66666666666"
                     :luonut "Teppo Tekijä"
                     :paivittanyt "Pekka Päivittäjä"
                     :hyvaksynyt "Heikki Hyväksyjä"}
          response
          (utils/with-authentication
            app
            (-> (mock/request :post url)
                (mock/json-body hoks-data)))
          body (utils/parse-body (:body response))]
      (let [hoks (-> (get-in body [:data :uri]) get-authenticated :data)
            patch-response
            (utils/with-authentication
              app
              (-> (mock/request :patch (get-in body [:data :uri]))
                  (mock/json-body
                    {:id (:id hoks)
                     :paivittanyt "Kalle Käyttäjä"})))]
        (is (= (:status patch-response) 204))
        (let [updated-hoks
              (-> (get-in body [:data :uri]) get-authenticated :data)]
          (eq
            updated-hoks
            (assoc
              hoks
              :paivitetty (:paivitetty updated-hoks)
              :versio 2
              :paivittanyt "Kalle Käyttäjä")))))))

(deftest patch-non-existing-hoks
  (testing "PATCH prevents updating non existing HOKS"
    (db/clear)
    (let [response
          (utils/with-authentication
            app
            (-> (mock/request :patch (format "%s/1" url))
                (mock/json-body {:id 1
                                 :paivittanyt "Kalle Käyttäjä"})))]
      (is (= (:status response) 404)))))
