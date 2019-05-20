(ns oph.ehoks.hoks.handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.virkailija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils :refer [eq]]
            [oph.ehoks.db.memory :as db]
            [oph.ehoks.db.migrations :as m]))

(def url "/ehoks-virkailija-backend/api/v1/hoks")

; TODO Change to use OHJ auth
; TODO Test also role access
; TODO update tests to use real-like data
; TODO add test for removing at update (for example ppto)

(defn with-database [f]
  (m/clean!)
  (m/migrate!)
  (f)
  (m/clean!))

(defn clean-db [f]
  (m/clean!)
  (m/migrate!)
  (f))

(use-fixtures :each with-database)

(use-fixtures :once clean-db)

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
    (db/clear)
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
                  :meta {:id 1}})
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
(def pao-data
  {:tutkinnon-osa-koodi-uri "tutkinnonosat_102499"
   :tutkinnon-osa-koodi-versio 4
   :vaatimuksista-tai-tavoitteista-poikkeaminen
   "Ei poikkeamia."
   :hankitun-osaamisen-naytto
   [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453924330"}
     :nayttoymparisto {:nimi "Testiympäristö 2"
                       :y-tunnus "12345671-2"
                       :kuvaus "Testi test"}
     :keskeiset-tyotehtavat-naytto ["Testaus"]
     :koulutuksen-jarjestaja-arvioijat
     [{:nimi "Timo Testaaja"
       :organisaatio {:oppilaitos-oid "1.2.246.562.10.54452521332"}}]
     :tyoelama-arvioijat
     [{:nimi "Taneli Työmies"
       :organisaatio {:nimi "Tanelin Paja Ky"
                      :y-tunnus "12345622-2"}}]
     :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_kl"
                   :koodi-versio 3}]
     :alku "2019-03-10"
     :loppu "2019-03-19"}]
   :osaamisen-hankkimistavat
   [{:jarjestajan-edustaja {:nimi "Ville Valvoja"
                            :rooli "Valvojan apulainen"
                            :oppilaitos-oid "1.2.246.562.10.54451211340"}
     :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_oppisopimus"
     :osaamisen-hankkimistapa-koodi-versio 2
     :tyopaikalla-hankittava-osaaminen
     {:vastuullinen-ohjaaja {:nimi "Aimo Ohjaaja"
                             :sahkoposti "aimo.ohjaaja@esimerkki2.com"}
      :tyopaikan-nimi "Ohjausyhtiö Oy"
      :tyopaikan-y-tunnus "12345212-4"
      :muut-osallistujat [{:organisaatio {:nimi "Kolmas Esimerkki Oy"
                                          :y-tunnus "12345233-5"}
                           :nimi "Kalle Kirjuri"
                           :rooli "Kirjuri"}]
      :keskeiset-tyotehtavat ["Testitehtävä"]
      :lisatiedot false}
     :muut-oppimisymparisto
     [{:oppimisymparisto-koodi-uri "oppimisymparistot_0002"
       :oppimisymparisto-koodi-versio 1
       :selite "Testioppilaitos 2"
       :lisatiedot false}]
     :ajanjakson-tarkenne "Ei tarkennettavia asioita"
     :hankkijan-edustaja
     {:nimi "Heikki Hankkija"
      :rooli "Opettaja"
      :oppilaitos-oid "1.2.246.562.10.54452422420"}
     :alku "2019-01-11"
     :loppu "2019-03-14"}]
   :koulutuksen-jarjestaja-oid "1.2.246.562.10.54411232222"})

(deftest post-and-get-pao
  (testing "POST puuttuva ammatillinen osaaminen and then get the created ppao"
    (with-hoks
      hoks
      (let [app (create-app nil)
            post-response
            (utils/with-service-ticket
              app
              (-> (mock/request
                    :post
                    (get-hoks-url hoks pao-path))
                  (mock/json-body pao-data)))
            get-response (utils/with-service-ticket
                           app
                           (mock/request
                             :get
                             (get-hoks-url hoks (str pao-path "/1"))))]
        (is (= (:status post-response) 200))
        (eq (utils/parse-body
              (:body post-response))
            {:meta {:id 1}
             :data {:uri
                    (format "%s/1/puuttuva-ammatillinen-osaaminen/1" url)}})
        (is (= (:status get-response) 200))
        (eq (utils/parse-body
              (:body get-response))
            {:meta {} :data (assoc pao-data :id 1)})))))

