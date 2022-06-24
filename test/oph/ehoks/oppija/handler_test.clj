(ns oph.ehoks.oppija.handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.oppija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils
             :refer [eq with-authentication parse-body]]
            [oph.ehoks.session-store :refer [test-session-store]]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.hoks.hoks-save-test :refer [hoks-data]]
            [clojure.walk :as w]
            [oph.ehoks.external.http-client :as client])
  (:import [java.time LocalDate]))

(def url "/ehoks-oppija-backend/api/v1/oppija/oppijat")

(use-fixtures :once utils/migrate-database)
(use-fixtures :each utils/empty-database-after-test)

(def dates #{:alku :loppu :lahetetty-arvioitavaksi :ensikertainen-hyvaksyminen})

(defn v-to-str [m k]
  (if (some? (get m k))
    (update m k str)
    m))

(defn dates-to-str [c]
  (w/postwalk
    #(if (map? %)
       (reduce v-to-str % dates)
       %)
    c))

(defn- mock-oppija-get-request [store oppija-oid oppija-app]
  (utils/with-authenticated-oid
    store
    oppija-oid
    oppija-app
    (mock/request
      :get
      (format "%s/%s/hoks" url oppija-oid))))

(deftest get-hoks
  (testing "GET enriched HOKS"
    (h/save-hoks! hoks-data)
    (let [oppija-oid (:oppija-oid hoks-data)
          store (atom {})
          oppija-app (common-api/create-app
                handler/app-routes (test-session-store store))
          get-response (mock-oppija-get-request store oppija-oid oppija-app)
          body (utils/parse-body (:body get-response))]
      (is (= (:status get-response) 200))
      (eq
        (utils/dissoc-module-ids (:data body))
        [(dates-to-str
           (assoc hoks-data
                  :eid (get-in body [:data 0 :eid])
                  :manuaalisyotto false))]))))

(defn- mock-authenticated [request]
  (let [store (atom {})
        app (common-api/create-app
              handler/app-routes (test-session-store store))]
    (utils/with-authenticated-oid
      store
      "1.2.246.562.24.12312312312"
      app
      (mock/header request "Caller-Id" "test"))))

(deftest get-organisaatio
  (testing "GET organisaatio"
    (client/with-mock-responses
      [(fn [_ __]
         {:status 200
          :body {:oid "1.2.246.562.15.4042"
                 :nimi {:fi "Test"}}})]
      (let [response (mock-authenticated
                       (mock/request
                         :get
                         (str
                           "/ehoks-oppija-backend/api/v1/oppija"
                           "/external/organisaatio/1.2.246.562.15.4042")))
            body (utils/parse-body (:body response))]
        (is (= (:status response) 200))
        (eq
          (:data body)
          {:oid "1.2.246.562.15.4042"
           :nimi {:fi "Test"}})))))

(deftest get-not-found-organisaatio
  (testing "GET organisaatio not found"
    (client/with-mock-responses
      [(fn [_ __]
         (throw (ex-info
                  "HTTP exception"
                  {:status 404
                   :body {:errorMessage
                          "organisaatio.exception.organisaatio.not.found"
                          :errorKey ""}})))]
      (let [response (mock-authenticated
                       (mock/request
                         :get
                         (str
                           "/ehoks-oppija-backend/api/v1/oppija"
                           "/external/organisaatio/1.2.246.562.15.404")))]
        (is (= (:status response) 404))))))

