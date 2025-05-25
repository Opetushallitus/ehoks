(ns oph.ehoks.palaute.tyoelama-test
  (:require [clojure.set :as s]
            [clojure.test :refer [are deftest is testing use-fixtures]]
            [clojure.tools.logging.test :refer [logged? with-log]]
            [medley.core :refer [find-first remove-vals]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.db-helpers :as db-helpers]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.external.organisaatio-test :as organisaatio-test]
            [oph.ehoks.heratepalvelu :as heratepalvelu]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.hoks-test :as hoks-test]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils]
            [oph.ehoks.hoks.osaamisen-hankkimistapa :as oht]
            [oph.ehoks.opiskeluoikeus-test :as oo-test]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.tapahtuma :as tapahtuma]
            [oph.ehoks.palaute.tyoelama :as tep]
            [oph.ehoks.palaute.vastaajatunnus :as vt]
            [oph.ehoks.test-utils :as test-utils]
            [oph.ehoks.utils.date :as date]
            [taoensso.faraday :as far])
  (:import (java.time LocalDate)
           (java.util UUID)))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-both-dbs-after-test)

;; FIXME: there is some kind of misunderstanding in the format of this
;; data.  It's fed to initiate-if-needed! and
;; initial-state-and-reason but seems mostly to be in SQS
;; format, which is not what is fed to those functions.
(def test-jakso
  {:hoks-id (:id hoks-test/hoks-1)
   :yksiloiva-tunniste "1"
   :tyopaikan-nimi "Testityöpaikka"
   :tyopaikan-ytunnus "1234567-8"
   :tyopaikkaohjaaja-nimi "Testi Ohjaaja"
   :alku (LocalDate/of 2023 9 9)
   :loppu (LocalDate/of 2023 12 15)
   :osa-aikaisuustieto 100
   :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_oppisopimus"
   :oppija-oid "123.456.789"
   :tyyppi "test-tyyppi"
   :tutkinnonosa-id "test-tutkinnonosa-id"
   :keskeytymisajanjaksot
   [{:alku  (LocalDate/of 2023 9 28) :loppu (LocalDate/of 2023 9 29)}]
   :tyopaikalla-jarjestettava-koulutus
   {:vastuullinen-tyopaikka-ohjaaja "Esi Merkki"
    :tyopaikan-nimi "Kohtuu mesta Oy"
    :tyopaikan-y-tunnus "1234567-1"}
   :hankkimistapa-id 2
   :hankkimistapa-tyyppi "koulutussopimus_01"})

