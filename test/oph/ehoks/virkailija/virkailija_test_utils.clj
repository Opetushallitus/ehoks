(ns oph.ehoks.virkailija.virkailija-test-utils
  (:require [clojure.test :refer :all]
            [oph.ehoks.db.db-operations.oppija :as db-oppija]
            [oph.ehoks.db.db-operations.opiskeluoikeus :as db-opiskeluoikeus]))

(def dummy-user {:oid "1.2.246.562.24.12312312312"
                 :nimi "Teuvo Testaaja"
                 :opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                 :oppilaitos-oid "1.2.246.562.10.12000000000"
                 :tutkinto-nimi {:fi "Testitutkinto 1" :sv "Testskrivning 1"}
                 :osaamisala-nimi
                 {:fi "Testiosaamisala numero 1" :sv "Kunnande 1"}
                 :koulutustoimija-oid ""})

(defn add-oppija [oppija]
  (db-oppija/insert-oppija!
    {:oid (:oid oppija)
     :nimi (:nimi oppija)})
  (db-opiskeluoikeus/insert-opiskeluoikeus!
    {:oid (:opiskeluoikeus-oid oppija)
     :oppija_oid (:oid oppija)
     :oppilaitos_oid (:oppilaitos-oid oppija)
     :koulutustoimija_oid (:koulutustoimija-oid oppija)
     :tutkinto-nimi (:tutkinto-nimi oppija
                                    {:fi "Testialan perustutkinto"
                                     :sv "Grundexamen inom testsbranschen"
                                     :en "Testing"})
     :osaamisala-nimi (:osaamisala-nimi oppija
                                        {:fi "Osaamisala suomeksi"
                                         :sv "På svenska"})}))
