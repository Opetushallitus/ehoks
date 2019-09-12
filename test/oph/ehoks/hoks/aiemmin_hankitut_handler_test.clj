(ns oph.ehoks.hoks.aiemmin-hankitut-handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.utils :as utils]
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
