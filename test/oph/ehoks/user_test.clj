(ns oph.ehoks.user-test
  (:require [clojure.test :as t :refer [deftest testing is]]
            [oph.ehoks.utils :as utils :refer [eq with-db]]
            [oph.ehoks.user :as user]
            [oph.ehoks.external.http-client :as client]
            [clj-time.coerce :as c]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-opiskeluoikeus]
            [oph.ehoks.db.db-operations.oppija :as db-oppija]))

(t/use-fixtures :once utils/migrate-database)

(deftest get-auth-info-test
  (testing "Mapping kayttooikeus-service data to eHOKS privileges"
    (with-db
      (eq (user/get-auth-info
            {:organisaatiot [{:organisaatioOid "1.2.246.562.10.00000000002"
                              :kayttooikeudet [{:palvelu "EHOKS"
                                                :oikeus "CRUD"}]}
                             {:organisaatioOid "1.2.246.562.10.00000000001"
                              :kayttooikeudet [{:palvelu "EHOKS"
                                                :oikeus "OPHPAAKAYTTAJA"}]}]})
          {:organisation-privileges
           '({:oid "1.2.246.562.10.00000000002"
              :privileges #{:read :write :update :delete}
              :roles #{}
              :child-organisations []}
              {:oid "1.2.246.562.10.00000000001"
               :privileges #{:read :write :update :delete}
               :roles #{:oph-super-user}
               :child-organisations []})})

      (eq (user/get-auth-info
            {:organisaatiot [{:organisaatioOid "1.2.246.562.10.00000000002"
                              :kayttooikeudet [{:palvelu "EHOKS"
                                                :oikeus "READ"}]}]})
          {:organisation-privileges
           '({:oid "1.2.246.562.10.00000000002"
              :privileges #{:read}
              :roles #{}
              :child-organisations []})})

      (eq (user/get-auth-info
            {:organisaatiot [{:organisaatioOid "1.2.246.562.10.00000000002"
                              :kayttooikeudet [{:palvelu "SERVICE"
                                                :oikeus "CRUD"}]}]})
          {:organisation-privileges
           '({:oid "1.2.246.562.10.00000000002"
              :privileges #{}
              :roles #{}
              :child-organisations []})})

      (db-oppija/insert-oppija!
        {:oid "1.2.246.562.24.44000000002"
         :nimi "Tellervo Testi"})
      (db-opiskeluoikeus/insert-opiskeluoikeus!
        {:oid "1.2.246.562.15.76000000002"
         :oppija_oid "1.2.246.562.24.44000000002"
         :oppilaitos_oid "1.2.246.562.10.00000000003"
         :koulutustoimija_oid "1.2.246.562.10.00000000002"
         :tutkinto-nimi {:fi "Testitutkinto 1"}
         :osaamisala-nimi {:fi "Testiosaamisala numero 1"}})
      (db-opiskeluoikeus/insert-opiskeluoikeus!
        {:oid "1.2.246.562.15.76000000003"
         :oppija_oid "1.2.246.562.24.44000000002"
         :oppilaitos_oid "1.2.246.562.10.00000000004"
         :koulutustoimija_oid "1.2.246.562.10.00000000002"
         :tutkinto-nimi {:fi "Testitutkinto 1"}
         :osaamisala-nimi {:fi "Testiosaamisala numero 1"}})
      (db-hoks/insert-hoks! {:opiskeluoikeus_oid "1.2.246.562.15.76000000002"
                             :oppija_oid "1.2.246.562.24.44000000002"
                             :ensikertainen_hyvaksyminen
                             (c/to-sql-date (c/from-string "2019-07-17"))})
      (db-hoks/insert-hoks! {:opiskeluoikeus_oid "1.2.246.562.15.76000000003"
                             :oppija_oid "1.2.246.562.24.44000000002"
                             :ensikertainen_hyvaksyminen
                             (c/to-sql-date (c/from-string "2019-07-17"))})

      (eq (user/get-auth-info
            {:organisaatiot [{:organisaatioOid "1.2.246.562.10.00000000002"
                              :kayttooikeudet [{:palvelu "SERVICE"
                                                :oikeus "CRUD"}]}]})
          {:organisation-privileges
           '({:oid "1.2.246.562.10.00000000002"
              :privileges #{}
              :roles #{}
              :child-organisations ["1.2.246.562.10.00000000003"
                                    "1.2.246.562.10.00000000004"]})}))))

(deftest oph-super-user
  (testing "Check if user is OPH super user"
    (is (user/oph-super-user?
          {:organisation-privileges
           '({:oid "1.2.246.562.10.00000000003"
              :privileges #{}
              :roles #{}}
              {:oid "1.2.246.562.10.00000000001"
               :privileges #{}
               :roles #{:oph-super-user}}
              {:oid "1.2.246.562.10.00000000002"
               :privileges #{}
               :roles #{}})}))
    (is (not
          (user/oph-super-user?
            {:organisation-privileges
             '({:oid "1.2.246.562.10.00000000001"
                :privileges #{}
                :roles #{}})})))))

(deftest get-organisation-privileges
  (testing "Get organisation privileges"
    (client/set-get!
      (fn [^String url options]
        (cond
          (.endsWith
            url "/rest/organisaatio/v4/1.2.246.562.10.00000000003")
          {:status 200
           :body {:parentOidPath
                  "|1.2.246.562.10.00000000001|1.2.246.562.10.00000000002"}}
          (.endsWith
            url "/rest/organisaatio/v4/1.2.246.562.10.00000000001")
          {:status 200
           :body {}}
          (.endsWith
            url "/rest/organisaatio/v4/1.2.246.562.10.00000000002")
          {:status 200
           :body {:parentOidPath
                  "|1.2.246.562.10.00000000001|"}})))

    (eq (user/get-organisation-privileges
          {:organisation-privileges
           '({:oid "1.2.246.562.10.00000000002"
              :privileges #{:read :write :update :delete}
              :roles #{}}
              {:oid "1.2.246.562.10.00000000001"
               :privileges #{}
               :roles #{:oph-super-user}})}
          "1.2.246.562.10.00000000002")
        #{:read :write :update :delete})

    (is (nil?
          (user/get-organisation-privileges
            {:organisation-privileges
             '({:oid "1.2.246.562.10.00000000002"
                :privileges #{:read :write :update :delete}
                :roles #{}}
                {:oid "1.2.246.562.10.00000000003"
                 :privileges #{}
                 :roles #{:oph-super-user}})}
            "1.2.246.562.10.00000000001")))

    ; From parentOidPath
    (eq (user/get-organisation-privileges
          {:organisation-privileges
           '({:oid "1.2.246.562.10.00000000002"
              :privileges #{:read :write :update :delete}
              :roles #{}})}
          "1.2.246.562.10.00000000003")
        #{:read :write :update :delete})
    (client/reset-functions!)))
