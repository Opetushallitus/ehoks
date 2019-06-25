(ns oph.ehoks.hoks.handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.virkailija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils :refer [eq]]
            [oph.ehoks.db.memory :as db]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.external.cache :as cache]))

(def url "/ehoks-virkailija-backend/api/v1/hoks")

; TODO Change to use OHJ auth
; TODO Test also role access
; TODO update tests to use real-like data
; TODO add test for removing at update (for example ppto)

(use-fixtures :each utils/with-database)

(use-fixtures :once utils/clean-db)

(defn create-app [session-store]
  (cache/clear-cache!)
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
                   :ensikertainen-hyvaksyminen
                   (java.time.LocalDate/of 2019 3 18)
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

(defn- create-mock-post-request
  ([path body app hoks]
    (create-mock-post-request (format "%d/%s" (:id hoks) path) body app))
  ([path body app]
    (utils/with-service-ticket
      app
      (-> (mock/request
            :post
            (format "%s/%s" url path))
          (mock/json-body body)))))

(defn- create-mock-get-request [path app]
  (utils/with-service-ticket
    app
    (mock/request
      :get
      path)))

(defn- create-mock-hoks-osa-get-request [path app hoks]
  (create-mock-get-request (get-hoks-url hoks (str path "/1")) app))

(defn- create-mock-hoks-get-request [hoks-id app]
  (create-mock-get-request (format "%s/%d" url hoks-id) app))

(defn- create-mock-patch-request [path patched-data app]
  (utils/with-service-ticket
    app
    (-> (mock/request
          :patch
          path)
        (mock/json-body patched-data))))

(defn- create-mock-hoks-osa-patch-request [path app patched-data]
  (create-mock-patch-request (format "%s/1/%s/1" url path) patched-data app))

(defn- create-mock-hoks-patch-request [hoks-id patched-data app]
  (create-mock-patch-request (format "%s/%d" url hoks-id) patched-data app))

(def hpto-path "hankittava-paikallinen-tutkinnon-osa")
(def hpto-data {:nimi "222"
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

(deftest post-and-get-hankittava-paikallinen-tutkinnon-osa
  (testing "GET newly created hankittava paikallinen tutkinnon osa"
    (db/clear)
    (with-hoks
      hoks
      (let [ppto-response
            (utils/with-service-ticket
              (create-app nil)
              (-> (mock/request
                    :post
                    (get-hoks-url hoks hpto-path))
                  (mock/json-body hpto-data)))
            body (utils/parse-body (:body ppto-response))]
        (is (= (:status ppto-response) 200))
        (eq body {:data
                  {:uri
                   (get-hoks-url hoks (format "%s/1" hpto-path))}
                  :meta {:id 1}})
        (let [ppto-new
              (utils/with-service-ticket
                (create-app nil)
                (mock/request
                  :get
                  (get-hoks-url hoks (format "%s/1" hpto-path))))]
          (eq
            (:data (utils/parse-body (:body ppto-new)))
            (assoc
              hpto-data
              :id 1)))))))

(deftest patch-all-hankittavat-paikalliset-tutkinnon-osat
  (testing "PATCH all hankittava paikallinen tutkinnon osa"
    (with-hoks
      hoks
      (let [hpto-response
            (utils/with-service-ticket
              (create-app nil)
              (-> (mock/request
                    :post
                    (get-hoks-url hoks hpto-path))
                  (mock/json-body hpto-data)))
            patch-response
            (utils/with-service-ticket
              (create-app nil)
              (->
                (mock/request
                  :patch
                  (get-hoks-url hoks (format "%s/1" hpto-path)))
                (mock/json-body
                  (assoc hpto-data :nimi "333" :olennainen-seikka false))))]
        (is (= (:status patch-response) 204))))))

(deftest patch-one-hankittava-paikallinen-tutkinnon-osa
  (testing "PATCH one value hankittava paikallinen tutkinnon osa"
    (with-hoks
      hoks
      (let [ppto-response
            (utils/with-service-ticket
              (create-app nil)
              (-> (mock/request
                    :post
                    (get-hoks-url hoks hpto-path))
                  (mock/json-body hpto-data)))
            ppto-body (utils/parse-body
                        (:body ppto-response))
            patch-response
            (utils/with-service-ticket
              (create-app nil)
              (-> (mock/request
                    :patch
                    (get-hoks-url hoks (format "%s/1" hpto-path)))
                  (mock/json-body
                    {:id 1 :nimi "2223"})))
            get-response (-> (get-in ppto-body [:data :uri])
                             get-authenticated :data)]
        (is (= (:status patch-response) 204))
        (eq get-response
            (assoc hpto-data
                   :id 1
                   :nimi "2223"))))))

(def hao-path "hankittava-ammat-tutkinnon-osa")
(def hao-data {:tutkinnon-osa-koodi-uri "tutkinnonosat_300268"
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
                 :muut-oppimisymparistot
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

(deftest post-and-get-hankittava-ammatillinen-osaaminen
  (testing "POST hankittava ammatillinen osaaminen and then get created hao"
    (with-hoks
      hoks
      (let [app (create-app nil)
            post-response (create-mock-post-request hao-path hao-data app hoks)
            get-response (create-mock-hoks-osa-get-request hao-path app hoks)]
        (is (= (:status post-response) 200))
        (eq (utils/parse-body
              (:body post-response))
            {:meta {:id 1}
             :data {:uri
                    (format "%s/1/hankittava-ammat-tutkinnon-osa/1" url)}})
        (is (= (:status get-response) 200))
        (eq (utils/parse-body
              (:body get-response))
            {:meta {} :data (assoc hao-data :id 1)})))))

(def patch-all-hao-data
  (merge
    hao-data
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
       :muut-oppimisymparistot []
       :ajanjakson-tarkenne "Ei ole"
       :hankkijan-edustaja
       {:nimi "Harri Hankkija"
        :rooli "Opettajan sijainen"
        :oppilaitos-oid "1.2.246.562.10.55552422420"}
       :alku "2019-01-12"
       :loppu "2019-02-11"}]
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000116"}))

(deftest patch-all-hankittava-ammatillinen-osaaminen
  (testing "PATCH ALL hankittava ammat osaaminen"
    (db/clear)
    (with-hoks
      hoks
      (let [app (create-app nil)
            post-response (create-mock-post-request hao-path hao-data app hoks)
            patch-response
            (utils/with-service-ticket
              app
              (-> (mock/request
                    :patch
                    (get-hoks-url hoks (str hao-path "/1")))
                  (mock/json-body (assoc patch-all-hao-data :id 1))))
            get-response (create-mock-hoks-osa-get-request hao-path app hoks)]
        (is (= (:status patch-response) 204))
        (eq (utils/parse-body (:body get-response))
            {:meta {} :data  (assoc patch-all-hao-data :id 1)})))))

(deftest patch-one-hankittava-ammatilinen-osaaminen
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
                    hao-data)))
            response
            (utils/with-service-ticket
              app
              (-> (mock/request
                    :patch
                    (format
                      "%s/1/%s/1"
                      url hao-path))
                  (mock/json-body
                    {:id 1
                     :vaatimuksista-tai-tavoitteista-poikkeaminen "Test"})))]
        (is (= (:status response) 204))))))

