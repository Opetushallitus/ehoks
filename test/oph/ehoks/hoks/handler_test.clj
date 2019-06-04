(ns oph.ehoks.hoks.handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.virkailija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils :refer [eq]]
            [oph.ehoks.db.memory :as db]))

(def url "/ehoks-virkailija-backend/api/v1/hoks")

; TODO Change to use OHJ auth
; TODO Test also role access
; TODO update tests to use real-like data
; TODO add test for removing at update (for example ppto)

(use-fixtures :each utils/with-database)

(use-fixtures :once utils/clean-db)

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
                   :ensikertainen-hyvaksyminen "2018-12-15"
                   :osaamisen-hankkimisen-tarve false}]
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

(def ppto-data {:nimi "222"
                :osaamisen-hankkimistavat []
                :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000001"
                :olennainen-seikka true
                :osaamisen-osoittaminen
                [{:jarjestaja {:oppilaitos-oid
                               "1.2.246.562.10.00000000002"}
                  :koulutuksen-jarjestaja-osaamisen-arvioijat []
                  :osa-alueet []
                  :sisallon-kuvaus ["ensimmäinen sisältö" "toinenkin"]
                  :nayttoymparisto {:nimi "aaa"}
                  :alku "2018-12-12"
                  :loppu "2018-12-20"
                  :tyoelama-osaamisen-arvioijat [{:nimi "Nimi" :organisaatio
                                                  {:nimi "Organisaation nimi"}}]
                  :vaatimuksista-tai-tavoitteista-poikkeaminen
                  "Poikkeama onpi tämä."
                  :yksilolliset-kriteerit ["kriteeri 1" "kriteeri2"]}]})

(deftest post-and-get-ppto
  (testing "GET newly created hankittava paikallinen tutkinnon osa"
    (db/clear)
    (with-hoks
      hoks
      (let [ppto-response
            (utils/with-service-ticket
              (create-app nil)
              (-> (mock/request
                    :post
                    (get-hoks-url hoks "hankittava-paikallinen-tutkinnon-osa"))
                  (mock/json-body ppto-data)))
            body (utils/parse-body (:body ppto-response))]
        (is (= (:status ppto-response) 200))
        (eq body {:data
                  {:uri
                   (get-hoks-url hoks "hankittava-paikallinen-tutkinnon-osa/1")}
                  :meta {:id 1}})
        (let [ppto-new
              (utils/with-service-ticket
                (create-app nil)
                (mock/request
                  :get
                  (get-hoks-url hoks
                                "hankittava-paikallinen-tutkinnon-osa/1")))]
          (eq
            (:data (utils/parse-body (:body ppto-new)))
            (assoc
              ppto-data
              :id 1)))))))

(deftest patch-all-ppto
  (testing "PATCH all hankittava paikallinen tutkinnon osa"
    (with-hoks
      hoks
      (let [ppto-response
            (utils/with-service-ticket
              (create-app nil)
              (-> (mock/request
                    :post
                    (get-hoks-url hoks "hankittava-paikallinen-tutkinnon-osa"))
                  (mock/json-body ppto-data)))
            patch-response
            (utils/with-service-ticket
              (create-app nil)
              (->
                (mock/request
                  :patch
                  (get-hoks-url hoks "hankittava-paikallinen-tutkinnon-osa/1"))
                (mock/json-body
                  (assoc ppto-data :nimi "333" :olennainen-seikka false))))]
        (is (= (:status patch-response) 204))))))

(deftest patch-one-ppto
  (testing "PATCH one value hankittava paikallinen tutkinnon osa"
    (with-hoks
      hoks
      (let [ppto-response
            (utils/with-service-ticket
              (create-app nil)
              (-> (mock/request
                    :post
                    (get-hoks-url hoks "hankittava-paikallinen-tutkinnon-osa"))
                  (mock/json-body ppto-data)))
            ppto-body (utils/parse-body
                        (:body ppto-response))
            patch-response
            (utils/with-service-ticket
              (create-app nil)
              (-> (mock/request
                    :patch
                    (get-hoks-url hoks
                                  "hankittava-paikallinen-tutkinnon-osa/1"))
                  (mock/json-body
                    {:id 1 :nimi "2223"})))
            get-response (-> (get-in ppto-body [:data :uri])
                             get-authenticated :data)]
        (is (= (:status patch-response) 204))
        (eq get-response
            (assoc ppto-data
                   :id 1
                   :nimi "2223"))))))

(def pao-path "hankittava-ammat-tutkinnon-osa")