(def patch-all-pao-data
  (merge
    pao-data
    {:tutkinnon-osa-koodi-uri "tutkinnonosat_3002681"
     :tutkinnon-osa-koodi-versio 1
     :hankitun-osaamisen-naytto []
     :osaamisen-hankkimistavat
     [{:jarjestajan-edustaja
       {:nimi "Veikko Valvoja"
        :rooli "Valvoja"
        :oppilaitos-oid "1.2.246.562.10.54451211340"}
       :osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_oppisopimus"
       :osaamisen-hankkimistapa-koodi-versio 2
       :tyopaikalla-hankittava-osaaminen
       {:vastuullinen-ohjaaja
        {:nimi "Oiva Ohjaaja"
         :sahkoposti "oiva.ohjaaja@esimerkki2.com"}
        :tyopaikan-nimi "Ohjaus Oyk"
        :tyopaikan-y-tunnus "12345222-4"
        :muut-osallistujat []
        :keskeiset-tyotehtavat ["Testitehtävä2"]
        :lisatiedot false}
       :muut-oppimisymparisto []
       :ajanjakson-tarkenne "Ei ole"
       :hankkijan-edustaja
       {:nimi "Harri Hankkija"
        :rooli "Opettajan sijainen"
        :oppilaitos-oid "1.2.246.562.10.55552422420"}
       :alku "2019-01-12"
       :loppu "2019-02-11"}]
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000116"}))

(deftest patch-all-pao
  (testing "PATCH ALL puuttuva ammatillinen osaaminen"
    (with-hoks
      hoks
      (let [app (create-app nil)
            post-response
            (utils/with-service-ticket
              app
              (-> (mock/request
                    :post
                    (get-hoks-url hoks pao-path))
                  (mock/json-body pao-data)))
            patch-response
            (utils/with-service-ticket
              app
              (-> (mock/request
                    :patch
                    (get-hoks-url hoks (str pao-path "/1")))
                  (mock/json-body (assoc patch-all-pao-data :id 1))))
            get-response  (utils/with-service-ticket
                            (create-app nil)
                            (mock/request
                              :get
                              (get-hoks-url hoks (str pao-path "/1"))))]
        (is (= (:status patch-response) 204))
        (eq (utils/parse-body (:body get-response))
            {:meta {} :data  (assoc patch-all-pao-data :id 1)})))))

(deftest patch-one-pao
  (testing "PATCH one value puuttuva ammatillinen osaaminen"
    (with-hoks
      hoks
      (let [app (create-app nil)
            post-response
            (utils/with-service-ticket
              app
              (-> (mock/request
                    :post
                    (format
                      "%s/1/puuttuva-ammatillinen-osaaminen"
                      url))
                  (mock/json-body
                    pao-data)))
            response
            (utils/with-service-ticket
              app
              (-> (mock/request
                    :patch
                    (format
                      "%s/1/%s/1"
                      url pao-path))
                  (mock/json-body
                    {:id 1
                     :vaatimuksista-tai-tavoitteista-poikkeaminen "Test"})))]
        (is (= (:status response) 204))))))

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
                           url pyto-path)} :meta {:id 1}})
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
                         url ovatu-path)} :meta {:id 1}})
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
      (eq body {:data {:uri (format "%s/1" url)} :meta {:id 1}})
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
        (eq body {:data {:uri (format "%s/1" url)} :meta {:id 1}})
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

(deftest get-hoks-by-opiskeluoikeus-oid
  (testing "GET HOKS by opiskeluoikeus-oid")
  (let [opiskeluoikeus-oid "1.2.246.562.15.00000000001"
        hoks-data {:opiskeluoikeus-oid opiskeluoikeus-oid
                   :oppija-oid "1.2.246.562.24.12312312312"
                   :laatija {:nimi "Teppo Tekijä"}
                   :paivittaja {:nimi "Pekka Päivittäjä"}
                   :hyvaksyja {:nimi "Heikki Hyväksyjä"}
                   :ensikertainen-hyvaksyminen "2018-12-15"}
        app (create-app nil)]
    (utils/with-service-ticket
      app
      (-> (mock/request :post url)
          (mock/json-body hoks-data)))
    (let [response
          (utils/with-service-ticket
            app
            (mock/request :get
                          (format "%s/opiskeluoikeus/%s"
                                  url opiskeluoikeus-oid)))
          body (utils/parse-body (:body response))]

      (is (= (:status response) 200))
      (is (= (-> body
                 :data
                 :opiskeluoikeus-oid)
             opiskeluoikeus-oid)))))
