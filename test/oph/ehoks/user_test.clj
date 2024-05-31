(ns oph.ehoks.user-test
  (:require [clj-time.coerce :as c]
            [clojure.test :as t :refer [deftest is testing]]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-opiskeluoikeus]
            [oph.ehoks.db.db-operations.oppija :as db-oppija]
            [oph.ehoks.user :as user]
            [oph.ehoks.test-utils :as test-utils :refer [eq with-db]]))

(t/use-fixtures :once test-utils/migrate-database)

(deftest get-auth-info-test
  (testing "Mapping kayttooikeus-service data to eHOKS privileges"
    (with-db
      (eq (user/get-auth-info
            {:organisaatiot [{:organisaatioOid "1.2.246.562.10.20000000008"
                              :kayttooikeudet [{:palvelu "EHOKS"
                                                :oikeus "CRUD"}]}
                             {:organisaatioOid "1.2.246.562.10.10000000009"
                              :kayttooikeudet [{:palvelu "EHOKS"
                                                :oikeus "OPHPAAKAYTTAJA"}]}]})
          {:organisation-privileges
           '({:oid "1.2.246.562.10.20000000008"
              :privileges #{:read :write :update :delete}
              :roles #{}
              :child-organisations []}
              {:oid "1.2.246.562.10.10000000009"
               :privileges #{:read :write :update :delete}
               :roles #{:oph-super-user}
               :child-organisations []})})

      (eq (user/get-auth-info
            {:organisaatiot [{:organisaatioOid "1.2.246.562.10.20000000008"
                              :kayttooikeudet [{:palvelu "EHOKS"
                                                :oikeus "READ"}]}]})
          {:organisation-privileges
           '({:oid "1.2.246.562.10.20000000008"
              :privileges #{:read}
              :roles #{}
              :child-organisations []})})

      (eq (user/get-auth-info
            {:organisaatiot [{:organisaatioOid "1.2.246.562.10.20000000008"
                              :kayttooikeudet [{:palvelu "SERVICE"
                                                :oikeus "CRUD"}]}]})
          {:organisation-privileges
           '({:oid "1.2.246.562.10.20000000008"
              :privileges #{}
              :roles #{}
              :child-organisations []})})

      (db-oppija/insert-oppija!
        {:oid "1.2.246.562.24.44000000008"
         :nimi "Tellervo Testi"})
      (db-opiskeluoikeus/insert-opiskeluoikeus!
        {:oid "1.2.246.562.15.76000000000"
         :oppija_oid "1.2.246.562.24.44000000008"
         :oppilaitos_oid "1.2.246.562.10.30000000007"
         :koulutustoimija_oid "1.2.246.562.10.20000000008"
         :tutkinto-nimi {:fi "Testitutkinto 1"}
         :osaamisala-nimi {:fi "Testiosaamisala numero 1"}})
      (db-opiskeluoikeus/insert-opiskeluoikeus!
        {:oid "1.2.246.562.15.76300000007"
         :oppija_oid "1.2.246.562.24.44000000008"
         :oppilaitos_oid "1.2.246.562.10.40000000006"
         :koulutustoimija_oid "1.2.246.562.10.20000000008"
         :tutkinto-nimi {:fi "Testitutkinto 1"}
         :osaamisala-nimi {:fi "Testiosaamisala numero 1"}})
      (db-hoks/insert-hoks! {:opiskeluoikeus_oid "1.2.246.562.15.76000000000"
                             :oppija_oid "1.2.246.562.24.44000000008"
                             :ensikertainen_hyvaksyminen
                             (c/to-sql-date (c/from-string "2019-07-17"))})
      (db-hoks/insert-hoks! {:opiskeluoikeus_oid "1.2.246.562.15.76300000007"
                             :oppija_oid "1.2.246.562.24.44000000008"
                             :ensikertainen_hyvaksyminen
                             (c/to-sql-date (c/from-string "2019-07-17"))})

      (eq (user/get-auth-info
            {:organisaatiot [{:organisaatioOid "1.2.246.562.10.20000000008"
                              :kayttooikeudet [{:palvelu "SERVICE"
                                                :oikeus "CRUD"}]}]})
          {:organisation-privileges
           '({:oid "1.2.246.562.10.20000000008"
              :privileges #{}
              :roles #{}
              :child-organisations ["1.2.246.562.10.30000000007"
                                    "1.2.246.562.10.40000000006"]})}))))

(deftest oph-super-user
  (testing "Check if user is OPH super user"
    (is (user/oph-super-user?
          {:organisation-privileges
           '({:oid "1.2.246.562.10.30000000007"
              :privileges #{}
              :roles #{}}
              {:oid "1.2.246.562.10.10000000009"
               :privileges #{}
               :roles #{:oph-super-user}}
              {:oid "1.2.246.562.10.20000000008"
               :privileges #{}
               :roles #{}})}))
    (is (not
          (user/oph-super-user?
            {:organisation-privileges
             '({:oid "1.2.246.562.10.10000000009"
                :privileges #{}
                :roles #{}})})))))

(deftest organisation-privileges
  (testing "Returns `nil` if user has no privileges in organisation"
    (is (nil? (user/organisation-privileges
                {:oid           "1.2.246.562.10.20000000008"
                 :parentOidPath "|1.2.246.562.10.10000000009|"}
                {:organisation-privileges
                 '({:oid        "1.2.246.562.10.20000000008"
                    :privileges #{}
                    :roles      #{:oph-super-user}})}))))
  (testing (str "Returns `nil` if `organisation` (or parent organisation) "
                "doesn't match any of users orgs")
    (is (nil? (user/organisation-privileges
                {:oid           "1.2.246.562.10.20000000008"
                 :parentOidPath "|1.2.246.562.10.10000000009|"}
                {:organisation-privileges
                 '({:oid        "1.2.246.562.10.30000000007"
                    :privileges #{:read :write :update :delete}
                    :roles      #{:oph-super-user}})}))))
  (testing "Returns `nil` if `organisation` is `nil` or empty map"
    (is (nil? (user/organisation-privileges
                nil
                {:organisation-privileges
                 '({:oid        "1.2.246.562.10.30000000007"
                    :privileges #{:read :write :update :delete}
                    :roles      #{:oph-super-user}})})))
    (is (nil? (user/organisation-privileges
                {}
                {:organisation-privileges
                 '({:oid        "1.2.246.562.10.30000000007"
                    :privileges #{:read :write :update :delete}
                    :roles      #{:oph-super-user}})}))))
  (testing (str "Returns user privileges in organisation if it matches to one "
                "of users organisations")
    (eq (user/organisation-privileges
          {:oid           "1.2.246.562.10.20000000008"
           :parentOidPath "|1.2.246.562.10.10000000009|"}
          {:organisation-privileges
           '({:oid        "1.2.246.562.10.20000000008"
              :privileges #{:read :write :update :delete}
              :roles      #{}}
              {:oid        "1.2.246.562.10.10000000009"
               :privileges #{}
               :roles      #{:oph-super-user}})})
        #{:read :write :update :delete})))
