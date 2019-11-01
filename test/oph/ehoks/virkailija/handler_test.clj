(ns oph.ehoks.virkailija.handler-test
  (:require [oph.ehoks.virkailija.handler :as handler]
            [oph.ehoks.virkailija.middleware :as m]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [clojure.test :as t]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.session-store :refer [test-session-store]]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-opiskeluoikeus]
            [oph.ehoks.db.db-operations.oppija :as db-oppija]))

(def base-url "/ehoks-virkailija-backend/api/v1")

(t/deftest buildversion
  (t/testing "GET /buildversion.txt"
    (let [app (common-api/create-app handler/app-routes)
          response (app (mock/request
                          :get "/ehoks-virkailija-backend/buildversion.txt"))
          body (slurp (:body response))]
      (t/is (= (:status response) 200))
      (t/is (re-find #"^artifactId=" body)))))

(t/deftest not-found
  (t/testing "GET route which does not exist"
    (let [app (common-api/create-app handler/app-routes)
          response (app
                     (mock/request
                       :get (str base-url "/non-existing-resource/")))]
      (t/is (= (:status response) 404)))))

(t/deftest healthcheck
  (t/testing "GET healthcheck"
    (let [app (common-api/create-app handler/app-routes)
          response (app
                     (mock/request
                       :get (str base-url "/healthcheck")))
          body (utils/parse-body (:body response))]
      (t/is (= (:status response) 200))
      (t/is (= body {})))))

(t/deftest test-environment
  (t/testing "GET environment info"
    (let [app (common-api/create-app handler/app-routes nil)
          response (app
                     (mock/request
                       :get (str base-url "/misc/environment")))]
      (t/is (= (:status response) 200))
      (let [data (-> response :body utils/parse-body :data)]
        (t/is (some? (:opintopolku-login-url-fi data)))
        (t/is (some? (:opintopolku-login-url-sv data)))
        (t/is (some? (:opintopolku-logout-url-fi data)))
        (t/is (some? (:opintopolku-logout-url-sv data)))
        (t/is (some? (:eperusteet-peruste-url data)))
        (t/is (some? (:virkailija-login-url data)))))))

(defn with-test-virkailija
  ([request virkailija]
    (client/with-mock-responses
      [(fn [url options]
         (cond
           (.contains
             url "/rest/organisaatio/v4/")
           {:status 200
            :body {:parentOidPath "|"}}
           (.endsWith
             url "/koski/api/opiskeluoikeus/1.2.246.562.15.00000000001")
           {:status 200
            :body {:oppilaitos {:oid "1.2.246.562.10.12944436166"}}}
           (.endsWith
             url "/koski/api/opiskeluoikeus/1.2.246.562.15.760000000010")
           {:status 200
            :body {:oppilaitos {:oid "1.2.246.562.10.1200000000010"}}}
           (.endsWith
             url "/koski/api/opiskeluoikeus/1.2.246.562.15.000000000020")
           {:status 200
            :body {:oppilaitos {:oid "1.2.246.562.10.1200000000200"}}}))]
      (let [session "12345678-1234-1234-1234-1234567890ab"
            cookie (str "ring-session=" session)
            store (atom
                    {session
                     {:virkailija-user virkailija}})
            app (common-api/create-app
                  handler/app-routes (test-session-store store))]
        (app (mock/header request :cookie cookie)))))
  ([request] (with-test-virkailija
               request
               {:name "Test"
                :kayttajaTyyppi "VIRKAILIJA"
                :organisation-privileges
                [{:oid "1.2.246.562.10.12000000000"
                  :privileges #{:read}}]})))

(t/deftest test-unauthorized-virkailija
  (t/testing "GET unauthorized virkailija"
    (let [response (with-test-virkailija
                     (mock/request
                       :get
                       (str base-url "/virkailija/oppijat")
                       {:oppilaitos-oid "1.2.246.562.10.12000000000"})
                     nil)]
      (t/is (= (:status response) 401)))))

(t/deftest test-list-oppijat-with-empty-index
  (t/testing "GET empty oppijat list"
    (utils/with-db
      (let [response (with-test-virkailija
                       (mock/request
                         :get
                         (str base-url "/virkailija/oppijat")
                         {:oppilaitos-oid "1.2.246.562.10.12000000000"}))]
        (t/is (= (:status response) 200))))))

(t/deftest test-virkailija-privileges
  (t/testing "Prevent getting other organisation oppijat"
    (utils/with-db
      (let [response (with-test-virkailija
                       (mock/request
                         :get
                         (str base-url "/virkailija/oppijat")
                         {:oppilaitos-oid "1.2.246.562.10.12000000001"}))]
        (t/is (= (:status response) 403))))))

(defn- add-oppija [oppija]
  (db-oppija/insert-oppija!
    {:oid (:oid oppija)
     :nimi (:nimi oppija)})
  (db-opiskeluoikeus/insert-opiskeluoikeus!
    {:oid (:opiskeluoikeus-oid oppija)
     :oppija_oid (:oid oppija)
     :oppilaitos_oid (:oppilaitos-oid oppija)
     :koulutustoimija_oid (:koulutustoimija-oid oppija)
     :tutkinto-nimi (:tutkinto-nimi oppija
                                    {:fi "Testialan perustutkinto"
                                     :sv "Grundexamen inom testsbranschen"
                                     :en "Testing"})
     :osaamisala-nimi (:osaamisala-nimi oppija
                                        {:fi "Osaamisala suomeksi"
                                         :sv "På svenska"})}))

(defn- get-search
  ([params virkailija]
    (let [response
          (if (some? virkailija)
            (with-test-virkailija
              (mock/request
                :get
                (str base-url "/virkailija/oppijat")
                params)
              virkailija)
            (with-test-virkailija
              (mock/request
                :get
                (str base-url "/virkailija/oppijat")
                (assoc params :oppilaitos-oid "1.2.246.562.10.12000000000"))))]
      (t/is (= (:status response) 200))
      (utils/parse-body (:body response))))
  ([params] (get-search params nil)))

(defn- add-oppijat []
  (add-oppija {:oid "1.2.246.562.24.44000000001"
               :nimi "Teuvo Testaaja"
               :opiskeluoikeus-oid "1.2.246.562.15.76000000001"
               :oppilaitos-oid "1.2.246.562.10.12000000000"
               :tutkinto-nimi {:fi "Testitutkinto 1" :sv "Testskrivning 1"}
               :osaamisala-nimi
               {:fi "Testiosaamisala numero 1" :sv "Kunnande 1"}
               :koulutustoimija-oid ""})
  (add-oppija {:oid "1.2.246.562.24.44000000002"
               :nimi "Tellervo Testi"
               :opiskeluoikeus-oid "1.2.246.562.15.76000000002"
               :oppilaitos-oid "1.2.246.562.10.12000000001"
               :tutkinto-nimi {:fi "Testitutkinto 2" :sv "Testskrivning 2"}
               :osaamisala-nimi
               {:fi "Testiosaamisala numero 2" :sv "Kunnande 2"}
               :koulutustoimija-oid ""})
  (add-oppija {:oid "1.2.246.562.24.44000000003"
               :nimi "Olli Oppija"
               :opiskeluoikeus-oid "1.2.246.562.15.76000000003"
               :oppilaitos-oid "1.2.246.562.10.12000000000"
               :tutkinto-nimi {:fi "Testitutkinto 3" :sv "Testskrivning 3"}
               :osaamisala-nimi {:fi "Osaamisala Kolme" :sv "Kunnande 3"}
               :koulutustoimija-oid ""})
  (add-oppija {:oid "1.2.246.562.24.44000000004"
               :nimi "Oiva Oppivainen"
               :opiskeluoikeus-oid "1.2.246.562.15.76000000004"
               :oppilaitos-oid "1.2.246.562.10.12000000000"
               :tutkinto-nimi {:fi "Tutkinto 4"}
               :koulutustoimija-oid ""}))

(t/deftest get-oppijat-without-filtering
  (t/testing "GET virkailija oppijat without any search filters"
    (utils/with-db
      (add-oppijat)
      (let [body (get-search {})]
        (t/is (= (count (:data body)) 3))
        (t/is (= (get-in body [:meta :total-count]) 3))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.44000000004"))
        (t/is (= (get-in body [:data 1 :oid])
                 "1.2.246.562.24.44000000003"))
        (t/is (= (get-in body [:data 2 :oid])
                 "1.2.246.562.24.44000000001"))))))

(t/deftest get-oppijat-with-name-filter
  (t/testing "GET virkailija oppijat with name filtered"
    (utils/with-db
      (add-oppijat)
      (let [body (get-search {:nimi "teu"})]
        (t/is (= (count (:data body)) 1))
        (t/is (= (get-in body [:meta :total-count]) 1))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.44000000001"))))))

(t/deftest get-oppijat-with-name-filter-and-order-desc
  (t/testing "GET virkailija oppijat ordered descending and filtered with name"
    (utils/with-db
      (add-oppijat)
      (let [body (get-search {:nimi "oppi"
                              :order-by-column :nimi
                              :desc true})]
        (t/is (= (count (:data body)) 2))
        (t/is (= (get-in body [:meta :total-count]) 2))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.44000000003"))
        (t/is (= (get-in body [:data 1 :oid])
                 "1.2.246.562.24.44000000004"))))))

(t/deftest get-oppijat-with-name-filter-and-order-asc
  (t/testing "GET virkailija oppijat ordered ascending and filtered with name"
    (utils/with-db
      (add-oppijat)
      (let [body (get-search {:nimi "oppi"
                              :order-by-column :nimi})]
        (t/is (= (count (:data body)) 2))
        (t/is (= (get-in body [:meta :total-count]) 2))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.44000000004"))
        (t/is (= (get-in body [:data 1 :oid])
                 "1.2.246.562.24.44000000003"))))))

(t/deftest get-oppijat-filtered-with-tutkinto-and-osaamisala
  (t/testing "GET virkailija oppijat filtered with tutkinto and osaamisala"
    (utils/with-db
      (add-oppijat)
      (let [body (get-search {:tutkinto "testitutkinto"
                              :osaamisala "kolme"})]
        (t/is (= (count (:data body)) 1))
        (t/is (= (get-in body [:meta :total-count]) 1))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.44000000003"))))))

(t/deftest get-oppijat-filtered-with-swedish-locale
  (t/testing "GET virkailija oppijat filtered with swedish locale"
    (utils/with-db
      (add-oppijat)
      (let [body (get-search {:tutkinto "testskrivning"
                              :osaamisala "kunnande"
                              :order-by-column "tutkinto"
                              :desc true
                              :locale "sv"})]
        (t/is (= (count (:data body)) 2))
        (t/is (= (get-in body [:meta :total-count]) 2))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.44000000003"))
        (t/is (= (get-in body [:data 1 :oid])
                 "1.2.246.562.24.44000000001"))))))

(t/deftest get-oppijat-with-swedish-locale-without-translation
  (t/testing
   "Doesn't have swedish translation and no search filters, shouldn't filter"
    (utils/with-db
      (add-oppija {:oid "1.2.246.562.24.44000000003"
                   :nimi "Olli Oppija"
                   :opiskeluoikeus-oid "1.2.246.562.15.76000000003"
                   :oppilaitos-oid "1.2.246.562.10.12000000000"
                   :tutkinto-nimi {:fi "Testitutkinto 3" :sv "Testskrivning 3"}
                   :osaamisala-nimi {:fi "Osaamisala Kolme"}
                   :koulutustoimija-oid ""})
      (let [body (get-search {:order-by-column "tutkinto"
                              :desc true
                              :locale "sv"})]
        (t/is (= (count (:data body)) 1))
        (t/is (= (get-in body [:meta :total-count]) 1))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.44000000003"))))))

(t/deftest test-list-virkailija-oppija-with-multi-opiskeluoikeus
  (t/testing "GET virkailija oppijat"
    (utils/with-db
      (add-oppija {:oid "1.2.246.562.24.44000000001"
                   :nimi "Teuvo Testaaja"
                   :opiskeluoikeus-oid "1.2.246.562.15.760000000010"
                   :oppilaitos-oid "1.2.246.562.10.1200000000010"
                   :tutkinto-nimi {:fi "Testitutkinto 1"}
                   :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
                   :koulutustoimija-oid ""})
      (db-opiskeluoikeus/insert-opiskeluoikeus!
        {:oid "1.2.246.562.15.760000000020"
         :oppija_oid "1.2.246.562.24.44000000001"
         :oppilaitos_oid "1.2.246.562.10.1200000000020"
         :koulutustoimija_oid ""
         :tutkinto-nimi {:fi "Tutkinto 2"}
         :osaamisala-nimi {:fi "Osaamisala 2"}})

      (let [body (get-search
                   {:oppilaitos-oid "1.2.246.562.10.1200000000020"}
                   {:name "Test"
                    :kayttajaTyyppi "VIRKAILIJA"
                    :oidHenkilo "1.2.246.562.24.220000000030"
                    :organisation-privileges
                    [{:oid "1.2.246.562.10.1200000000020"
                      :privileges #{:read}}]})]
        (t/is (= (count (:data body)) 1))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.44000000001")))
      (let [body (get-search
                   {:oppilaitos-oid "1.2.246.562.10.1200000000010"}
                   {:name "Test"
                    :kayttajaTyyppi "VIRKAILIJA"
                    :oidHenkilo "1.2.246.562.24.220000000020"
                    :organisation-privileges
                    [{:oid "1.2.246.562.10.1200000000010"
                      :privileges #{:read}}]})]
        (t/is (= (count (:data body)) 1))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.44000000001"))))))

(t/deftest test-virkailija-with-no-read
  (t/testing "Prevent GET virkailija oppijat without read privilege"
    (utils/with-db
      (add-oppija {:oid "1.2.246.562.24.44000000001"
                   :nimi "Testi 1"
                   :opiskeluoikeus-oid "1.2.246.562.15.76000000001"
                   :oppilaitos-oid "1.2.246.562.10.12000000000"
                   :koulutustoimija-oid ""})
      (let [response (with-test-virkailija
                       (mock/request
                         :get
                         (str base-url "/virkailija/oppijat")
                         {:oppilaitos-oid "1.2.246.562.10.12000000000"})
                       {:name "Test"
                        :kayttajaTyyppi "VIRKAILIJA"
                        :organisation-privileges
                        [{:oid "1.2.246.562.10.12000000000"
                          :privileges #{}}]})]
        (t/is (= (:status response) 403))))))

(t/deftest test-virkailija-has-access
  (t/testing "Virkailija has oppija access"
    (utils/with-db
      (client/with-mock-responses
        [(fn [url options]
           (cond
             (.contains
               url "/rest/organisaatio/v4/")
             {:status 200
              :body {:parentOidPath "|"}}))]

        (add-oppija {:oid "1.2.246.562.24.44000000001"
                     :nimi "Testi 1"
                     :opiskeluoikeus-oid "1.2.246.562.15.76000000001"
                     :oppilaitos-oid "1.2.246.562.10.12000000000"
                     :koulutustoimija-oid ""})
        (t/is
          (not
            (m/virkailija-has-access?
              {:organisation-privileges
               [{:oid "1.2.246.562.10.12000000002"
                 :privileges #{:read}}]}
              "1.2.246.562.24.44000000001")))
        (t/is
          (not
            (m/virkailija-has-access?
              {:organisation-privileges
               [{:oid "1.2.246.562.10.12000000000"
                 :privileges #{}}]}
              "1.2.246.562.24.44000000001")))
        (t/is
          (m/virkailija-has-access?
            {:organisation-privileges
             [{:oid "1.2.246.562.10.12000000000"
               :privileges #{:read}}]}
            "1.2.246.562.24.44000000001"))))))

(t/deftest test-virkailija-hoks-write-forbidden
  (t/testing "Virkailija HOKS write is forbidden"
    (utils/with-db
      (add-oppija {:oid "1.2.246.562.24.44000000001"
                   :nimi "Teuvo Testaaja"
                   :opiskeluoikeus-oid "1.2.246.562.15.760000000010"
                   :oppilaitos-oid "1.2.246.562.10.1200000000010"
                   :tutkinto-nimi {:fi "Testitutkinto 1"}
                   :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
                   :koulutustoimija-oid ""})
      (db-opiskeluoikeus/insert-opiskeluoikeus!
        {:oid "1.2.246.562.15.760000000020"
         :oppija_oid "1.2.246.562.24.44000000001"
         :oppilaitos_oid "1.2.246.562.10.1200000000200"
         :tutkinto-nimi {:fi "Testitutkinto 2"}
         :osaamisala-nimi {:fi "Testiosaamisala 2"}})
      (let [response
            (with-test-virkailija
              (mock/json-body
                (mock/request
                  :post
                  (str
                    base-url
                    "/virkailija/oppijat/1.2.246.562.24.44000000001/hoksit"))
                {:opiskeluoikeus-oid "1.2.246.562.15.760000000010"
                 :oppija-oid "1.2.246.562.24.44000000001"
                 :ensikertainen-hyvaksyminen "2018-12-15"
                 :osaamisen-hankkimisen-tarve false})
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.1200000000200"
                 :privileges #{:write :read :update :delete}}
                {:oid "1.2.246.562.10.1200000000010"
                 :privileges #{:read}}]})]
        (t/is (= (:status response) 403))))))

(defn- create-oppija-for-hoks-post [oppilaitos-oid]
  (add-oppija {:oid "1.2.246.562.24.44000000001"
               :nimi "Teuvo Testaaja"
               :opiskeluoikeus-oid "1.2.246.562.15.76000000001"
               :oppilaitos-oid oppilaitos-oid
               :tutkinto-nimi {:fi "Testitutkinto 1"}
               :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
               :koulutustoimija-oid ""}))

(defn- post-new-hoks [opiskeluoikeus-oid organisaatio-oid]
  (with-test-virkailija
    (mock/json-body
      (mock/request
        :post
        (str
          base-url
          "/virkailija/oppijat/1.2.246.562.24.44000000001/hoksit"))
      {:opiskeluoikeus-oid opiskeluoikeus-oid
       :oppija-oid "1.2.246.562.24.44000000001"
       :ensikertainen-hyvaksyminen "2018-12-15"
       :osaamisen-hankkimisen-tarve false})
    {:name "Testivirkailija"
     :kayttajaTyyppi "VIRKAILIJA"
     :oidHenkilo "1.2.246.562.24.44000000333"
     :organisation-privileges
     [{:oid organisaatio-oid
       :privileges #{:write :read :update :delete}}]}))

(t/deftest test-virkailija-hoks-forbidden
  (t/testing "Virkailija HOKS forbidden"
    (utils/with-db
      (add-oppija {:oid "1.2.246.562.24.44000000001"
                   :nimi "Teuvo Testaaja"
                   :opiskeluoikeus-oid "1.2.246.562.15.76000000001"
                   :oppilaitos-oid "1.2.246.562.10.12000000000"
                   :tutkinto-nimi {:fi "Testitutkinto 1"}
                   :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
                   :koulutustoimija-oid ""})
      (let [response
            (post-new-hoks
              "1.2.246.562.15.00000000001" "1.2.246.562.10.12000000001")]
        (t/is (= (:status response) 403)))
      (let [hoks-db (db-hoks/insert-hoks!
                      {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                       :oppija-oid "1.2.246.562.24.44000000001"
                       :osaamisen-hankkimisen-tarve false
                       :ensikertainen-hyvaksyminen
                       (java.time.LocalDate/of 2018 12 15)})
            response
            (with-test-virkailija
              (mock/request
                :get
                (str
                  base-url
                  "/virkailija/oppijat/1.2.246.562.24.44000000001/hoksit/"
                  (:id hoks-db)))
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.12000000001"
                 :privileges #{:write :read :update :delete}}]})]
        (t/is (= (:status response) 403))))))

(defn- get-created-hoks [post-response]
  (with-test-virkailija
    (mock/request
      :get
      (get-in (utils/parse-body (:body post-response)) [:data :uri]))
    {:name "Testivirkailija"
     :kayttajaTyyppi "VIRKAILIJA"
     :oidHenkilo "1.2.246.562.24.44000000333"
     :organisation-privileges
     [{:oid "1.2.246.562.10.12000000001"
       :privileges #{:write :read :update :delete}}]}))

(t/deftest test-virkailija-create-hoks
  (t/testing "POST hoks virkailija"
    (utils/with-db
      (create-oppija-for-hoks-post "1.2.246.562.10.12000000001")
      (let [post-response
            (post-new-hoks
              "1.2.246.562.15.76000000001" "1.2.246.562.10.12000000001")
            get-response (get-created-hoks post-response)]
        (t/is (get-in (utils/parse-body (:body get-response))
                      [:data :manuaalisyotto]))
        (t/is (= (:status post-response) 200))))))

(defn mocked-get-opiskeluoikeus-info-raw [oid]
  (throw (ex-info "Opiskeluoikeus fetch failed" {:status 404})))

(t/deftest test-hoks-create-when-opiskeluoikeus-fetch-fails
  (t/testing "Error thrown from koski is propagated to handler"
    (utils/with-db
      (create-oppija-for-hoks-post "1.2.246.562.10.12000000001")
      (with-redefs [oph.ehoks.external.koski/get-opiskeluoikeus-info-raw
                    mocked-get-opiskeluoikeus-info-raw]
        (let [post-response
              (post-new-hoks
                "1.2.246.562.15.76000000002" "1.2.246.562.10.12000000001")]
          (t/is (= (:status post-response) 400))
          (t/is (= (utils/parse-body (:body post-response))
                   {:error "Opiskeluoikeus not found in Koski"})))))))

(defn mocked-find-student-by-oid [oid]
  (throw (ex-info "Opiskelija fetch failed" {:status 404})))

(t/deftest test-hoks-create-when-oppijanumerorekisteri-fails
  (t/testing "Error thrown from oppijanumerorekisteri is propagated to handler"
    (utils/with-db
      (with-redefs [oph.ehoks.external.oppijanumerorekisteri/find-student-by-oid
                    mocked-find-student-by-oid]
        (let [post-response
              (post-new-hoks
                "1.2.246.562.15.76000000002" "1.2.246.562.10.12000000001")]
          (t/is (= (:status post-response) 400))
          (t/is (= (utils/parse-body (:body post-response))
                   {:error "Oppija not found in Oppijanumerorekisteri"})))))))

(t/deftest test-virkailija-patch-hoks
  (t/testing "PATCH hoks virkailija"
    (utils/with-db
      (create-oppija-for-hoks-post "1.2.246.562.10.12000000001")
      (let [post-response
            (post-new-hoks
              "1.2.246.562.15.760000000010" "1.2.246.562.10.1200000000010")
            body (utils/parse-body (:body post-response))
            hoks-url (get-in body [:data :uri])
            patch-response
            (with-test-virkailija
              (mock/json-body
                (mock/request
                  :patch
                  hoks-url)
                {:osaamisen-hankkimisen-tarve true
                 :id (get-in body [:meta :id])})
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.1200000000010"
                 :privileges #{:write :read :update :delete}}]})
            get-response
            (with-test-virkailija
              (mock/request
                :get
                hoks-url)
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.1200000000010"
                 :privileges #{:write :read :update :delete}}]})]
        (t/is (get-in (utils/parse-body (:body get-response))
                      [:data :osaamisen-hankkimisen-tarve]))
        (t/is (= (:status patch-response) 204))))))

(t/deftest test-prevent-virkailija-patch-hoks
  (t/testing "PATCH hoks virkailija"
    (utils/with-db
      (add-oppija {:oid "1.2.246.562.24.44000000001"
                   :nimi "Teuvo Testaaja"
                   :opiskeluoikeus-oid "1.2.246.562.15.760000000010"
                   :oppilaitos-oid "1.2.246.562.10.1200000000010"
                   :tutkinto-nimi {:fi "Testitutkinto 1"}
                   :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
                   :koulutustoimija-oid ""})
      (db-opiskeluoikeus/insert-opiskeluoikeus!
        {:oid "1.2.246.562.15.760000000020"
         :oppija_oid "1.2.246.562.24.44000000001"
         :oppilaitos_oid "1.2.246.562.10.1200000000200"
         :tutkinto-nimi {:fi "Testitutkinto 2"}
         :osaamisala-nimi {:fi "Testiosaamisala 2"}})
      (let [response
            (with-test-virkailija
              (mock/json-body
                (mock/request
                  :post
                  (str
                    base-url
                    "/virkailija/oppijat/1.2.246.562.24.44000000001/hoksit"))
                {:opiskeluoikeus-oid "1.2.246.562.15.760000000010"
                 :oppija-oid "1.2.246.562.24.44000000001"
                 :ensikertainen-hyvaksyminen "2018-12-15"
                 :osaamisen-hankkimisen-tarve false})
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.1200000000010"
                 :privileges #{:write :read :update :delete}}]})
            body (utils/parse-body (:body response))
            hoks-url (get-in body [:data :uri])
            patch-response
            (with-test-virkailija
              (mock/json-body
                (mock/request
                  :patch
                  hoks-url)
                {:osaamisen-hankkimisen-tarve true
                 :id (get-in body [:meta :id])})
              {:name "Testivirkailija 2"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.1200000000200"
                 :privileges #{:write :read :update :delete}}
                {:oid "1.2.246.562.10.1200000000010"
                 :privileges #{:read}}]})]
        (t/is (= (:status patch-response) 403))))))

(def hoks-data
  {:opiskeluoikeus-oid "1.2.246.562.15.760000000010"
   :oppija-oid "1.2.246.562.24.44000000001"
   :ensikertainen-hyvaksyminen "2018-12-15"
   :osaamisen-hankkimisen-tarve false})

(def hato-data
  [{:tutkinnon-osa-koodi-uri "tutkinnonosat_102499"
    :tutkinnon-osa-koodi-versio 4
    :vaatimuksista-tai-tavoitteista-poikkeaminen
    "Ei poikkeamia."
    :osaamisen-osoittaminen
    [{:jarjestaja
      {:oppilaitos-oid "1.2.246.562.10.54453924330"}
      :nayttoymparisto {:nimi "Testiympäristö 2"
                        :y-tunnus "12345671-2"
                        :kuvaus "Testi test"}
      :sisallon-kuvaus ["Testaus"]
      :yksilolliset-kriteerit ["kriteeri 1" "kriteeri2"]
      :koulutuksen-jarjestaja-osaamisen-arvioijat
      [{:nimi "Timo Testaaja"
        :organisaatio
        {:oppilaitos-oid "1.2.246.562.10.54452521332"}}]
      :tyoelama-osaamisen-arvioijat
      [{:nimi "Taneli Työmies"
        :organisaatio {:nimi "Tanelin Paja Ky"
                       :y-tunnus "12345622-2"}}]
      :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_kl"
                    :koodi-versio 3}]
      :alku "2019-03-10"
      :loppu "2019-03-19"}]
    :osaamisen-hankkimistavat
    [{:jarjestajan-edustaja
      {:nimi "Ville Valvoja"
       :rooli "Valvojan apulainen"
       :oppilaitos-oid "1.2.246.562.10.54451211340"}
      :osaamisen-hankkimistapa-koodi-uri
      "osaamisenhankkimistapa_oppisopimus"
      :osaamisen-hankkimistapa-koodi-versio 2
      :tyopaikalla-jarjestettava-koulutus
      {:vastuullinen-tyopaikka-ohjaaja
       {:nimi "Aimo Ohjaaja"
        :sahkoposti "aimo.ohjaaja@esimerkki2.com"}
       :tyopaikan-nimi "Ohjausyhtiö Oy"
       :tyopaikan-y-tunnus "12345212-4"
       :keskeiset-tyotehtavat ["Testitehtävä"]}
      :muut-oppimisymparistot
      [{:oppimisymparisto-koodi-uri "oppimisymparistot_0002"
        :oppimisymparisto-koodi-versio 1
        :alku "2019-01-13"
        :loppu "2019-02-19"}]
      :ajanjakson-tarkenne "Ei tarkennettavia asioita"
      :hankkijan-edustaja
      {:nimi "Heikki Hankkija"
       :rooli "Opettaja"
       :oppilaitos-oid "1.2.246.562.10.54452422420"}
      :alku "2019-01-11"
      :loppu "2019-03-14"}]
    :koulutuksen-jarjestaja-oid "1.2.246.562.10.54411232222"}])

(t/deftest test-virkailija-put-hoks
  (t/testing "PUT hoks virkailija"
    (utils/with-db
      (add-oppija {:oid "1.2.246.562.24.44000000001"
                   :nimi "Teuvo Testaaja"
                   :opiskeluoikeus-oid "1.2.246.562.15.760000000010"
                   :oppilaitos-oid "1.2.246.562.10.1200000000010"
                   :tutkinto-nimi {:fi "Testitutkinto 1"}
                   :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
                   :koulutustoimija-oid ""})
      (let [response
            (with-test-virkailija
              (mock/json-body
                (mock/request
                  :post
                  (str
                    base-url
                    "/virkailija/oppijat/1.2.246.562.24.44000000001/hoksit"))
                hoks-data)
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.1200000000010"
                 :privileges #{:write :read :update :delete}}]})
            body (utils/parse-body (:body response))
            hoks-url (get-in body [:data :uri])
            put-response
            (with-test-virkailija
              (mock/json-body
                (mock/request
                  :put
                  hoks-url)
                (assoc
                  hoks-data
                  :id (get-in body [:meta :id])
                  :hankittavat-ammat-tutkinnon-osat
                  hato-data))
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.1200000000010"
                 :privileges #{:write :read :update :delete}}]})
            get-response
            (with-test-virkailija
              (mock/request
                :get
                hoks-url)
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.1200000000010"
                 :privileges #{:write :read :update :delete}}]})]
        (let [body (utils/parse-body (:body get-response))]
          (utils/eq (get-in body
                            [:data :hankittavat-ammat-tutkinnon-osat])
                    hato-data))
        (t/is (= (:status put-response) 204))))))
