(ns oph.ehoks.hoks.handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils :refer [eq]]
            [oph.ehoks.db.memory :as db]
            [oph.ehoks.db.migrations :as m]))

(def url "/ehoks-backend/api/v1/hoks")

; TODO Change to use OHJ auth
; TODO Test also role access
; TODO update tests to use real-like data
; TODO add test for removing at update (for example ppto)

(defn with-database [f]
  (f)
  (m/clean!)
  (m/migrate!))

(defn create-db [f]
  (m/migrate!)
  (f)
  (m/clean!))

(use-fixtures :each with-database)

(use-fixtures :once create-db)

(defn create-app [session-store]
  (common-api/create-app handler/app-routes session-store))

(defn get-authenticated [url]
  (-> (utils/with-service-ticket
        (create-app nil)
        (mock/request :get url))
      :body
      utils/parse-body))

(defn create-hoks []
  (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                   :oppija-oid "1.2.246.562.24.12312312312"
                   :laatija {:nimi "Teppo Tekijä"}
                   :paivittaja {:nimi "Pekka Päivittäjä"}
                   :hyvaksyja {:nimi "Heikki Hyväksyjä"}
                   :ensikertainen-hyvaksyminen "2018-12-15"}]
    (-> (create-app nil)
        (utils/with-service-ticket
          (-> (mock/request :post url)
              (mock/json-body hoks-data)))
        :body
        utils/parse-body
        (get-in [:data :uri])
        get-authenticated
        :data)))