(defn- assert-post-response-is-ok [post-path post-response]
  (is (= (:status post-response) 200))
  (eq (utils/parse-body (:body post-response))
      {:meta {:id 1}
       :data {:uri
              (format
                "%1s/1/%2s/1"
                url post-path)}}))

(defn- test-patch-of-aiemmin-hankittu-osa
  [osa-path osa-data osa-patched-data assert-function]
  (with-hoks
    hoks
    (let [app (create-app nil)
          post-response (create-mock-post-request
                          osa-path osa-data app hoks)
          patch-response (create-mock-hoks-osa-patch-request
                           osa-path app osa-patched-data)
          get-response (create-mock-hoks-osa-get-request osa-path app hoks)
          get-response-data (:data (utils/parse-body (:body get-response)))]
      (is (= (:status post-response) 200))
      (is (= (:status patch-response) 204))
      (is (= (:status get-response) 200))
      (assert-function get-response-data osa-data))))

(defn- test-post-and-get-of-aiemmin-hankittu-osa [osa-path osa-data]
  (with-hoks
    hoks
    (let [app (create-app nil)
          post-response (create-mock-post-request
                          osa-path osa-data app hoks)
          get-response (create-mock-hoks-osa-get-request osa-path app hoks)]
      (assert-post-response-is-ok osa-path post-response)
      (is (= (:status get-response) 200))
      (eq (utils/parse-body
            (:body get-response))
          {:meta {} :data (assoc osa-data :id 1)}))))

(defn- compare-tarkentavat-tiedot-naytto-values
  [updated original selector-function]
  (let [ttn-after-update
        (selector-function (:tarkentavat-tiedot-naytto updated))
        ttn-patch-values
        (assoc (selector-function (:tarkentavat-tiedot-naytto original))
               :osa-alueet [] :tyoelama-osaamisen-arvioijat [])]
    (eq ttn-after-update ttn-patch-values)))

