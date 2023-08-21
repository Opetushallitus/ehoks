(ns oph.ehoks.hoks.invalid-hoks-handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [ring.mock.request :as mock]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils :refer [base-url]]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.hoks.test-data :as test-data]
            [oph.ehoks.hoks.hoks-parts.parts-test-data :as parts-test-data]
            [oph.ehoks.utils :as utils])
  (:import [java.time LocalDate]))

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

(deftest prevent-invalid-osaamisen-hankkimistapa
  (testing "Start and end dates of OHT are checked"
    (let [app (hoks-utils/create-app nil)
          invalid-data-1
          (assoc-in test-data/hoks-data
                    [:hankittavat-yhteiset-tutkinnon-osat 0
                     :osa-alueet 0 :osaamisen-hankkimistavat 0 :alku]
                    "2020-10-02")
          invalid-post-response-1
          (hoks-utils/create-mock-post-request "" invalid-data-1 app)
          invalid-data-2
          (assoc-in test-data/hoks-data
                    [:hankittavat-ammat-tutkinnon-osat 0
                     :osaamisen-hankkimistavat 0 :loppu]
                    "2030-12-08")
          invalid-post-response-2
          (hoks-utils/create-mock-post-request "" invalid-data-2 app)
          invalid-data-3
          (assoc test-data/hoks-data
                 :opiskeluoikeus-oid
                 "1.2.246.562.15.00000000004")
          invalid-post-response-3
          (hoks-utils/create-mock-post-request "" invalid-data-3 app)
          invalid-data-4
          (assoc test-data/hoks-data
                 :opiskeluoikeus-oid
                 "1.2.246.562.15.00000000005")
          invalid-post-response-4
          (hoks-utils/create-mock-post-request "" invalid-data-4 app)]
      (is (= (:status invalid-post-response-1) 400))
      (is (-> (utils/parse-body (:body invalid-post-response-1))
              (get-in [:errors :hankittavat-yhteiset-tutkinnon-osat 0
                       :osa-alueet 0 :osaamisen-hankkimistavat 0])
              (->> (re-find #"Korjaa alku- ja loppupäivä"))))
      (is (= (:status invalid-post-response-2) 400))
      (is (-> (utils/parse-body (:body invalid-post-response-2))
              (get-in [:errors :hankittavat-ammat-tutkinnon-osat 0
                       :osaamisen-hankkimistavat 0])
              (->> (re-find #"5 vuoden pituiseksi"))))
      (is (= (:status invalid-post-response-3) 400))
      (is (= (:status invalid-post-response-4) 400))
      (is (-> (utils/parse-body (:body invalid-post-response-3))
              :error
              (->> (re-find #"ajallisesti opiskeluoikeuden"))))
      (is (-> (utils/parse-body (:body invalid-post-response-4))
              :error
              (->> (re-find #"ajallisesti opiskeluoikeuden")))))))

(deftest require-yksiloiva-tunniste-in-oht
  (testing "Osaamisen hankkimistavassa pitää olla yksilöivä tunniste."
    (let [app (hoks-utils/create-app nil)
          invalid-data
          (update-in test-data/hoks-data
                     [:hankittavat-ammat-tutkinnon-osat 0
                      :osaamisen-hankkimistavat 0]
                     dissoc :yksiloiva-tunniste)
          invalid-post-response
          (hoks-utils/create-mock-post-request "" invalid-data app)
          ok-data
          (update-in invalid-data
                     [:hankittavat-ammat-tutkinnon-osat 0
                      :osaamisen-hankkimistavat 0]
                     assoc :osaamisen-hankkimistapa-koodi-uri
                     "osaamisenhankkimistapa_mulkoilu")
          ok-post-response
          (hoks-utils/create-mock-post-request "" ok-data app)]
      (is (= (:status ok-post-response) 200))
      (is (= (:status invalid-post-response) 400))
      (is (-> (utils/parse-body (:body invalid-post-response))
              (get-in [:errors :hankittavat-ammat-tutkinnon-osat 0
                       :osaamisen-hankkimistavat 0])
              (->> (re-find #"yksilöivä tunniste")))))))

(deftest prevent-osaamisen-saavuttaminen-out-of-range
  (testing "The allowed range of osaaminen-saavuttamisen-pvm is from 1.1.2018
           to two weeks in the future (from the time of saving the HOKS)."
    (let [app (hoks-utils/create-app nil)
          too-much-in-future (str (.plusDays (LocalDate/now) 20))
          invalid-data
          (assoc test-data/hoks-data :osaamisen-saavuttamisen-pvm "2017-01-01")
          invalid-post-response
          (hoks-utils/create-mock-post-request "" invalid-data app)
          post-response
          (hoks-utils/create-mock-post-request "" test-data/hoks-data app)
          invalid-patch-response-1
          (hoks-utils/create-mock-hoks-patch-request
            1 {:id 1 :osaamisen-saavuttamisen-pvm "2017-01-01"} app)
          invalid-patch-response-2
          (hoks-utils/create-mock-hoks-patch-request
            1 {:id 1 :osaamisen-saavuttamisen-pvm too-much-in-future} app)]
      (is (= (:status invalid-post-response) 400))
      (is (->> invalid-post-response
               :body
               utils/parse-body
               :errors
               :osaamisen-saavuttamisen-pvm
               (re-find #"kaksi viikkoa")))
      (is (= (:status post-response) 200))
      (is (= (:status invalid-patch-response-1) 400))
      (is (= [:osaamisen-saavuttamisen-pvm]
             (-> invalid-patch-response-1 :body utils/parse-body :errors keys)))
      (is (= (:status invalid-patch-response-2) 400)))))

(deftest patching-of-hoks-part-not-allowed
  (testing "PATCH of HOKS can't be used to update sub entities of HOKS"
    (let [app (hoks-utils/create-app nil)
          post-response (hoks-utils/create-mock-post-request
                          "" test-data/hoks-data app)
          patch-response (hoks-utils/create-mock-hoks-patch-request
                           1 test-data/hoks-data app)]
      (is (= (:status post-response) 200))
      (is (= (:status patch-response) 400)))))

(deftest patch-non-existing-hoks
  (testing "PATCH prevents updating non existing HOKS"
    (let [response (hoks-utils/create-mock-hoks-patch-request
                     1 {:id 1} (hoks-utils/create-app nil))]
      (is (= (:status response) 404)))))

(deftest put-non-existing-hoks
  (testing "PUT prevents updating non existing HOKS"
    (let [response (hoks-utils/create-mock-hoks-put-request
                     1 {:id 1} (hoks-utils/create-app nil))]
      (is (= (:status response) 404)))))

(deftest get-hoks-by-id-not-found
  (testing "GET HOKS by hoks-id"
    (let [response
          (hoks-utils/mock-st-get
            (hoks-utils/create-app nil)
            (format "%s/%s" base-url 43857))]
      (is (= (:status response) 404)))))

(deftest y-tunnus-validation-failure
  (testing "Y-tunnus validation failure scenarios"
    (let [hoks-data (assoc test-data/hoks-data
                           :hankittavat-ammat-tutkinnon-osat
                           [parts-test-data/patch-all-hao-data])
          error-details-template (fn [y-tunnus]
                                   {:hankittavat-ammat-tutkinnon-osat
                                    [{:osaamisen-hankkimistavat
                                      [{:tyopaikalla-jarjestettava-koulutus
                                        {:tyopaikan-y-tunnus y-tunnus}}]}]})
          regex-mismatch
          #(error-details-template
             (str "(not (re-find #\"^[0-9]{7}-[0-9]$\" \"" % "\"))"))
          checksum-mismatch
          #(error-details-template
             (str "(not (\"Kelvollinen Y-tunnus\" \"" % "\"))"))
          response-1
          (hoks-utils/mock-st-post
            (hoks-utils/create-app nil)
            base-url
            (update-in hoks-data [:hankittavat-ammat-tutkinnon-osat 0
                                  :osaamisen-hankkimistavat 0
                                  :tyopaikalla-jarjestettava-koulutus]
                       dissoc :tyopaikan-y-tunnus))]
      (is (= (:status response-1) 400))
      (is (-> (:body response-1)
              (utils/parse-body)
              (get-in [:errors :hankittavat-ammat-tutkinnon-osat 0
                       :osaamisen-hankkimistavat 0])
              (->> (re-find #"Y-tunnus"))))
      (doseq [[y-tunnus expected-error]
              [["Ei y-tunnusta" regex-mismatch]
               ["1234567-1 "    regex-mismatch]
               ["1234567-2"     checksum-mismatch]
               ["7654321-8"     checksum-mismatch]]
              :let [response (hoks-utils/mock-st-post
                               (hoks-utils/create-app nil)
                               base-url
                               (assoc-in hoks-data
                                         [:hankittavat-ammat-tutkinnon-osat 0
                                          :osaamisen-hankkimistavat 0
                                          :tyopaikalla-jarjestettava-koulutus
                                          :tyopaikan-y-tunnus]
                                         y-tunnus))]]
        (is (= (:status response) 400))
        (is (= (:errors (utils/parse-body (:body response)))
               (expected-error y-tunnus)))))))