(def expected-ddb-jaksot
  [{:osa_aikaisuus 100
    :ohjaaja_nimi "Olli Ohjaaja"
    :opiskeluoikeus_oid "1.2.246.562.15.10000000009"
    :hankkimistapa_tyyppi "oppisopimus"
    :hoks_id 1
    :oppisopimuksen_perusta "01"
    :tyopaikan_nimi "Ohjaus Oy"
    :tyopaikan_ytunnus "5523718-7"
    :jakso_loppupvm "2023-12-05"
    :ohjaaja_puhelinnumero "0401111111"
    :osaamisala "(\"test-osaamisala\")"
    :yksiloiva_tunniste "1"
    :tutkinnonosa_koodi "tutkinnonosat_300268"
    :tpk-niputuspvm "ei_maaritelty"
    :tallennuspvm "2024-06-30"
    :oppilaitos "1.2.246.562.10.12944436166"
    :ohjaaja_ytunnus_kj_tutkinto
    "Olli Ohjaaja/5523718-7/1.2.246.562.10.346830761110/123456"
    :niputuspvm "2024-07-01"
    :tyopaikan_normalisoitu_nimi "ohjaus_oy"
    :toimipiste_oid "1.2.246.562.10.12345678903"
    :tutkinto "123456"
    :alkupvm "2023-12-16"
    :koulutustoimija "1.2.246.562.10.346830761110"
    :jakso_alkupvm "2023-12-01"
    :ohjaaja_email "olli.ohjaaja@esimerkki.com"
    :oppija_oid "1.2.246.562.24.12312312319"
    :rahoituskausi "2023-2024"
    :tutkintonimike "(\"12345\" \"23456\")"
    :viimeinen_vastauspvm "2024-01-14"}
   {:osa_aikaisuus 100
    :ohjaaja_nimi "Olli Ohjaaja"
    :tutkinnonosa_nimi "Testiosa"
    :opiskeluoikeus_oid "1.2.246.562.15.10000000009"
    :hankkimistapa_tyyppi "koulutussopimus"
    :hoks_id 1
    :tyopaikan_nimi "Ohjaus Oy"
    :tyopaikan_ytunnus "5523718-7"
    :jakso_loppupvm "2024-01-06"
    :ohjaaja_puhelinnumero "0401111111"
    :osaamisala "(\"test-osaamisala\")"
    :yksiloiva_tunniste "4"
    :tpk-niputuspvm "ei_maaritelty"
    :tallennuspvm "2024-06-30"
    :oppilaitos "1.2.246.562.10.12944436166"
    :ohjaaja_ytunnus_kj_tutkinto
    "Olli Ohjaaja/5523718-7/1.2.246.562.10.346830761110/123456"
    :niputuspvm "2024-07-01"
    :tyopaikan_normalisoitu_nimi "ohjaus_oy"
    :toimipiste_oid "1.2.246.562.10.12345678903"
    :tutkinto "123456"
    :alkupvm "2024-01-16"
    :koulutustoimija "1.2.246.562.10.346830761110"
    :jakso_alkupvm "2024-01-01"
    :ohjaaja_email "olli.ohjaaja@esimerkki.com"
    :oppija_oid "1.2.246.562.24.12312312319"
    :rahoituskausi "2023-2024"
    :tutkintonimike "(\"12345\" \"23456\")"
    :viimeinen_vastauspvm "2024-02-14"}
   {:osa_aikaisuus 80
    :ohjaaja_nimi "Matti Meikäläinen"
    :opiskeluoikeus_oid "1.2.246.562.15.10000000009"
    :hankkimistapa_tyyppi "koulutussopimus"
    :hoks_id 1
    :tyopaikan_nimi "Ohjaus Oy"
    :tyopaikan_ytunnus "5523718-7"
    :jakso_loppupvm "2023-11-25"
    :ohjaaja_puhelinnumero "0402222222"
    :osaamisala "(\"test-osaamisala\")"
    :yksiloiva_tunniste "3"
    :tutkinnonosa_koodi "tutkinnonosat_300269"
    :tpk-niputuspvm "ei_maaritelty"
    :tallennuspvm "2024-06-30"
    :oppilaitos "1.2.246.562.10.12944436166"
    :ohjaaja_ytunnus_kj_tutkinto
    "Matti Meikäläinen/5523718-7/1.2.246.562.10.346830761110/123456"
    :niputuspvm "2024-07-01"
    :tyopaikan_normalisoitu_nimi "ohjaus_oy"
    :toimipiste_oid "1.2.246.562.10.12345678903"
    :tutkinto "123456"
    :alkupvm "2023-12-01"
    :koulutustoimija "1.2.246.562.10.346830761110"
    :jakso_alkupvm "2023-11-01"
    :ohjaaja_email "matti.meikalainen@esimerkki.com"
    :oppija_oid "1.2.246.562.24.12312312319"
    :rahoituskausi "2023-2024"
    :tutkintonimike "(\"12345\" \"23456\")"
    :viimeinen_vastauspvm "2023-12-30"}
   {:osa_aikaisuus 60
    :ohjaaja_nimi "Matti Meikäläinen"
    :opiskeluoikeus_oid "1.2.246.562.15.10000000009"
    :hankkimistapa_tyyppi "koulutussopimus"
    :hoks_id 1
    :tyopaikan_nimi "Ohjaus Oy"
    :tyopaikan_ytunnus "5523718-7"
    :jakso_loppupvm "2024-01-25"
    :ohjaaja_puhelinnumero "0402222222"
    :osaamisala "(\"test-osaamisala\")"
    :yksiloiva_tunniste "7"
    :tutkinnonosa_koodi "tutkinnonosat_300270"
    :tpk-niputuspvm "ei_maaritelty"
    :tallennuspvm "2024-06-30"
    :oppilaitos "1.2.246.562.10.12944436166"
    :ohjaaja_ytunnus_kj_tutkinto
    "Matti Meikäläinen/5523718-7/1.2.246.562.10.346830761110/123456"
    :niputuspvm "2024-07-01"
    :tyopaikan_normalisoitu_nimi "ohjaus_oy"
    :toimipiste_oid "1.2.246.562.10.12345678903"
    :tutkinto "123456"
    :alkupvm "2024-02-01"
    :koulutustoimija "1.2.246.562.10.346830761110"
    :jakso_alkupvm "2024-01-01"
    :ohjaaja_email "matti.meikalainen@esimerkki.com"
    :oppija_oid "1.2.246.562.24.12312312319"
    :rahoituskausi "2023-2024"
    :tutkintonimike "(\"12345\" \"23456\")"
    :viimeinen_vastauspvm "2024-03-01"}
   {:osa_aikaisuus 80
    :ohjaaja_nimi "Olli Ohjaaja"
    :opiskeluoikeus_oid "1.2.246.562.15.10000000009"
    :hankkimistapa_tyyppi "oppisopimus"
    :hoks_id 1
    :oppisopimuksen_perusta "01"
    :tyopaikan_nimi "Ohjaus Oy"
    :tyopaikan_ytunnus "5523718-7"
    :jakso_loppupvm "2024-04-05"
    :ohjaaja_puhelinnumero "0401111111"
    :osaamisala "(\"test-osaamisala\")"
    :yksiloiva_tunniste "9"
    :tutkinnonosa_koodi "tutkinnonosat_300271"
    :tpk-niputuspvm "ei_maaritelty"
    :tallennuspvm "2024-06-30"
    :oppilaitos "1.2.246.562.10.12944436166"
    :ohjaaja_ytunnus_kj_tutkinto
    "Olli Ohjaaja/5523718-7/1.2.246.562.10.346830761110/123456"
    :niputuspvm "2024-07-01"
    :tyopaikan_normalisoitu_nimi "ohjaus_oy"
    :toimipiste_oid "1.2.246.562.10.12345678903"
    :tutkinto "123456"
    :alkupvm "2024-04-16"
    :koulutustoimija "1.2.246.562.10.346830761110"
    :jakso_alkupvm "2024-04-01"
    :ohjaaja_email "olli.ohjaaja@esimerkki.com"
    :oppija_oid "1.2.246.562.24.12312312319"
    :rahoituskausi "2023-2024"
    :tutkintonimike "(\"12345\" \"23456\")"
    :viimeinen_vastauspvm "2024-05-15"}])

