(ns oph.ehoks.oppija.handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.handler :refer [create-app]]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils :refer [eq]]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.session-store :refer [test-session-store]]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.db.migrations :as m]
            [oph.ehoks.hoks.hoks-test :refer [hoks-data]]
            [clojure.walk :as w]))

(def url "/ehoks-backend/api/v1/oppija/oppijat")

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
          app (create-app (test-session-store store))
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
