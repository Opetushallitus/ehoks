(ns oph.ehoks.hoks.invalid-hoks-handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils :refer [base-url]]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.utils :as utils :refer [eq]]))

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