(defmacro with-hoks [hoks & body]
  `(let [~hoks (create-hoks)]
     (do ~@body)))

(defn get-hoks-url [hoks path]
  (format "%s/%d/%s" url (:id hoks) path))

(defn get-new-hoks-url [path]
  (with-hoks hoks (get-hoks-url hoks path)))

(deftest post-and-get-ppto
  (testing "GET newly created puuttuva paikallinen tutkinnon osa"
    (with-hoks
      hoks
      (let [ppto-data {:nimi "222"
                       :osaamisen-hankkimistavat []
                       :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000001"
                       :hankitun-osaamisen-naytto
                       [{:jarjestaja {:oppilaitos-oid
                                      "1.2.246.562.10.00000000002"}
                         :koulutuksen-jarjestaja-arvioijat []
                         :osa-alueet []
                         :keskeiset-tyotehtavat-naytto []
                         :nayttoymparisto {:nimi "aaa"}
                         :alku "2018-12-12"
                         :loppu "2018-12-20"
                         :tyoelama-arvioijat [{:nimi "Nimi" :organisaatio
                                               {:nimi "Organisaation nimi"}}]}]}
            ppto-response
            (utils/with-service-ticket
              (create-app nil)
              (-> (mock/request
                    :post
                    (get-hoks-url hoks "puuttuva-paikallinen-tutkinnon-osa"))
                  (mock/json-body ppto-data)))
            body (utils/parse-body (:body ppto-response))]
        (is (= (:status ppto-response) 200))
        (eq body {:data
                  {:uri
                   (get-hoks-url hoks "puuttuva-paikallinen-tutkinnon-osa/1")}
                  :meta {}})
        (let [ppto-new
              (utils/with-service-ticket
                (create-app nil)
                (mock/request
                  :get
                  (get-hoks-url hoks "puuttuva-paikallinen-tutkinnon-osa/1")))]
          (eq
            (:data (utils/parse-body (:body ppto-new)))
            (assoc
              ppto-data
              :id 1)))))))

(deftest patch-all-ppto
  (testing "PATCH all puuttuva paikallinen tutkinnon osa"
    (with-hoks
      hoks
      (let [ppto-data {:nimi "222"
                       :osaamisen-hankkimistavat []
                       :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000001"
                       :hankitun-osaamisen-naytto
                       [{:jarjestaja {:oppilaitos-oid
                                      "1.2.246.562.10.00000000002"}
                         :koulutuksen-jarjestaja-arvioijat []
                         :osa-alueet []
                         :keskeiset-tyotehtavat-naytto []
                         :nayttoymparisto {:nimi "aaa"}
                         :alku "2018-12-12"
                         :loppu "2018-12-20"
                         :tyoelama-arvioijat [{:nimi "Nimi" :organisaatio
                                               {:nimi "Organisaation nimi"}}]}]}
            ppto-response
            (utils/with-service-ticket
              (create-app nil)
              (-> (mock/request
                    :post
                    (get-hoks-url hoks "puuttuva-paikallinen-tutkinnon-osa"))
                  (mock/json-body ppto-data)))
            patch-response
            (utils/with-service-ticket
              (create-app nil)
              (->
                (mock/request
                  :patch
                  (get-hoks-url hoks "puuttuva-paikallinen-tutkinnon-osa/1"))
                (mock/json-body
                  {:id 1
                   :nimi "222"
                   :osaamisen-hankkimistavat []
                   :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000003"
                   :hankitun-osaamisen-naytto
                   [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.00000000004"}
                     :koulutuksen-jarjestaja-arvioijat []
                     :osa-alueet []
                     :keskeiset-tyotehtavat-naytto []
                     :nayttoymparisto {:nimi "aaaf"}
                     :alku "2018-12-14"
                     :loppu "2018-12-22"
                     :tyoelama-arvioijat [{:nimi "Nimi" :organisaatio
                                           {:nimi "Organisaation nimi"}}]}]})))]
        (is (= (:status patch-response) 204))))))

(deftest patch-one-ppto
  (testing "PATCH one value puuttuva paikallinen tutkinnon osa"
    (with-hoks
      hoks
      (let [ppto-data {:nimi "222"
                       :osaamisen-hankkimistavat []
                       :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000001"
                       :hankitun-osaamisen-naytto
                       [{:jarjestaja {:oppilaitos-oid
                                      "1.2.246.562.10.00000000002"}
                         :nayttoymparisto {:nimi "aaa"}
                         :koulutuksen-jarjestaja-arvioijat []
                         :osa-alueet []
                         :keskeiset-tyotehtavat-naytto []
                         :alku "2018-12-12"
                         :loppu "2018-12-20"
                         :tyoelama-arvioijat [{:nimi "Nimi" :organisaatio
                                               {:nimi "Organisaation nimi"}}]}]}
            ppto-response
            (utils/with-service-ticket
              (create-app nil)
              (-> (mock/request
                    :post
                    (get-hoks-url hoks "puuttuva-paikallinen-tutkinnon-osa"))
                  (mock/json-body ppto-data)))
            ppto-body (utils/parse-body
                        (:body ppto-response))
            patch-response
            (utils/with-service-ticket
              (create-app nil)
              (-> (mock/request
                    :patch
                    (get-hoks-url hoks "puuttuva-paikallinen-tutkinnon-osa/1"))
                  (mock/json-body
                    {:id 1 :nimi "2223"})))
            get-response (-> (get-in ppto-body [:data :uri])
                             get-authenticated :data)]
        (is (= (:status patch-response) 204))
        (eq get-response
            (assoc ppto-data
                   :id 1
                   :nimi "2223"))))))

(def pao-path "puuttuva-ammatillinen-osaaminen")
(def pao-data {:tutkinnon-osa-koodi-uri "tutkinnonosat_300268"
               :tutkinnon-osa-koodi-versio 1
               :osaamisen-hankkimistavat
               [{:alku "2018-12-12"
                 :loppu "2018-12-20"
                 :ajanjakson-tarkenne "Tarkenne tässä"
                 :osaamisen-hankkimistapa-koodi-uri
                 "osaamisenhankkimistapa_koulutussopimus"
                 :osaamisen-hankkimistapa-koodi-versio 1}]
               :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000005"})

(deftest post-and-get-pao
  (testing "POST puuttuva ammatillinen osaaminen and then get the created ppao"
    (db/clear)
    (let [post-response
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request
                  :post
                  (format
                    "%s/1/puuttuva-ammatillinen-osaaminen"
                    url))
                (mock/json-body
                  pao-data)))
          get-response  (utils/with-service-ticket
                          (create-app nil)
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
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request
                  :post
                  (format
                    "%s/1/puuttuva-ammatillinen-osaaminen"
                    url))
                (mock/json-body
                  pao-data)))
          put-response
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request
                  :put
                  (format
                    "%s/1/%s/1"
                    url pao-path))
                (mock/json-body
                  (assoc
                    pao-data
                    :id 1
                    :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000001"))))
          get-response  (utils/with-service-ticket
                          (create-app nil)
                          (mock/request
                            :get
                            (format
                              "%s/1/%s/1"
                              url pao-path)))]
      (is (= (:status put-response) 204))
      (eq (utils/parse-body
            (:body get-response))
          {:meta {} :data
           (assoc
             pao-data
             :id 1
             :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000001")}))))

(def patch-all-pao-data
  {:tutkinnon-osa-koodi-uri "tutkinnonosat_3002681"
   :tutkinnon-osa-koodi-versio 1
   :osaamisen-hankkimistavat
   [{:alku "2018-12-11"
     :loppu "2018-12-21"
     :ajanjakson-tarkenne "Tarkenne tässä uusi"
     :osaamisen-hankkimistapa-koodi-uri
     "osaamisenhankkimistapa_koulutussopimus1"
     :osaamisen-hankkimistapa-koodi-versio 1}]
   :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000006"})

(deftest patch-all-pao
  (testing "PATCH ALL puuttuva ammatillinen osaaminen"
    (db/clear)
    (let [post-response
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request
                  :post
                  (format
                    "%s/1/puuttuva-ammatillinen-osaaminen"
                    url))
                (mock/json-body
                  pao-data)))
          patch-response
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/%s/1"
                    url pao-path))
                (mock/json-body
                  (assoc patch-all-pao-data :id 1))))
          get-response  (utils/with-service-ticket
                          (create-app nil)
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
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request
                  :post
                  (format
                    "%s/1/puuttuva-ammatillinen-osaaminen"
                    url))
                (mock/json-body
                  pao-data)))
          response
          (utils/with-service-ticket
            (create-app nil)
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
  {:osa-alueet
   [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ku"
     :osa-alue-koodi-versio 1
     :osaamisen-hankkimistavat
     [{:alku "2018-12-15"
       :loppu "2018-12-23"
       :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_oppisopimus"
       :osaamisen-hankkimistapa-koodi-versio 1}]
     :hankitun-osaamisen-naytto
     [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.00000000002"}
       :nayttoymparisto {:nimi "aaa"}
       :alku "2018-12-12"
       :loppu "2018-12-20"
       :tyoelama-arvioijat [{:nimi "Nimi" :organisaatio
                             {:nimi "Organisaation nimi"}}]}]}]
   :tutkinnon-osa-koodi-uri "tutkinnonosat_3002683"
   :tutkinnon-osa-koodi-versio 1
   :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000007"})

(def pyto-patch-data
  {:tutkinnon-osa-koodi-uri "tutkinnonosat_3002683"
   :tutkinnon-osa-koodi-versio 1
   :osa-alueet
   [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ku"
     :osa-alue-koodi-versio 1
     :osaamisen-hankkimistavat
     [{:alku "2018-12-13"
       :loppu "2018-12-22"
       :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_oppisopimus"
       :osaamisen-hankkimistapa-koodi-versio 1}]
     :hankitun-osaamisen-naytto
     [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.00000000008"}
       :nayttoymparisto {:nimi "aaa2"}
       :alku "2018-12-15"
       :loppu "2018-12-21"
       :tyoelama-arvioijat [{:nimi "Nimi" :organisaatio
                             {:nimi "Organisaation nimi"}}]}]}]
   :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000009"})

(def pyto-patch-one-data
  {:tutkinnon-osa-koodi-uri "tutkinnonosat_3002683"
   :tutkinnon-osa-koodi-versio 1
   :osa-alueet
   [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ku"
     :osa-alue-koodi-versio 1
     :osaamisen-hankkimistavat
     [{:alku "2018-12-13"
       :loppu "2018-12-22"
       :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_oppisopimus"
       :osaamisen-hankkimistapa-koodi-versio 1}]
     :hankitun-osaamisen-naytto
     [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.00000000010"}
       :nayttoymparisto {:nimi "aaa2"}
       :alku "2018-12-15"
       :loppu "2018-12-21"
       :tyoelama-arvioijat [{:nimi "Nimi" :organisaatio
                             {:nimi "Organisaation nimi"}}]}]}]
   :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000011"})

(deftest post-and-get-pyto
  (testing "POST puuttuvat yhteisen tutkinnon osat"
    (db/clear)
    (let [post-response
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s"
                    url pyto-path))
                (mock/json-body pyto-data)))
          get-response
          (utils/with-service-ticket
            (create-app nil)
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
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s"
                    url pyto-path))
                (mock/json-body pyto-data)))
          response
          (utils/with-service-ticket
            (create-app nil)
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
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s"
                    url pyto-path))
                (mock/json-body pyto-data)))
          response
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/%s/1"
                    url pyto-path))
                (mock/json-body
                  {:koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000012"})))]
      (is (= (:status response) 204)))))

(deftest patch-all-pyto
  (testing "PATCH all puuttuvat yhteisen tutkinnon osat"
    (db/clear)
    (let [post-response
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s"
                    url pyto-path))
                (mock/json-body pyto-data)))
          response
          (utils/with-service-ticket
            (create-app nil)
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
                 :alku "2018-12-12"
                 :loppu "2018-12-20"})

(deftest post-and-get-ovatu
  (testing "GET opiskeluvalmiuksia tukevat opinnot"
    (db/clear)
    (let [post-response
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s"
                    url ovatu-path))
                (mock/json-body ovatu-data)))
          get-response
          (utils/with-service-ticket
            (create-app nil)
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
                  :alku "2018-12-12"
                  :loppu "2018-12-20"}
           :meta {}}))))

(deftest put-ovatu
  (testing "PUT opiskeluvalmiuksia tukevat opinnot"
    (db/clear)
    (let [post-response
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s"
                    url ovatu-path))
                (mock/json-body ovatu-data)))
          put-response
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request
                  :put
                  (format
                    "%s/1/%s/1"
                    url ovatu-path))
                (mock/json-body
                  {:id 1
                   :nimi "Uusi nimi"
                   :kuvaus "Uusi kuvaus"
                   :alku "2018-12-15"
                   :loppu "2018-12-25"})))]
      (is (= (:status put-response) 204)))))

(deftest patch-one-ovatu
  (testing "PATCH one value opiskeluvalmiuksia tukevat opinnot"
    (db/clear)
    (let [post-response
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s"
                    url ovatu-path))
                (mock/json-body ovatu-data)))
          patch-response
          (utils/with-service-ticket
            (create-app nil)
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
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request
                  :post
                  (format
                    "%s/1/%s"
                    url ovatu-path))
                (mock/json-body ovatu-data)))
          patch-response
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request
                  :patch
                  (format
                    "%s/1/%s/1"
                    url ovatu-path))
                (mock/json-body
                  {:id 1
                   :nimi "Uusi nimi"
                   :kuvaus "Uusi kuvaus"
                   :alku "2018-12-11"
                   :loppu "2018-12-21"})))]
      (is (= (:status patch-response) 204)))))

(defn add-empty-hoks-values [hoks]
  (assoc
    hoks
    :olemassa-olevat-ammatilliset-tutkinnon-osat []
    :puuttuvat-paikalliset-tutkinnon-osat []
    :puuttuvat-ammatilliset-tutkinnon-osat []
    :olemassa-olevat-yhteiset-tutkinnon-osat []
    :puuttuvat-yhteiset-tutkinnon-osat []
    :olemassa-olevat-paikalliset-tutkinnon-osat []
    :opiskeluvalmiuksia-tukevat-opinnot []))

(deftest get-created-hoks
  (testing "GET newly created HOKS"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :laatija {:nimi "Teppo Tekijä"}
                     :paivittaja {:nimi "Pekka Päivittäjä"}
                     :hyvaksyja {:nimi "Heikki Hyväksyjä"}
                     :ensikertainen-hyvaksyminen "2018-12-15"}
          response
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request :post url)
                (mock/json-body hoks-data)))
          body (utils/parse-body (:body response))]
      (is (= (:status response) 200))
      (eq body {:data {:uri (format "%s/1" url)} :meta {}})
      (let [hoks (-> (get-in body [:data :uri]) get-authenticated :data)]
        (eq
          hoks
          (assoc (add-empty-hoks-values hoks-data)
                 :id 1
                 :eid (:eid hoks)))))))

(deftest prevent-creating-hoks-with-existing-opiskeluoikeus
  (testing "Prevent POST HOKS with existing opiskeluoikeus"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :laatija {:nimi "Teppo Tekijä"}
                     :paivittaja {:nimi "Pekka Päivittäjä"}
                     :hyvaksyja {:nimi "Heikki Hyväksyjä"}
                     :ensikertainen-hyvaksyminen "2018-12-15"}]
      (utils/with-service-ticket
        (create-app nil)
        (-> (mock/request :post url)
            (mock/json-body hoks-data)))
      (let [response
            (utils/with-service-ticket
              (create-app nil)
              (-> (mock/request :post url)
                  (mock/json-body hoks-data)))]
        (is (= (:status response) 400))))))

(deftest prevent-creating-unauthorized-hoks
  (testing "Prevent POST unauthorized HOKS"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000002"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :laatija {:nimi "Teppo Tekijä"}
                     :paivittaja {:nimi "Pekka Päivittäjä"}
                     :hyvaksyja {:nimi "Heikki Hyväksyjä"}
                     :ensikertainen-hyvaksyminen "2018-12-15"}]
      (let [response
            (utils/with-service-ticket
              (create-app nil)
              (-> (mock/request :post url)
                  (mock/json-body hoks-data)))]
        (is (= (:status response) 401))))))

(deftest prevent-getting-unauthorized-hoks
  (testing "Prevent GET unauthorized HOKS"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000002"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :laatija {:nimi "Teppo Tekijä"}
                     :paivittaja {:nimi "Pekka Päivittäjä"}
                     :hyvaksyja {:nimi "Heikki Hyväksyjä"}
                     :ensikertainen-hyvaksyminen "2018-12-15"}]

      (let [response
            (utils/with-service-ticket
              (create-app nil)
              (-> (mock/request :post url)
                  (mock/json-body hoks-data))
              "1.2.246.562.10.12944436166")
            body (utils/parse-body (:body response))]
        (is (= (:status
                 (utils/with-service-ticket
                   (create-app nil)
                   (mock/request :get (get-in body [:data :uri]))))
               401))))))

(deftest get-last-version-of-hoks
  (testing "GET latest (second) version of HOKS"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :laatija {:nimi "Teppo Tekijä"}
                     :paivittaja {:nimi "Pekka Päivittäjä"}
                     :hyvaksyja {:nimi "Heikki Hyväksyjä"}
                     :ensikertainen-hyvaksyminen "2018-12-15"}]
      (let [response
            (utils/with-service-ticket
              (create-app nil)
              (-> (mock/request :post url)
                  (mock/json-body hoks-data)))
            body (utils/parse-body (:body response))]
        (is (= (:status response) 200))
        (eq body {:data {:uri (format "%s/1" url)} :meta {}})
        (let [hoks (-> (get-in body [:data :uri]) get-authenticated :data)]
          (is (= (count (:eid hoks)) 36))
          (eq
            hoks
            (assoc (add-empty-hoks-values hoks-data)
                   :id 1
                   :eid (:eid hoks))))))))

(deftest patch-created-hoks
  (testing "PATCH updates value of created HOKS"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :laatija {:nimi "Teppo Tekijä"}
                     :paivittaja {:nimi "Pekka Päivittäjä"}
                     :hyvaksyja {:nimi "Heikki Hyväksyjä"}
                     :ensikertainen-hyvaksyminen "2018-12-15"}
          response
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request :post url)
                (mock/json-body hoks-data)))
          body (utils/parse-body (:body response))]
      (let [hoks (-> (get-in body [:data :uri]) get-authenticated :data)
            patch-response
            (utils/with-service-ticket
              (create-app nil)
              (-> (mock/request :patch (get-in body [:data :uri]))
                  (mock/json-body
                    {:id (:id hoks)
                     :paivittaja {:nimi "Kalle Käyttäjä"}})))]
        (is (= (:status patch-response) 204))
        (let [updated-hoks
              (-> (get-in body [:data :uri]) get-authenticated :data)]
          (eq
            updated-hoks
            (assoc
              hoks
              :paivittaja {:nimi "Kalle Käyttäjä"})))))))

(deftest patch-non-existing-hoks
  (testing "PATCH prevents updating non existing HOKS"
    (let [response
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request :patch (format "%s/1" url))
                (mock/json-body {:id 1
                                 :paivittaja {:nimi "Kalle Käyttäjä"}})))]
      (is (= (:status response) 404)))))
