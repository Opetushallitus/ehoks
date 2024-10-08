(ns oph.ehoks.virkailija.handler-test
  (:require [clojure.string :as string]
            [clojure.test :as t]
            [clojure.tools.logging.test :as logtest]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.db.db-operations.db-helpers :as db-helpers]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-opiskeluoikeus]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [oph.ehoks.hoks.hoks-test-utils :refer [virkailija-base-url]]
            [oph.ehoks.palaute.opiskelija.kyselylinkki :as kyselylinkki]
            [oph.ehoks.session-store :refer [test-session-store]]
            [oph.ehoks.user :as user]
            [oph.ehoks.test-utils :as test-utils]
            [oph.ehoks.virkailija.handler :as handler]
            [oph.ehoks.virkailija.virkailija-test-utils :as v-utils]
            [ring.mock.request :as mock])
  (:import (java.time LocalDate LocalDateTime)))

(t/use-fixtures :once test-utils/migrate-database)
(t/use-fixtures :each test-utils/empty-database-after-test)

(def base-url "/ehoks-virkailija-backend/api/v1")

(defn- add-caller-id [request]
  (mock/header request "Caller-Id" "test"))

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
          body (test-utils/parse-body (:body response))]
      (t/is (= (:status response) 200))
      (t/is (= body {})))))

(t/deftest test-environment
  (t/testing "GET environment info"
    (let [app (common-api/create-app handler/app-routes nil)
          response (app
                     (add-caller-id
                       (mock/request
                         :get (str base-url "/misc/environment"))))]
      (t/is (= (:status response) 200))
      (let [data (-> response :body test-utils/parse-body :data)]
        (t/is (some? (:eperusteet-peruste-url data)))
        (t/is (some? (:virkailija-login-url data)))
        (t/is (some? (:raamit-url data)))))))