(def ahato-path "aiemmin-hankittu-ammat-tutkinnon-osa")
(def ahato-data
  {:valittu-todentamisen-prosessi-koodi-versio 3
   :tutkinnon-osa-koodi-versio 100022
   :valittu-todentamisen-prosessi-koodi-uri "osaamisentodentamisenprosessi_3"
   :tutkinnon-osa-koodi-uri "tutkinnonosat_100022"
   :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921419"
   :tarkentavat-tiedot-osaamisen-arvioija
   {:lahetetty-arvioitavaksi "2019-03-18"
    :aiemmin-hankitun-osaamisen-arvioijat
    [{:nimi "Erkki Esimerkki"
      :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921623"}}
     {:nimi "Joku Tyyppi"
      :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921000"}}]}
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

(deftest post-and-get-aiemmin-hankitut-ammatilliset-tutkinnon-osat
  (testing "POST ahato and then get the created ahato"
    (test-post-and-get-of-aiemmin-hankittu-osa ahato-path ahato-data)))

(def ^:private multiple-ahato-values-patched
  {:tutkinnon-osa-koodi-versio 3000
   :tarkentavat-tiedot-osaamisen-arvioija
   {:lahetetty-arvioitavaksi "2020-01-01"
    :aiemmin-hankitun-osaamisen-arvioijat
    [{:nimi "Nimi Muutettu"
      :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453555555"}}
     {:nimi "Joku Tyyppi"
      :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921000"}}]}
   :tarkentavat-tiedot-naytto
   [{:koulutuksen-jarjestaja-osaamisen-arvioijat
     [{:nimi "Muutettu Arvioija"
       :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921674"}}]
     :jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921685"}
     :nayttoymparisto {:nimi "Testi Oy"
                       :y-tunnus "12345699-2"
                       :kuvaus "Testiyrityksen testiosasostalla"}
     :sisallon-kuvaus ["Tutkimustyö"
                       "Raportointi"]
     :yksilolliset-kriteerit ["testikriteeri"]
     :alku "2019-02-09"
     :loppu "2019-01-12"}]})

(defn- assert-ahato-data-is-patched-correctly [updated-data old-data]
  (is (= (:tutkinnon-osa-koodi-versio updated-data) 3000))
  (is (= (:valittu-todentamisen-prosessi-koodi-versio updated-data)
         (:valittu-todentamisen-prosessi-koodi-versio old-data)))
  (is (= (:tarkentavat-tiedot-osaamisen-arvioija updated-data)
         (:tarkentavat-tiedot-osaamisen-arvioija
           multiple-ahato-values-patched)))
  (compare-tarkentavat-tiedot-naytto-values
    updated-data multiple-ahato-values-patched first))

(deftest patch-aiemmin-hankitut-ammat-tutkinnon-osat
  (testing "Patching multiple values of ahato"
    (test-patch-of-aiemmin-hankittu-osa
      ahato-path
      ahato-data
      multiple-ahato-values-patched
      assert-ahato-data-is-patched-correctly)))

(def ahpto-path "aiemmin-hankittu-paikallinen-tutkinnon-osa")
(def ahpto-data
  {:valittu-todentamisen-prosessi-koodi-versio 2
   :laajuus 30
   :nimi "Testiopintojakso"
   :tavoitteet-ja-sisallot "Tavoitteena on testioppiminen."
   :valittu-todentamisen-prosessi-koodi-uri
   "osaamisentodentamisenprosessi_0001"
   :amosaa-tunniste "12345"
   :tarkentavat-tiedot-osaamisen-arvioija
   {:lahetetty-arvioitavaksi "2020-01-01"
    :aiemmin-hankitun-osaamisen-arvioijat
    [{:nimi "Aarne Arvioija"
      :organisaatio {:oppilaitos-oid
                     "1.2.246.562.10.54453923411"}}]}
   :tarkentavat-tiedot-naytto
   [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_li"
                   :koodi-versio 6}]
     :koulutuksen-jarjestaja-osaamisen-arvioijat
     [{:nimi "Teuvo Testaaja"
       :organisaatio {:oppilaitos-oid
                      "1.2.246.562.10.12346234690"}}]
     :jarjestaja {:oppilaitos-oid
                  "1.2.246.562.10.93270534262"}

     :nayttoymparisto {:nimi "Testi Oyj"
                       :y-tunnus "1289211-2"
                       :kuvaus "Testiyhtiö"}
     :tyoelama-osaamisen-arvioijat
     [{:nimi "Terttu Testihenkilö"
       :organisaatio {:nimi "Testi Oyj"
                      :y-tunnus "1289211-2"}}]
     :sisallon-kuvaus ["Testauksen suunnittelu"
                       "Jokin toinen testi"]
     :alku "2019-02-01"
     :loppu "2019-03-01"
     :yksilolliset-kriteerit ["Ensimmäinen kriteeri"]}]
   :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453945322"
   :vaatimuksista-tai-tavoitteista-poikkeaminen "Ei poikkeamaa."})

(deftest post-and-get-aiemmin-hankitut-paikalliset-tutkinnon-osat
  (testing "POST oopto and then get the created oopto"
    (test-post-and-get-of-aiemmin-hankittu-osa ahpto-path ahpto-data)))

(def ^:private multiple-ahpto-values-patched
  {:tavoitteet-ja-sisallot "Muutettu tavoite."
   :tarkentavat-tiedot-osaamisen-arvioija
   {:lahetetty-arvioitavaksi "2020-01-01"
    :aiemmin-hankitun-osaamisen-arvioijat
    [{:nimi "Aarne Arvioija"
      :organisaatio {:oppilaitos-oid
                     "1.2.246.562.10.54453923411"}}]}
   :tarkentavat-tiedot-naytto
   [{:koulutuksen-jarjestaja-osaamisen-arvioijat
     [{:nimi "Muutettu Arvioija"
       :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921674"}}]
     :jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921685"}
     :nayttoymparisto {:nimi "Testi Oy"
                       :y-tunnus "12345699-2"
                       :kuvaus "Testiyrityksen testiosasostalla"}
     :sisallon-kuvaus ["Tutkimustyö"
                       "Raportointi"]
     :alku "2019-02-09"
     :loppu "2019-01-12"
     :yksilolliset-kriteerit ["Toinen kriteeri"]
     :osa-alueet []
     :tyoelama-osaamisen-arvioijat []}]})

(defn- assert-ahpto-data-is-patched-correctly [updated-data old-data]
  (is (= (:tavoitteet-ja-sisallot updated-data) "Muutettu tavoite."))
  (is (= (:nimi updated-data) (:nimi old-data)))
  (eq (:tarkentavat-tiedot-osaamisen-arvioija updated-data)
      (:tarkentavat-tiedot-osaamisen-arvioija multiple-ahpto-values-patched))
  (eq (first (:tarkentavat-tiedot-naytto updated-data))
      (first (:tarkentavat-tiedot-naytto multiple-ahpto-values-patched)))
  (compare-tarkentavat-tiedot-naytto-values
    updated-data multiple-ahpto-values-patched first))

(deftest patch-aiemmin-hankittu-paikalliset-tutkinnon-osat
  (testing "Patching multiple values of ahpto"
    (test-patch-of-aiemmin-hankittu-osa
      ahpto-path
      ahpto-data
      multiple-ahpto-values-patched
      assert-ahpto-data-is-patched-correctly)))

(def ahyto-path "aiemmin-hankittu-yhteinen-tutkinnon-osa")
(def ahyto-data
  {:valittu-todentamisen-prosessi-koodi-uri
   "osaamisentodentamisenprosessi_0001"
   :valittu-todentamisen-prosessi-koodi-versio 3
   :tutkinnon-osa-koodi-versio 2
   :tutkinnon-osa-koodi-uri "tutkinnonosat_10203"
   :tarkentavat-tiedot-osaamisen-arvioija
   {:lahetetty-arvioitavaksi "2016-02-29"
    :aiemmin-hankitun-osaamisen-arvioijat
    [{:nimi "Arttu Arvioija"
      :organisaatio {:oppilaitos-oid
                     "1.2.246.562.10.54453931311"}}]}
   :osa-alueet
   [{:osa-alue-koodi-uri "ammatillisenoppiaineet_bi"
     :osa-alue-koodi-versio 4
     :koulutuksen-jarjestaja-oid
     "1.2.246.562.10.54453923578"
     :vaatimuksista-tai-tavoitteista-poikkeaminen
     "Testaus ei kuulu vaatimuksiin."
     :valittu-todentamisen-prosessi-koodi-uri
     "osaamisentodentamisenprosessi_0003"
     :valittu-todentamisen-prosessi-koodi-versio 4
     :tarkentavat-tiedot-naytto
     [{:sisallon-kuvaus ["kuvaus1"]
       :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_bi"
                     :koodi-versio 3}]
       :koulutuksen-jarjestaja-osaamisen-arvioijat
       [{:nimi "Teppo Testaaja"
         :organisaatio {:oppilaitos-oid
                        "1.2.246.562.10.54539267901"}}]
       :jarjestaja {:oppilaitos-oid
                    "1.2.246.562.10.55890967901"}

       :nayttoymparisto {:nimi "Ab Yhtiö Oy"
                         :y-tunnus "1234128-1"
                         :kuvaus "Testi"}
       :tyoelama-osaamisen-arvioijat
       [{:nimi "Tellervo Työntekijä"
         :organisaatio {:nimi "Ab Yhtiö Oy"
                        :y-tunnus "1234128-1"}}]
       :yksilolliset-kriteerit ["Joku kriteeri"]
       :alku "2019-01-04"
       :loppu "2019-03-01"}]}]
   :koulutuksen-jarjestaja-oid "1.2.246.562.10.13490590901"
   :tarkentavat-tiedot-naytto
   [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_ma"
                   :koodi-versio 6}]
     :koulutuksen-jarjestaja-osaamisen-arvioijat
     [{:nimi "Erkki Esimerkkitestaaja"
       :organisaatio {:oppilaitos-oid
                      "1.2.246.562.10.13490579090"}}]
     :jarjestaja {:oppilaitos-oid
                  "1.2.246.562.10.93270579090"}
     :nayttoymparisto {:nimi "Testi Oy"
                       :y-tunnus "1289235-2"
                       :kuvaus "Testiyhtiö"}
     :tyoelama-osaamisen-arvioijat
     [{:nimi "Tapio Testihenkilö"
       :organisaatio {:nimi "Testi Oy"
                      :y-tunnus "1289235-2"}}]
     :sisallon-kuvaus ["Testauksen suunnittelu"
                       "Jokin toinen testi"]
     :yksilolliset-kriteerit ["Ensimmäinen kriteeri"]
     :alku "2019-03-01"
     :loppu "2019-06-01"}]})

(deftest post-and-get-aiemmin-hankitut-yhteiset-tutkinnon-osat
  (testing "POST ahyto and then get the created ahyto"
    (test-post-and-get-of-aiemmin-hankittu-osa ahyto-path ahyto-data)))

(def ^:private multiple-ahyto-values-patched
  {:valittu-todentamisen-prosessi-koodi-uri
   "osaamisentodentamisenprosessi_2000"

   :tarkentavat-tiedot-osaamisen-arvioija
   {:lahetetty-arvioitavaksi "2020-04-01"
    :aiemmin-hankitun-osaamisen-arvioijat
    [{:nimi "Muutettu Arvioija"
      :organisaatio {:oppilaitos-oid
                     "1.2.246.562.10.54453932222"}}
     {:nimi "Toinen Arvioija"
      :organisaatio {:oppilaitos-oid
                     "1.2.246.562.10.54453933333"}}]}

   :osa-alueet
   [{:osa-alue-koodi-uri "ammatillisenoppiaineet_bi"
     :osa-alue-koodi-versio 4
     :valittu-todentamisen-prosessi-koodi-uri
     "osaamisentodentamisenprosessi_0003"
     :valittu-todentamisen-prosessi-koodi-versio 4
     :tarkentavat-tiedot-naytto
     [{:sisallon-kuvaus ["kuvaus1"]
       :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_en"
                     :koodi-versio 5}]
       :koulutuksen-jarjestaja-osaamisen-arvioijat
       [{:nimi "Teppo Testaaja2"
         :organisaatio {:oppilaitos-oid
                        "1.2.246.562.10.54539267000"}}]
       :jarjestaja {:oppilaitos-oid
                    "1.2.246.562.10.55890967000"}

       :nayttoymparisto {:nimi "Ab Betoni Oy"
                         :y-tunnus "1234128-1"
                         :kuvaus "Testi"}
       :tyoelama-osaamisen-arvioijat
       [{:nimi "Tellervo Työntekijä"
         :organisaatio {:nimi "Ab Betoni Oy"
                        :y-tunnus "1234128-1"}}]
       :yksilolliset-kriteerit ["testi"]
       :alku "2029-01-04"
       :loppu "2030-03-01"}]}]

   :tarkentavat-tiedot-naytto
   [{:nayttoymparisto {:nimi "Testi Oy"
                       :y-tunnus "1289235-2"
                       :kuvaus "Testiyhtiö"}
     :koulutuksen-jarjestaja-osaamisen-arvioijat
     [{:nimi "Joku Arvioija"
       :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453911333"}}]
     :sisallon-kuvaus ["Testauksen suunnittelu"
                       "Jokin toinen testi"]
     :yksilolliset-kriteerit ["kriteeri"]
     :alku "2015-03-31"
     :loppu "2021-06-01"}
    {:nayttoymparisto {:nimi "Toka Oy"}
     :koulutuksen-jarjestaja-osaamisen-arvioijat
     [{:nimi "Joku Toinen Arvioija"
       :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453911555"}}]
     :sisallon-kuvaus ["Jotakin sisaltoa"]
     :yksilolliset-kriteerit ["testi" "toinen"]
     :alku "2014-05-05"
     :loppu "2022-09-12"}]})

(defn- assert-ahyto-is-patched-correctly [updated-data initial-data]
  (is (= (:valittu-todentamisen-prosessi-koodi-uri updated-data)
         "osaamisentodentamisenprosessi_2000"))
  (is (= (:tutkinnon-osa-koodi-versio updated-data)
         (:tutkinnon-osa-koodi-versio initial-data)))
  (eq (:tarkentavat-tiedot-osaamisen-arvioija updated-data)
      (:tarkentavat-tiedot-osaamisen-arvioija multiple-ahyto-values-patched))
  (compare-tarkentavat-tiedot-naytto-values
    updated-data multiple-ahyto-values-patched first)
  (compare-tarkentavat-tiedot-naytto-values
    updated-data multiple-ahyto-values-patched second)
  (eq (:osa-alueet updated-data)
      (:osa-alueet multiple-ahyto-values-patched)))

(deftest patch-aiemmin-hankittu-yhteinen-tutkinnon-osa
  (testing "Patching values of ahyto"
    (test-patch-of-aiemmin-hankittu-osa
      ahyto-path
      ahyto-data
      multiple-ahyto-values-patched
      assert-ahyto-is-patched-correctly)))

(def hyto-path "hankittava-yhteinen-tutkinnon-osa")
(def hyto-data
  {:tutkinnon-osa-koodi-uri "tutkinnonosat_3002683"
   :tutkinnon-osa-koodi-versio 1
   :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000007"
   :osa-alueet
   [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ku"
     :osa-alue-koodi-versio 1
     :vaatimuksista-tai-tavoitteista-poikkeaminen "joku poikkeaminen"
     :olennainen-seikka false
     :osaamisen-hankkimistavat
     [{:alku "2018-12-15"
       :loppu "2018-12-23"
       :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_oppisopimus"
       :osaamisen-hankkimistapa-koodi-versio 1
       :muut-oppimisymparistot
       [{:oppimisymparisto-koodi-uri "oppimisymparistot_0222"
         :oppimisymparisto-koodi-versio 3
         :alku "2015-03-10"
         :loppu "2021-03-19"}]}]
     :osaamisen-osoittaminen
     [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.00000000002"}
       :nayttoymparisto {:nimi "aaa"}
       :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_en"
                     :koodi-versio 4}]
       :koulutuksen-jarjestaja-osaamisen-arvioijat
       [{:nimi "Erkki Esimerkkitetsaaja"
         :organisaatio {:oppilaitos-oid
                        "1.2.246.562.10.13490579333"}}]
       :alku "2018-12-12"
       :loppu "2018-12-20"
       :sisallon-kuvaus ["Kuvaus"]
       :tyoelama-osaamisen-arvioijat [{:nimi "Nimi" :organisaatio
                                       {:nimi "Organisaation nimi"}}]
       :yksilolliset-kriteerit ["Ensimmäinen kriteeri"]}]}]})

(deftest post-and-get-hankittava-yhteinen-tukinnon-osa
  (testing "POST hankittavat yhteisen tutkinnon osat"
    (with-hoks
      hoks
      (let [app (create-app nil)
            post-response (create-mock-post-request
                            hyto-path hyto-data app hoks)
            get-response (create-mock-hoks-osa-get-request hyto-path app hoks)]
        (assert-post-response-is-ok hyto-path post-response)
        (is (= (:status get-response) 200))
        (eq (utils/parse-body
              (:body get-response))
            {:meta {} :data (assoc hyto-data :id 1)})))))

(def ^:private one-value-of-hyto-patched
  {:koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000012"})

(deftest patch-one-value-of-hankittava-yhteinen-tutkinnon-osa
  (testing "PATCH one value hankittavat yhteisen tutkinnon osat"
    (with-hoks
      hoks
      (let [app (create-app nil)
            post-response (create-mock-post-request
                            hyto-path hyto-data app hoks)
            patch-response (create-mock-hoks-osa-patch-request
                             hyto-path app one-value-of-hyto-patched)
            get-response (create-mock-hoks-osa-get-request hyto-path app hoks)
            get-response-data (:data (utils/parse-body (:body get-response)))]
        (is (= (:status patch-response) 204))
        (is (= (:koulutuksen-jarjestaja-oid get-response-data)
               (:koulutuksen-jarjestaja-oid one-value-of-hyto-patched))
            "Patched value should change.")
        (is (= (:tutkinnon-osa-koodi-versio get-response-data)
               (:tutkinnon-osa-koodi-versio hyto-data))
            "Value should stay unchanged")))))

(def osa-alueet-of-hyto
  [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ru"
    :osa-alue-koodi-versio 4
    :vaatimuksista-tai-tavoitteista-poikkeaminen "uusi poikkeaminen"
    :olennainen-seikka true
    :osaamisen-hankkimistavat
    [{:alku "2019-01-15"
      :loppu "2020-02-23"
      :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_muutettu"
      :osaamisen-hankkimistapa-koodi-versio 3
      :ajanjakson-tarkenne "tarkenne"
      :muut-oppimisymparistot
      [{:oppimisymparisto-koodi-uri "oppimisymparistot_0222"
        :oppimisymparisto-koodi-versio 3
        :alku "2016-03-12"
        :loppu "2025-06-19"}]
      :jarjestajan-edustaja
      {:nimi "testi testaaja"
       :oppilaitos-oid "1.2.246.562.10.00000000421"}
      :hankkijan-edustaja
      {:nimi "testi edustaja"
       :oppilaitos-oid "1.2.246.562.10.00000000321"}
      :tyopaikalla-jarjestettava-koulutus
      {:tyopaikan-nimi "joku nimi"
       :keskeiset-tyotehtavat ["tehtava" "toinen"]
       :vastuullinen-tyopaikka-ohjaaja
       {:nimi "ohjaaja o"}}}]
    :osaamisen-osoittaminen
    [{:alku "2018-12-12"
      :loppu "2018-12-20"
      :sisallon-kuvaus ["Kuvaus"]
      :yksilolliset-kriteerit ["Ensimmäinen kriteeri"]
      :vaatimuksista-tai-tavoitteista-poikkeaminen "nyt poikettiin"
      :jarjestaja {:oppilaitos-oid "1.2.246.562.10.00000000002"}
      :nayttoymparisto {:nimi "aaa"}
      :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_en"
                    :koodi-versio 4}]
      :koulutuksen-jarjestaja-osaamisen-arvioijat
      [{:nimi "Erkki Esimerkkitetsaaja"
        :organisaatio {:oppilaitos-oid
                       "1.2.246.562.10.13490579333"}}]
      :tyoelama-osaamisen-arvioijat [{:nimi "Nimi" :organisaatio
                                      {:nimi "Organisaation nimi"}}]}]}])

(def multiple-hyto-values-patched
  {:tutkinnon-osa-koodi-uri "tutkinnonosat_3002683"
   :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000009"
   :osa-alueet osa-alueet-of-hyto})

(deftest patch-multiple-values-of-hankittavat-yhteiset-tutkinnon-osat
  (testing "PATCH all hankittavat yhteisen tutkinnon osat"
    (with-hoks
      hoks
      (let [app (create-app nil)
            post-response (create-mock-post-request
                            hyto-path hyto-data app hoks)
            patch-response (create-mock-hoks-osa-patch-request
                             hyto-path app multiple-hyto-values-patched)
            get-response (create-mock-hoks-osa-get-request hyto-path app hoks)
            get-response-data (:data (utils/parse-body (:body get-response)))]
        (is (= (:status patch-response) 204))
        (eq (:osa-alueet get-response-data)
            (:osa-alueet multiple-hyto-values-patched))))))

(def hyto-sub-entity-patched
  {:osa-alueet osa-alueet-of-hyto})

(deftest only-sub-entity-of-hyto-patched
  (testing "PATCH only osa-alueet of hyto and leave base hyto untouched."
    (with-hoks
      hoks
      (let [app (create-app nil)
            post-response (create-mock-post-request
                            hyto-path hyto-data app hoks)
            patch-response (create-mock-hoks-osa-patch-request
                             hyto-path app hyto-sub-entity-patched)
            get-response (create-mock-hoks-osa-get-request hyto-path app hoks)
            get-response-data (:data (utils/parse-body (:body get-response)))]
        (is (= (:status patch-response) 204))
        (eq (:osa-alueet get-response-data)
            (:osa-alueet hyto-sub-entity-patched))))))

(def oto-path "opiskeluvalmiuksia-tukevat-opinnot")
(def oto-data {:nimi "Nimi"
               :kuvaus "Kuvaus"
               :alku "2018-12-12"
               :loppu "2018-12-20"})

(deftest post-and-get-opiskeluvalmiuksia-tukevat-opinnot
  (testing "GET opiskeluvalmiuksia tukevat opinnot"
    (with-hoks
      hoks
      (let [app (create-app nil)
            post-response (create-mock-post-request
                            oto-path oto-data app hoks)
            get-response (create-mock-hoks-osa-get-request oto-path app hoks)]
        (assert-post-response-is-ok oto-path post-response)
        (is (= (:status get-response) 200))
        (eq (utils/parse-body
              (:body get-response))
            {:meta {} :data (assoc oto-data :id 1)})))))

(def ^:private one-value-of-oto-patched
  {:nimi "Muuttunut Nimi"})

(deftest patch-one-value-of-opiskeluvalmiuksia-tukevat-opinnot
  (testing "PATCH one value of opiskeluvalmiuksia tukevat opinnot"
    (with-hoks
      hoks
      (let [app (create-app nil)
            post-response (create-mock-post-request
                            oto-path oto-data app hoks)
            patch-response (create-mock-hoks-osa-patch-request
                             oto-path app one-value-of-oto-patched)
            get-response (create-mock-hoks-osa-get-request oto-path app hoks)
            get-response-data (:data (utils/parse-body (:body get-response)))]
        (is (= (:status patch-response) 204))
        (is (= (:nimi get-response-data)
               (:nimi one-value-of-oto-patched))
            "Patched value should change.")
        (is (= (:kuvaus get-response-data)
               (:kuvaus oto-data))
            "Value should stay unchanged")))))

(def ^:private all-values-of-oto-patched
  {:nimi "Muuttunut Nimi"
   :kuvaus "Uusi Kuvaus"
   :alku "2020-01-12"
   :loppu "2021-10-20"})

(deftest patch-multiple-values-of-oto
  (testing "PATCH all opiskeluvalmiuksia tukevat opinnot"
    (with-hoks
      hoks
      (let [app (create-app nil)
            post-response (create-mock-post-request
                            oto-path oto-data app hoks)
            patch-response (create-mock-hoks-osa-patch-request
                             oto-path app all-values-of-oto-patched)
            get-response (create-mock-hoks-osa-get-request oto-path app hoks)
            get-response-data (:data (utils/parse-body (:body get-response)))]
        (is (= (:status patch-response) 204))
        (eq get-response-data (assoc all-values-of-oto-patched :id 1))))))

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
    (db/clear)
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                     :oppija-oid "1.2.246.562.24.12312312312"
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
    (db/clear)
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
        (is (= (:status response) 400))
        (is (= (utils/parse-body (:body response))
               {:error
                "HOKS with the same opiskeluoikeus-oid already exists"}))))))

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