(def pao-data {:tutkinnon-osa-koodi-uri "tutkinnonosat_300268"
               :tutkinnon-osa-koodi-versio 1
               :vaatimuksista-tai-tavoitteista-poikkeaminen
               "Ei poikkeamia."
               :osaamisen-hankkimistavat
               [{:alku "2018-12-12"
                 :loppu "2018-12-20"
                 :ajanjakson-tarkenne "Tarkenne tässä"
                 :osaamisen-hankkimistapa-koodi-uri
                 "osaamisenhankkimistapa_koulutussopimus"
                 :osaamisen-hankkimistapa-koodi-versio 1
                 :muut-oppimisymparisto
                 [{:oppimisymparisto-koodi-uri "oppimisymparistot_0002"
                   :oppimisymparisto-koodi-versio 1
                   :alku "2019-03-10"
                   :loppu "2019-03-19"}]
                 :hankkijan-edustaja
                 {:nimi "Heikki Hankkija"
                  :rooli "Opettaja"
                  :oppilaitos-oid "1.2.246.562.10.54452422420"}}]
               :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000005"
               :osaamisen-osoittaminen
               [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453924330"}
                 :nayttoymparisto {:nimi "Testiympäristö 2"
                                   :y-tunnus "12345671-2"
                                   :kuvaus "Testi test"}
                 :sisallon-kuvaus ["Testaus"]
                 :koulutuksen-jarjestaja-osaamisen-arvioijat
                 [{:nimi "Timo Testaaja"
                   :organisaatio {:oppilaitos-oid
                                  "1.2.246.562.10.54452521332"}}]
                 :tyoelama-osaamisen-arvioijat
                 [{:nimi "Taneli Työmies"
                   :organisaatio {:nimi "Tanelin Paja Ky"
                                  :y-tunnus "12345622-2"}}]
                 :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_kl"
                               :koodi-versio 3}]
                 :alku "2019-03-10"
                 :loppu "2019-03-19"
                 :yksilolliset-kriteerit ["Yksi kriteeri"]}]})

(deftest post-and-get-pao
  (testing "POST hankittava ammatillinen osaaminen and then get created ppao"
    (db/clear)
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
                    (format "%s/1/hankittava-ammat-tutkinnon-osa/1" url)}})
        (is (= (:status get-response) 200))
        (eq (utils/parse-body
              (:body get-response))
            {:meta {} :data (assoc pao-data :id 1)})))))