(defn with-test-virkailija
  ([request virkailija]
    (client/with-mock-responses
      [(fn [^String url options]
         (cond
           (.contains
             url "/rest/organisaatio/v4/")
           {:status 200
            :body {:oid           (last (string/split url #"/"))
                   :parentOidPath (str "|1.2.246.562.10.00000000001|"
                                       (last (string/split url #"/"))
                                       "|")}}
           (.endsWith
             url "/koski/api/opiskeluoikeus/1.2.246.562.15.10000000009")
           {:status 200
            :body {:oid "1.2.246.562.15.10000000009"
                   :oppilaitos {:oid "1.2.246.562.10.12944436166"}
                   :tyyppi {:koodiarvo "ammatillinenkoulutus"}}}
           (.endsWith
             url "/koski/api/opiskeluoikeus/1.2.246.562.15.12000000203")
           {:status 200
            :body {:oid "1.2.246.562.15.12000000203"
                   :oppilaitos {:oid "1.2.246.562.10.12944436166"}
                   :tyyppi {:koodiarvo "ammatillinenkoulutus"}
                   :päättymispäivä "2018-12-01"
                   :tila {:opiskeluoikeusjaksot
                          [{:alku "2018-12-01"
                            :tila {:koodiarvo "valmistunut"
                                   :nimi {:fi "Valmistunut"}
                                   :koodistoUri
                                   "koskiopiskeluoikeudentila"
                                   :koodistoVersio 1}}]}}}
           (.endsWith
             url "/koski/api/opiskeluoikeus/1.2.246.562.15.12000000005")
           {:status 200
            :body {:oid "1.2.246.562.15.12000000005"
                   :oppilaitos {:oid "1.2.246.562.10.12000000013"}
                   :tyyppi {:koodiarvo "ammatillinenkoulutus"}
                   :päättymispäivä "2019-12-01"
                   :tila {:opiskeluoikeusjaksot
                          [{:alku "2019-12-01"
                            :tila {:koodiarvo "katsotaaneronneeksi"
                                   :nimi {:fi "Katsotaan eronneeksi"}
                                   :koodistoUri
                                   "koskiopiskeluoikeudentila"
                                   :koodistoVersio 1}}]}}}
           (.endsWith
             url "/koski/api/opiskeluoikeus/1.2.246.562.15.44000000008")
           (throw
             (ex-info
               "Testing404"
               {:status 404
                :body (str "[{\"key\":\"notFound."
                           "opiskeluoikeuttaEiLöydyTaiEiOikeuksia\"}]")}))
           (.endsWith
             url "/koski/api/opiskeluoikeus/1.2.246.562.15.76000000018")
           {:status 200
            :body {:oid "1.2.246.562.15.76000000018"
                   :oppilaitos {:oid "1.2.246.562.10.12000000013"}
                   :tila {:opiskeluoikeusjaksot
                          [{:alku "2023-07-03"
                            :tila {:koodiarvo "lasna"
                                   :nimi {:fi "Läsnä"}
                                   :koodistoUri
                                   "koskiopiskeluoikeudentila"
                                   :koodistoVersio 1}}]}
                   :tyyppi {:koodiarvo "ammatillinenkoulutus"}}}
           (.endsWith
             url "/koski/api/opiskeluoikeus/1.2.246.562.15.76000000000")
           {:status 200
            :body {:oid "1.2.246.562.15.76000000000"
                   :oppilaitos {:oid "1.2.246.562.10.12000000013"}
                   :tyyppi {:koodiarvo "ammatillinenkoulutus"}}}
           (.contains
             url "/koski/api/opiskeluoikeus/")
           {:status 200
            :body {:oid (last (string/split url #"/"))
                   :oppilaitos {:oid "1.2.246.562.10.12000000203"}
                   :tyyppi {:koodiarvo "ammatillinenkoulutus"}}}
           (.endsWith
             url
             (str "/oppijanumerorekisteri-service/henkilo/"
                  "1.2.246.562.24.45000000007"))
           {:status 200
            :body {:oidHenkilo "1.2.246.562.24.45000000007"
                   :duplicate false}}
           (.endsWith
             url (str "/oppijanumerorekisteri-service/henkilo/"
                      "1.2.246.562.24.45000000007/slaves"))
           {:status 200
            :body []}
           (.endsWith
             url
             (str "/oppijanumerorekisteri-service/henkilo/"
                  "1.2.246.562.24.12312312319"))
           {:status 200
            :body {:oidHenkilo "1.2.246.562.24.12312312319"
                   :duplicate false}}
           (.endsWith
             url (str "/oppijanumerorekisteri-service/henkilo/"
                      "1.2.246.562.24.12312312319/slaves"))
           {:status 200
            :body [{:oidHenkilo "1.2.246.562.24.44000000008"}]}
           (.endsWith
             url (str "/oppijanumerorekisteri-service/henkilo/"
                      "1.2.246.562.24.44000000008/master"))
           {:status 200
            :body {:oidHenkilo "1.2.246.562.24.12312312319"}}
           (.endsWith
             url (str "/oppijanumerorekisteri-service/henkilo/"
                      "1.2.246.562.24.12000000014"))
           {:status 200
            :body {:oidHenkilo "1.2.246.562.24.12000000014"
                   :duplicate true}}))
       (fn [^String url options]
         (cond
           (.endsWith url "/v1/tickets")
           {:status 201
            :headers {"location" "http://test.ticket/1234"}}
           (= url "http://test.ticket/1234")
           {:status 200
            :body "ST-1234-testi"}
           (.endsWith
             url "/koski/api/sure/oids")
           {:status 200
            :body [{:henkilö {:oid "1.2.246.562.24.44000000008"}
                    :opiskeluoikeudet
                    [{:oid "1.2.246.562.15.76000000000"
                      :oppilaitos {:oid "1.2.246.562.10.12000000005"
                                   :nimi {:fi "TestiFi"
                                          :sv "TestiSv"
                                          :en "TestiEn"}}
                      :alkamispäivä "2020-03-12"}]}]}))]
      (let [session "12345678-1234-1234-1234-1234567890ab"
            cookie (str "ring-session=" session)
            store (atom
                    {session
                     {:virkailija-user virkailija}})
            app (common-api/create-app
                  handler/app-routes (test-session-store store))]
        (app (-> request
                 (mock/header :cookie cookie)
                 (mock/header "caller-id" "test"))))))
  ([request] (with-test-virkailija
               request
               {:name "Test"
                :kayttajaTyyppi "VIRKAILIJA"
                :organisation-privileges
                [{:oid "1.2.246.562.10.12000000005"
                  :privileges #{:read}}]})))

(t/deftest test-unauthorized-virkailija
  (t/testing "GET unauthorized virkailija"
    (let [response (with-test-virkailija
                     (mock/request
                       :get
                       (str base-url "/virkailija/oppijat")
                       {:oppilaitos-oid "1.2.246.562.10.12000000005"})
                     nil)]
      (t/is (= (:status response) 401)))))

(t/deftest test-list-oppijat-with-empty-index
  (t/testing "GET empty oppijat list"
    (test-utils/with-db
      (let [response (with-test-virkailija
                       (mock/request
                         :get
                         (str base-url "/virkailija/oppijat")
                         {:oppilaitos-oid "1.2.246.562.10.12000000005"}))]
        (t/is (= (:status response) 200))))))

(t/deftest test-virkailija-privileges
  (t/testing "Prevent getting other organisation oppijat"
    (test-utils/with-db
      (let [response (with-test-virkailija
                       (mock/request
                         :get
                         (str base-url "/virkailija/oppijat")
                         {:oppilaitos-oid "1.2.246.562.10.12100000004"}))]
        (t/is (= (:status response) 403))))))

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
                (assoc params :oppilaitos-oid "1.2.246.562.10.12000000005"))))]
      (t/is (= (:status response) 200))
      (test-utils/parse-body (:body response))))
  ([params] (get-search params nil)))

(defn- add-oppijat []
  (v-utils/add-oppija {:oid "1.2.246.562.24.43000000009"
                       :nimi "Teuvo Testaaja"
                       :opiskeluoikeus-oid "1.2.246.562.15.76100000002"
                       :oppilaitos-oid "1.2.246.562.10.12000000005"
                       :tutkinto-nimi {:fi "Testitutkinto 1"
                                       :sv "Testskrivning 1"}
                       :osaamisala-nimi
                       {:fi "Testiosaamisala numero 1" :sv "Kunnande 1"}
                       :koulutustoimija-oid ""})
  (v-utils/add-oppija {:oid "1.2.246.562.24.44000000008"
                       :nimi "Tellervo Testi"
                       :opiskeluoikeus-oid "1.2.246.562.15.76200000009"
                       :oppilaitos-oid "1.2.246.562.10.12100000004"
                       :tutkinto-nimi {:fi "Testitutkinto 2"
                                       :sv "Testskrivning 2"}
                       :osaamisala-nimi
                       {:fi "Testiosaamisala numero 2" :sv "Kunnande 2"}
                       :koulutustoimija-oid ""})
  (v-utils/add-oppija {:oid "1.2.246.562.24.45000000007"
                       :nimi "Olli Oppija"
                       :opiskeluoikeus-oid "1.2.246.562.15.76300000007"
                       :oppilaitos-oid "1.2.246.562.10.12000000005"
                       :tutkinto-nimi {:fi "Testitutkinto 3"
                                       :sv "Testskrivning 3"}
                       :osaamisala-nimi {:fi "Osaamisala Kolme"
                                         :sv "Kunnande 3"}
                       :koulutustoimija-oid ""})
  (v-utils/add-oppija {:oid "1.2.246.562.24.46000000006"
                       :nimi "Oiva Oppivainen"
                       :opiskeluoikeus-oid "1.2.246.562.15.76400000006"
                       :oppilaitos-oid "1.2.246.562.10.12000000005"
                       :tutkinto-nimi {:fi "Tutkinto 4"}
                       :koulutustoimija-oid ""}))

(defn- add-hoksit []
  (v-utils/add-hoks {:oid "1.2.246.562.24.43000000009"
                     :opiskeluoikeus-oid "1.2.246.562.15.76100000002"})
  (v-utils/add-hoks {:oid "1.2.246.562.24.44000000008"
                     :opiskeluoikeus-oid "1.2.246.562.15.76200000009"})
  (v-utils/add-hoks {:oid "1.2.246.562.24.45000000007"
                     :opiskeluoikeus-oid "1.2.246.562.15.76300000007"})
  (v-utils/add-hoks {:oid "1.2.246.562.24.46000000006"
                     :opiskeluoikeus-oid "1.2.246.562.15.76400000006"}))

(t/deftest get-oppijat-without-filtering
  (t/testing "GET virkailija oppijat without any search filters"
    (test-utils/with-db
      (add-oppijat)
      (add-hoksit)
      (let [body (get-search {})]
        (t/is (= (count (:data body)) 3))
        (t/is (= (get-in body [:meta :total-count]) 3))
        (t/is (= (set (map :hoks-id (:data body)))
                 #{1 3 4}))
        (t/is (= (set (map :oid (:data body)))
                 #{"1.2.246.562.24.46000000006"
                   "1.2.246.562.24.45000000007"
                   "1.2.246.562.24.43000000009"}))))))

(t/deftest get-oppijat-with-name-filter
  (t/testing "GET virkailija oppijat with name filtered"
    (test-utils/with-db
      (add-oppijat)
      (add-hoksit)
      (let [body (get-search {:nimi "teu"})]
        (t/is (= (count (:data body)) 1))
        (t/is (= (get-in body [:meta :total-count]) 1))
        (t/is (= (get-in body [:data 0 :hoks-id]) 1))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.43000000009"))))))

(t/deftest get-oppijat-with-hoks-id
  (t/testing "oppijat endpoint returns correct result by exact hoks-id"
    (test-utils/with-db
      (add-oppijat)
      (add-hoksit)
      (let [body (get-search {:hoks-id 3})]
        (t/is (= (count (:data body)) 1))
        (t/is (= (:total-count (:meta body)) 1))
        (t/is (= (get-in body [:data 0 :oid]) "1.2.246.562.24.45000000007"))
        (t/is (= (get-in body [:data 0 :hoks-id]) 3)))
      (let [body (get-search {:hoks-id 30033})]
        (t/is (= (count (:data body)) 0))
        (t/is (= (:total-count (:meta body)) 0))))))

(t/deftest get-oppijat-with-name-filter-and-order-desc
  (t/testing "GET virkailija oppijat ordered descending and filtered with name"
    (test-utils/with-db
      (add-oppijat)
      (add-hoksit)
      (let [body (get-search {:nimi "oppi"
                              :order-by-column "nimi"
                              :desc true})]
        (t/is (= (count (:data body)) 2))
        (t/is (= (get-in body [:meta :total-count]) 2))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.45000000007"))
        (t/is (= (get-in body [:data 1 :oid])
                 "1.2.246.562.24.46000000006"))))))

(t/deftest get-oppijat-with-name-filter-and-order-asc
  (t/testing "GET virkailija oppijat ordered ascending and filtered with name"
    (test-utils/with-db
      (add-oppijat)
      (add-hoksit)
      (let [body (get-search {:nimi "oppi"
                              :order-by-column "nimi"})]
        (t/is (= (count (:data body)) 2))
        (t/is (= (get-in body [:meta :total-count]) 2))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.46000000006"))
        (t/is (= (get-in body [:data 1 :oid])
                 "1.2.246.562.24.45000000007"))))))

(t/deftest get-oppijat-filtered-with-tutkinto-and-osaamisala
  (t/testing "GET virkailija oppijat filtered with tutkinto and osaamisala"
    (test-utils/with-db
      (add-oppijat)
      (add-hoksit)
      (let [body (get-search {:tutkinto "testitutkinto"
                              :osaamisala "kolme"})]
        (t/is (= (count (:data body)) 1))
        (t/is (= (get-in body [:meta :total-count]) 1))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.45000000007"))))))

(t/deftest oppijat-sql-injection
  (t/testing "oppijat endpoint doesn't have the SQL injection it used to"
    (test-utils/with-db
      (add-oppijat)
      (add-hoksit)
      (let [body (get-search {:tutkinto "';drop table hoksit;commit;--"})]
        (t/is (= (count (:data body)) 0))
        (t/is (= (get-in body [:meta :total-count]) 0))))))

(t/deftest get-oppijat-filtered-with-swedish-locale
  (t/testing "GET virkailija oppijat filtered with swedish locale"
    (test-utils/with-db
      (add-oppijat)
      (add-hoksit)
      (let [body (get-search {:tutkinto "testskrivning"
                              :osaamisala "kunnande"
                              :order-by-column "tutkinto"
                              :desc true
                              :locale "sv"})]
        (t/is (= (count (:data body)) 2))
        (t/is (= (get-in body [:meta :total-count]) 2))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.45000000007"))
        (t/is (= (get-in body [:data 1 :oid])
                 "1.2.246.562.24.43000000009"))))))

(t/deftest get-oppijat-with-swedish-locale-without-translation
  (t/testing
   "Doesn't have swedish translation and no search filters, shouldn't filter"
    (test-utils/with-db
      (v-utils/add-oppija {:oid "1.2.246.562.24.45000000007"
                           :nimi "Olli Oppija"
                           :opiskeluoikeus-oid "1.2.246.562.15.76300000007"
                           :oppilaitos-oid "1.2.246.562.10.12000000005"
                           :tutkinto-nimi {:fi "Testitutkinto 3"
                                           :sv "Testskrivning 3"}
                           :osaamisala-nimi {:fi "Osaamisala Kolme"}
                           :koulutustoimija-oid ""})
      (v-utils/add-hoks {:oid "1.2.246.562.24.45000000007"
                         :opiskeluoikeus-oid "1.2.246.562.15.76300000007"})
      (let [body (get-search {:order-by-column "tutkinto"
                              :desc true
                              :locale "sv"})]
        (t/is (= (count (:data body)) 1))
        (t/is (= (get-in body [:meta :total-count]) 1))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.45000000007"))))))

(t/deftest test-list-virkailija-oppija-with-multi-opiskeluoikeus
  (t/testing "GET virkailija oppijat"
    (test-utils/with-db
      (v-utils/add-oppija {:oid "1.2.246.562.24.44000000008"
                           :nimi "Teuvo Testaaja"
                           :opiskeluoikeus-oid "1.2.246.562.15.76000000018"
                           :oppilaitos-oid "1.2.246.562.10.12000000013"
                           :tutkinto-nimi {:fi "Testitutkinto 1"}
                           :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
                           :koulutustoimija-oid ""})
      (db-opiskeluoikeus/insert-opiskeluoikeus!
        {:oid "1.2.246.562.15.760000000000"
         :oppija_oid "1.2.246.562.24.44000000008"
         :oppilaitos_oid "1.2.246.562.10.12000000526"
         :koulutustoimija_oid ""
         :tutkinto-nimi {:fi "Tutkinto 2"}
         :osaamisala-nimi {:fi "Osaamisala 2"}})
      (v-utils/add-hoks {:oid "1.2.246.562.24.44000000008"
                         :opiskeluoikeus-oid "1.2.246.562.15.76000000018"})
      (v-utils/add-hoks {:oid "1.2.246.562.24.44000000008"
                         :opiskeluoikeus-oid "1.2.246.562.15.760000000000"})

      (let [body (get-search
                   {:oppilaitos-oid "1.2.246.562.10.12000000526"}
                   {:name "Test"
                    :kayttajaTyyppi "VIRKAILIJA"
                    :oidHenkilo "1.2.246.562.24.22000000033"
                    :organisation-privileges
                    [{:oid "1.2.246.562.10.12000000526"
                      :privileges #{:read}}]})]
        (t/is (= (count (:data body)) 1))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.44000000008")))
      (let [body (get-search
                   {:oppilaitos-oid "1.2.246.562.10.12000000013"}
                   {:name "Test"
                    :kayttajaTyyppi "VIRKAILIJA"
                    :oidHenkilo "1.2.246.562.24.22000000020"
                    :organisation-privileges
                    [{:oid "1.2.246.562.10.12000000013"
                      :privileges #{:read}}]})]
        (t/is (= (count (:data body)) 1))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.44000000008"))))))

(t/deftest test-list-virkailija-oppija-with-multi-opiskeluoikeus-one-hoks
  (t/testing "GET virkailija oppijat"
    (test-utils/with-db
      (v-utils/add-oppija {:oid "1.2.246.562.24.44000000008"
                           :nimi "Teuvo Testaaja"
                           :opiskeluoikeus-oid "1.2.246.562.15.76000000018"
                           :oppilaitos-oid "1.2.246.562.10.12000000013"
                           :tutkinto-nimi {:fi "Testitutkinto 1"}
                           :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
                           :koulutustoimija-oid ""})
      (db-opiskeluoikeus/insert-opiskeluoikeus!
        {:oid "1.2.246.562.15.760000000000"
         :oppija_oid "1.2.246.562.24.44000000008"
         :oppilaitos_oid "1.2.246.562.10.12000000526"
         :koulutustoimija_oid ""
         :tutkinto-nimi {:fi "Tutkinto 2"}
         :osaamisala-nimi {:fi "Osaamisala 2"}})
      (v-utils/add-hoks {:oid "1.2.246.562.24.44000000008"
                         :opiskeluoikeus-oid "1.2.246.562.15.76000000018"})

      (let [body (get-search
                   {:oppilaitos-oid "1.2.246.562.10.12000000526"}
                   {:name "Test"
                    :kayttajaTyyppi "VIRKAILIJA"
                    :oidHenkilo "1.2.246.562.24.22000000033"
                    :organisation-privileges
                    [{:oid "1.2.246.562.10.12000000526"
                      :privileges #{:read}}]})]
        (t/is (= (count (:data body)) 0))
        (t/is (= (get-in body [:data 0 :oid])
                 nil)))
      (let [body (get-search
                   {:oppilaitos-oid "1.2.246.562.10.12000000013"}
                   {:name "Test"
                    :kayttajaTyyppi "VIRKAILIJA"
                    :oidHenkilo "1.2.246.562.24.22000000020"
                    :organisation-privileges
                    [{:oid "1.2.246.562.10.12000000013"
                      :privileges #{:read}}]})]
        (t/is (= (count (:data body)) 1))
        (t/is (= (get-in body [:data 0 :oid])
                 "1.2.246.562.24.44000000008"))))))

(t/deftest test-virkailija-with-no-read
  (t/testing "Prevent GET virkailija oppijat without read privilege"
    (test-utils/with-db
      (v-utils/add-oppija {:oid "1.2.246.562.24.44000000008"
                           :nimi "Testi 1"
                           :opiskeluoikeus-oid "1.2.246.562.15.76000000000"
                           :oppilaitos-oid "1.2.246.562.10.12000000005"
                           :koulutustoimija-oid ""})
      (let [response (with-test-virkailija
                       (mock/request
                         :get
                         (str base-url "/virkailija/oppijat")
                         {:oppilaitos-oid "1.2.246.562.10.12000000005"})
                       {:name "Test"
                        :kayttajaTyyppi "VIRKAILIJA"
                        :organisation-privileges
                        [{:oid "1.2.246.562.10.12000000005"
                          :privileges #{}}]})]
        (t/is (= (:status response) 403))))))

(t/deftest test-virkailija-has-read-access
  (t/testing "Virkailija has oppija access"
    (test-utils/with-db
      (client/with-mock-responses
        [(fn [^String url options]
           (cond
             (.contains
               url "/rest/organisaatio/v4/")
             {:status 200
              :body {:oid           (last (string/split url #"/"))
                     :parentOidPath "|"}}))]

        (v-utils/add-oppija {:oid "1.2.246.562.24.44000000008"
                             :nimi "Testi 1"
                             :opiskeluoikeus-oid "1.2.246.562.15.76000000000"
                             :oppilaitos-oid "1.2.246.562.10.12000000005"
                             :koulutustoimija-oid ""})
        (t/is
          (not
            (user/has-read-privileges-to-oppija?
              {:organisation-privileges
               [{:oid "1.2.246.562.10.12000000002"
                 :privileges #{:read}}]}
              "1.2.246.562.24.44000000008")))
        (t/is
          (not
            (user/has-read-privileges-to-oppija?
              {:organisation-privileges
               [{:oid "1.2.246.562.10.12000000005"
                 :privileges #{}}]}
              "1.2.246.562.24.44000000008")))
        (t/is
          (user/has-read-privileges-to-oppija?
            {:organisation-privileges
             [{:oid "1.2.246.562.10.12000000005"
               :privileges #{:read}}]}
            "1.2.246.562.24.44000000008"))))))

(t/deftest test-virkailija-hoks-write-forbidden
  (t/testing "Virkailija HOKS write is forbidden"
    (test-utils/with-db
      (v-utils/add-oppija {:oid "1.2.246.562.24.44000000008"
                           :nimi "Teuvo Testaaja"
                           :opiskeluoikeus-oid "1.2.246.562.15.76000000018"
                           :oppilaitos-oid "1.2.246.562.10.12000000013"
                           :tutkinto-nimi {:fi "Testitutkinto 1"}
                           :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
                           :koulutustoimija-oid ""})
      (db-opiskeluoikeus/insert-opiskeluoikeus!
        {:oid "1.2.246.562.15.760000000000"
         :oppija_oid "1.2.246.562.24.44000000008"
         :oppilaitos_oid "1.2.246.562.10.12000000203"
         :tutkinto-nimi {:fi "Testitutkinto 2"}
         :osaamisala-nimi {:fi "Testiosaamisala 2"}})
      (let [response
            (with-test-virkailija
              (mock/json-body
                (mock/request
                  :post
                  (str
                    base-url
                    "/virkailija/oppijat/1.2.246.562.24.44000000008/hoksit"))
                {:opiskeluoikeus-oid "1.2.246.562.15.76000000018"
                 :oppija-oid "1.2.246.562.24.44000000008"
                 :ensikertainen-hyvaksyminen "2018-12-15"
                 :osaamisen-hankkimisen-tarve false})
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.12000000203"
                 :privileges #{:write :read :update :delete}}
                {:oid "1.2.246.562.10.12000000013"
                 :privileges #{:read}}]})]
        (t/is (= (:status response) 403))))))

(defn- create-oppija-for-hoks-post [oppilaitos-oid]
  (v-utils/add-oppija {:oid "1.2.246.562.24.44000000008"
                       :nimi "Teuvo Testaaja"
                       :opiskeluoikeus-oid "1.2.246.562.15.76000000000"
                       :oppilaitos-oid oppilaitos-oid
                       :tutkinto-nimi {:fi "Testitutkinto 1"}
                       :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
                       :koulutustoimija-oid ""}))

(defn- post-new-hoks
  ([opiskeluoikeus-oid organisaatio-oid additional-keys]
    (with-test-virkailija
      (mock/json-body
        (mock/request
          :post
          (str base-url
               "/virkailija/oppijat/1.2.246.562.24.44000000008/hoksit"))
        (merge {:opiskeluoikeus-oid opiskeluoikeus-oid
                :oppija-oid "1.2.246.562.24.44000000008"
                :ensikertainen-hyvaksyminen "2018-12-15"
                :osaamisen-hankkimisen-tarve false}
               additional-keys))
      {:name "Testivirkailija"
       :kayttajaTyyppi "VIRKAILIJA"
       :oidHenkilo "1.2.246.562.24.44000000338"
       :organisation-privileges
       [{:oid organisaatio-oid
         :privileges #{:write :read :update :delete}}]}))
  ([opiskeluoikeus-oid organisaatio-oid]
    (post-new-hoks opiskeluoikeus-oid organisaatio-oid {})))

(defn- shallow-delete-hoks
  [oppija-oid hoks-id organisaatio-oid oppilaitos-oid]
  (with-test-virkailija
    (mock/json-body
      (mock/request
        :patch (str virkailija-base-url "/oppijat/" oppija-oid "/hoksit/"
                    hoks-id "/shallow-delete"))
      {:oppilaitos-oid oppilaitos-oid})
    {:name "Testivirkailija"
     :kayttajaTyyppi "VIRKAILIJA"
     :oidHenkilo "1.2.246.562.24.44000000338"
     :organisation-privileges
     [{:oid organisaatio-oid
       :privileges #{:write :read :update :delete :hoks_delete}}]}))

(t/deftest test-virkailija-hoks-forbidden
  (t/testing "Virkailija HOKS forbidden"
    (test-utils/with-db
      (v-utils/add-oppija {:oid "1.2.246.562.24.44000000008"
                           :nimi "Teuvo Testaaja"
                           :opiskeluoikeus-oid "1.2.246.562.15.76000000000"
                           :oppilaitos-oid "1.2.246.562.10.12000000005"
                           :tutkinto-nimi {:fi "Testitutkinto 1"}
                           :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
                           :koulutustoimija-oid ""})
      (let [response
            (post-new-hoks
              "1.2.246.562.15.10000000009" "1.2.246.562.10.12100000004")]
        (t/is (= (:status response) 403)))
      (let [hoks-db (db-hoks/insert-hoks!
                      {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                       :oppija-oid "1.2.246.562.24.44000000008"
                       :osaamisen-hankkimisen-tarve false
                       :ensikertainen-hyvaksyminen
                       (java.time.LocalDate/of 2018 12 15)})
            response
            (with-test-virkailija
              (mock/request
                :get
                (str
                  base-url
                  "/virkailija/oppijat/1.2.246.562.24.44000000008/hoksit/"
                  (:id hoks-db)))
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.12100000004"
                 :privileges #{:write :read :update :delete}}]})]
        (t/is (= (:status response) 403))))))

(defn- get-created-hoks [post-response]
  (with-test-virkailija
    (mock/request
      :get
      (get-in (test-utils/parse-body (:body post-response)) [:data :uri]))
    {:name "Testivirkailija"
     :kayttajaTyyppi "VIRKAILIJA"
     :oidHenkilo "1.2.246.562.24.44000000338"
     :organisation-privileges
     [{:oid "1.2.246.562.10.12000000005"
       :privileges #{:write :read :update :delete}}]}))

(def hato-data
  [{:tutkinnon-osa-koodi-uri "tutkinnonosat_102499"
    :tutkinnon-osa-koodi-versio 4
    :vaatimuksista-tai-tavoitteista-poikkeaminen
    "Ei poikkeamia."
    :osaamisen-osoittaminen
    [{:jarjestaja
      {:oppilaitos-oid "1.2.246.562.10.54453924331"}
      :nayttoymparisto {:nimi "Testiympäristö 2"
                        :y-tunnus "1234567-1"
                        :kuvaus "Testi test"}
      :sisallon-kuvaus ["Testaus"]
      :yksilolliset-kriteerit ["kriteeri 1" "kriteeri2"]
      :koulutuksen-jarjestaja-osaamisen-arvioijat
      [{:nimi "Timo Testaaja"
        :organisaatio
        {:oppilaitos-oid "1.2.246.562.10.54452521336"}}]
      :tyoelama-osaamisen-arvioijat
      [{:nimi "Taneli Työmies"
        :organisaatio {:nimi "Tanelin Paja Ky"
                       :y-tunnus "1234561-2"}}]
      :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_kl"
                    :koodi-versio 3}]
      :alku "2019-03-10"
      :loppu "2019-03-19"}]
    :osaamisen-hankkimistavat
    [{:jarjestajan-edustaja
      {:nimi "Ville Valvoja"
       :rooli "Valvojan apulainen"
       :oppilaitos-oid "1.2.246.562.10.54451211343"}
      :osaamisen-hankkimistapa-koodi-uri
      "osaamisenhankkimistapa_oppisopimus"
      :osaamisen-hankkimistapa-koodi-versio 2
      :oppisopimuksen-perusta-koodi-uri
      "oppisopimuksenperusta_01"
      :oppisopimuksen-perusta-koodi-versio 1
      :tyopaikalla-jarjestettava-koulutus
      {:vastuullinen-tyopaikka-ohjaaja
       {:nimi "Aimo Ohjaaja"
        :sahkoposti "aimo.ohjaaja@esimerkki2.com"}
       :tyopaikan-nimi "Ohjausyhtiö 1 Oy "
       :tyopaikan-y-tunnus "1234564-7"
       :keskeiset-tyotehtavat ["Testitehtävä"]}
      :muut-oppimisymparistot
      [{:oppimisymparisto-koodi-uri "oppimisymparistot_0002"
        :oppimisymparisto-koodi-versio 1
        :alku "2019-01-13"
        :loppu "2019-02-19"}]
      :keskeytymisajanjaksot []
      :ajanjakson-tarkenne "Ei tarkennettavia asioita"
      :osa-aikaisuustieto 50
      :hankkijan-edustaja
      {:nimi "Heikki Hankkija"
       :rooli "Opettaja"
       :oppilaitos-oid "1.2.246.562.10.54452422428"}
      :alku "2019-01-11"
      :loppu "2019-03-14"
      :yksiloiva-tunniste "testi-yksilöivä-tunniste"}
     {:jarjestajan-edustaja
      {:nimi "Ville Valvoja"
       :rooli "Valvojan apulainen"
       :oppilaitos-oid "1.2.246.562.10.54451211343"}
      :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_oppisopimus"
      :osaamisen-hankkimistapa-koodi-versio 2
      :oppisopimuksen-perusta-koodi-uri "oppisopimuksenperusta_01"
      :oppisopimuksen-perusta-koodi-versio 1
      :tyopaikalla-jarjestettava-koulutus
      {:vastuullinen-tyopaikka-ohjaaja
       {:nimi "Aimo Ohjaaja"
        :sahkoposti "aimo.ohjaaja@esimerkki2.com"}
       :tyopaikan-nimi " Ohjausyhtiö 2 Oy "
       :tyopaikan-y-tunnus "1234564-7"
       :keskeiset-tyotehtavat ["Testitehtävä"]}
      :muut-oppimisymparistot []
      :keskeytymisajanjaksot []
      :osa-aikaisuustieto 100
      :hankkijan-edustaja
      {:nimi "Heikki Hankkija"
       :rooli "Opettaja"
       :oppilaitos-oid "1.2.246.562.10.54452422428"}
      :alku "2023-01-11"
      :loppu "2023-03-14"
      :yksiloiva-tunniste "testi-toinen-yks-tunn"}]
    :koulutuksen-jarjestaja-oid "1.2.246.562.10.54411232223"}])

(t/deftest test-virkailija-create-hoks
  (t/testing "POST hoks virkailija"
    (test-utils/with-db
      (create-oppija-for-hoks-post "1.2.246.562.10.12000000005")
      (let [post-response
            (post-new-hoks
              "1.2.246.562.15.76000000018" "1.2.246.562.10.12000000013"
              {:hankittavat-ammat-tutkinnon-osat
               (update-in hato-data [0 :osaamisen-hankkimistavat 1]
                          dissoc :yksiloiva-tunniste)})]
        (t/is (= (:status post-response) 200))
        (let [get-response (get-created-hoks post-response)
              body (test-utils/parse-body (:body get-response))]
          (t/is (= (:status get-response) 200))
          (t/is (= (get-in body [:data :hankittavat-ammat-tutkinnon-osat 0
                                 :osaamisen-hankkimistavat 0
                                 :yksiloiva-tunniste])
                   "testi-yksilöivä-tunniste"))
          (t/is (= (get-in body [:data :hankittavat-ammat-tutkinnon-osat 0
                                 :osaamisen-hankkimistavat 0
                                 :tyopaikalla-jarjestettava-koulutus
                                 :tyopaikan-nimi])
                   "Ohjausyhtiö 1 Oy"))
          (t/is (= (get-in body [:data :hankittavat-ammat-tutkinnon-osat 0
                                 :osaamisen-hankkimistavat 1
                                 :tyopaikalla-jarjestettava-koulutus
                                 :tyopaikan-nimi])
                   "Ohjausyhtiö 2 Oy"))
          (t/is (re-matches
                  #"[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}"
                  (get-in body [:data :hankittavat-ammat-tutkinnon-osat 0
                                :osaamisen-hankkimistavat 1
                                :yksiloiva-tunniste])))
          (t/is (get-in body [:data :manuaalisyotto])))))))

(defn mocked-get-opiskeluoikeus-info-raw [oid]
  (throw
    (ex-info
      "Opiskeluoikeus fetch failed"
      {:body "[{\"key\": \"notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia\"}]"
       :status 404})))

(t/deftest test-hoks-create-when-opiskeluoikeus-fetch-fails
  (t/testing "Error thrown from koski is propagated to handler"
    (test-utils/with-db
      (logtest/with-log
        (create-oppija-for-hoks-post "1.2.246.562.10.12000000005")
        (with-redefs [k/get-opiskeluoikeus-info-raw
                      mocked-get-opiskeluoikeus-info-raw]
          (let [post-response
                (post-new-hoks "1.2.246.562.15.76000000018"
                               "1.2.246.562.10.12000000013")]
            (t/is (= (:status post-response) 400)
                  (str "Log entries:" (logtest/the-log)))
            (t/is (= (test-utils/parse-body (:body post-response))
                     {:error (str "Opiskeluoikeus `1.2.246.562.15.76000000018` "
                                  "not found in Koski")}))))))))

(defn mocked-get-oo-tuva [oid]
  {:oid oid
   :oppilaitos {:oid "1.2.246.562.10.12000000013"}
   :tyyppi {:koodiarvo "tuva"}})

(t/deftest test-tuva-hoks-with-tuva-opiskeluoikeus-oid-fails
  (t/testing (str "Error is thrown if trying to save tuva hoks with "
                  "tuva-opiskeluoikeus-oid")
    (test-utils/with-db
      (logtest/with-log
        (create-oppija-for-hoks-post "1.2.246.562.10.12000000005")
        (with-redefs [k/get-opiskeluoikeus-info-raw
                      mocked-get-oo-tuva]
          (let [post-response
                (post-new-hoks "1.2.246.562.15.76000000018"
                               "1.2.246.562.10.12000000013"
                               {:tuva-opiskeluoikeus-oid
                                "1.2.246.562.15.76000000018"
                                :hankittavat-koulutuksen-osat
                                [{:koulutuksen-osa-koodi-uri
                                  "koulutuksenosattuva_104"
                                  :koulutuksen-osa-koodi-versio 1
                                  :alku "2022-09-01"
                                  :loppu "2022-09-21"
                                  :laajuus 10}]})]
            (t/is (= (:status post-response) 400))
            (t/is (logtest/logged? "audit"
                                   :info
                                   #"\"status\":\"failed\".*24.44000000008")
                  (str "log entries:" (logtest/the-log)))
            (t/is (re-find #"Ota tuva-opiskeluoikeus-oid pois"
                           (slurp (:body post-response))))))))))

(defn mocked-get-oo-non-tuva [oid]
  {:oid oid
   :oppilaitos {:oid "1.2.246.562.10.12000000013"}
   :tyyppi {:koodiarvo "ammatillinenkoulutus"}})

(t/deftest test-hoks-with-hankittavat-koulutuksen-osat
  (t/testing (str "Error is thrown if trying to save (non-tuva) hoks with "
                  "hankittavat-koulutuksen-osat")
    (test-utils/with-db
      (create-oppija-for-hoks-post "1.2.246.562.10.12000000005")
      (with-redefs [k/get-opiskeluoikeus-info-raw
                    mocked-get-oo-non-tuva]
        (let [post-response
              (post-new-hoks "1.2.246.562.15.76000000018"
                             "1.2.246.562.10.12000000013"
                             {:hankittavat-koulutuksen-osat
                              [{:koulutuksen-osa-koodi-uri
                                "koulutuksenosattuva_104"
                                :koulutuksen-osa-koodi-versio 1
                                :alku "2022-09-01"
                                :loppu "2022-09-21"
                                :laajuus 10}]})]
          (t/is (= (:status post-response) 400))
          (let [body (test-utils/parse-body (:body post-response))]
            (t/is (= (get-in body [:errors :hankittavat-koulutuksen-osat 0])
                     (str "(not (\"Ota koulutuksenosa pois, koska "
                          "opiskeluoikeus ei ole TUVA.\" "
                          "a-clojure.lang.PersistentArrayMap))"))
                  (str body))))))))

(defn mock-get-oppija-raw! [_]
  (throw (ex-info "Opiskelija fetch failed" {:status 404})))

(t/deftest test-hoks-create-when-oppijanumerorekisteri-fails
  (t/testing "Error thrown from oppijanumerorekisteri is propagated to handler"
    (test-utils/with-db
      (with-redefs [onr/get-oppija-raw! mock-get-oppija-raw!]
        (let [post-response
              (post-new-hoks "1.2.246.562.15.76000000018"
                             "1.2.246.562.10.12000000013")]
          (t/is (= (:status post-response) 400))
          (t/is (= (test-utils/parse-body (:body post-response))
                   {:error (str "Oppija `1.2.246.562.24.44000000008` not found "
                                "in Oppijanumerorekisteri")})))))))

(t/deftest test-virkailija-patch-hoks
  (t/testing "PATCH hoks virkailija"
    (test-utils/with-db
      (create-oppija-for-hoks-post "1.2.246.562.24.44000000008")
      (let [post-response
            (post-new-hoks
              "1.2.246.562.15.76000000018" "1.2.246.562.10.12000000013")
            body (test-utils/parse-body (:body post-response))
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
               [{:oid "1.2.246.562.10.12000000013"
                 :privileges #{:write :read :update :delete}}]})
            get-response
            (with-test-virkailija
              (mock/request
                :get
                hoks-url)
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.12000000013"
                 :privileges #{:write :read :update :delete}}]})]
        (t/is (get-in (test-utils/parse-body (:body get-response))
                      [:data :osaamisen-hankkimisen-tarve]))
        (t/is (= (:status patch-response) 204))))))

(def virkailija-for-test
  {:name "Testivirkailija"
   :kayttajaTyyppi "VIRKAILIJA"
   :organisation-privileges
   [{:oid "1.2.246.562.10.12000000013"
     :privileges #{:write :read :update :delete}}]})

(t/deftest test-prevent-virkailija-patch-hoks
  (t/testing "PATCH hoks virkailija"
    (test-utils/with-db
      (v-utils/add-oppija {:oid "1.2.246.562.24.44000000008"
                           :nimi "Teuvo Testaaja"
                           :opiskeluoikeus-oid "1.2.246.562.15.76000000018"
                           :oppilaitos-oid "1.2.246.562.10.12000000013"
                           :tutkinto-nimi {:fi "Testitutkinto 1"}
                           :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
                           :koulutustoimija-oid ""})
      (db-opiskeluoikeus/insert-opiskeluoikeus!
        {:oid "1.2.246.562.15.760000000000"
         :oppija_oid "1.2.246.562.24.44000000008"
         :oppilaitos_oid "1.2.246.562.10.12000000203"
         :tutkinto-nimi {:fi "Testitutkinto 2"}
         :osaamisala-nimi {:fi "Testiosaamisala 2"}})
      (let [response
            (with-test-virkailija
              (mock/json-body
                (mock/request
                  :post
                  (str
                    base-url
                    "/virkailija/oppijat/1.2.246.562.24.44000000008/hoksit"))
                {:opiskeluoikeus-oid "1.2.246.562.15.76000000018"
                 :oppija-oid "1.2.246.562.24.44000000008"
                 :ensikertainen-hyvaksyminen "2018-12-15"
                 :osaamisen-hankkimisen-tarve false})
              virkailija-for-test)
            body (test-utils/parse-body (:body response))
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
               [{:oid "1.2.246.562.10.12000000203"
                 :privileges #{:write :read :update :delete}}
                {:oid "1.2.246.562.10.12000000013"
                 :privileges #{:read}}]})]
        (t/is (= (:status patch-response) 403))))))

(t/deftest prevent-patch-hoks-with-updated-opiskeluoikeus
  (t/testing "PATCH hoks virkailija"
    (test-utils/with-db
      (create-oppija-for-hoks-post "1.2.246.562.10.12000000005")
      (let [post-response
            (post-new-hoks
              "1.2.246.562.15.76000000018" "1.2.246.562.10.12000000013")
            body (test-utils/parse-body (:body post-response))
            hoks-url (get-in body [:data :uri])
            patch-response
            (with-test-virkailija
              (mock/json-body
                (mock/request
                  :patch
                  hoks-url)
                {:osaamisen-hankkimisen-tarve true
                 :id (get-in body [:meta :id])
                 :opiskeluoikeus-oid "1.2.246.562.15.76000000000"})
              virkailija-for-test)]
        (t/is (= (:status patch-response) 400))
        (t/is (= (test-utils/parse-body (:body patch-response))
                 {:error
                  (str "Tried to update `opiskeluoikeus-oid` from "
                       "`1.2.246.562.15.76000000018` to "
                       "`1.2.246.562.15.76000000000` but updating "
                       "`opiskeluoikeus-oid` in HOKS is not allowed!")}))))))

(t/deftest prevent-patch-hoks-with-updated-oppija-oid
  (t/testing "PATCH hoks virkailija"
    (test-utils/with-db
      (create-oppija-for-hoks-post "1.2.246.562.10.12000000005")
      (let [post-response
            (post-new-hoks
              "1.2.246.562.15.76000000018" "1.2.246.562.10.12000000013")
            body (test-utils/parse-body (:body post-response))
            hoks-url (get-in body [:data :uri])
            patch-response
            (with-test-virkailija
              (mock/json-body
                (mock/request
                  :patch
                  hoks-url)
                {:osaamisen-hankkimisen-tarve true
                 :id (get-in body [:meta :id])
                 :oppija-oid "1.2.246.562.24.12000000014"})
              virkailija-for-test)]
        (t/is (= (:status patch-response) 400))
        (t/is (= (test-utils/parse-body (:body patch-response))
                 {:error (str "Tried to update `oppija-oid` from "
                              "`1.2.246.562.24.44000000008` to "
                              "`1.2.246.562.24.12000000014` but updating "
                              "`oppija-oid` in HOKS is only allowed with "
                              "latest master oppija oid!")}))))))

(def hoks-data
  {:opiskeluoikeus-oid "1.2.246.562.15.76000000018"
   :oppija-oid "1.2.246.562.24.44000000008"
   :ensikertainen-hyvaksyminen "2018-12-15"
   :osaamisen-hankkimisen-tarve false})

(t/deftest test-virkailija-put-hoks
  (t/testing "PUT hoks virkailija"
    (logtest/with-log
      (test-utils/with-db
        (v-utils/add-oppija {:oid "1.2.246.562.24.44000000008"
                             :nimi "Teuvo Testaaja"
                             :opiskeluoikeus-oid "1.2.246.562.15.76000000018"
                             :oppilaitos-oid "1.2.246.562.10.12000000013"
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
                      "/virkailija/oppijat/1.2.246.562.24.44000000008/hoksit"))
                  hoks-data)
                virkailija-for-test)
              body (test-utils/parse-body (:body response))
              hoks-url (get-in body [:data :uri])
              put-response-just-dates
              (with-test-virkailija
                (mock/json-body
                  (mock/request :put hoks-url)
                  (assoc hoks-data
                         :id (get-in body [:meta :id])
                         :ensikertainen-hyvaksyminen "2018-12-17"
                         :paivitetty "2019-01-02T10:20:30.000Z"))
                virkailija-for-test)
              put-response
              (with-test-virkailija
                (mock/json-body
                  (mock/request :put hoks-url)
                  (assoc
                    hoks-data
                    :id (get-in body [:meta :id])
                    :ensikertainen-hyvaksyminen "2018-12-17"
                    :paivitetty "2019-01-02T10:20:30.001Z"
                    :hankittavat-ammat-tutkinnon-osat
                    (update-in hato-data [0 :osaamisen-hankkimistavat 1]
                               dissoc :yksiloiva-tunniste)))
                virkailija-for-test)
              get-response
              (with-test-virkailija
                (mock/request
                  :get
                  hoks-url)
                virkailija-for-test)]
          (let [body (test-utils/parse-body (:body get-response))]
            (t/is (= (get-in body [:data :hankittavat-ammat-tutkinnon-osat 0
                                   :osaamisen-hankkimistavat 0
                                   :yksiloiva-tunniste])
                     "testi-yksilöivä-tunniste"))
            (t/is (re-matches
                    #"[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}"
                    (get-in body [:data :hankittavat-ammat-tutkinnon-osat 0
                                  :osaamisen-hankkimistavat 1
                                  :yksiloiva-tunniste])))
            (test-utils/eq
              (test-utils/dissoc-module-ids
                (get-in body [:data :hankittavat-ammat-tutkinnon-osat]))
              (test-utils/dissoc-module-ids
                (-> hato-data
                    (assoc-in [0 :osaamisen-hankkimistavat 1
                               :yksiloiva-tunniste]
                              (get-in body [:data
                                            :hankittavat-ammat-tutkinnon-osat 0
                                            :osaamisen-hankkimistavat 1
                                            :yksiloiva-tunniste]))
                    (update-in [0
                                :osaamisen-hankkimistavat
                                0
                                :tyopaikalla-jarjestettava-koulutus
                                :tyopaikan-nimi]
                               string/trim)
                    (update-in [0
                                :osaamisen-hankkimistavat
                                1
                                :tyopaikalla-jarjestettava-koulutus
                                :tyopaikan-nimi]
                               string/trim)))))
          (t/is (= (:status put-response-just-dates) 204))
          (t/is (logtest/logged? "audit" :info #"overwrite.*2018-12-17")
                (str "log entries:" (logtest/the-log)))
          (t/is (logtest/logged?
                  "audit" :info #"overwrite.*2019-01-02T10:20:30Z"))
          (t/is (= (:status put-response) 204))
          (t/is (logtest/logged? "audit" :info #"overwrite.*Tanelin Paja")))))))

(t/deftest test-delete-vastaajatunnus
  (t/testing "DELETE vastaajatunnus is logged to auditlog"
    (logtest/with-log
      (with-redefs [oph.ehoks.db.postgresql.common/select-kyselylinkit-by-tunnus
                    (fn [_]
                      [{:kyselylinkki "https://arvo-dev.csc.fi/x/foofaa"}])
                    oph.ehoks.external.arvo/delete-kyselytunnus identity
                    oph.ehoks.db.postgresql.common/delete-kyselylinkki-by-tunnus
                    identity
                    oph.ehoks.external.aws-sqs/send-delete-tunnus-message
                    identity]
        (let [response (with-test-virkailija
                         (mock/request
                           :delete
                           (str "/ehoks-virkailija-backend/api/v1/virkailija"
                                "/vastaajatunnus/foofaa"))
                         virkailija-for-test)]
          (t/is (= 200 (:status response)))
          (t/is (logtest/logged? "audit" :info #"delete.*foofaa")))))))

(t/deftest test-put-prevent-updating-opiskeluoikeus
  (t/testing "PUT hoks virkailija"
    (test-utils/with-db
      (v-utils/add-oppija {:oid "1.2.246.562.24.44000000008"
                           :nimi "Teuvo Testaaja"
                           :opiskeluoikeus-oid "1.2.246.562.15.76000000018"
                           :oppilaitos-oid "1.2.246.562.10.12000000013"
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
                    "/virkailija/oppijat/1.2.246.562.24.44000000008/hoksit"))
                hoks-data)
              virkailija-for-test)
            body (test-utils/parse-body (:body response))
            hoks-url (get-in body [:data :uri])
            put-response
            (with-test-virkailija
              (mock/json-body
                (mock/request
                  :put
                  hoks-url)
                {:id (get-in body [:meta :id])
                 :osaamisen-hankkimisen-tarve true
                 :ensikertainen-hyvaksyminen "2018-12-15"
                 :opiskeluoikeus-oid "1.2.246.562.15.76000000000"
                 :hankittavat-ammat-tutkinnon-osat
                 hato-data})
              virkailija-for-test)
            put-body (test-utils/parse-body (:body put-response))]
        (t/is (= (:status put-response) 400))
        (t/is (= put-body
                 {:error
                  (str "Tried to update `opiskeluoikeus-oid` from "
                       "`1.2.246.562.15.76000000018` to "
                       "`1.2.246.562.15.76000000000` but updating "
                       "`opiskeluoikeus-oid` in HOKS is not allowed!")}))))))

(t/deftest test-put-prevent-updating-oppija-oid
  (t/testing "PUT hoks virkailija"
    (test-utils/with-db
      (v-utils/add-oppija {:oid "1.2.246.562.24.44000000008"
                           :nimi "Teuvo Testaaja"
                           :opiskeluoikeus-oid "1.2.246.562.15.76000000018"
                           :oppilaitos-oid "1.2.246.562.10.12000000013"
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
                    "/virkailija/oppijat/1.2.246.562.24.44000000008/hoksit"))
                hoks-data)
              virkailija-for-test)
            body (test-utils/parse-body (:body response))
            hoks-url (get-in body [:data :uri])
            put-response
            (with-test-virkailija
              (mock/json-body
                (mock/request
                  :put
                  hoks-url)
                {:id (get-in body [:meta :id])
                 :osaamisen-hankkimisen-tarve true
                 :ensikertainen-hyvaksyminen "2018-12-15"
                 :opiskeluoikeus-oid "1.2.246.562.15.76000000018"
                 :oppija-oid "1.2.246.562.24.45000000007"
                 :hankittavat-ammat-tutkinnon-osat
                 hato-data})
              virkailija-for-test)
            put-body (test-utils/parse-body (:body put-response))]
        (t/is (= (:status put-response) 400))
        (t/is (= put-body
                 {:error (str "Tried to update `oppija-oid` from "
                              "`1.2.246.562.24.44000000008` to "
                              "`1.2.246.562.24.45000000007` but updating "
                              "`oppija-oid` in HOKS is only allowed with "
                              "latest master oppija oid!")}))))))

(t/deftest test-allow-updating-oppija-oid
  (t/testing "Allows changing a slave oppija-oid to master"
    (test-utils/with-db
      (v-utils/add-oppija {:oid "1.2.246.562.24.44000000008"
                           :nimi "Teuvo Testaaja"
                           :opiskeluoikeus-oid "1.2.246.562.15.76000000018"
                           :oppilaitos-oid "1.2.246.562.10.12000000013"
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
                    "/virkailija/oppijat/1.2.246.562.24.44000000008/hoksit"))
                hoks-data)
              virkailija-for-test)
            body (test-utils/parse-body (:body response))
            hoks-url (get-in body [:data :uri])
            put-response
            (with-test-virkailija
              (mock/json-body
                (mock/request
                  :put
                  hoks-url)
                {:id (get-in body [:meta :id])
                 :osaamisen-hankkimisen-tarve false
                 :ensikertainen-hyvaksyminen "2018-12-15"
                 :opiskeluoikeus-oid "1.2.246.562.15.76000000018"
                 :oppija-oid "1.2.246.562.24.12312312319"})
              virkailija-for-test)]
        (t/is (= (:status put-response) 204))
        (t/is (= (:oppija-oid
                   (db-opiskeluoikeus/select-opiskeluoikeus-by-oid
                     "1.2.246.562.15.76000000018"))
                 "1.2.246.562.24.12312312319"))))))

(t/deftest test-get-amount
  (t/testing "Test getting the amount of hokses"
    (test-utils/with-db
      (v-utils/add-oppija {:oid "1.2.246.562.24.44000000008"
                           :nimi "Teuvo Testaaja"
                           :opiskeluoikeus-oid "1.2.246.562.15.76000000018"
                           :oppilaitos-oid "1.2.246.562.10.12000000013"
                           :tutkinto-nimi {:fi "Testitutkinto 1"}
                           :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
                           :koulutustoimija-oid ""})
      (db-hoks/insert-hoks!
        {:opiskeluoikeus-oid "1.2.246.562.15.76000000018"
         :oppija-oid "1.2.246.562.24.44000000008"
         :osaamisen-hankkimisen-tarve false
         :ensikertainen-hyvaksyminen
         (java.time.LocalDate/of 2018 12 15)})
      (let [amount-response
            (with-test-virkailija
              (mock/request
                :get
                (str
                  base-url
                  "/virkailija/system-info/hoksit"))
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.12000000013"
                 :privileges #{:write :read :update :delete}
                 :oikeus "OPHPAAKAYTTAJA"
                 :palvelu "EHOKS"
                 :roles {:oph-super-user true}}]})
            amount-body (test-utils/parse-body (:body amount-response))]
        (t/is (= (:status amount-response) 200))
        (t/is (= (:data amount-body) {:amount 1}))))))

(t/deftest test-get-oppijan-hoksit-for-virkailija
  (t/testing "Test getting oppija's other hokses when opiskeluoikeus active"
    (test-utils/with-db
      (v-utils/add-oppija {:oid "1.2.246.562.24.44000000008"
                           :nimi "Teuvo Testaaja"
                           :opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                           :oppilaitos-oid "1.2.246.562.10.12944436166"
                           :tutkinto-nimi {:fi "Testitutkinto 1"}
                           :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
                           :koulutustoimija-oid ""})
      (v-utils/add-another-opiskeluoikeus
        {:oppija-oid "1.2.246.562.24.44000000008"
         :oid "1.2.246.562.15.76000000018"
         :oppilaitos-oid "1.2.246.562.10.12000000013"
         :tutkinto-nimi {:fi "Testitutkinto 2"}
         :osaamisala-nimi {:fi "Testiosaamisala numero 2"}
         :koulutustoimija-oid ""})
      (db-hoks/insert-hoks!
        {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
         :oppija-oid "1.2.246.562.24.44000000008"
         :osaamisen-hankkimisen-tarve false
         :ensikertainen-hyvaksyminen
         (java.time.LocalDate/of 2017 12 15)})
      (db-hoks/insert-hoks!
        {:opiskeluoikeus-oid "1.2.246.562.15.76000000018"
         :oppija-oid "1.2.246.562.24.44000000008"
         :osaamisen-hankkimisen-tarve false
         :ensikertainen-hyvaksyminen
         (java.time.LocalDate/of 2018 12 15)})

      (let [get-response
            (with-test-virkailija
              (mock/request
                :get
                (str
                  base-url
                  "/virkailija/oppijat/1.2.246.562.24.44000000008/hoksit"))
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.12000000013"
                 :privileges #{:write :read :update :delete}
                 :oikeus "OPHPAAKAYTTAJA"
                 :palvelu "EHOKS"
                 :roles {:oph-super-user true}}]})
            get-body (test-utils/parse-body (:body get-response))]
        (t/is (= (:status get-response) 200))
        (t/is (= (count (:data get-body)) 2)))))
  (t/testing "Test getting only oppija's hokses in virkailija's oppilaitos"
    (test-utils/with-db
      (v-utils/add-oppija {:oid "1.2.246.562.24.44000000008"
                           :nimi "Teuvo Testaaja"
                           :opiskeluoikeus-oid "1.2.246.562.15.12000000203"
                           :oppilaitos-oid "1.2.246.562.10.12944436166"
                           :tutkinto-nimi {:fi "Testitutkinto 1"}
                           :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
                           :koulutustoimija-oid ""})
      (v-utils/add-another-opiskeluoikeus
        {:oppija-oid "1.2.246.562.24.44000000008"
         :oid "1.2.246.562.15.12000000005"
         :oppilaitos-oid "1.2.246.562.10.12000000013"
         :tutkinto-nimi {:fi "Testitutkinto 1"}
         :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
         :koulutustoimija-oid ""})
      (db-hoks/insert-hoks!
        {:opiskeluoikeus-oid "1.2.246.562.15.12000000203"
         :oppija-oid "1.2.246.562.24.44000000008"
         :osaamisen-hankkimisen-tarve false
         :ensikertainen-hyvaksyminen
         (java.time.LocalDate/of 2017 12 15)})
      (db-hoks/insert-hoks!
        {:opiskeluoikeus-oid "1.2.246.562.15.12000000005"
         :oppija-oid "1.2.246.562.24.44000000008"
         :osaamisen-hankkimisen-tarve false
         :ensikertainen-hyvaksyminen
         (java.time.LocalDate/of 2018 12 15)})
      (let [get-response
            (with-test-virkailija
              (mock/request
                :get
                (str
                  base-url
                  "/virkailija/oppijat/1.2.246.562.24.44000000008/hoksit"))
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.12000000013"
                 :privileges #{:write :read :update :delete}
                 :oikeus "OPHPAAKAYTTAJA"
                 :palvelu "EHOKS"
                 :roles {:oph-super-user true}}]})
            get-body (test-utils/parse-body (:body get-response))]
        (t/is (= (:status get-response) 200))
        (t/is (= (count (:data get-body)) 1))
        (t/is (= (-> get-body
                     :data
                     first
                     :opiskeluoikeus-oid) "1.2.246.562.15.12000000005")))))
  (t/testing "Missing opiskeluoikeus is handled correctly"
    (test-utils/with-db
      (v-utils/add-oppija {:oid "1.2.246.562.24.44000000008"
                           :nimi "Teuvo Testaaja"
                           :opiskeluoikeus-oid "1.2.246.562.15.12000000203"
                           :oppilaitos-oid "1.2.246.562.10.12944436166"
                           :tutkinto-nimi {:fi "Testitutkinto 1"}
                           :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
                           :koulutustoimija-oid ""})
      (v-utils/add-another-opiskeluoikeus
        {:oppija-oid "1.2.246.562.24.44000000008"
         :oid "1.2.246.562.15.44000000008"
         :oppilaitos-oid "1.2.246.562.10.12000000013"
         :tutkinto-nimi {:fi "Testitutkinto 1"}
         :osaamisala-nimi {:fi "Testiosaamisala numero 1"}
         :koulutustoimija-oid ""})
      (db-hoks/insert-hoks!
        {:opiskeluoikeus-oid "1.2.246.562.15.12000000203"
         :oppija-oid "1.2.246.562.24.44000000008"
         :osaamisen-hankkimisen-tarve false
         :ensikertainen-hyvaksyminen
         (java.time.LocalDate/of 2017 12 15)})
      (db-hoks/insert-hoks!
        {:opiskeluoikeus-oid "1.2.246.562.15.44000000008"
         :oppija-oid "1.2.246.562.24.44000000008"
         :osaamisen-hankkimisen-tarve false
         :ensikertainen-hyvaksyminen
         (java.time.LocalDate/of 2018 12 15)})
      (let [get-response
            (with-test-virkailija
              (mock/request
                :get
                (str
                  base-url
                  "/virkailija/oppijat/1.2.246.562.24.44000000008/hoksit"))
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.12000000013"
                 :privileges #{:write :read :update :delete}
                 :oikeus "OPHPAAKAYTTAJA"
                 :palvelu "EHOKS"
                 :roles {:oph-super-user true}}]})
            get-body (test-utils/parse-body (:body get-response))]
        (t/is (= (:status get-response) 200))
        (t/is (= (count (:data get-body)) 1))
        (t/is (= (-> get-body
                     :data
                     first
                     :opiskeluoikeus-oid) "1.2.246.562.15.44000000008"))))))

(t/deftest test-get-kyselylinkit
  (t/testing "Test getting kyselylinkit"
    (let [alkupvm (LocalDate/now)
          loppupvm (.plusMonths (LocalDateTime/now) 1)]
      (with-redefs [oph.ehoks.external.arvo/get-kyselylinkki-status
                    (fn [_]
                      {:vastattu false
                       :voimassa_loppupvm (str loppupvm "Z")})]
        (test-utils/with-db
          (v-utils/add-oppija
            {:oid "1.2.246.562.24.44000000008"
             :nimi "Teuvo Testaaja"
             :opiskeluoikeus-oid "1.2.246.562.15.76000000018"
             :oppilaitos-oid "1.2.246.562.10.12000000013"
             :koulutustoimija-oid "1.2.246.562.10.1200000000511"
             :tutkinto-nimi {:fi "Testitutkinto 1"}
             :osaamisala-nimi {:fi "Testiosaamisala numero 1"}})
          (db-hoks/insert-hoks!
            {:opiskeluoikeus-oid "1.2.246.562.15.76000000018"
             :oppija-oid "1.2.246.562.24.44000000008"
             :osaamisen-hankkimisen-tarve false
             :ensikertainen-hyvaksyminen
             (java.time.LocalDate/of 2018 12 15)})
          (kyselylinkki/insert!
            {:kyselylinkki "https://kysely.linkki/ABC123"
             :hoks-id 1
             :tyyppi "aloittaneet"
             :oppija-oid "1.2.246.562.24.44000000008"
             :alkupvm alkupvm
             :lahetystila "ei_lahetetty"})
          (kyselylinkki/insert!
            {:kyselylinkki "https://kysely.linkki/DEF456"
             :hoks-id 1
             :tyyppi "tutkinnonsuorittaneet"
             :oppija-oid "1.2.246.562.24.44000000008"
             :alkupvm alkupvm})
          (let [resp (with-test-virkailija
                       (mock/request
                         :get
                         (str
                           base-url
                           "/virkailija/oppijat/1.2.246.562.24.44000000008"
                           "/hoksit/1/opiskelijapalaute"))
                       {:name "Testivirkailija"
                        :kayttajaTyyppi "VIRKAILIJA"
                        :organisation-privileges
                        [{:oid "1.2.246.562.10.12000000013"
                          :privileges #{:write :read :update :delete}
                          :oikeus "OPHPAAKAYTTAJA"
                          :palvelu "EHOKS"
                          :roles {:oph-super-user true}}]})
                body (test-utils/parse-body (:body resp))]
            (t/is (= 200 (:status resp)))
            (t/is (empty? (:data body))))
          (kyselylinkki/update! {:kyselylinkki "https://kysely.linkki/ABC123"
                                 :lahetyspvm alkupvm
                                 :sahkoposti "testi@testi.fi"
                                 :lahetystila "viestintapalvelussa"})
          (let [resp (with-test-virkailija
                       (mock/request
                         :get
                         (str
                           base-url
                           "/virkailija/oppijat/1.2.246.562.24.44000000008"
                           "/hoksit/1/opiskelijapalaute"))
                       {:name "Testivirkailija"
                        :kayttajaTyyppi "VIRKAILIJA"
                        :organisation-privileges
                        [{:oid "1.2.246.562.10.12000000013"
                          :privileges #{:write :read :update :delete}
                          :oikeus "OPHPAAKAYTTAJA"
                          :palvelu "EHOKS"
                          :roles {:oph-super-user true}}]})
                body (test-utils/parse-body (:body resp))]
            (t/is (= 200 (:status resp)))
            (t/is
              (= (first (:data body))
                 {:hoks-id           1
                  :tyyppi            "aloittaneet"
                  :oppija-oid        "1.2.246.562.24.44000000008"
                  :alkupvm           (str alkupvm)
                  :lahetyspvm        (str alkupvm)
                  :sahkoposti        "testi@testi.fi"
                  :lahetystila       "viestintapalvelussa"
                  :voimassa-loppupvm (str (LocalDate/from loppupvm))}))))))))

(t/deftest test-shallow-delete
  (t/testing "shallow delete works"
    (test-utils/with-db
      (create-oppija-for-hoks-post "1.2.246.562.10.12000000005")
      (let [organisaatio-oid "1.2.246.562.10.12000000013"
            post-response
            (post-new-hoks
              "1.2.246.562.15.76000000018" organisaatio-oid
              {:hankittavat-ammat-tutkinnon-osat hato-data})]
        (t/is (= (:status post-response) 200))
        (let [get-response (get-created-hoks post-response)
              body (test-utils/parse-body (:body get-response))
              hoks-id (-> body :data :id)
              delete-response
              (shallow-delete-hoks "1.2.246.562.24.44000000008"
                                   hoks-id
                                   organisaatio-oid
                                   organisaatio-oid)
              delete-body (test-utils/parse-body (:body delete-response))]
          (t/is (= (:status delete-response) 200))
          (t/is (= (:success delete-body) hoks-id))))))

  (t/testing (str "shallow delete works without oppilaitos oid "
                  "(in case of opiskeluoikeus voided)")
    (test-utils/with-db
      (create-oppija-for-hoks-post "1.2.246.562.10.12000000005")
      (let [organisaatio-oid "1.2.246.562.10.12000000013"
            post-response
            (post-new-hoks
              "1.2.246.562.15.76000000018" organisaatio-oid
              {:hankittavat-ammat-tutkinnon-osat hato-data})]
        (t/is (= (:status post-response) 200))
        (let [get-response (get-created-hoks post-response)
              body (test-utils/parse-body (:body get-response))
              hoks-id (-> body :data :id)
              delete-response
              (shallow-delete-hoks "1.2.246.562.24.44000000008"
                                   hoks-id
                                   organisaatio-oid
                                   nil)
              delete-body (test-utils/parse-body (:body delete-response))]
          (t/is (= (:status delete-response) 200))
          (t/is (= (:success delete-body) hoks-id)))))))

(t/deftest test-tep-jakso-raportti-structure
  (t/testing "report tep-jakso-raportti returns expected structure"
    (test-utils/with-db
      (create-oppija-for-hoks-post "1.2.246.562.10.12000000005")
      (t/is (= (-> (post-new-hoks
                     "1.2.246.562.15.76000000018" "1.2.246.562.10.12000000013"
                     {:hankittavat-ammat-tutkinnon-osat hato-data})
                   :status) 200))
      (db-helpers/update! :osaamisen_hankkimistavat
                          {:osa_aikaisuustieto nil}
                          [])

      (t/testing "with oppilaitos user"
        (let [report-response
              (with-test-virkailija
                (mock/request
                  :get
                  (str base-url
                       "/virkailija/tep-jakso-raportti"
                       "?tutkinto=%7B%7D&start=2019-01-01&end=2023-12-31"
                       "&pagesize=10&pageindex=0"
                       "&oppilaitos=1.2.246.562.10.12000000013"))
                {:name "Testivirkailija"
                 :kayttajaTyyppi "VIRKAILIJA"
                 :organisation-privileges
                 [{:oid "1.2.246.562.10.12000000013"
                   :privileges #{:write :read :update :delete}}]})
              data (-> report-response
                       :body
                       test-utils/parse-body
                       :data)]
          (t/is (= (:status report-response) 200))
          (t/is (= (:count data) 2))
          (t/is (= (:pagecount data) 1))
          (t/is (= (map :tyopaikanNimi (:result data))
                   ["Ohjausyhtiö 1 Oy" "Ohjausyhtiö 2 Oy"]))))

      (t/testing "with OPH user"
        (let [report-response
              (with-test-virkailija
                (mock/request
                  :get
                  (str base-url
                       "/virkailija/tep-jakso-raportti"
                       "?tutkinto=%7B%7D&start=2019-01-01&end=2023-12-31"
                       "&pagesize=10&pageindex=0"
                       "&oppilaitos=1.2.246.562.10.12000000013"))
                {:name "OPH-virkailija"
                 :kayttajaTyyppi "VIRKAILIJA"
                 :organisation-privileges
                 [{:oid "1.2.246.562.10.00000000001"
                   :privileges #{:write :read :update :delete}
                   :oikeus "OPHPAAKAYTTAJA"
                   :palvelu "EHOKS"
                   :roles {:oph-super-user true}}]})
              data (-> report-response
                       :body
                       test-utils/parse-body
                       :data)]
          (t/is (= (:status report-response) 200))
          (t/is (= (:count data) 2))
          (t/is (= (:pagecount data) 1))
          (t/is (= (map :tyopaikanNimi (:result data))
                   ["Ohjausyhtiö 1 Oy" "Ohjausyhtiö 2 Oy"])))))))

(t/deftest test-missing-oo-hoksit-structure
  (t/testing "report missing-oo-hoksit returns expected structure"
    (test-utils/with-db
      (create-oppija-for-hoks-post "1.2.246.562.10.12000000005")
      (t/is (= (-> (post-new-hoks "1.2.246.562.15.76000000018"
                                  "1.2.246.562.10.12000000013"
                                  {})
                   :status) 200))
      (db-helpers/update! :opiskeluoikeudet
                          {:koski404 true}
                          ["oid = ?" "1.2.246.562.15.76000000018"])

      (let [report-response
            (with-test-virkailija
              (mock/request
                :get
                (str base-url
                     "/virkailija/missing-oo-hoksit/1.2.246.562.10.12000000013"
                     "?pagesize=10&pageindex=0"))
              {:name "Testivirkailija"
               :kayttajaTyyppi "VIRKAILIJA"
               :organisation-privileges
               [{:oid "1.2.246.562.10.12000000013"
                 :privileges #{:write :read :update :delete}}]})
            data (-> report-response
                     :body
                     test-utils/parse-body
                     :data)]
        (t/is (= (:status report-response) 200))
        (t/is (= (:count data) 1))
        (t/is (= (:pagecount data) 1))
        (t/is (= 1 (-> data
                       :result
                       first
                       :hoksId)))))))