(def hoks-data
  {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
   :oppija-oid "1.2.246.562.24.12312312312"
   :ensikertainen-hyvaksyminen "2018-12-15"
   :osaamisen-hankkimisen-tarve false
   :opiskeluvalmiuksia-tukevat-opinnot [oto-data]
   :hankittavat-ammat-tutkinnon-osat [hao-data]
   :hankittavat-paikalliset-tutkinnon-osat [hpto-data]
   :hankittavat-yhteiset-tutkinnon-osat [hyto-data]
   :aiemmin-hankitut-ammat-tutkinnon-osat [ahato-data]
   :aiemmin-hankitut-paikalliset-tutkinnon-osat [ahpto-data]
   :aiemmin-hankitut-yhteiset-tutkinnon-osat [ahyto-data]})

(defn- assert-partial-patch-of-hoks [patched-hoks hoks-part]
  (let [app (create-app nil)
        post-response (create-mock-post-request "" hoks-data app)
        patch-response (create-mock-hoks-patch-request 1 patched-hoks app)
        get-response (create-mock-hoks-get-request 1 app)
        get-response-data (:data (utils/parse-body (:body get-response)))]
    (is (= (:status post-response) 200))
    (is (= (:status patch-response) 204))
    (is (= (:status get-response) 200))
    (eq (hoks-part get-response-data)
        (hoks-part patched-hoks))))

