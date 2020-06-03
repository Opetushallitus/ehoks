(ns oph.ehoks.oppija.share-handler-test
  (:require [clojure.test :as t]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.oppija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.db.db-operations.shared-modules :as sdb]
            [ring.mock.request :as mock]
            [oph.ehoks.session-store :refer [test-session-store]]
            [oph.ehoks.db.db-operations.hoks :as db-hoks])
  (:import [java.time LocalDate]))

(t/use-fixtures :each utils/with-database)

(def share-base-url "/ehoks-oppija-backend/api/v1/oppija/jaot")

(def min-hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"})

(def jakolinkki-data
  {:tutkinnonosa-module-uuid "5b92f3f4-dc73-4ce0-8ec7-64d2cf96b47c"
   :tutkinnonosa-tyyppi "tutkinnonosa-tyyppi"
   :shared-module-uuid "992fe41a-c8e6-43e2-a305-2fc5f393a462"
   :shared-module-tyyppi "shared-tyyppi"
   :voimassaolo-alku (LocalDate/now)
   :voimassaolo-loppu (.plusMonths (LocalDate/now) 1)})

(defn- mock-authenticated [request]
  (let [store (atom {})
        app (common-api/create-app
              handler/app-routes (test-session-store store))]
    (utils/with-authenticated-oid
      store
      "1.2.246.562.24.12312312312"
      app
      request)))

(t/deftest create-shared-link
  (t/testing "Shared link with valid data can be created"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)
          response (mock-authenticated
                     (mock/json-body
                       (mock/request
                         :post
                         (format "%s/%s" share-base-url "jakolinkit"))
                       (assoc jakolinkki-data :hoks-eid (:eid hoks))))
          body (utils/parse-body (:body response))]
      (t/is (= 200 (:status response)))
      (t/is (:meta body))
      (t/is (get-in body [:data :uri]))))

  (t/testing "Invalid shared link data returns error"
    (let [response (mock-authenticated
                     (mock/json-body
                       (mock/request
                         :post
                         (format "%s/%s" share-base-url "jakolinkit"))
                       {:wrong "things"}))
          body (utils/parse-body (:body response))]
      (t/is (= 400 (:status response)))
      (t/is (:schema body))))

  (t/testing "Shared link end date cannot be in the past"
    (let [response (mock-authenticated
                     (mock/json-body
                       (mock/request
                         :post
                         (format "%s/%s" share-base-url "jakolinkit"))
                       (assoc jakolinkki-data
                              :voimassaolo-loppu (LocalDate/of 1986 11 17)
                              :hoks-eid "not relevant")))
          body (utils/parse-body (:body response))]
      (t/is (= 400 (:status response)))
      (t/is (= "Shared link end date cannot be in the past"
               (:error body)))))

  (t/testing "Shared link start date cannot be before end date")
  (let [response (mock-authenticated
                   (mock/json-body
                     (mock/request
                       :post
                       (format "%s/%s" share-base-url "jakolinkit"))
                     (assoc
                       jakolinkki-data
                       :voimassaolo-alku (.plusMonths (LocalDate/now) 2)
                       :voimassaolo-loppu (.plusMonths (LocalDate/now) 1)
                       :hoks-eid "not relevant")))
        body (utils/parse-body (:body response))]
    (t/is (= 400 (:status response)))
    (t/is (= "Shared link end date cannot be before the start date"
             (:error body)))))

(t/deftest get-shared-link
  (t/testing "Existing shared link info can be retrieved"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)
          share (sdb/insert-shared-module!
                  (assoc jakolinkki-data :hoks-eid (:eid hoks)))
          share-id (:share_id share)
          response (mock-authenticated
                     (mock/request
                       :get
                       (format "%s/%s/%s"
                               share-base-url
                               "jakolinkit"
                               share-id)))
          body (utils/parse-body (:body response))]
      (t/is (= 200 (:status response)))
      (t/is (get-in body [:data 0 :share-id]))
      (t/is (= (:tutkinnonosa-module-uuid jakolinkki-data)
               (get-in body [:data 0 :tutkinnonosa-module-uuid])))
      (t/is (= (:shared-module-uuid jakolinkki-data)
               (get-in body [:data 0 :shared-module-uuid])))))

  (t/testing "Nonexisting shared link returns not found"
    (let [response (mock-authenticated
                     (mock/request
                       :get
                       (format "%s/%s/%s"
                               share-base-url
                               "jakolinkit"
                               "00000000-0000-0000-0000-000000000000")))]
      (t/is (= 404 (:status response))))))

(t/deftest delete-shared-link
  (t/testing "Existing shared link can be deleted"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)
          share (sdb/insert-shared-module!
                  (assoc jakolinkki-data :hoks-eid (:eid hoks)))
          share-id (:share_id share)
          delete-res (mock-authenticated
                       (mock/request
                         :delete
                         (format "%s/%s/%s"
                                 share-base-url
                                 "jakolinkit"
                                 share-id)))
          fetch-res (mock-authenticated
                      (mock/request
                        :get
                        (format "%s/%s/%s"
                                share-base-url
                                "jakolinkit"
                                share-id)))]
      (t/is (= 200 (:status delete-res)))
      (t/is (= 404 (:status fetch-res)))))

  (t/testing "Nonexisting shared link deletion returns not found"
    (let [response (mock-authenticated
                     (mock/request
                       :delete
                       (format "%s/%s/%s"
                               share-base-url
                               "jakolinkit"
                               "00000000-0000-0000-0000-000000000000")))]
      (t/is (= 404 (:status response))))))

(t/deftest get-shared-modules
  (t/testing "Multiple shared links for a single module can be fetched"
    (let [hoks (db-hoks/insert-hoks! min-hoks-data)
          share1 (sdb/insert-shared-module!
                   (assoc jakolinkki-data :hoks-eid (:eid hoks)))
          share2 (sdb/insert-shared-module!
                   (assoc jakolinkki-data
                          :tutkinnonosa-module-uuid
                          "5b92f3f4-ABBA-4ce0-8ec7-64d2cf96b47c"
                          :hoks-eid (:eid hoks)))
          _ (sdb/insert-shared-module!
              (assoc jakolinkki-data
                     :shared-module-uuid
                     "00000000-0000-0000-0000-000000000000"
                     :hoks-eid (:eid hoks)))
          response (mock-authenticated
                     (mock/request
                       :get
                       (format "%s/%s/%s"
                               share-base-url
                               "moduulit"
                               (:shared-module-uuid jakolinkki-data))))
          body (utils/parse-body (:body response))]
      (t/is (= 200 (:status response)))
      (t/is (= 2 (count (:data body))))

      (t/is (some?
              (filter
                #(= (or
                      (:tutkinnonosa-module-uuid share1)
                      (:tutkinnonosa-module-uuid share2))
                    (:tutkinnonosa-module-uuid %)) (:data body))))))

  (t/testing "Trying to fetch links for a module with none returns empty list"
    (let [response (mock-authenticated
                     (mock/request
                       :get
                       (format "%s/%s/%s"
                               share-base-url
                               "moduulit"
                               "10000000-1000-1000-1000-100000000000")))]
      (t/is (= 200 (:status response)))
      (t/is (empty? (:data (utils/parse-body (:body response))))))))
