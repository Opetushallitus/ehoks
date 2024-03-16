(ns oph.ehoks.db.opiskelijan_yhteystiedot_test
  (:require [clojure.test :refer :all]
            [oph.ehoks.utils :as utils :refer [empty-database-after-test
                                               migrate-database]]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(use-fixtures :once migrate-database)
(use-fixtures :each empty-database-after-test)

(def hpto-data
  [{:koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
    :tavoitteet-ja-sisallot "Testitavoite"
    :nimi "Orientaatio alaan"
    :osaamisen-hankkimistavat
    [{:jarjestajan-edustaja
      {:nimi "Erkki Edustaja"
       :rooli "Valvoja"
       :oppilaitos-oid "1.2.246.562.10.54453921340"}
      :osaamisen-hankkimistapa-koodi-uri
      "osaamisenhankkimistapa_oppisopimus"
      :osaamisen-hankkimistapa-koodi-versio 1
      :tyopaikalla-jarjestettava-koulutus
      {:vastuullinen-tyopaikka-ohjaaja
       {:nimi "Olli Ohjaaja"
        :sahkoposti "olli.ohjaaja@esimerkki.com"
        :puhelinnumero "0401111111"}
       :tyopaikan-nimi "Ohjaus Oy"
       :tyopaikan-y-tunnus "12345689-4"
       :keskeiset-tyotehtavat ["Hälytysten valvonta"
                               "Vuoronvaihdon tarkistukset"]}
      :muut-oppimisymparistot []
      :keskeytymisajanjaksot []
      :ajanjakson-tarkenne "Ei tarkennettavaa"
      :hankkijan-edustaja
      {:nimi "Harri Hankkija"
       :rooli "Opettaja"
       :oppilaitos-oid "1.2.246.562.10.54453921350"}
      :alku (java.time.LocalDate/of 2022 7 1)
      :loppu (java.time.LocalDate/of 2022 7 31)
      :yksiloiva-tunniste "abcd"}]
    :osaamisen-osoittaminen []}])

(def simple-hoks-data
  {:opiskeluoikeus-oid          "1.2.246.562.15.10000000009"
   :oppija-oid                  "1.2.246.562.24.12312312319"
   :ensikertainen-hyvaksyminen  (java.time.LocalDate/of 2022 7 1)
   :osaamisen-hankkimisen-tarve true
   :osaamisen-saavuttamisen-pvm (java.time.LocalDate/of 2022 9 1)
   :sahkoposti                  "erkki.esimerkki@esimerkki.com"
   :puhelinnumero               "0401234567"})

(def full-hoks-data
  {:opiskeluoikeus-oid          "1.2.246.562.15.20000000008"
   :oppija-oid                  "1.2.246.562.24.12312312319"
   :ensikertainen-hyvaksyminen  (java.time.LocalDate/of 2022 7 1)
   :osaamisen-hankkimisen-tarve true
   :sahkoposti                  "erkki.esimerkki@esimerkki.com"
   :puhelinnumero               "0401234567"
   :hankittavat-paikalliset-tutkinnon-osat hpto-data})

(def recent-hoks-data
  {:opiskeluoikeus-oid          "1.2.246.562.15.30000000007"
   :oppija-oid                  "1.2.246.562.24.12312312319"
   :ensikertainen-hyvaksyminen  (java.time.LocalDate/of 2022 7 1)
   :osaamisen-saavuttamisen-pvm (.minusDays (java.time.LocalDate/now) 10)
   :osaamisen-hankkimisen-tarve true
   :sahkoposti                  "matti.esimerkki@esimerkki.com"
   :puhelinnumero               "0401234568"})

(defn mocked-get-opiskeluoikeus-info-raw [oid]
  {:päättymispäivä "2022-09-01"})

(deftest delete-opiskelijan-yhteystiedot-test
  (testing "Opiskelijan yhteystiedot poistetaan yli kolme kuukautta sitten
            päättyneestä hoksista"
    (with-redefs [oph.ehoks.external.koski/get-opiskeluoikeus-info-raw
                  mocked-get-opiskeluoikeus-info-raw]
      (let [saved-hoks (hoks/save! simple-hoks-data)
            another-hoks (hoks/save! recent-hoks-data)
            _ (db-ops/update! :hoksit
                              {:updated_at (java.time.LocalDate/of 2022 9 1)}
                              ["id = ?" (:id saved-hoks)])
            _ (db-ops/update! :hoksit
                              {:updated_at (java.time.LocalDate/of 2022 9 1)}
                              ["id = ?" (:id another-hoks)])
            affected-hoks-ids (db-hoks/delete-opiskelijan-yhteystiedot!)
            hoks (hoks/get-by-id (:id saved-hoks))
            second-hoks (hoks/get-by-id (:id another-hoks))]
        (is (nil? (:puhelinnumero hoks)))
        (is (nil? (:sahkoposti hoks)))
        (is (= (:puhelinnumero second-hoks) "0401234568"))
        (is (= (:sahkoposti second-hoks) "matti.esimerkki@esimerkki.com"))
        (is (= affected-hoks-ids #{(:id hoks)}))))))

(deftest delete-opiskelijan-yhteystiedot-by-jakso-test
  (testing "Opiskelijan yhteystiedot poistetaan yli kolme kuukautta sitten
            päättyneen jakson perusteella"
    (with-redefs [oph.ehoks.external.koski/get-opiskeluoikeus-info-raw
                  mocked-get-opiskeluoikeus-info-raw]
      (let [saved-hoks (hoks/save! full-hoks-data)
            _ (db-ops/update! :hoksit
                              {:updated_at (java.time.LocalDate/of 2022 9 1)}
                              ["id = ?" (:id saved-hoks)])
            affected-hoks-ids (db-hoks/delete-opiskelijan-yhteystiedot!)
            hoks (hoks/get-by-id (:id saved-hoks))]
        (is (nil? (:puhelinnumero hoks)))
        (is (nil? (:sahkoposti hoks)))
        (is (= affected-hoks-ids #{(:id hoks)}))))))