(def one-value-of-hoks-patched
  {:id 1
   :ensikertainen-hyvaksyminen "2020-01-05"})

(deftest patch-one-value-of-hoks
  (testing "PATCH updates value of created HOKS"
    (let [app (create-app nil)
          post-response (create-mock-post-request "" hoks-data app)
          patch-response (create-mock-hoks-patch-request
                           1 one-value-of-hoks-patched app)
          get-response (create-mock-hoks-get-request 1 app)
          get-response-data (:data (utils/parse-body (:body get-response)))]
      (is (= (:status post-response) 200))
      (is (= (:status patch-response) 204))
      (is (= (:status get-response) 200))
      (is (= (:ensikertainen-hyvaksyminen get-response-data)
             (:ensikertainen-hyvaksyminen one-value-of-hoks-patched))
          "Patched value should change.")
      (is (= (:kuvaus get-response-data)
             (:kuvaus one-value-of-hoks-patched))
          "Value should stay unchanged"))))

(def oto-of-hoks-patched
  {:id 1
   :opiskeluvalmiuksia-tukevat-opinnot
   [{:nimi "Uusi Nimi"
     :kuvaus "joku kuvaus"
     :alku "2019-06-22"
     :loppu "2021-05-07"}]})

(deftest patch-oto-of-hoks
  (testing "PATCHes opiskeluvalmiuksia tukevat opinnot of HOKS"
    (assert-partial-patch-of-hoks
      oto-of-hoks-patched :opiskeluvalmiuksia-tukevat-opinnot)))

