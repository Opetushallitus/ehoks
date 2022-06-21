(ns oph.ehoks.hoks.invalid-hoks-handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [ring.mock.request :as mock]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils :refer [base-url]]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.hoks.test-data :as test-data]
            [oph.ehoks.utils :as utils :refer [eq]]))

(use-fixtures :once utils/migrate-database)
(use-fixtures :each utils/empty-database-after-test)

(deftest prevent-creating-hoks-with-existing-opiskeluoikeus
  (testing "Prevent POST HOKS with existing opiskeluoikeus"
    (let [app (hoks-utils/create-app nil)
          hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}]
      (hoks-utils/mock-st-post app base-url hoks-data)
      (let [response
            (hoks-utils/mock-st-post app base-url hoks-data)]
        (is (= (:status response) 400))
        (is (= (utils/parse-body (:body response))
               {:error
                "HOKS with the same opiskeluoikeus-oid already exists"}))))))

(deftest prevent-creating-hoks-with-existing-shallow-deleted-opiskeluoikeus
  (testing (str "Prevent creation and show correct error message when "
                "shallow-deleted HOKS with same opiskeluoikeus is found")
    (let [app (hoks-utils/create-app nil)
          hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}
          new-hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                         :oppija-oid "1.2.246.562.24.12312312313"
                         :ensikertainen-hyvaksyminen "2018-12-15"
                         :osaamisen-hankkimisen-tarve false}]
      (hoks-utils/mock-st-post app base-url hoks-data)
      (db-hoks/shallow-delete-hoks-by-hoks-id 1)
      (let [post-response (hoks-utils/mock-st-post app base-url new-hoks-data)]
        (is (= (:status post-response) 400))
        (is (= (utils/parse-body (:body post-response))
               {:error
                (str "Archived HOKS with given opiskeluoikeus "
                     "oid found. Contact eHOKS support for more "
                     "information.")}))))))

(deftest prevent-creating-hoks-with-non-existing-oppija
  (testing "Prevent POST HOKS with non-existing oppija"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                     :oppija-oid "1.2.246.562.24.40404040404"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}]
      (let [response
            (hoks-utils/mock-st-post
              (hoks-utils/create-app nil) base-url hoks-data)]
        (is (= (:status response) 400))
        (is (= (utils/parse-body (:body response))
               {:error
                "Oppija not found in Oppijanumerorekisteri"}))))))

(deftest prevent-creating-unauthorized-hoks
  (testing "Prevent POST unauthorized HOKS"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000002"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}]
      (let [response
            (hoks-utils/mock-st-post
              (hoks-utils/create-app nil) base-url hoks-data)]
        (is (= (:status response) 401))))))

(deftest prevent-getting-unauthorized-hoks
  (testing "Prevent GET unauthorized HOKS"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000002"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}]

      (let [response
            (utils/with-service-ticket
              (hoks-utils/create-app nil)
              (-> (mock/request :post base-url)
                  (mock/json-body hoks-data))
              "1.2.246.562.24.47861388608")
            body (utils/parse-body (:body response))]
        (is (= (:status
                 (hoks-utils/mock-st-get (hoks-utils/create-app nil)
                                         (get-in body [:data :uri])))
               401))))))

