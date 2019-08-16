(ns oph.ehoks.user-settings-test
  (:require [oph.ehoks.user-settings :as sut]
            [clojure.test :as t]
            [oph.ehoks.utils :as utils]))

(t/use-fixtures :each utils/with-database)

(t/use-fixtures :once utils/clean-db)

(t/deftest save-settings-new-test
  (t/testing "Save new settings"
    (sut/save-settings! "1.2.246.562.24.42345678625" {:hello "World!"})
    (t/is (= (sut/get-settings "1.2.246.562.24.42345678625")
             {:hello "World!"}))))

(t/deftest save-settings-update-test
  (t/testing "Save updated settings"
    (sut/save-settings! "1.2.246.562.24.42345678625" {:hello "World!"})
    (sut/save-settings! "1.2.246.562.24.42345678625"
                        {:hello-second "Other World!"})
    (t/is (= (sut/get-settings "1.2.246.562.24.42345678625")
             {:hello-second "Other World!"}))))

(t/deftest delete-settings-test
  (t/testing "Delete settings"
    (sut/save-settings! "1.2.246.562.24.42345678625" {:hello "World!"})
    (t/is (= (sut/get-settings "1.2.246.562.24.42345678625")
             {:hello "World!"}))
    (sut/delete-settings! "1.2.246.562.24.42345678625")
    (t/is (nil? (sut/get-settings "1.2.246.562.24.42345678625")))))