(def hato-of-hoks-patched
  {:id 1
   :hankittavat-ammat-tutkinnon-osat
   [{:tutkinnon-osa-koodi-uri "tutkinnonosat_300222"
     :tutkinnon-osa-koodi-versio 2
     :vaatimuksista-tai-tavoitteista-poikkeaminen
     "Ei poikkeamia."
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000005"
     :osaamisen-hankkimistavat
     [{:alku "2018-12-12"
       :loppu "2018-12-20"
       :ajanjakson-tarkenne "Tarkenne muuttunut"
       :osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_koulutussopimus"
       :osaamisen-hankkimistapa-koodi-versio 1
       :muut-oppimisymparistot
       [{:oppimisymparisto-koodi-uri "oppimisymparistot_0003"
         :oppimisymparisto-koodi-versio 1
         :alku "2019-03-10"
         :loppu "2019-03-19"}]
       :hankkijan-edustaja
       {:nimi "Heikki Hank"
        :rooli "Opettaja"
        :oppilaitos-oid "1.2.246.562.10.54452422420"}}]
     :osaamisen-osoittaminen
     [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453924330"}
       :nayttoymparisto {:nimi "Testiympäristö 2"
                         :y-tunnus "12345671-2"
                         :kuvaus "Testi test"}
       :sisallon-kuvaus ["Testaus"]
       :koulutuksen-jarjestaja-osaamisen-arvioijat
       [{:nimi "Timo Testaaja2"
         :organisaatio {:oppilaitos-oid
                        "1.2.246.562.10.54452521332"}}]
       :tyoelama-osaamisen-arvioijat
       [{:nimi "Taneli Työmies2"
         :organisaatio {:nimi "Tanelin Paja Oy"
                        :y-tunnus "12345622-2"}}]
       :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_kl"
                     :koodi-versio 3}]
       :alku "2019-03-10"
       :loppu "2019-03-19"
       :yksilolliset-kriteerit ["Yksi kriteeri" "toinen kriteeri"]}]}]})

(deftest patch-hato-of-hoks
  (testing "PATCHes hankittavat ammatilliset tutkinnon osat of HOKS"
    (assert-partial-patch-of-hoks
      hato-of-hoks-patched :hankittavat-ammat-tutkinnon-osat)))

(def hpto-of-hoks-patched
  {:id 1
   :hankittavat-paikalliset-tutkinnon-osat
   [{:nimi "testinimi"
     :koulutuksen-jarjestaja-oid
     "1.2.246.562.10.00000000001"
     :olennainen-seikka false
     :osaamisen-hankkimistavat
     [{:alku "2019-12-12"
       :loppu "2020-12-20"
       :ajanjakson-tarkenne "Tarkenne muuttunut"
       :osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_koulutussopimus"
       :osaamisen-hankkimistapa-koodi-versio 1
       :muut-oppimisymparistot
       [{:oppimisymparisto-koodi-uri
         "oppimisymparistot_0004"
         :oppimisymparisto-koodi-versio 2
         :alku "2019-03-10"
         :loppu "2021-03-19"}]
       :hankkijan-edustaja
       {:nimi "Heikki Hankk"
        :rooli "Opettaja"
        :oppilaitos-oid "1.2.246.562.10.54452422420"}}]
     :osaamisen-osoittaminen
     [{:jarjestaja {:oppilaitos-oid
                    "1.2.246.562.10.00000000022"}
       :koulutuksen-jarjestaja-osaamisen-arvioijat []
       :osa-alueet []
       :sisallon-kuvaus
       ["ensimmäinen sisältö" "toinenkin" "kolkki"]
       :nayttoymparisto {:nimi "aaab"}
       :alku "2018-12-12"
       :loppu "2018-12-20"
       :tyoelama-osaamisen-arvioijat
       [{:nimi "Nimi2"
         :organisaatio {:nimi "Organisaation nimi"}}]
       :vaatimuksista-tai-tavoitteista-poikkeaminen
       "Poikkeama tämä."
       :yksilolliset-kriteerit
       ["kriteeri 1"]}]}]})

