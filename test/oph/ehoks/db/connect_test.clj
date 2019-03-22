(ns oph.ehoks.db.connect-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.java.jdbc :as jdbc]
            [oph.ehoks.config :refer [config]]))

(def pg-uri
  {:connection-uri (:database-url config)})

(def create-table
  (jdbc/create-table-ddl :test_table
                         [[:name "varchar(32)"]
                          [:value :int]]))

(defn clear-table []
  (jdbc/db-do-commands pg-uri (str "DROP TABLE IF EXISTS test_table;")))

(deftest connect-to-db
  (clear-table)
  (testing "Connecting to test db"
    (let [response-create (jdbc/db-do-commands pg-uri create-table)
          response-insert
          (jdbc/insert-multi! pg-uri :test_table
                              [{:name "First Name" :value 24}
                               {:name "Second Name" :value 49}])
          response-query  (jdbc/query pg-uri ["select * from test_table"])]
      (is (= (:value (first response-query)) 24)))))