(deftest prevent-oppija-opiskeluoikeus-patch
  (testing "Prevent patching opiskeluoikeus or oppija oid"
    (let [app (hoks-utils/create-app nil)]
      (let [response
            (hoks-utils/mock-st-post
              app
              base-url
              {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
               :oppija-oid "1.2.246.562.24.12312312312"
               :ensikertainen-hyvaksyminen "2018-12-15"
               :osaamisen-hankkimisen-tarve false})
            body (utils/parse-body (:body response))]
        (is (= (get
                 (hoks-utils/mock-st-patch
                   app
                   (get-in body [:data :uri])
                   {:id (get-in body [:meta :id])
                    :opiskeluoikeus-oid "1.2.246.562.15.00000000001"})
                 :status)
               204)
            "Should not return bad request for updating opiskeluoikeus oid
             if the oid is not changed")

        (is (= (get
                 (hoks-utils/mock-st-patch
                   app
                   (get-in body [:data :uri])
                   {:id (get-in body [:meta :id])
                    :oppija-oid "1.2.246.562.24.12312312312"})
                 :status)
               204)
            "Should not return bad request for updating oppija oid if the
             oid is not changed")
        (let [get-body (utils/parse-body
                         (get
                           (hoks-utils/mock-st-get
                             app
                             (get-in body [:data :uri]))
                           :body))]
          (is (= (get-in get-body [:data :opiskeluoikeus-oid])
                 "1.2.246.562.15.00000000001")
              "Opiskeluoikeus oid should be unchanged")
          (is (= (get-in get-body [:data :oppija-oid])
                 "1.2.246.562.24.12312312312")
              "Oppija oid should be unchanged"))))))

(deftest prevent-oppija-oid-patch
  (testing "Prevent patching oppija oid"
    (let [app (hoks-utils/create-app nil)]
      (let [response
            (hoks-utils/mock-st-post
              app
              base-url
              {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
               :oppija-oid "1.2.246.562.24.12312312312"
               :ensikertainen-hyvaksyminen "2018-12-15"
               :osaamisen-hankkimisen-tarve false})
            body (utils/parse-body (:body response))]
        (is (= (get
                 (hoks-utils/mock-st-patch
                   app
                   (get-in body [:data :uri])
                   {:id (get-in body [:meta :id])
                    :oppija-oid "1.2.246.562.24.12312312313"})
                 :status)
               400)
            "Should return bad request for updating oppija oid if the
             oid is changed")
        (let [get-body (utils/parse-body
                         (get
                           (hoks-utils/mock-st-get
                             app
                             (get-in body [:data :uri]))
                           :body))]
          (is (= (get-in get-body [:data :oppija-oid])
                 "1.2.246.562.24.12312312312")
              "Oppija oid should be unchanged"))))))

(deftest prevent-opiskeluoikeus-patch
  (testing "Prevent patching opiskeluoikeus-oid"
    (let [app (hoks-utils/create-app nil)]
      (let [response
            (hoks-utils/mock-st-post
              app
              base-url
              {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
               :oppija-oid "1.2.246.562.24.12312312312"
               :ensikertainen-hyvaksyminen "2018-12-15"
               :osaamisen-hankkimisen-tarve false})
            body (utils/parse-body (:body response))]
        (is (= (get
                 (hoks-utils/mock-st-patch
                   app
                   (get-in body [:data :uri])
                   {:id (get-in body [:meta :id])
                    :opiskeluoikeus-oid "1.2.246.562.15.00000000002"})
                 :status)
               400)
            "Should return bad request for updating opiskeluoikeus oid
             if the oid is changed")
        (let [get-body (utils/parse-body
                         (get
                           (hoks-utils/mock-st-get
                             app
                             (get-in body [:data :uri]))
                           :body))]
          (is (= (get-in get-body [:data :opiskeluoikeus-oid])
                 "1.2.246.562.15.00000000001")
              "Opiskeluoikeus oid should be unchanged"))))))

(deftest prevent-updating-opiskeluoikeus
  (testing "Prevent PUT HOKS with existing opiskeluoikeus"
    (let [app (hoks-utils/create-app nil)
          post-response
          (hoks-utils/create-mock-post-request
            ""
            (dissoc test-data/hoks-data :opiskeluvalmiuksia-tukevat-opinnot)
            app)
          put-response (hoks-utils/create-mock-hoks-put-request
                         1
                         (-> test-data/hoks-data
                             (assoc :id 1)
                             (dissoc :opiskeluoikeus-oid :oppija-oid)
                             (assoc :opiskeluoikeus-oid
                                    "1.2.246.562.15.00000000002"))
                         app)]
      (is (= (:status post-response) 200))
      (is (= (:status put-response) 400))
      (is (= (utils/parse-body (:body put-response))
             {:error "Opiskeluoikeus update not allowed!"})))))

(deftest prevent-updating-oppija-oid
  (testing "Prevent PUT HOKS with existing opiskeluoikeus"
    (let [app (hoks-utils/create-app nil)
          post-response
          (hoks-utils/create-mock-post-request
            ""
            (dissoc test-data/hoks-data :opiskeluvalmiuksia-tukevat-opinnot)
            app)
          put-response (hoks-utils/create-mock-hoks-put-request
                         1
                         (-> test-data/hoks-data
                             (assoc :id 1)
                             (dissoc :opiskeluoikeus-oid :oppija-oid)
                             (assoc :oppija-oid
                                    "1.2.246.562.24.12312312313"))
                         app)]
      (is (= (:status post-response) 200))
      (is (= (:status put-response) 400))
      (is (= (utils/parse-body (:body put-response))
             {:error "Oppija-oid update not allowed!"})))))

(deftest patching-of-hoks-part-not-allowed
  (testing "PATCH of HOKS can't be used to update sub entities of HOKS"
    (let [app (hoks-utils/create-app nil)
          post-response (hoks-utils/create-mock-post-request
                          "" test-data/hoks-data app)
          patch-response (hoks-utils/create-mock-hoks-patch-request
                           1 test-data/hoks-data app)]
      (is (= (:status post-response) 200))
      (is (= (:status patch-response) 400)))))