(deftest patch-hpto-of-hoks
  (testing "PATCHes hankittavat paikalliset tutkinnon osat of HOKS"
    (assert-partial-patch-of-hoks
      hpto-of-hoks-patched :hankittavat-paikalliset-tutkinnon-osat)))

(def hyto-of-hoks-patched
  {:id 1
   :hankittavat-yhteiset-tutkinnon-osat
   [{:tutkinnon-osa-koodi-uri "tutkinnonosat_3002690"
     :tutkinnon-osa-koodi-versio 3
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000007"
     :osa-alueet
     [{:osa-alue-koodi-uri "ammatillisenoppiaineet_bi"
       :osa-alue-koodi-versio 1
       :vaatimuksista-tai-tavoitteista-poikkeaminen "poikkeaminen"
       :olennainen-seikka true
       :osaamisen-hankkimistavat
       [{:alku "2018-12-15"
         :loppu "2018-12-23"
         :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_joku"
         :osaamisen-hankkimistapa-koodi-versio 3
         :muut-oppimisymparistot
         [{:oppimisymparisto-koodi-uri "oppimisymparistot_0232"
           :oppimisymparisto-koodi-versio 3
           :alku "2016-03-10"
           :loppu "2021-03-19"}]}]
       :osaamisen-osoittaminen
       [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.00000000032"}
         :nayttoymparisto {:nimi "aaab"}
         :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_ru"
                       :koodi-versio 4}]
         :koulutuksen-jarjestaja-osaamisen-arvioijat
         [{:nimi "Erkki Esimerkkitest"
           :organisaatio {:oppilaitos-oid
                          "1.2.246.562.10.13490579322"}}]
         :alku "2018-12-12"
         :loppu "2019-12-20"
         :sisallon-kuvaus ["Kuvaus" "toinen"]
         :tyoelama-osaamisen-arvioijat [{:nimi "Nimi" :organisaatio
                                         {:nimi "Organisaation name"}}]
         :yksilolliset-kriteerit ["Ensimmäinen kriteeri" "toka"]}]}]}]})

(deftest patch-hyto-of-hoks
  (testing "PATCHes hankittavat yhteiset tutkinnon osat of HOKS"
    (assert-partial-patch-of-hoks
      hyto-of-hoks-patched :hankittavat-yhteiset-tutkinnon-osat)))

(def ahato-of-hoks-patched
  {:id 1
   :aiemmin-hankitut-ammat-tutkinnon-osat
   [{:valittu-todentamisen-prosessi-koodi-versio 5
     :tutkinnon-osa-koodi-versio 100033
     :valittu-todentamisen-prosessi-koodi-uri "osaamisentodentamisenprosessi_2"
     :tutkinnon-osa-koodi-uri "tutkinnonosat_100022"
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921429"
     :tarkentavat-tiedot-osaamisen-arvioija
     {:lahetetty-arvioitavaksi "2012-03-18"
      :aiemmin-hankitun-osaamisen-arvioijat
      [{:nimi "Erkki Esimerk"
        :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921633"}}
       {:nimi "Joku Tyyp"
        :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921001"}}]}
     :tarkentavat-tiedot-naytto
     [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_en"
                     :koodi-versio 3}]
       :koulutuksen-jarjestaja-osaamisen-arvioijat
       [{:nimi "Aapo Arvo"
         :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921684"}}]
       :jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921785"}
       :nayttoymparisto {:nimi "Esimerkki Oyj"
                         :y-tunnus "12345699-3"
                         :kuvaus "Testiyrityksen testiosa"}
       :tyoelama-osaamisen-arvioijat [{:nimi "Teppo Työm"
                                       :organisaatio
                                       {:nimi "Testiyrityksen Sisar"
                                        :y-tunnus "12345689-5"}}]
       :sisallon-kuvaus ["Tutkimustyö" "Raportointi" "joku"]
       :yksilolliset-kriteerit ["Ensimmäinen kriteeri" "toinen"]
       :alku "2018-02-09"
       :loppu "2021-01-12"}]}]})

(deftest patch-ahato-of-hoks
  (testing "PATCHes aiemmin hankitut ammatilliset tutkinnon osat of HOKS"
    (assert-partial-patch-of-hoks
      ahato-of-hoks-patched :aiemmin-hankitut-ammat-tutkinnon-osat)))

(def ahpto-of-hoks-patched
  {:id 1
   :aiemmin-hankitut-paikalliset-tutkinnon-osat
   [{:valittu-todentamisen-prosessi-koodi-versio 3
     :laajuus 40
     :nimi "Testiopintojaksoo"
     :tavoitteet-ja-sisallot "Tavoitteena on oppiminen."
     :valittu-todentamisen-prosessi-koodi-uri
     "osaamisentodentamisenprosessi_0003"
     :amosaa-tunniste "12345"
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453945325"
     :vaatimuksista-tai-tavoitteista-poikkeaminen "Ei poikkeamaa"
     :tarkentavat-tiedot-osaamisen-arvioija
     {:lahetetty-arvioitavaksi "2021-01-01"
      :aiemmin-hankitun-osaamisen-arvioijat
      [{:nimi "Aarne Arvioija toinen"
        :organisaatio {:oppilaitos-oid
                       "1.2.246.562.10.54453923421"}}]}
     :tarkentavat-tiedot-naytto
     [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_bi"
                     :koodi-versio 6}]
       :koulutuksen-jarjestaja-osaamisen-arvioijat
       [{:nimi "Teuvo Test"
         :organisaatio {:oppilaitos-oid
                        "1.2.246.562.10.12346234691"}}]
       :jarjestaja {:oppilaitos-oid
                    "1.2.246.562.10.93270534263"}
       :nayttoymparisto {:nimi "Testi Oy"
                         :y-tunnus "1289212-2"
                         :kuvaus "Testiyhtiöö"}
       :tyoelama-osaamisen-arvioijat
       [{:nimi "Terttu Test"
         :organisaatio {:nimi "Testi Oy"
                        :y-tunnus "1289211-4"}}]
       :sisallon-kuvaus ["Testauksen suunnittelu"
                         "Jokin toinen testi"]
       :alku "2018-02-01"
       :loppu "2021-03-01"
       :yksilolliset-kriteerit ["Ensimmäinen kriteeri" "Toinen"]}]}]})

(deftest patch-ahpto-of-hoks
  (testing "PATCHes aiemmin hankitut paikalliset tutkinnon osat of HOKS"
    (assert-partial-patch-of-hoks
      ahpto-of-hoks-patched :aiemmin-hankitut-paikalliset-tutkinnon-osat)))