(deftest buildversion
  (testing "GET /buildversion.txt"
    (let [app (common-api/create-app handler/app-routes)
          response (app (mock/request
                          :get "/ehoks-oppija-backend/buildversion.txt"))
          body (slurp (:body response))]
      (is (= (:status response) 200))
      (is (re-find #"^artifactId=" body)))))

(deftest not-found
  (testing "GET route which does not exist"
    (let [app (common-api/create-app handler/app-routes)
          response
          (with-authentication
            app
            (mock/request
              :get "/ehoks-oppija-backend/api/v1/non-existing-resource/"))]
      (is (= (:status response) 404)))))

(deftest healthcheck
  (testing "GET healthcheck"
    (let [app (common-api/create-app handler/app-routes)
          response (app
                     (mock/request
                       :get "/ehoks-oppija-backend/api/v1/healthcheck"))
          body (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (= body {})))))

(deftest kyselylinkit
  (testing "GET kyselylinkit"
    (let [oppija-oid (:oppija-oid hoks-data)
          store (atom {})
          app (common-api/create-app
                handler/app-routes (test-session-store store))]
      (h/save-hoks! hoks-data)
      (h/insert-kyselylinkki! {:kyselylinkki "https://palaute.fi/abc123"
                               :alkupvm (LocalDate/now)
                               :tyyppi "aloittaneet"
                               :oppija-oid oppija-oid
                               :hoks-id 1})
      (client/set-get!
        (fn [url options]
          (cond
            (.endsWith
              url "/status/abc123")
            {:status 200
             :body {:tunnus "abc123",
                    :voimassa_loppupvm  (str
                                          (.plusMonths (LocalDate/now) 1)
                                          "T00:00:00.000Z"),
                    :vastattu false}})))
      (let [response
            (utils/with-authenticated-oid
              store
              oppija-oid
              app
              (mock/request
                :get
                (format "%s/%s/kyselylinkit" url oppija-oid)))
            body (utils/parse-body (:body response))]
        (is (= (:status response) 200))
        (eq
          (:data body)
          ["https://palaute.fi/abc123"])))))

(deftest kyselylinkit-vastattu
  (testing "GET kyselylinkit vastattu linkki"
    (let [oppija-oid (:oppija-oid hoks-data)
          store (atom {})
          app (common-api/create-app
                handler/app-routes (test-session-store store))]
      (h/save-hoks! hoks-data)
      (h/insert-kyselylinkki! {:kyselylinkki "https://palaute.fi/abc123"
                               :alkupvm (LocalDate/now)
                               :tyyppi "aloittaneet"
                               :oppija-oid oppija-oid
                               :hoks-id 1})
      (h/insert-kyselylinkki! {:kyselylinkki "https://palaute.fi/vastattu"
                               :alkupvm (LocalDate/now)
                               :tyyppi "aloittaneet"
                               :oppija-oid (:oppija-oid hoks-data)
                               :hoks-id 1})
      (client/set-get!
        (fn [url options]
          (cond
            (.endsWith
              url "/status/abc123")
            {:status 200
             :body {:tunnus "abc123",
                    :voimassa_loppupvm  (str
                                          (.plusMonths (LocalDate/now) 1)
                                          "T00:00:00.000Z"),
                    :vastattu false}}
            (.endsWith
              url "/status/vastattu")
            {:status 200
             :body {:tunnus "abc123",
                    :voimassa_loppupvm  (str
                                          (.plusMonths (LocalDate/now) 1)
                                          "T00:00:00.000Z"),
                    :vastattu true}})))
      (let [response
            (utils/with-authenticated-oid
              store
              oppija-oid
              app
              (mock/request
                :get
                (format "%s/%s/kyselylinkit" url oppija-oid)))
            body (utils/parse-body (:body response))]
        (is (= (:status response) 200))
        (eq
          (:data body)
          ["https://palaute.fi/abc123"])))))

(deftest kyselylinkit-vanhentunut-linkki
  (testing "GET kyselylinkit vanhentunut linkki"
    (let [oppija-oid (:oppija-oid hoks-data)
          store (atom {})
          app (common-api/create-app
                handler/app-routes (test-session-store store))]
      (h/save-hoks! hoks-data)
      (h/insert-kyselylinkki! {:kyselylinkki "https://palaute.fi/abc123"
                               :alkupvm (LocalDate/now)
                               :tyyppi "aloittaneet"
                               :oppija-oid oppija-oid
                               :hoks-id 1})
      (h/insert-kyselylinkki! {:kyselylinkki "https://palaute.fi/vanha"
                               :alkupvm (LocalDate/now)
                               :tyyppi "aloittaneet"
                               :oppija-oid (:oppija-oid hoks-data)
                               :hoks-id 1})
      (client/set-get!
        (fn [url options]
          (cond
            (.endsWith
              url "/status/abc123")
            {:status 200
             :body {:tunnus "abc123",
                    :voimassa_loppupvm  (str
                                          (.plusMonths (LocalDate/now) 1)
                                          "T00:00:00.000Z"),
                    :vastattu false}}
            (.endsWith
              url "/status/vanha")
            {:status 200
             :body {:tunnus "abc123",
                    :voimassa_loppupvm  (str
                                          (.minusDays (LocalDate/now) 1)
                                          "T00:00:00.000Z"),
                    :vastattu false}})))
      (let [response
            (utils/with-authenticated-oid
              store
              oppija-oid
              app
              (mock/request
                :get
                (format "%s/%s/kyselylinkit" url oppija-oid)))
            body (utils/parse-body (:body response))]
        (is (= (:status response) 200))
        (eq
          (:data body)
          ["https://palaute.fi/abc123"])))))

(deftest kyselylinkit-vastausaika-ei-alkanut
  (testing "GET kyselylinkit vanhentunut linkki"
    (let [oppija-oid (:oppija-oid hoks-data)
          store (atom {})
          app (common-api/create-app
                handler/app-routes (test-session-store store))]
      (h/save-hoks! hoks-data)
      (h/insert-kyselylinkki! {:kyselylinkki "https://palaute.fi/abc123"
                               :alkupvm (.plusDays (LocalDate/now) 1)
                               :tyyppi "aloittaneet"
                               :oppija-oid oppija-oid
                               :hoks-id 1})
      (client/set-get!
        (fn [url options]
          (cond
            (.endsWith
              url "/status/abc123")
            {:status 200
             :body {:tunnus "abc123",
                    :voimassa_loppupvm  (str
                                          (.plusMonths (LocalDate/now) 1)
                                          "T00:00:00.000Z"),
                    :vastattu false}})))
      (let [response
            (utils/with-authenticated-oid
              store
              oppija-oid
              app
              (mock/request
                :get
                (format "%s/%s/kyselylinkit" url oppija-oid)))
            body (utils/parse-body (:body response))]
        (is (= (:status response) 200))
        (eq
          (:data body)
          [])))))
