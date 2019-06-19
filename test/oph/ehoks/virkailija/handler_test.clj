(ns oph.ehoks.virkailija.handler-test
  (:require [oph.ehoks.virkailija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [clojure.test :as t]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.session-store :refer [test-session-store]]
            [oph.ehoks.db.postgresql :as db]
            [oph.ehoks.external.http-client :as client]))

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
        (t/is (some? (:opintopolku-login-url data)))
        (t/is (some? (:opintopolku-logout-url data)))
        (t/is (some? (:eperusteet-peruste-url data)))
        (t/is (some? (:virkailija-login-url data)))))))

(defn with-test-virkailija
  ([request virkailija]
    (client/set-get!
      (fn [url options]
        (cond
          (.endsWith
            url "/rest/organisaatio/v4/1.2.246.562.10.12000000001")
          {:status 200
           :body {:parentOidPath "|"}}
          (.endsWith
            url "/rest/organisaatio/v4/1.2.246.562.10.12000000000")
          {:status 200
           :body {:parentOidPath "|"}})))
    (let [session "12345678-1234-1234-1234-1234567890ab"
          cookie (str "ring-session=" session)
          store (atom
                  {session
                   {:virkailija-user virkailija}})
          app (common-api/create-app
                handler/app-routes (test-session-store store))]
      (app (mock/header request :cookie cookie))))
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
  (db/insert-oppija
    {:oid (:oid oppija)
     :nimi (:nimi oppija)})
  (db/insert-opiskeluoikeus
    {:oid (:opiskeluoikeus-oid oppija)
     :oppija_oid (:oid oppija)
     :oppilaitos_oid (:oppilaitos-oid oppija)
     :koulutustoimija_oid (:koulutustoimija-oid oppija)
     :tutkinto (:tutkinto oppija "")
     :osaamisala (:osaamisala oppija "")}))

(defn- get-search [params]
  (let [response
        (with-test-virkailija
          (mock/request
            :get
            (str base-url "/virkailija/oppijat")
            (assoc params :oppilaitos-oid "1.2.246.562.10.12000000000")))]
    (t/is (= (:status response) 200))
    (utils/parse-body (:body response))))