(def ahyto-of-hoks-patched
  {:id 1
   :aiemmin-hankitut-yhteiset-tutkinnon-osat
   [{:valittu-todentamisen-prosessi-koodi-uri
     "osaamisentodentamisenprosessi_0002"
     :valittu-todentamisen-prosessi-koodi-versio 4
     :tutkinnon-osa-koodi-versio 2
     :tutkinnon-osa-koodi-uri "tutkinnonosat_10203"
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.13490590921"
     :tarkentavat-tiedot-osaamisen-arvioija
     {:lahetetty-arvioitavaksi "2017-02-29"
      :aiemmin-hankitun-osaamisen-arvioijat
      [{:nimi "Arttu Arvioi"
        :organisaatio {:oppilaitos-oid
                       "1.2.246.562.10.54453931312"}}]}
     :osa-alueet
     [{:osa-alue-koodi-uri "ammatillisenoppiaineet_ru"
       :osa-alue-koodi-versio 4
       :koulutuksen-jarjestaja-oid
       "1.2.246.562.10.54453923577"
       :vaatimuksista-tai-tavoitteista-poikkeaminen
       "Testaus ei kuulu."
       :valittu-todentamisen-prosessi-koodi-uri
       "osaamisentodentamisenprosessi_0004"
       :valittu-todentamisen-prosessi-koodi-versio 4
       :tarkentavat-tiedot-naytto
       [{:sisallon-kuvaus ["kuvaus1" "kuvaus2"]
         :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_ai"
                       :koodi-versio 5}]
         :koulutuksen-jarjestaja-osaamisen-arvioijat
         [{:nimi "Teppo Test"
           :organisaatio {:oppilaitos-oid
                          "1.2.246.562.10.54539267911"}}]
         :jarjestaja {:oppilaitos-oid
                      "1.2.246.562.10.55890967911"}
         :nayttoymparisto {:nimi "Ab Yhtiö"
                           :y-tunnus "1234128-2"
                           :kuvaus "Testi1"}
         :tyoelama-osaamisen-arvioijat
         [{:nimi "Tellervo Tekijä"
           :organisaatio {:nimi "Ab Yhtiö"
                          :y-tunnus "1234128-1"}}]
         :yksilolliset-kriteerit ["Joku kriteeri" "Toinen"]
         :alku "2019-01-04"
         :loppu "2021-03-01"}]}]
     :tarkentavat-tiedot-naytto
     [{:osa-alueet [{:koodi-uri "ammatillisenoppiaineet_ru"
                     :koodi-versio 7}]
       :koulutuksen-jarjestaja-osaamisen-arvioijat
       [{:nimi "Erkki Esimerkkitest"
         :organisaatio {:oppilaitos-oid
                        "1.2.246.562.10.13490579091"}}]
       :jarjestaja {:oppilaitos-oid
                    "1.2.246.562.10.93270579093"}
       :nayttoymparisto {:nimi "Testi"
                         :y-tunnus "1289235-3"
                         :kuvaus "Testiyht"}
       :tyoelama-osaamisen-arvioijat
       [{:nimi "Tapio Testi"
         :organisaatio {:nimi "Testi Oyj"
                        :y-tunnus "1289235-3"}}]
       :sisallon-kuvaus ["Testauksen suunnittelu" "Jokin toinen testi" "kolmas"]
       :yksilolliset-kriteerit ["Ensimmäinen kriteeri"]
       :alku "2019-03-01"
       :loppu "2019-06-01"}]}]})

(deftest patch-ahyto-of-hoks
  (testing "PATCHes aiemmin hankitut yhteiset tutkinnon osat of HOKS"
    (assert-partial-patch-of-hoks
      ahyto-of-hoks-patched :aiemmin-hankitut-yhteiset-tutkinnon-osat)))

(deftest patch-non-existing-hoks
  (testing "PATCH prevents updating non existing HOKS"
    (let [response
          (utils/with-service-ticket
            (create-app nil)
            (-> (mock/request :patch (format "%s/1" url))
                (mock/json-body {:id 1})))]
      (is (= (:status response) 404)))))

(deftest get-hoks-by-id-not-found
  (testing "GET HOKS by hoks-id"

    (let [app (create-app nil)]
      (let [response
            (utils/with-service-ticket
              app
              (mock/request :get
                            (format "%s/%s"
                                    url 43857)))]
        (is (= (:status response) 404))))))

(deftest get-hoks-by-opiskeluoikeus-oid
  (testing "GET HOKS by opiskeluoikeus-oid"

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
               opiskeluoikeus-oid))))))

(deftest non-service-user-test
  (testing "Deny access from non-service user"
    (client/set-post!
      (fn [url options]
        (cond
          (.endsWith url "/v1/tickets")
          {:status 201
           :headers {"location" "http://test.ticket/1234"}}
          (= url "http://test.ticket/1234")
          {:status 200
           :body "ST-1234-testi"})))
    (client/set-get!
      (fn [url options]
        (cond (.endsWith
                url "/koski/api/opiskeluoikeus/1.2.246.562.15.00000000001")
              {:status 200
               :body {:oppilaitos {:oid "1.2.246.562.10.12944436166"}}}
              (.endsWith url "/serviceValidate")
              {:status 200
               :body
               (str "<cas:serviceResponse"
                    "  xmlns:cas='http://www.yale.edu/tp/cas'>"
                    "<cas:authenticationSuccess><cas:user>ehoks</cas:user>"
                    "<cas:attributes>"
                    "<cas:longTermAuthenticationRequestTokenUsed>false"
                    "</cas:longTermAuthenticationRequestTokenUsed>"
                    "<cas:isFromNewLogin>false</cas:isFromNewLogin>"
                    "<cas:authenticationDate>2019-02-20T10:14:24.046+02:00"
                    "</cas:authenticationDate></cas:attributes>"
                    "</cas:authenticationSuccess></cas:serviceResponse>")}
              (.endsWith url "/kayttooikeus-service/kayttooikeus/kayttaja")
              {:status 200
               :body [{:oidHenkilo "1.2.246.562.24.11474338834"
                       :username "ehoks"
                       :kayttajaTyyppi "VIRKAILIJA"
                       :organisaatiot
                       [{:organisaatioOid "1.2.246.562.10.12944436166"
                         :kayttooikeudet [{:palvelu "EHOKS"
                                           :oikeus "CRUD"}]}]}]})))
    (let [app (create-app nil)
          hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :laatija {:nimi "Teppo Tekijä"}
                     :paivittaja {:nimi "Pekka Päivittäjä"}
                     :hyvaksyja {:nimi "Heikki Hyväksyjä"}
                     :ensikertainen-hyvaksyminen "2018-12-15"}
          response (app (-> (mock/request :post url)
                            (mock/json-body hoks-data)
                            (mock/header "Caller-Id" "test")
                            (mock/header "ticket" "ST-testitiketti")))]
      (is (= (:status response) 403))
      (is (= (utils/parse-body (:body response))
             {:error "User type 'PALVELU' is required"}))
      (client/reset-functions!))))
