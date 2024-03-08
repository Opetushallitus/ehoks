(ns oph.ehoks.oppija.share-handler-test
  (:require [clojure.test :as t]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.oppija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.db.db-operations.shared-modules :as sdb]
            [ring.mock.request :as mock]
            [oph.ehoks.session-store :refer [test-session-store]]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.hoks.hoks-save-test :as hoks-data]
            [oph.ehoks.hoks.hoks :as hoks]
            [oph.ehoks.virkailija.virkailija-test-utils :as v-utils])
  (:import [java.time LocalDate]))

(t/use-fixtures :once utils/migrate-database)
(t/use-fixtures :each utils/empty-database-after-test)

(def share-base-url "/ehoks-oppija-backend/api/v1/oppija/jaot")

(def min-hoks-data hoks-data/min-hoks-data)

(def full-hoks-data
  (let [hoks hoks-data/hoks-data
        haot (conj hoks-data/hao-data (first hoks-data/hao-data))]
    (assoc hoks :hankittavat-ammat-tutkinnon-osat haot)))

(def jakolinkki-data
  {:tutkinnonosa-module-uuid "5b92f3f4-dc73-4ce0-8ec7-64d2cf96b47c"
   :tutkinnonosa-tyyppi "HankittavaAmmatillinenTutkinnonOsa"
   :shared-module-uuid "992fe41a-c8e6-43e2-a305-2fc5f393a462"
   :shared-module-tyyppi "osaamisenosoittaminen"
   :voimassaolo-alku (LocalDate/now)
   :voimassaolo-loppu (.plusMonths (LocalDate/now) 1)})

