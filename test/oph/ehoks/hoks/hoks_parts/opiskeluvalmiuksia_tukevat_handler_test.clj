(ns oph.ehoks.hoks.hoks-parts.opiskeluvalmiuksia-tukevat-handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.test-utils :as test-utils :refer [eq]]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils]
            [oph.ehoks.hoks.hoks-parts.parts-test-data :as test-data]))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-database-after-test)

(def oto-path "opiskeluvalmiuksia-tukevat-opinnot")

(deftest post-and-get-opiskeluvalmiuksia-tukevat-opinnot
  (testing "GET opiskeluvalmiuksia tukevat opinnot"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (let [post-response (hoks-utils/create-mock-post-request
                            oto-path test-data/oto-data app hoks)
            get-response
            (hoks-utils/create-mock-hoks-osa-get-request oto-path app hoks)]
        (hoks-utils/assert-post-response-is-ok oto-path post-response)
        (is (= (:status get-response) 200))
        (eq (test-utils/parse-body
              (:body get-response))
            {:meta {} :data (assoc test-data/oto-data :id 1)})))))

(def ^:private one-value-of-oto-patched
  {:nimi "Muuttunut Nimi"})

(deftest patch-one-value-of-opiskeluvalmiuksia-tukevat-opinnot
  (testing "PATCH one value of opiskeluvalmiuksia tukevat opinnot"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/create-mock-post-request
        oto-path test-data/oto-data app hoks)
      (let [patch-response (hoks-utils/create-mock-hoks-osa-patch-request
                             oto-path app one-value-of-oto-patched)
            get-response
            (hoks-utils/create-mock-hoks-osa-get-request oto-path app hoks)
            get-response-data (-> (:body get-response)
                                  test-utils/parse-body
                                  :data)]
        (is (= (:status patch-response) 204))
        (is (= (:nimi get-response-data)
               (:nimi one-value-of-oto-patched))
            "Patched value should change.")
        (is (= (:kuvaus get-response-data)
               (:kuvaus test-data/oto-data))
            "Value should stay unchanged")))))

(def ^:private all-values-of-oto-patched
  {:nimi "Muuttunut Nimi"
   :kuvaus "Uusi Kuvaus"
   :alku "2020-01-12"
   :loppu "2021-10-20"})

(deftest patch-multiple-values-of-oto
  (testing "PATCH all opiskeluvalmiuksia tukevat opinnot"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/create-mock-post-request
        oto-path test-data/oto-data app hoks)
      (let [patch-response (hoks-utils/create-mock-hoks-osa-patch-request
                             oto-path app all-values-of-oto-patched)
            get-response
            (hoks-utils/create-mock-hoks-osa-get-request oto-path app hoks)
            get-response-data (-> (:body get-response)
                                  test-utils/parse-body
                                  :data)]
        (is (= (:status patch-response) 204))
        (eq get-response-data (assoc all-values-of-oto-patched :id 1))))))