(def expected-ddb-niput
  [{:tyopaikka                   "Ohjaus Oy"
    :ytunnus                     "5523718-7"
    :ohjaaja                     "Matti Meikäläinen"
    :ohjaaja_ytunnus_kj_tutkinto (str "Matti Meikäläinen/5523718-7/"
                                      "1.2.246.562.10.346830761110/123456")
    :niputuspvm                  "2024-07-01"
    :sms_kasittelytila           "ei_lahetetty"
    :tutkinto                    "123456"
    :koulutuksenjarjestaja       "1.2.246.562.10.346830761110"
    :kasittelytila               "ei_niputettu"}
   {:tyopaikka                   "Ohjaus Oy"
    :ytunnus                     "5523718-7"
    :ohjaaja                     "Olli Ohjaaja"
    :ohjaaja_ytunnus_kj_tutkinto (str "Olli Ohjaaja/5523718-7/"
                                      "1.2.246.562.10.346830761110/123456")
    :niputuspvm                  "2024-07-01"
    :sms_kasittelytila           "ei_lahetetty"
    :tutkinto                    "123456"
    :koulutuksenjarjestaja       "1.2.246.562.10.346830761110"
    :kasittelytila               "ei_niputettu"}])

(deftest test-next-niputus-date
  (testing "The function returns the correct niputus date when given `pvm-str`."
    (are [pvm-str expected]
         (= (palaute/next-niputus-date (LocalDate/parse pvm-str))
            (LocalDate/parse expected))
      "2021-12-03" "2021-12-16"
      "2021-12-27" "2022-01-01"
      "2021-04-25" "2021-05-01"
      "2022-06-24" "2022-07-01")))

