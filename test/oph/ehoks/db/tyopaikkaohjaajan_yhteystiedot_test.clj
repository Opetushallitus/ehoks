(ns oph.ehoks.db.tyopaikkaohjaajan_yhteystiedot_test
  (:require [clojure.test :refer :all]
            [oph.ehoks.utils :as utils :refer [empty-database-after-test
                                               migrate-database]]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]))

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
      :yksiloiva-tunniste "abcd"}
     {:jarjestajan-edustaja
      {:nimi "Eetu Edustaja"
       :rooli "Valvoja"
       :oppilaitos-oid "1.2.246.562.10.54453921340"}
      :osaamisen-hankkimistapa-koodi-uri
      "osaamisenhankkimistapa_oppisopimus"
      :osaamisen-hankkimistapa-koodi-versio 1
      :tyopaikalla-jarjestettava-koulutus
      {:vastuullinen-tyopaikka-ohjaaja
       {:nimi "Opo Ohjaaja"
        :sahkoposti "opo.ohjaaja@esimerkki.com"
        :puhelinnumero "0402222222"}
       :tyopaikan-nimi "Ohjaus Oy"
       :tyopaikan-y-tunnus "12345689-4"
       :keskeiset-tyotehtavat ["Hälytysten valvonta"
                               "Vuoronvaihdon tarkistukset"]}
      :muut-oppimisymparistot []
      :keskeytymisajanjaksot []
      :ajanjakson-tarkenne "Ei tarkennettavaa"
      :hankkijan-edustaja
      {:nimi "Henna Hankkija"
       :rooli "Opettaja"
       :oppilaitos-oid "1.2.246.562.10.54453921350"}
      :alku (.minusDays (java.time.LocalDate/now) 100)
      :loppu (.minusDays (java.time.LocalDate/now) 80)
      :yksiloiva-tunniste "abce"}]
    :osaamisen-osoittaminen []}])

(def hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                :oppija-oid "1.2.246.562.24.12312312319"
                :ensikertainen-hyvaksyminen (java.time.LocalDate/of 2022 7 1)
                :osaamisen-hankkimisen-tarve true
                :sahkoposti "erkki.esimerkki@esimerkki.com"
                :hankittavat-paikalliset-tutkinnon-osat hpto-data})

(deftest delete-tyopaikkaohjaajan-yhteystiedot-test
  (testing "Työpaikkaohjaajan yhteystiedot poistetaan yli kolme kuukautta
            sitten päättyneestä työelämäjaksosta"
    (let [saved-hoks
          (with-redefs [oph.ehoks.external.koski/get-opiskeluoikeus-info
                        (fn [_] {:tyyppi {:koodiarvo "ammatillinenkoulutus"}})]
            (hoks/save! hoks-data))
          affected-jakso-ids (db-hoks/delete-tyopaikkaohjaajan-yhteystiedot!)
          hoks (hoks/get-by-id (:id saved-hoks))
          osaamisen-hankkimistavat (-> hoks
                                       :hankittavat-paikalliset-tutkinnon-osat
                                       first
                                       :osaamisen-hankkimistavat)]
      (is (= (-> osaamisen-hankkimistavat
                 first
                 :tyopaikalla-jarjestettava-koulutus
                 :vastuullinen-tyopaikka-ohjaaja)
             {:nimi "Olli Ohjaaja"}))
      (is (= (-> osaamisen-hankkimistavat
                 second
                 :tyopaikalla-jarjestettava-koulutus
                 :vastuullinen-tyopaikka-ohjaaja)
             {:nimi "Opo Ohjaaja"
              :sahkoposti "opo.ohjaaja@esimerkki.com"
              :puhelinnumero "0402222222"}))
      (is (= affected-jakso-ids #{1})))))