(def patch-all-pao-data
  (merge
    pao-data
    {:tutkinnon-osa-koodi-uri "tutkinnonosat_3002681"
     :tutkinnon-osa-koodi-versio 1
     :osaamisen-osoittaminen []
     :osaamisen-hankkimistavat
     [{:jarjestajan-edustaja
       {:nimi "Veikko Valvoja"
        :rooli "Valvoja"
        :oppilaitos-oid "1.2.246.562.10.54451211340"}
       :osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_oppisopimus"
       :osaamisen-hankkimistapa-koodi-versio 2
       :tyopaikalla-jarjestettava-koulutus
       {:vastuullinen-tyopaikka-ohjaaja
        {:nimi "Oiva Ohjaaja"
         :sahkoposti "oiva.ohjaaja@esimerkki2.com"}
        :tyopaikan-nimi "Ohjaus Oyk"
        :tyopaikan-y-tunnus "12345222-4"
        :keskeiset-tyotehtavat ["Testitehtävä2"]}
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
  (testing "PATCH ALL hankittava ammat osaaminen"
    (db/clear)
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
  (testing "PATCH one value hankittava ammatillinen osaaminen"
    (db/clear)
    (with-hoks
      hoks
      (let [app (create-app nil)
            post-response
            (utils/with-service-ticket
              app
              (-> (mock/request
                    :post
                    (format
                      "%s/1/hankittava-ammat-tutkinnon-osa"
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

(def ooato-path "aiemmin-hankittu-ammat-tutkinnon-osa")
(def ooato-data
  {:valittu-todentamisen-prosessi-koodi-versio 3
   :tutkinnon-osa-koodi-versio 100022
   :valittu-todentamisen-prosessi-koodi-uri "osaamisentodentamisenprosessi_3"
   :tutkinnon-osa-koodi-uri "tutkinnonosat_100022"
   :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921419"
   :tarkentavat-tiedot-arvioija
   {:lahetetty-arvioitavaksi "2019-03-18"
    :aiemmin-hankitun-osaamisen-arvioijat
    [{:nimi "Erkki Esimerkki"
      :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921623"}}]}
   :tarkentavat-tiedot-naytto
   [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_fy"
                   :koodi-versio 1}]
     :koulutuksen-jarjestaja-osaamisen-arvioijat
     [{:nimi "Aapo Arvioija"
       :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921674"}}]
     :jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921685"}
     :nayttoymparisto {:nimi "Toinen Esimerkki Oyj"
                       :y-tunnus "12345699-2"
                       :kuvaus "Testiyrityksen testiosasostalla"}
     :tyoelama-osaamisen-arvioijat [{:nimi "Teppo Työmies"
                                     :organisaatio
                                     {:nimi "Testiyrityksen Sisar Oy"
                                      :y-tunnus "12345689-3"}}]
     :sisallon-kuvaus ["Tutkimustyö"
                       "Raportointi"]
     :yksilolliset-kriteerit ["Ensimmäinen kriteeri"]
     :alku "2019-02-09"
     :loppu "2019-01-12"}]})

(defn- create-mock-post-request [url body app hoks]
  (utils/with-service-ticket
    app
    (-> (mock/request
          :post
          (get-hoks-url hoks url))
        (mock/json-body body))))

(defn- create-mock-get-request [url app hoks]
  (utils/with-service-ticket
    app
    (mock/request
      :get
      (get-hoks-url hoks (str url "/1")))))

(deftest post-and-get-aiemmin-hankitut-ammat-tutkinnon-osat
  (testing "POST ooato and then get the created ooato"
    (db/clear)
    (with-hoks
      hoks
      (let [app (create-app nil)
            post-response (create-mock-post-request
                            ooato-path ooato-data app hoks)
            get-response (create-mock-get-request ooato-path app hoks)]
        (is (= (:status post-response) 200))
        (eq (utils/parse-body
              (:body post-response))
            {:meta {:id 1}
             :data {:uri
                    (format
                      "%s/1/aiemmin-hankittu-ammat-tutkinnon-osa/1"
                      url)}})
        (is (= (:status get-response) 200))
        (eq (utils/parse-body
              (:body get-response))
            {:meta {} :data ooato-data})))))

(def pyto-path "hankittava-yhteinen-tutkinnon-osa")

(def pyto-data
  {:osa-alueet
   [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ku"
     :osa-alue-koodi-versio 1
     :osaamisen-hankkimistavat
     [{:alku "2018-12-15"
       :loppu "2018-12-23"
       :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_oppisopimus"
       :osaamisen-hankkimistapa-koodi-versio 1}]
     :osaamisen-osoittaminen
     [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.00000000002"}
       :nayttoymparisto {:nimi "aaa"}
       :alku "2018-12-12"
       :loppu "2018-12-20"
       :sisallon-kuvaus ["Kuvaus"]
       :tyoelama-osaamisen-arvioijat [{:nimi "Nimi" :organisaatio
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
     :osaamisen-osoittaminen
     [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.00000000008"}
       :nayttoymparisto {:nimi "aaa2"}
       :alku "2018-12-15"
       :loppu "2018-12-21"
       :sisallon-kuvaus ["Kuvaus"]
       :tyoelama-osaamisen-arvioijat [{:nimi "Nimi" :organisaatio
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
     :osaamisen-osoittaminen
     [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.00000000010"}
       :nayttoymparisto {:nimi "aaa2"}
       :alku "2018-12-15"
       :loppu "2018-12-21"
       :sisallon-kuvaus ["Kuvaus"]
       :tyoelama-osaamisen-arvioijat [{:nimi "Nimi" :organisaatio
                                       {:nimi "Organisaation nimi"}}]}]}]
   :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000011"})

(deftest post-and-get-pyto
  (testing "POST hankittavat yhteisen tutkinnon osat"
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
  (testing "PUT hankittavat yhteisen tutkinnon osat"
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
  (testing "PATCH one value hankittavat yhteisen tutkinnon osat"
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
  (testing "PATCH all hankittavat yhteisen tutkinnon osat"
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
    :aiemmin-hankitut-ammat-tutkinnon-osat []
    :hankittavat-paikalliset-tutkinnon-osat []
    :hankittavat-ammat-tutkinnon-osat []
    :aiemmin-hankitut-yhteiset-tutkinnon-osat []
    :hankittavat-yhteiset-tutkinnon-osat []
    :aiemmin-hankitut-paikalliset-tutkinnon-osat []
    :opiskeluvalmiuksia-tukevat-opinnot []))

(deftest get-created-hoks
  (testing "GET newly created HOKS"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}
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
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}]
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
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}]
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
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}]

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
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}]
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
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}
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
                    {:id 1
                     :urasuunnitelma-koodi-uri "urasuunnitelma_0001"
                     :urasuunnitelma-koodi-versio 1})))]
        (is (= (:status patch-response) 204))
        (let [updated-hoks
              (-> (get-in body [:data :uri]) get-authenticated :data)]
          (eq
            updated-hoks
            (assoc
              hoks
              :id 1
              :urasuunnitelma-koodi-uri "urasuunnitelma_0001"
              :urasuunnitelma-koodi-versio 1)))))))

(deftest patch-non-existing-hoks
  (testing "PATCH prevents updating non existing HOKS"
    (let [response
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request :patch (format "%s/1" url))
                (mock/json-body {:id 1})))]
      (is (= (:status response) 404)))))

(deftest get-hoks-by-opiskeluoikeus-oid
  (testing "GET HOKS by opiskeluoikeus-oid")
  (let [opiskeluoikeus-oid "1.2.246.562.15.00000000001"
        hoks-data {:opiskeluoikeus-oid opiskeluoikeus-oid
                   :oppija-oid "1.2.246.562.24.12312312312"
                   :ensikertainen-hyvaksyminen "2018-12-15"
                   :osaamisen-hankkimisen-tarve false}
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
