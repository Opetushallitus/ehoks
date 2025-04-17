(ns oph.ehoks.user.settings-test
  (:require [oph.ehoks.user.settings :as user-settings]
            [clojure.test :as t]
            [oph.ehoks.test-utils :as test-utils]))

(t/use-fixtures :once test-utils/migrate-database)
(t/use-fixtures :each test-utils/empty-database-after-test)

(t/deftest save-new-test
  (t/testing "Save new settings"
    (user-settings/save! "1.2.246.562.24.42345678625" {:hello "World!"})
    (t/is (= (user-settings/get! "1.2.246.562.24.42345678625")
             {:hello "World!"}))))

(t/deftest save-update-test
  (t/testing "Save updated settings"
    (user-settings/save! "1.2.246.562.24.42345678625" {:hello "World!"})
    (user-settings/save! "1.2.246.562.24.42345678625"
                         {:hello-second "Other World!"})
    (t/is (= (user-settings/get! "1.2.246.562.24.42345678625")
             {:hello-second "Other World!"}))))
