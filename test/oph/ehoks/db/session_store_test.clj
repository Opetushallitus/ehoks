(ns oph.ehoks.db.session-store-test
  (:require [ring.middleware.session.store :refer [SessionStore]]
            [oph.ehoks.db.session-store :as sut]
            [clojure.test :as t]
            [oph.ehoks.utils :as u]))

(t/use-fixtures :once u/migrate-database)
(t/use-fixtures :each u/empty-database-after-test)

(t/deftest get-no-session
  (t/testing "Get no session"
    (let [ss (sut/db-store)]
      (let [k (.write-session ss nil {:hello "World!"})]
        (t/is (= (.read-session ss (str k "-not-found")) nil))))))

(t/deftest create-session
  (t/testing "Create new session"
    (let [ss (sut/db-store)]
      (let [k (.write-session ss nil {:hello "World!"})]
        (t/is (= (.read-session ss k) {:hello "World!"}))))))

(t/deftest delete-session
  (t/testing "Delete session"
    (let [ss (sut/db-store)]
      (let [k1 (.write-session ss nil {:hello "World!"})
            k2 (.write-session ss nil {:hello "Second World!"})]
        (.delete-session ss k1)
        (t/is (= (.read-session ss k1) nil))
        (t/is (= (.read-session ss k2) {:hello "Second World!"}))))))

(t/deftest update-session
  (t/testing "Update session"
    (let [ss (sut/db-store)]
      (let [k (.write-session ss nil {:hello "World!"})
            ku (.write-session ss k {:hello "World Again!"})]
        (t/is (= k ku))
        (t/is (= (.read-session ss k) {:hello "World Again!"}))))))