(t/deftest test-list-virkailija-oppijat
  (t/testing "GET virkailija oppijat"
    (utils/with-db
      (add-oppija {:oid "1.2.246.562.24.44000000001"
                   :nimi "Teuvo Testaaja"
                   :opiskeluoikeus-oid "1.2.246.562.15.76000000001"
                   :oppilaitos-oid "1.2.246.562.10.12000000000"
                   :tutkinto "Testitutkinto 1"
                   :osaamisala "Testiosaamisala numero 1"
                   :koulutustoimija-oid ""})
      (add-oppija {:oid "1.2.246.562.24.44000000002"
                   :nimi "Tellervo Testi"
                   :opiskeluoikeus-oid "1.2.246.562.15.76000000002"
                   :oppilaitos-oid "1.2.246.562.10.12000000001"
                   :tutkinto "Testitutkinto 2"
                   :osaamisala "Testiosaamisala numero 2"
                   :koulutustoimija-oid ""})
      (add-oppija {:oid "1.2.246.562.24.44000000003"
                   :nimi "Olli Oppija"
                   :opiskeluoikeus-oid "1.2.246.562.15.76000000003"
                   :oppilaitos-oid "1.2.246.562.10.12000000000"
                   :tutkinto "Testitutkinto 3"
                   :osaamisala "Osaamisala Kolme"
                   :koulutustoimija-oid ""})
      (add-oppija {:oid "1.2.246.562.24.44000000004"
                   :nimi "Oiva Oppivainen"
                   :opiskeluoikeus-oid "1.2.246.562.15.76000000004"
                   :oppilaitos-oid "1.2.246.562.10.12000000000"
                   :tutkinto "Tutkinto 4"
                   :koulutustoimija-oid ""})
      (let [body (get-search {})]
        (t/is (= (count (:data body)) 3))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.44000000004"))
        (t/is (= (get-in body [:data 1 :oid])
                 "1.2.246.562.24.44000000003"))
        (t/is (= (get-in body [:data 2 :oid])
                 "1.2.246.562.24.44000000001")))
      (let [body (get-search {:nimi "teu"})]
        (t/is (= (count (:data body)) 1))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.44000000001")))
      (let [body (get-search {:nimi "oppi"
                              :order-by-column :nimi
                              :desc true})]
        (t/is (= (count (:data body)) 2))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.44000000003"))
        (t/is (= (get-in body [:data 1 :oid])
                 "1.2.246.562.24.44000000004")))
      (let [body (get-search {:nimi "oppi"
                              :order-by-column :nimi})]
        (t/is (= (count (:data body)) 2))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.44000000004"))
        (t/is (= (get-in body [:data 1 :oid])
                 "1.2.246.562.24.44000000003")))
      (let [body (get-search {:tutkinto "testitutkinto"
                              :osaamisala "kolme"})]
        (t/is (= (count (:data body)) 1))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.44000000003"))))))

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
      (add-oppija {:oid "1.2.246.562.24.44000000001"
                   :nimi "Testi 1"
                   :opiskeluoikeus-oid "1.2.246.562.15.76000000001"
                   :oppilaitos-oid "1.2.246.562.10.12000000000"
                   :koulutustoimija-oid ""})
      (t/is
        (not
          (handler/virkailija-has-access?
            {:organisation-privileges
             [{:oid "1.2.246.562.10.12000000002"
               :privileges #{:read}}]}
            "1.2.246.562.24.44000000001")))
      (t/is
        (not
          (handler/virkailija-has-access?
            {:organisation-privileges
             [{:oid "1.2.246.562.10.12000000000"
               :privileges #{}}]}
            "1.2.246.562.24.44000000001")))
      (t/is
        (handler/virkailija-has-access?
          {:organisation-privileges
           [{:oid "1.2.246.562.10.12000000000"
             :privileges #{:read}}]}
          "1.2.246.562.24.44000000001")))))

(t/deftest test-virkailija-hoks-forbidden
  (t/testing "Virkailija HOKS forbidden"
    (utils/with-db
      (add-oppija {:oid "1.2.246.562.24.44000000001"
                   :nimi "Teuvo Testaaja"
                   :opiskeluoikeus-oid "1.2.246.562.15.76000000001"
                   :oppilaitos-oid "1.2.246.562.10.12000000000"
                   :tutkinto "Testitutkinto 1"
                   :osaamisala "Testiosaamisala numero 1"
                   :koulutustoimija-oid ""})
      (let [response
            (with-test-virkailija
              (mock/json-body
                (mock/request
                  :post
                  (str
                    base-url
                    "/virkailija/oppijat/1.2.246.562.24.44000000001/hoksit"))
                {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                 :oppija-oid "1.2.246.562.24.44000000001"
                 :ensikertainen-hyvaksyminen "2018-12-15"
                 :osaamisen-hankkimisen-tarve false})
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.12000000001"
                 :privileges #{:write :read :update :delete}}]})]
        (t/is (= (:status response) 403)))
      (let [hoks-db (db/insert-hoks!
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

(t/deftest test-virkailija-create-hoks
  (t/testing "POST hoks virkailija"
    (utils/with-db
      (add-oppija {:oid "1.2.246.562.24.44000000001"
                   :nimi "Teuvo Testaaja"
                   :opiskeluoikeus-oid "1.2.246.562.15.76000000001"
                   :oppilaitos-oid "1.2.246.562.10.12000000001"
                   :tutkinto "Testitutkinto 1"
                   :osaamisala "Testiosaamisala numero 1"
                   :koulutustoimija-oid ""})
      (let [response
            (with-test-virkailija
              (mock/json-body
                (mock/request
                  :post
                  (str
                    base-url
                    "/virkailija/oppijat/1.2.246.562.24.44000000001/hoksit"))
                {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                 :oppija-oid "1.2.246.562.24.44000000001"
                 :ensikertainen-hyvaksyminen "2018-12-15"
                 :osaamisen-hankkimisen-tarve false})
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.12000000001"
                 :privileges #{:write :read :update :delete}}]})]
        (t/is (= (:status response) 200))))))
