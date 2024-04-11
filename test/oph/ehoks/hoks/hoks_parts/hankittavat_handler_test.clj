(ns oph.ehoks.hoks.hoks-parts.hankittavat-handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.test-utils :as test-utils :refer [eq]]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils]
            [oph.ehoks.hoks.hoks-parts.parts-test-data :as parts-test-data]))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-database-after-test)

(def hpto-path "hankittava-paikallinen-tutkinnon-osa")
(def hyto-path "hankittava-yhteinen-tutkinnon-osa")
(def hao-path "hankittava-ammat-tutkinnon-osa")

(deftest post-and-get-hankittava-paikallinen-tutkinnon-osa
  (testing "GET newly created hankittava paikallinen tutkinnon osa"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (let [ppto-response (hoks-utils/mock-st-post
                            app (hoks-utils/get-hoks-url hoks hpto-path)
                            parts-test-data/hpto-data)
            body (test-utils/parse-body (:body ppto-response))]
        (is (= (:status ppto-response) 200))
        (eq body {:data
                  {:uri
                   (hoks-utils/get-hoks-url hoks (format "%s/1" hpto-path))}
                  :meta {:id 1}})
        (let [ppto-new (hoks-utils/mock-st-get
                         app
                         (hoks-utils/get-hoks-url
                           hoks (format "%s/1" hpto-path)))]
          (eq
            (test-utils/dissoc-module-ids
              (:data (test-utils/parse-body (:body ppto-new))))
            (assoc
              (test-utils/dissoc-module-ids parts-test-data/hpto-data)
              :id 1)))))))

(deftest patch-all-hankittavat-paikalliset-tutkinnon-osat
  (testing "PATCH all hankittava paikallinen tutkinnon osa"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/mock-st-post
        app (hoks-utils/get-hoks-url hoks hpto-path) parts-test-data/hpto-data)
      (let [patch-response
            (hoks-utils/mock-st-patch
              app
              (hoks-utils/get-hoks-url hoks (format "%s/1" hpto-path))
              (assoc parts-test-data/hpto-data
                     :nimi "333" :olennainen-seikka false))]
        (is (= (:status patch-response) 204))))))

(deftest patch-one-hankittava-paikallinen-tutkinnon-osa
  (testing "PATCH one value hankittava paikallinen tutkinnon osa"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (let [ppto-response
            (hoks-utils/mock-st-post
              app
              (hoks-utils/get-hoks-url hoks hpto-path)
              parts-test-data/hpto-data)
            ppto-body (test-utils/parse-body (:body ppto-response))
            patch-response
            (hoks-utils/mock-st-patch
              app
              (hoks-utils/get-hoks-url hoks (format "%s/1" hpto-path))
              {:id 1 :nimi "2223"})
            get-response (-> (get-in ppto-body [:data :uri])
                             hoks-utils/get-authenticated
                             :data)]
        (is (= (:status patch-response) 204))
        (eq (test-utils/dissoc-module-ids get-response)
            (assoc (test-utils/dissoc-module-ids parts-test-data/hpto-data)
                   :id 1
                   :nimi "2223"))))))

(deftest post-and-get-hankittava-yhteinen-tukinnon-osa
  (testing "POST hankittavat yhteisen tutkinnon osat"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (let [post-response (hoks-utils/create-mock-post-request
                            hyto-path parts-test-data/hyto-data app hoks)
            get-response
            (hoks-utils/create-mock-hoks-osa-get-request hyto-path app hoks)]
        (hoks-utils/assert-post-response-is-ok hyto-path post-response)
        (is (= (:status get-response) 200))
        (eq (test-utils/dissoc-module-ids (test-utils/parse-body
                                            (:body get-response)))
            {:meta {} :data (assoc (test-utils/dissoc-module-ids
                                     parts-test-data/hyto-data) :id 1)})))))

(def ^:private one-value-of-hyto-patched
  {:koulutuksen-jarjestaja-oid "1.2.246.562.10.10000000017"})

(deftest patch-one-value-of-hankittava-yhteinen-tutkinnon-osa
  (testing "PATCH one value hankittavat yhteisen tutkinnon osat"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/create-mock-post-request
        hyto-path parts-test-data/hyto-data app hoks)
      (let [patch-response (hoks-utils/create-mock-hoks-osa-patch-request
                             hyto-path app one-value-of-hyto-patched)
            get-response
            (hoks-utils/create-mock-hoks-osa-get-request hyto-path app hoks)
            get-response-data (-> (:body get-response)
                                  test-utils/parse-body
                                  :data)]
        (is (= (:status patch-response) 204))
        (is (= (:koulutuksen-jarjestaja-oid get-response-data)
               (:koulutuksen-jarjestaja-oid one-value-of-hyto-patched))
            "Patched value should change.")
        (is (= (:tutkinnon-osa-koodi-versio get-response-data)
               (:tutkinnon-osa-koodi-versio parts-test-data/hyto-data))
            "Value should stay unchanged")))))