(defn- mock-authenticated [request]
  (let [store (atom {})
        app (common-api/create-app
              handler/app-routes (test-session-store store))]
    (utils/with-authenticated-oid
      store
      "1.2.246.562.24.12312312319"
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

(t/deftest get-shared-hato-osaamisenhankkiminen-link
  (t/testing "Existing shared hato with osaamisenhankkiminen can be retrieved"
    (let [_ (v-utils/add-oppija v-utils/dummy-user)
          hoks (with-redefs [oph.ehoks.external.koski/get-opiskeluoikeus-info
                             utils/mock-get-opiskeluoikeus-info]
                 (hoks/save-hoks! full-hoks-data))
          tuo-uuid (str (get-in hoks [:hankittavat-ammat-tutkinnon-osat 0
                                      :module_id]))
          module-uuid (str (get-in hoks [:hankittavat-ammat-tutkinnon-osat 0
                                         :osaamisen-hankkimistavat 0
                                         :module_id]))
          share (sdb/insert-shared-module!
                  (assoc jakolinkki-data
                         :hoks-eid (:eid hoks)
                         :tutkinnonosa-module-uuid tuo-uuid
                         :shared-module-uuid module-uuid
                         :shared-module-tyyppi "osaamisenhankkiminen"))
          share-id (:share_id share)
          response (mock-authenticated
                     (mock/request
                       :get
                       (format "%s/%s/%s"
                               share-base-url
                               "jakolinkit"
                               share-id)))
          data (:data (utils/parse-body (:body response)))]
      (t/is (= 200 (:status response)))
      (t/is (= (:module-id (:osaamisen-hankkimistapa data)) module-uuid))
      (t/is (nil? (:osaamisen-osoittaminen data)))
      (t/is (= (get-in data [:tutkinnonosa :module-id]) tuo-uuid)))))

(t/deftest get-shared-hato-osaamisenosoittaminen-link
  (t/testing "Existing shared hato with osaamisenosoittaminen can be retrieved"
    (let [_ (v-utils/add-oppija v-utils/dummy-user)
          hoks (with-redefs [oph.ehoks.external.koski/get-opiskeluoikeus-info
                             utils/mock-get-opiskeluoikeus-info]
                 (hoks/save-hoks! full-hoks-data))
          tuo-uuid (str (get-in hoks [:hankittavat-ammat-tutkinnon-osat 0
                                      :module_id]))
          module-uuid (str (get-in hoks [:hankittavat-ammat-tutkinnon-osat 0
                                         :osaamisen-osoittaminen 0 :module_id]))
          share (sdb/insert-shared-module!
                  (assoc jakolinkki-data
                         :hoks-eid (:eid hoks)
                         :tutkinnonosa-module-uuid tuo-uuid
                         :shared-module-uuid module-uuid
                         :shared-module-tyyppi "osaamisenosoittaminen"))
          share-id (:share_id share)
          response (mock-authenticated
                     (mock/request
                       :get
                       (format "%s/%s/%s"
                               share-base-url
                               "jakolinkit"
                               share-id)))
          data (:data (utils/parse-body (:body response)))]
      (t/is (= 200 (:status response)))
      (t/is (= (:module-id (:osaamisen-osoittaminen data)) module-uuid))
      (t/is (nil? (:osaamisen-hankkimistapa data)))
      (t/is (= (get-in data [:tutkinnonosa :module-id]) tuo-uuid)))))

(t/deftest get-shared-hpto-osaamisenosoittaminen-link
  (t/testing "Existing shared hpto with osaamisenosoittaminen can be retrieved"
    (let [_ (v-utils/add-oppija v-utils/dummy-user)
          hoks (with-redefs [oph.ehoks.external.koski/get-opiskeluoikeus-info
                             utils/mock-get-opiskeluoikeus-info]
                 (hoks/save-hoks! full-hoks-data))
          tuo-uuid (str (get-in hoks [:hankittavat-paikalliset-tutkinnon-osat 0
                                      :module_id]))
          module-uuid (str (get-in hoks
                                   [:hankittavat-paikalliset-tutkinnon-osat 0
                                    :osaamisen-osoittaminen 0 :module_id]))
          share (sdb/insert-shared-module!
                  (assoc jakolinkki-data
                         :hoks-eid (:eid hoks)
                         :tutkinnonosa-tyyppi
                         "HankittavaPaikallinenTutkinnonOsa"
                         :tutkinnonosa-module-uuid tuo-uuid
                         :shared-module-uuid module-uuid
                         :shared-module-tyyppi "osaamisenosoittaminen"))
          share-id (:share_id share)
          response (mock-authenticated
                     (mock/request
                       :get
                       (format "%s/%s/%s"
                               share-base-url
                               "jakolinkit"
                               share-id)))
          data (:data (utils/parse-body (:body response)))]
      (t/is (= 200 (:status response)))
      (t/is (= (:module-id (:osaamisen-osoittaminen data)) module-uuid))
      (t/is (nil? (:osaamisen-hankkimistapa data)))
      (t/is (= (get-in data [:tutkinnonosa :module-id]) tuo-uuid)))))

(t/deftest get-shared-hyto-osaamisenhankkiminen-link
  (t/testing "Existing shared hpto with osaamisenosoittaminen can be retrieved"
    (let [_ (v-utils/add-oppija v-utils/dummy-user)
          hoks (with-redefs [oph.ehoks.external.koski/get-opiskeluoikeus-info
                             utils/mock-get-opiskeluoikeus-info]
                 (hoks/save-hoks! full-hoks-data))
          tuo-uuid (str (get-in hoks [:hankittavat-yhteiset-tutkinnon-osat 0
                                      :module_id]))
          module-uuid (str (get-in hoks
                                   [:hankittavat-yhteiset-tutkinnon-osat 0
                                    :osa-alueet 0 :osaamisen-hankkimistavat 0
                                    :module_id]))
          share (sdb/insert-shared-module!
                  (assoc jakolinkki-data
                         :hoks-eid (:eid hoks)
                         :tutkinnonosa-tyyppi "HankittavaYTOOsaAlue"
                         :tutkinnonosa-module-uuid tuo-uuid
                         :shared-module-uuid module-uuid
                         :shared-module-tyyppi "osaamisenhankkiminen"))
          share-id (:share_id share)
          response (mock-authenticated
                     (mock/request
                       :get
                       (format "%s/%s/%s"
                               share-base-url
                               "jakolinkit"
                               share-id)))
          data (:data (utils/parse-body (:body response)))]
      (t/is (= 200 (:status response)))
      (t/is (= (:module-id (:osaamisen-hankkimistapa data)) module-uuid))
      (t/is (nil? (:osaamisen-osoittaminen data)))
      (t/is (= (get-in data [:tutkinnonosa :module-id]) tuo-uuid)))))

(t/deftest get-nonexisting-shared-link
  (t/testing "Nonexisting shared link returns not found"
    (let [response (mock-authenticated
                     (mock/request
                       :get
                       (format "%s/%s/%s"
                               share-base-url
                               "jakolinkit"
                               "00000000-0000-0000-0000-000000000000")))]
      (t/is (= 404 (:status response))))))

(t/deftest get-expired-shared-link
  (with-redefs [sdb/validate-share-dates (fn [_])]
    (t/testing "Expired shared link returns gone"
      (let [hoks (db-hoks/insert-hoks! min-hoks-data)
            share (sdb/insert-shared-module!
                    (assoc jakolinkki-data
                           :hoks-eid (:eid hoks)
                           :voimassaolo-loppu (LocalDate/parse "1902-01-01")))
            share-id (:share_id share)
            response (mock-authenticated
                       (mock/request
                         :get
                         (format "%s/%s/%s"
                                 share-base-url
                                 "jakolinkit"
                                 share-id)))
            body (utils/parse-body (:body response))]
        (t/is (= 410 (:status response)))
        (t/is (= "Shared link is expired" (:message body)))))))

(t/deftest get-not-yet-active-shared-link
  (with-redefs [sdb/validate-share-dates (fn [_])]
    (t/testing "Not yet active shared link returns locked"
      (let [hoks (db-hoks/insert-hoks! min-hoks-data)
            share (sdb/insert-shared-module!
                    (assoc jakolinkki-data
                           :hoks-eid (:eid hoks)
                           :voimassaolo-alku (.plusMonths (LocalDate/now) 1)))
            share-id (:share_id share)
            response (mock-authenticated
                       (mock/request
                         :get
                         (format "%s/%s/%s"
                                 share-base-url
                                 "jakolinkit"
                                 share-id)))
            body (utils/parse-body (:body response))]
        (t/is (= 423 (:status response)))
        (t/is (= "Shared link not yet active" (:message body)))))))

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
    (let [_ (v-utils/add-oppija v-utils/dummy-user)
          hoks (with-redefs [oph.ehoks.external.koski/get-opiskeluoikeus-info
                             utils/mock-get-opiskeluoikeus-info]
                 (hoks/save-hoks! full-hoks-data))
          tuo1-uuid (str (get-in hoks [:hankittavat-ammat-tutkinnon-osat 0
                                       :module_id]))
          module1-uuid (str (get-in hoks [:hankittavat-ammat-tutkinnon-osat 0
                                          :osaamisen-hankkimistavat 0
                                          :module_id]))
          tuo2-uuid (str (get-in hoks [:hankittavat-ammat-tutkinnon-osat 1
                                       :module_id]))
          _ (sdb/insert-shared-module!
              (assoc jakolinkki-data
                     :hoks-eid (:eid hoks)
                     :tutkinnonosa-module-uuid tuo1-uuid
                     :shared-module-uuid module1-uuid
                     :shared-module-tyyppi "osaamisenhankkiminen"))
          _ (sdb/insert-shared-module!
              (assoc jakolinkki-data
                     :hoks-eid (:eid hoks)
                     :tutkinnonosa-module-uuid tuo2-uuid
                     :shared-module-uuid module1-uuid
                     :shared-module-tyyppi "osaamisenhankkiminen"))
          wrong-share (sdb/insert-shared-module!
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
                               module1-uuid)))
          data (:data (utils/parse-body (:body response)))]
      (t/is (= 200 (:status response)))
      (t/is (= 2 (count data)))
      (t/is (empty? (filter #(= (:share-id wrong-share) (:share-id %)) data)))))

  (t/testing "Trying to fetch links for a module with none returns empty list"
    (let [response (mock-authenticated
                     (mock/request
                       :get
                       (format "%s/%s/%s"
                               share-base-url
                               "moduulit"
                               "11111111-1111-1111-1111-111111111111")))]
      (t/is (= 200 (:status response)))
      (t/is (empty? (:data (utils/parse-body (:body response))))))))