(deftest test-initial-state-and-reason
  (testing "On HOKS creation or update"
    (with-redefs [date/now #(LocalDate/of 2023 7 1)]
      (testing "don't initiate kysely if"
        (testing "there is already a handled herate for tyopaikkajakso."
          (is (= (palaute/initial-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-1
                    :jakso          test-jakso
                    :existing-palaute {:yksiloiva-tunniste "asd"
                                       :tila "vastaajatunnus_muodostettu"}
                    ::palaute/type :ohjaajakysely})
                 [nil :yksiloiva-tunniste :jo-lahetetty])))
        (testing "a corresponding heräte exists in herätepalvelu."
          (is (= (palaute/initial-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-1
                    :jakso          test-jakso
                    :existing-ddb-herate (delay {:hankkimistapa_id 12343254})
                    ::palaute/type :ohjaajakysely})
                 [:heratepalvelussa :loppu :heratepalvelun-vastuulla])))
        (testing "the jakso has been deleted from HOKS"
          (is (= (palaute/initial-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-5
                    :jakso nil
                    ::palaute/type :ohjaajakysely})
                 [nil :osaamisen-hankkimistapa :poistunut])))
        (testing "opiskeluoikeus is in terminal state."
          (is (= (palaute/initial-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-5
                    :jakso test-jakso
                    ::palaute/type :ohjaajakysely})
                 [:ei-laheteta :opiskeluoikeus-oid :opiskelu-paattynyt])))
        (testing "osa-aikaisuus is missing from työpaikkajakso"
          (is (= (palaute/initial-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-1
                    :jakso (dissoc test-jakso :osa-aikaisuustieto)
                    ::palaute/type :ohjaajakysely})
                 [:ei-laheteta :osa-aikaisuustieto :ei-ole])))
        (testing "workplace information is missing from työpaikkajakso"
          (is (= (palaute/initial-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-1
                    :jakso (update test-jakso
                                   :tyopaikalla-jarjestettava-koulutus
                                   dissoc
                                   :tyopaikan-y-tunnus)
                    ::palaute/type :ohjaajakysely})
                 [:ei-laheteta :tyopaikalla-jarjestettava-koulutus
                  :puuttuva-yhteystieto])))
        (testing "työpaikkajakso is interrupted on its end date"
          (is (= (palaute/initial-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-1
                    :jakso (assoc-in test-jakso
                                     [:keskeytymisajanjaksot 1]
                                     {:alku  (LocalDate/of 2023 12 1)
                                      :loppu (LocalDate/of 2023 12 15)})
                    ::palaute/type :ohjaajakysely})
                 [:ei-laheteta :keskeytymisajanjaksot :jakso-keskeytynyt])))
        (testing "opiskeluoikeus doesn't have any ammatillinen suoritus"
          (is (= (palaute/initial-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-2
                    :jakso test-jakso
                    ::palaute/type :ohjaajakysely})
                 [:ei-laheteta :opiskeluoikeus-oid :ei-ammatillinen])))
        (testing "there is a feedback preventing code in opiskeluoikeusjakso."
          (is (= (palaute/initial-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-4
                    :jakso test-jakso
                    ::palaute/type :ohjaajakysely})
                 [:ei-laheteta :opiskeluoikeus-oid :ulkoisesti-rahoitettu])))
        (testing "HOKS is a TUVA-HOKS or a HOKS related to TUVA-HOKS."
          (doseq [test-hoks [(assoc hoks-test/hoks-1
                                    :hankittavat-koulutuksen-osat
                                    ["koulutuksen-osa"])
                             (assoc hoks-test/hoks-1
                                    :tuva-opiskeluoikeus-oid
                                    "1.2.246.562.15.88406700034")]]
            (is (= (palaute/initial-state-and-reason
                     {:hoks           test-hoks
                      :opiskeluoikeus oo-test/opiskeluoikeus-1
                      :jakso test-jakso
                      ::palaute/type :ohjaajakysely})
                   [:ei-laheteta
                    :tuva-opiskeluoikeus-oid
                    :tuva-opiskeluoikeus]))))
        (testing "opiskeluoikeus is TUVA related."
          (is (= (palaute/initial-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus (assoc-in oo-test/opiskeluoikeus-1
                                              [:tyyppi :koodiarvo] "tuva")
                    :jakso test-jakso
                    ::palaute/type :ohjaajakysely})
                 [:ei-laheteta :opiskeluoikeus-oid :tuva-opiskeluoikeus])))
        (testing "opiskeluoikeus is linked to another opiskeluoikeus"
          (is (= (palaute/initial-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-3
                    :jakso test-jakso
                    ::palaute/type :ohjaajakysely})
                 [:ei-laheteta :opiskeluoikeus-oid :liittyva-opiskeluoikeus]))))
      (testing "initiate kysely if when all of the checks are OK."
        (is (= (palaute/initial-state-and-reason
                 {:hoks           hoks-test/hoks-1
                  :opiskeluoikeus oo-test/opiskeluoikeus-1
                  :jakso test-jakso
                  ::palaute/type :ohjaajakysely})
               [:odottaa-kasittelya :loppu :hoks-tallennettu]))))))

(defn- build-expected-herate
  [jakso hoks]
  (let [heratepvm (:loppu jakso)
        voimassa-alkupvm (palaute/next-niputus-date heratepvm)]
    {:tila                           "odottaa_kasittelya"
     :kyselytyyppi                   "tyopaikkajakson_suorittaneet"
     :hoks-id                        (:id hoks)
     :jakson-yksiloiva-tunniste      (:yksiloiva-tunniste jakso)
     :heratepvm                      heratepvm
     :suorituskieli                  "fi"
     :tutkintotunnus                 351407
     :tutkintonimike                 "(\"12345\",\"23456\")"
     :voimassa-alkupvm               voimassa-alkupvm
     :voimassa-loppupvm              (palaute/vastaamisajan-loppupvm
                                       heratepvm voimassa-alkupvm)
     :koulutustoimija                "1.2.246.562.10.346830761110"
     :toimipiste-oid                 "1.2.246.562.10.12312312312"
     :herate-source                  "ehoks_update"}))

