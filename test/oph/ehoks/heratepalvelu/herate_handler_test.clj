(ns oph.ehoks.heratepalvelu.herate-handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.koski-test :as koski-test]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils]
            [oph.ehoks.test-utils :as test-utils]
            [ring.mock.request :as mock])
  (:import (java.time LocalDate)))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-database-after-test)

(def base-url "/ehoks-virkailija-backend/api/v1")

(deftest get-kasittelemattomat-heratteet
  (testing "GET /heratepalvelu/kasittelemattomat-heratteet"
    (with-redefs [koski/get-opiskeluoikeus-info-raw
                  koski-test/mock-get-opiskeluoikeus-raw]
      (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                       :oppija-oid "1.2.246.562.24.12312312319"
                       :osaamisen-hankkimisen-tarve true
                       :ensikertainen-hyvaksyminen (str (LocalDate/now))
                       :osaamisen-saavuttamisen-pvm (str (LocalDate/now))
                       :puhelinnumero "04011111111"}
            app (hoks-utils/create-app nil)
            _ (hoks-utils/mock-st-post app (str base-url "/hoks") hoks-data)
            req (mock/request
                  :get
                  (str base-url "/heratepalvelu/kasittelemattomat-heratteet")
                  {:start (.minusDays (LocalDate/now) 1)
                   :end (.plusDays (LocalDate/now) 1)
                   :limit 10})
            res (test-utils/with-service-ticket
                  app req "1.2.246.562.10.00000000001")]
        (let [body (test-utils/parse-body (:body res))]
          (is (= 1 (:data body))))))))