(deftest patch-multiple-values-of-hankittavat-yhteiset-tutkinnon-osat
  (testing "PATCH all hankittavat yhteisen tutkinnon osat"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/create-mock-post-request
        hyto-path parts-test-data/hyto-data app hoks)
      (let [patch-response (hoks-utils/create-mock-hoks-osa-patch-request
                             hyto-path
                             app
                             parts-test-data/multiple-hyto-values-patched)
            get-response
            (hoks-utils/create-mock-hoks-osa-get-request hyto-path app hoks)
            get-response-data (-> (:body get-response)
                                  test-utils/parse-body
                                  :data)]
        (is (= (:status patch-response) 204))
        (eq (test-utils/dissoc-module-ids (:osa-alueet get-response-data))
            (test-utils/dissoc-module-ids
              (:osa-alueet parts-test-data/multiple-hyto-values-patched)))))))

(def hyto-sub-entity-patched
  {:osa-alueet parts-test-data/osa-alueet-of-hyto})

(deftest only-sub-entity-of-hyto-patched
  (testing "PATCH only osa-alueet of hyto and leave base hyto untouched."
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/create-mock-post-request
        hyto-path parts-test-data/hyto-data app hoks)
      (let [patch-response (hoks-utils/create-mock-hoks-osa-patch-request
                             hyto-path app hyto-sub-entity-patched)
            get-response
            (hoks-utils/create-mock-hoks-osa-get-request hyto-path app hoks)
            get-response-data (-> (:body get-response)
                                  test-utils/parse-body
                                  :data)]
        (is (= (:status patch-response) 204))
        (eq (test-utils/dissoc-module-ids (:osa-alueet get-response-data))
            (test-utils/dissoc-module-ids
              (:osa-alueet hyto-sub-entity-patched)))))))

(deftest post-and-get-hankittava-ammatillinen-osaaminen
  (testing "POST hankittava ammatillinen osaaminen and then get created hao"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (let [post-response
            (hoks-utils/create-mock-post-request
              hao-path parts-test-data/hao-data app hoks)
            get-response
            (hoks-utils/create-mock-hoks-osa-get-request hao-path app hoks)]
        (is (= (:status post-response) 200))
        (eq (test-utils/parse-body
              (:body post-response))
            {:meta {:id 1}
             :data
             {:uri
              (format
                "%s/1/hankittava-ammat-tutkinnon-osa/1"
                hoks-utils/base-url)}})
        (is (= (:status get-response) 200))
        (eq (test-utils/dissoc-module-ids (test-utils/parse-body
                                            (:body get-response)))
            {:meta {} :data (assoc (test-utils/dissoc-module-ids
                                     parts-test-data/hao-data) :id 1)})))))

(deftest patch-all-hankittava-ammatillinen-osaaminen
  (testing "PATCH ALL hankittava ammat osaaminen"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/create-mock-post-request
        hao-path parts-test-data/hao-data app hoks)
      (let [patch-response
            (hoks-utils/mock-st-patch
              app
              (hoks-utils/get-hoks-url hoks (str hao-path "/1"))
              (assoc parts-test-data/patch-all-hao-data :id 1))
            get-response
            (hoks-utils/create-mock-hoks-osa-get-request hao-path app hoks)]
        (is (= (:status patch-response) 204))
        (eq (test-utils/dissoc-module-ids (test-utils/parse-body
                                            (:body get-response)))
            {:meta {} :data
             (assoc (test-utils/dissoc-module-ids
                      parts-test-data/patch-all-hao-data)
                    :id 1)})))))

(deftest patch-one-hankittava-ammatilinen-osaaminen
  (testing "PATCH one value hankittava ammatillinen osaaminen"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/mock-st-post
        app
        (format
          "%s/1/hankittava-ammat-tutkinnon-osa"
          hoks-utils/base-url) parts-test-data/hao-data)
      (let [response
            (hoks-utils/mock-st-patch
              app
              (format
                "%s/1/%s/1"
                hoks-utils/base-url hao-path)
              {:id 1
               :vaatimuksista-tai-tavoitteista-poikkeaminen "Test"})]
        (is (= (:status response) 204))))))
