(ns oph.ehoks.heratepalvelu.herate-handler-test
  (:require [clj-time.core :as t]
            [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils]
            [oph.ehoks.utils :as utils]
            [ring.mock.request :as mock]))

(use-fixtures :once utils/migrate-database)
(use-fixtures :each utils/empty-database-after-test)

(def base-url "/ehoks-virkailija-backend/api/v1")

(deftest get-kasittelemattomat-heratteet
  (testing "GET /heratepalvelu/kasittelemattomat-heratteet")
  (with-redefs []
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :osaamisen-hankkimisen-tarve true
                     :ensikertainen-hyvaksyminen (str (t/today))
                     :osaamisen-saavuttamisen-pvm (str (t/today))
                     :puhelinnumero "04011111111"}
          app (hoks-utils/create-app nil)
          _ (hoks-utils/mock-st-post app (str base-url "/hoks") hoks-data)
          req (mock/request
                :get
                (str base-url "/heratepalvelu/kasittelemattomat-heratteet")
                {:start (str (t/minus (t/today) (t/days 1)))
                 :end (str (t/plus (t/today) (t/days 1)))
                 :limit 10})
          res (utils/with-service-ticket app req "1.2.246.562.10.00000000001")]
      (is (= 1
             (:data (utils/parse-body (:body res))))))))
