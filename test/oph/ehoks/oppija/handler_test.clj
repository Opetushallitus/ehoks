(ns oph.ehoks.oppija.handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils :refer [eq with-authentication parse-body]]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.session-store :refer [test-session-store]]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.db.migrations :as m]
            [oph.ehoks.hoks.hoks-test :refer [hoks-data]]
            [clojure.walk :as w]))

(def url "/ehoks-oppija-backend/api/v1/oppija/oppijat")

(defn with-database [f]
  (m/migrate!)
  (f)
  (m/clean!))

(defn clean-db [f]
  (m/clean!)
  (m/migrate!)
  (f))

(use-fixtures :each with-database)

(use-fixtures :once clean-db)

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

(deftest get-hoks
  (testing "GET enriched HOKS"
    (h/save-hoks! hoks-data)
    (let [oppija-oid (:oppija-oid hoks-data)
          store (atom {})
          app (common-api/create-app
                handler/app-routes (test-session-store store))
          response
          (utils/with-authenticated-oid
            store
            oppija-oid
            app
            (mock/request
              :get
              (format "%s/%s/hoks" url oppija-oid)))]
      (is (= (:status response) 200))
      (let [body (utils/parse-body (:body response))]
        (eq
          (:data body)
          [(dates-to-str
             (assoc hoks-data :eid (get-in body [:data 0 :eid])))])))))

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
