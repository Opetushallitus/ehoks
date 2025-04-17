(ns oph.ehoks.hoks.hoks-parts.aiemmin-hankitut-handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.test-utils :as test-utils :refer [eq]]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils]
            [oph.ehoks.hoks.test-data :as test-data]
            [oph.ehoks.hoks.hoks-parts.parts-test-data :as parts-test-data]))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-database-after-test)

(deftest put-ahyto-of-hoks
  (testing "PUTs aiemmin hankitut yhteiset tutkinnon osat of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      parts-test-data/ahyto-of-hoks-updated
      :aiemmin-hankitut-yhteiset-tutkinnon-osat
      test-data/hoks-data)))

(deftest put-ahpto-of-hoks
  (testing "PUTs aiemmin hankitut paikalliset tutkinnon osat of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      parts-test-data/ahpto-of-hoks-updated
      :aiemmin-hankitut-paikalliset-tutkinnon-osat
      test-data/hoks-data)))

(deftest put-ahato-of-hoks
  (testing "PUTs aiemmin hankitut ammatilliset tutkinnon osat of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      parts-test-data/ahato-of-hoks-updated
      :aiemmin-hankitut-ammat-tutkinnon-osat
      test-data/hoks-data)))

(defn- get-arvioija [model]
  (-> model
      :aiemmin-hankitut-yhteiset-tutkinnon-osat
      first
      :osa-alueet
      first
      :tarkentavat-tiedot-osaamisen-arvioija))

(deftest ahyto-osa-alue-has-arvioija
  (testing "tarkentavat-tiedot-osaamisen-arvioija was addded to ahyto osa-alue
            according to EH-806"
    (let [app (hoks-utils/create-app nil)
          post-response
          (hoks-utils/create-mock-post-request "" test-data/hoks-data app)
          get-response (hoks-utils/create-mock-hoks-get-request 1 app)
          get-response-data (-> (:body get-response)
                                test-utils/parse-body
                                :data)]
      (is (= (:status post-response) 200))
      (is (= (:status get-response) 200))
      (let [output-arvioija (get-arvioija get-response-data)
            input-arvioija (get-arvioija test-data/hoks-data)]
        (eq output-arvioija input-arvioija)))))
