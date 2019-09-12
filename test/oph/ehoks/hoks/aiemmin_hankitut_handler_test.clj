(ns oph.ehoks.hoks.aiemmin-hankitut-handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.utils :as utils :refer [eq]]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils]
            [oph.ehoks.hoks.test-data :as test-data]))

(use-fixtures :each utils/with-database)

(def ahyto-path "aiemmin-hankittu-yhteinen-tutkinnon-osa")

(deftest post-and-get-aiemmin-hankitut-yhteiset-tutkinnon-osat
  (testing "POST ahyto and then get the created ahyto"
    (hoks-utils/test-post-and-get-of-aiemmin-hankittu-osa ahyto-path test-data/ahyto-data)))

(deftest put-ahyto-of-hoks
  (testing "PUTs aiemmin hankitut yhteiset tutkinnon osat of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      test-data/ahyto-of-hoks-updated
      :aiemmin-hankitut-yhteiset-tutkinnon-osat
      test-data/hoks-data)))

(deftest put-ahpto-of-hoks
  (testing "PUTs aiemmin hankitut paikalliset tutkinnon osat of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      test-data/ahpto-of-hoks-updated
      :aiemmin-hankitut-paikalliset-tutkinnon-osat
      test-data/hoks-data)))

(deftest put-ahato-of-hoks
  (testing "PUTs aiemmin hankitut ammatilliset tutkinnon osat of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      test-data/ahato-of-hoks-updated
      :aiemmin-hankitut-ammat-tutkinnon-osat
      test-data/hoks-data)))

(defn- test-patch-of-aiemmin-hankittu-osa
  [osa-path osa-data osa-patched-data assert-function]
  (hoks-utils/with-hoks-and-app
    [hoks app]
    (let [post-response (hoks-utils/create-mock-post-request
                          osa-path osa-data app hoks)
          patch-response (hoks-utils/create-mock-hoks-osa-patch-request
                           osa-path app osa-patched-data)
          get-response (hoks-utils/create-mock-hoks-osa-get-request osa-path app hoks)
          get-response-data (:data (utils/parse-body (:body get-response)))]
      (is (= (:status post-response) 200))
      (is (= (:status patch-response) 204))
      (is (= (:status get-response) 200))
      (assert-function get-response-data osa-data))))

(defn- assert-ahyto-is-patched-correctly [updated-data initial-data]
  (is (= (:valittu-todentamisen-prosessi-koodi-uri updated-data)
         "osaamisentodentamisenprosessi_2000"))
  (is (= (:tutkinnon-osa-koodi-versio updated-data)
         (:tutkinnon-osa-koodi-versio initial-data)))
  (eq (:tarkentavat-tiedot-osaamisen-arvioija updated-data)
      (:tarkentavat-tiedot-osaamisen-arvioija test-data/multiple-ahyto-values-patched))
  (hoks-utils/compare-tarkentavat-tiedot-naytto-values
    updated-data test-data/multiple-ahyto-values-patched first)
  (hoks-utils/compare-tarkentavat-tiedot-naytto-values
    updated-data test-data/multiple-ahyto-values-patched second)
  (eq (:osa-alueet updated-data)
      (:osa-alueet test-data/multiple-ahyto-values-patched)))

(deftest patch-aiemmin-hankittu-yhteinen-tutkinnon-osa
  (testing "Patching values of ahyto"
    (test-patch-of-aiemmin-hankittu-osa
      ahyto-path
      test-data/ahyto-data
      test-data/multiple-ahyto-values-patched
      assert-ahyto-is-patched-correctly)))