(deftest test-initiate-if-needed!
  (with-redefs [date/now (constantly (LocalDate/of 2023 10 18))
                organisaatio/get-organisaatio!
                organisaatio-test/mock-get-organisaatio!]
    (db-hoks/insert-hoks!
      {:id                 (:id hoks-test/hoks-1)
       :oppija-oid         (:oppija-oid hoks-test/hoks-1)
       :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks-test/hoks-1)})
    (testing "Testing that function `initiate!`"
      (testing (str "stores kysely info to `palautteet` DB table and "
                    "tapahtuma info to `palaute_tapahtumat` table.")
        (palaute/initiate-if-needed!
          {:hoks            hoks-test/hoks-1
           :jakso           test-jakso
           :opiskeluoikeus  oo-test/opiskeluoikeus-1
           ::tapahtuma/type :hoks-tallennus}
          :ohjaajakysely)
        (let [real (-> (palaute/get-by-hoks-id-and-yksiloiva-tunniste!
                         db/spec
                         {:hoks-id            (:id hoks-test/hoks-1)
                          :yksiloiva-tunniste (:yksiloiva-tunniste test-jakso)})
                       (dissoc :id :created-at :updated-at)
                       (->> (remove-vals nil?)))
              expected (build-expected-herate test-jakso hoks-test/hoks-1)]
          (is (= real expected)
              ["diff: " (clojure.data/diff real expected)]))
        (let [tapahtumat (tapahtuma/get-all-by-hoks-id-and-kyselytyypit!
                           db/spec
                           {:hoks-id      (:id hoks-test/hoks-1)
                            :kyselytyypit ["tyopaikkajakson_suorittaneet"]})]
          (is (= (count tapahtumat) 1))
          (is (= (dissoc (first tapahtumat) :id :created-at :updated-at)
                 {:palaute-id   1
                  :kyselytyyppi "tyopaikkajakson_suorittaneet"
                  :vanha-tila   "odottaa_kasittelya"
                  :uusi-tila    "odottaa_kasittelya"
                  :heratepvm    (LocalDate/of 2023 12 15)
                  :tyyppi       "hoks_tallennus"
                  :syy          "hoks_tallennettu"
                  :lisatiedot   {:request-id nil
                                 :loppu "2023-12-15"}}))))
      (testing "doesn't initiate if it is found in herätepalvelu"
        (let [existing-palaute
              (palaute/get-by-hoks-id-and-yksiloiva-tunniste!
                db/spec
                {:hoks-id (:id hoks-test/hoks-1)
                 :yksiloiva-tunniste (:yksiloiva-tunniste test-jakso)})
              ctx {:hoks           hoks-test/hoks-1
                   :existing-palaute (assoc existing-palaute
                                            :hankkimistapa-id 123)
                   :vastaamisajan-alkupvm (LocalDate/of 2020 5 13)
                   :jakso          test-jakso
                   :opiskeluoikeus oo-test/opiskeluoikeus-1
                   ::tapahtuma/type :hoks-tallennus}]
          (heratepalvelu/sync-jakso!
            (tep/build-jaksoherate-record-for-heratepalvelu ctx))
          (palaute/initiate-if-needed!
            {:hoks            hoks-test/hoks-1
             :jakso           test-jakso
             :opiskeluoikeus  oo-test/opiskeluoikeus-1
             ::tapahtuma/type :hoks-tallennus}
            :ohjaajakysely)
          (is (= (map (juxt :vanha-tila :uusi-tila)
                      (tapahtuma/get-all-by-hoks-id-and-kyselytyypit!
                        db/spec
                        {:hoks-id      (:id hoks-test/hoks-1)
                         :kyselytyypit ["tyopaikkajakson_suorittaneet"]}))
                 [["odottaa_kasittelya" "odottaa_kasittelya"]
                  ["odottaa_kasittelya" "heratepalvelussa"]]))
          (palaute/initiate-if-needed!
            {:hoks            hoks-test/hoks-1
             :jakso           test-jakso
             :opiskeluoikeus  oo-test/opiskeluoikeus-1
             ::tapahtuma/type :hoks-tallennus}
            :ohjaajakysely)
          (is (= (map (juxt :vanha-tila :uusi-tila :syy)
                      (tapahtuma/get-all-by-hoks-id-and-kyselytyypit!
                        db/spec
                        {:hoks-id      (:id hoks-test/hoks-1)
                         :kyselytyypit ["tyopaikkajakson_suorittaneet"]}))
                 [["odottaa_kasittelya" "odottaa_kasittelya" "hoks_tallennettu"]
                  ["odottaa_kasittelya" "heratepalvelussa"
                   "heratepalvelun_vastuulla"]
                  ["heratepalvelussa" "heratepalvelussa" "jo_lahetetty"]]))))
      (testing "doesn't initiate if it has already been initiated"
        (with-log
          (let [existing
                (palaute/get-by-hoks-id-and-yksiloiva-tunniste!
                  db/spec
                  {:hoks-id (:id hoks-test/hoks-1)
                   :yksiloiva-tunniste (:yksiloiva-tunniste test-jakso)})]
            (palaute/update-tila! {:existing-palaute existing
                                   ::tapahtuma/type :arvo-luonti}
                                  "lahetetty" :arvo-kutsu-onnistui {})
            (palaute/initiate-if-needed!
              {:hoks            hoks-test/hoks-1
               :jakso           test-jakso
               :opiskeluoikeus  oo-test/opiskeluoikeus-1
               ::tapahtuma/type :hoks-tallennus}
              :ohjaajakysely)
            (is (= (map (juxt :vanha-tila :uusi-tila :syy)
                        (tapahtuma/get-all-by-hoks-id-and-kyselytyypit!
                          db/spec
                          {:hoks-id      (:id hoks-test/hoks-1)
                           :kyselytyypit ["tyopaikkajakson_suorittaneet"]}))
                   [["odottaa_kasittelya" "odottaa_kasittelya"
                     "hoks_tallennettu"]
                    ["odottaa_kasittelya" "heratepalvelussa"
                     "heratepalvelun_vastuulla"]
                    ["heratepalvelussa" "heratepalvelussa" "jo_lahetetty"]
                    ["heratepalvelussa" "lahetetty" "arvo_kutsu_onnistui"]
                    ["lahetetty" "lahetetty" "jo_lahetetty"]]))
            (is (logged? 'oph.ehoks.palaute
                         :info
                         #":jo-lahetetty"))))))))

(defn clear-ddb-jakso-table! []
  (doseq [jakso (far/scan @ddb/faraday-opts @(ddb/tables :jakso) {})]
    (far/delete-item @ddb/faraday-opts @(ddb/tables :jakso)
                     {:hankkimistapa_id (:hankkimistapa_id jakso)})))

(defn clear-ddb-tpo-nippu-table! []
  (doseq [tpo-nippu (far/scan @ddb/faraday-opts @(ddb/tables :nippu) {})]
    (far/delete-item @ddb/faraday-opts @(ddb/tables :nippu)
                     {:ohjaaja_ytunnus_kj_tutkinto (:ohjaaja_ytunnus_kj_tutkinto
                                                     tpo-nippu)
                      :niputuspvm                  (:niputuspvm tpo-nippu)})))

(def required-jakso-keys
  #{:ohjaaja_nimi
    :opiskeluoikeus_oid
    :oppija_oid
    :hoks_id
    :hankkimistapa_tyyppi
    :yksiloiva_tunniste
    :tyopaikan_nimi
    :tyopaikan_ytunnus
    :jakso_loppupvm
    :ohjaaja_puhelinnumero
    :osaamisala
    :osa_aikaisuus
    :tpk-niputuspvm
    :tallennuspvm
    :oppilaitos
    :tunnus
    :ohjaaja_ytunnus_kj_tutkinto
    :niputuspvm
    :tyopaikan_normalisoitu_nimi
    :toimipiste_oid
    :tutkinto
    :alkupvm
    :koulutustoimija
    :jakso_alkupvm
    :ohjaaja_email
    :hankkimistapa_id
    :rahoituskausi
    :tutkintonimike
    :viimeinen_vastauspvm
    :request_id})

(def optional-jakso-keys
  #{:tutkinnonosa_koodi
    :tutkinnonosa_nimi
    :tutkinnonosa_id
    :kasittelytila
    :tutkinnonosa_tyyppi
    :oppisopimuksen_perusta})

(deftest test-handle-tep-palautteet-on-heratepvm!
  (clear-ddb-jakso-table!)
  (clear-ddb-tpo-nippu-table!)
  (testing (str "create-and-save-arvo-vastaajatunnus-for-all-needed! "
                "calls Arvo, saves vastaajatunnus to db, "
                "and syncs both palaute and TPO-nippu to heratepalvelu")
    (with-redefs [organisaatio/get-organisaatio!
                  hoks-utils/mock-get-organisaatio!
                  koski/get-opiskeluoikeus-info-raw
                  hoks-utils/mock-get-opiskeluoikeus!
                  ;; FIXME: better to have real Arvo fake to test against
                  arvo/create-jaksotunnus! hoks-utils/mock-create-jaksotunnus
                  date/now #(LocalDate/of 2024 6 30)]
      (is (= (:status (hoks-utils/create-hoks-in-the-past!)) 200))
      (vt/handle-tep-palautteet-on-heratepvm! {})
      (let [palautteet (hoks-utils/palautteet-joissa-vastaajatunnus)
            ddb-jaksot (far/scan @ddb/faraday-opts @(ddb/tables :jakso) {})
            ddb-niput  (far/scan @ddb/faraday-opts @(ddb/tables :nippu) {})
            tapahtumat (db-helpers/query
                         [(str "select * from palaute_tapahtumat "
                               "where uusi_tila = "
                               "'vastaajatunnus_muodostettu'")])]
        (is (= (count palautteet) 5))
        (is (= (count ddb-jaksot) 5))
        (test-utils/eq
          (sort-by :yksiloiva_tunniste
                   (map #(dissoc % :tunnus :request_id :hankkimistapa_id)
                        ddb-jaksot))
          (sort-by :yksiloiva_tunniste expected-ddb-jaksot))
        (is (= ddb-niput expected-ddb-niput))
        (is (= (count tapahtumat) 5))
        (is (= (set (map :arvo_tunniste palautteet))
               (set (map :tunnus ddb-jaksot))))
        (doseq [jakso ddb-jaksot]
          (is (every? some? (map #(get jakso %) required-jakso-keys))
              (map #(vector % (get jakso %)) required-jakso-keys))
          (is (empty? (s/difference
                        (set (keys jakso))
                        (set (concat required-jakso-keys
                                     optional-jakso-keys))))))))))

(deftest test-handle-palaute-waiting-for-vastaajatunnus!-error-handling
  (clear-ddb-jakso-table!)
  (clear-ddb-tpo-nippu-table!)
  ; Test initialization
  (is (-> #(assoc-in % [:hankittavat-ammat-tutkinnon-osat 0
                        :osaamisen-hankkimistavat 0
                        :keskeytymisajanjaksot]
                     [{:alku  (LocalDate/of 2023 11 1)
                       :loppu (LocalDate/of 2023 11 16)}
                      {:alku  (LocalDate/of 2023 11 20)}])
          (hoks-utils/create-hoks-in-the-past!)
          :status
          (= 200)))
  ;; to ensure that the keskeytynyt jakso is not already marked non-handleable
  (db-helpers/query
    ["UPDATE palautteet SET tila='odottaa_kasittelya'
     WHERE jakson_yksiloiva_tunniste='1' RETURNING *"])

  (let [initial-palautteet (hoks-utils/palautteet)
        palautteet
        (->> {:kyselytyypit ["tyopaikkajakson_suorittaneet"]
              :hoks-id nil :palaute-id nil}
             (palaute/get-palautteet-waiting-for-vastaajatunnus! db/spec))
        tep-palaute (find-first
                      #(= (:jakson-yksiloiva-tunniste %) "4") palautteet)
        kesk-palaute (find-first
                       #(= (:jakson-yksiloiva-tunniste %) "1") palautteet)
        create-jaksotunnus-counter (atom 0)
        arvo-tunnukset (atom [])
        check-current-state-is-same-as-initial-state
        (fn []
          (is (= (hoks-utils/palautteet) initial-palautteet))
          (is (= (ddb/jaksot) []))
          (is (= (ddb/niput) []))
          (is (= @arvo-tunnukset [])))]
    (with-redefs [organisaatio/get-organisaatio!
                  hoks-utils/mock-get-organisaatio!
                  vt/get-hoks-by-id!
                  hoks/get-by-id
                  koski/get-opiskeluoikeus-info-raw
                  hoks-utils/mock-get-opiskeluoikeus!
                  arvo/create-jaksotunnus!
                  (fn [_] (let [tunnus (str (UUID/randomUUID))]
                            (swap! arvo-tunnukset #(conj % tunnus))
                            (swap! create-jaksotunnus-counter inc)
                            {:tunnus tunnus}))
                  arvo/delete-jaksotunnus
                  (fn [tunnus] (swap! arvo-tunnukset #(remove #{tunnus} %)))
                  date/now #(LocalDate/of 2024 6 30)]
      (testing (str "Everything (palaute-backend DB and DynamoDB tables) rolls "
                    "back to its initial state (before the function call) when")
        (testing "Arvo call for vastaajatunnus creation fails."
          (with-redefs [arvo/create-jaksotunnus!
                        (fn [_] (throw (ex-info "Arvo error" {})))]
            (is (nil? (vt/handle-palaute-waiting-for-heratepvm! tep-palaute))))
          (is (contains?
                (->> {:kyselytyypit ["tyopaikkajakson_suorittaneet"]
                      :hoks-id (:hoks-id tep-palaute)}
                     (tapahtuma/get-all-by-hoks-id-and-kyselytyypit! db/spec)
                     (map :syy) (set))
                "arvo_kutsu_epaonnistui"))
          (check-current-state-is-same-as-initial-state))
        (testing "jakso sync to Herätepalvelu fails."
          (with-redefs [heratepalvelu/sync-jakso!*
                        (fn [_] (throw (ex-info "Jakso sync error" {})))]
            (is (nil? (vt/handle-palaute-waiting-for-heratepvm! tep-palaute))))
          (is (contains?
                (->> {:kyselytyypit ["tyopaikkajakson_suorittaneet"]
                      :hoks-id (:hoks-id tep-palaute)}
                     (tapahtuma/get-all-by-hoks-id-and-kyselytyypit! db/spec)
                     (map :syy) (set))
                "heratepalvelu_sync_epaonnistui"))
          (check-current-state-is-same-as-initial-state))
        (testing "nippu sync to Herätepalvelu fails."
          (with-redefs
           [heratepalvelu/sync-tpo-nippu!*
            (fn [_] (throw (ex-info "TPO-nippu sync failed" {})))]
            (is (nil? (vt/handle-palaute-waiting-for-heratepvm! tep-palaute))))
          (check-current-state-is-same-as-initial-state)))
      (let [counter-value-before-fn-call @create-jaksotunnus-counter]
        (testing (str "When getting other than \"404 Not found\" error from "
                      "Koski the function should skip palaute processing. "
                      "No call to Arvo should be made.")
          (with-redefs
           [koski/get-opiskeluoikeus-info-raw
            (fn [oid]
              (throw (ex-info
                       "Koski error"
                       {:status 500
                        :type   ::koski/opiskeluoikeus-fetching-error})))]
            (is (nil? (vt/handle-palaute-waiting-for-heratepvm! tep-palaute))))
          (is (= counter-value-before-fn-call @create-jaksotunnus-counter))
          (check-current-state-is-same-as-initial-state))
        (testing (str "When opiskeluoikeus for palaute is not found from from "
                      "Koski, `tila` for it should be marked as `ei_laheteta`. "
                      "No call to Arvo should be made.")
          (with-redefs [koski/get-opiskeluoikeus! (fn [_] nil)]
            (vt/handle-palaute-waiting-for-heratepvm! tep-palaute)
            (is (= counter-value-before-fn-call @create-jaksotunnus-counter))
            (is (= (:tila (palaute/get-by-id! db/spec {:id (:id tep-palaute)}))
                   "ei_laheteta"))))
        (testing (str "If jakso has disappeared before checks, "
                      "it should be marked as `ei_laheteta`.")
          (db-helpers/query ["UPDATE osaamisen_hankkimistavat
                             SET deleted_at=now()
                             WHERE yksiloiva_tunniste='4' RETURNING *"])
          (db-helpers/query ["UPDATE palautteet
                             SET tila='odottaa_kasittelya'
                             WHERE jakson_yksiloiva_tunniste='4' RETURNING *"])
          (vt/handle-palaute-waiting-for-heratepvm! tep-palaute)
          (is (= counter-value-before-fn-call @create-jaksotunnus-counter))
          (is (= (:tila (palaute/get-by-id! db/spec {:id (:id tep-palaute)}))
                 "ei_laheteta")))
        (testing (str "Palaute should be marked as \"ei_laheteta\" when there "
                      "are one or more open keskeytymisajanjakso. No call to "
                      "Arvo should be made.")
          (vt/handle-palaute-waiting-for-heratepvm! kesk-palaute)
          (is (= counter-value-before-fn-call @create-jaksotunnus-counter))
          (is (= (:tila (palaute/get-by-id! db/spec {:id (:id kesk-palaute)}))
                 "ei_laheteta")))))))
