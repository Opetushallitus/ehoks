(ns oph.ehoks.palaute.opiskelija-test
  (:require [clojure.test :refer [are deftest is testing use-fixtures]]
            [clojure.tools.logging.test :refer [logged? with-log]]
            [medley.core :refer [remove-vals]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.koski-test :as koski-test]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.external.organisaatio-test :as organisaatio-test]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.oppijaindex-test :as oppijaindex-test]
            [oph.ehoks.palaute.opiskelija :as opiskelijapalaute]
            [oph.ehoks.test-utils :as test-utils]
            [oph.ehoks.utils.date :as date])
  (:import (java.time LocalDate)))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-database-after-test)

(def test-hoks
  {:id                          12345
   :oppija-oid                  "1.2.246.562.24.12312312319"
   :opiskeluoikeus-oid          "1.2.246.562.15.10000000009"
   :osaamisen-hankkimisen-tarve true
   :ensikertainen-hyvaksyminen  (LocalDate/of 2023 04 16)
   :osaamisen-saavuttamisen-pvm (LocalDate/of 2024 02 05)
   :sahkoposti                  "testi.testaaja@testidomain.testi"
   :puhelinnumero               "0123456789"})

(def test-hoks*
  (dissoc test-hoks :osaamisen-hankkimisen-tarve :osaamisen-saavuttamisen-pvm))

(def opiskeluoikeus-1
  "Opiskeluoikeus with ammatillinen suoritus"
  {:suoritukset     [{:tyyppi          {:koodiarvo "ammatillinentutkinto"}
                      :suorituskieli   {:koodiarvo "fi"}
                      :tutkintonimike  [{:koodiarvo "12345"}
                                        {:koodiarvo "23456"}]
                      :toimipiste      {:oid "1.2.246.562.10.12312312312"}
                      :koulutusmoduuli {:tunniste {:koodiarvo 351407}}}]
   :tyyppi          {:koodiarvo "ammatillinenkoulutus"}
   :koulutustoimija {:oid "1.2.246.562.10.346830761110"}})

(def opiskeluoikeus-2
  "Opiskeluoikeus without ammatillinen suoritus"
  {:suoritukset [{:tyyppi {:koodiarvo "joku_muu"}}]
   :tyyppi      {:koodiarvo "ammatillinenkoulutus"}})

(def opiskeluoikeus-3
  "Opiskeluoikeus that is linked to another opiskeluoikeus"
  {:suoritukset               [{:tyyppi {:koodiarvo "ammatillinentutkinto"}}]
   :tyyppi                    {:koodiarvo "ammatillinenkoulutus"}
   :sisältyyOpiskeluoikeuteen {:oid "1.2.246.562.15.10000000009"}})

(def opiskeluoikeus-4
  "Opiskeluoikeus that has opintojenRahoitus koodi preventing tyoelamapalaute
  initialization."
  {:tila {:opiskeluoikeusjaksot [{:tila {:koodiarvo "lasna"}
                                  :opintojenRahoitus {:koodiarvo "14"}
                                  :alku "2023-08-10"
                                  :loppu "2023-12-31"}
                                 {:tila {:koodiarvo "lasna"}
                                  :opintojenRahoitus {:koodiarvo "1"}
                                  :alku "2024-01-01"}]}
   :suoritukset     [{:tyyppi          {:koodiarvo "ammatillinentutkinto"}
                      :suorituskieli   {:koodiarvo "fi"}
                      :tutkintonimike  [{:koodiarvo "12345"}
                                        {:koodiarvo "23456"}]
                      :toimipiste      {:oid "1.2.246.562.10.12312312312"}
                      :koulutusmoduuli {:tunniste {:koodiarvo 351407}}}]
   :tyyppi          {:koodiarvo "ammatillinenkoulutus"}
   :koulutustoimija {:oid "1.2.246.562.10.346830761110"}})

(def opiskeluoikeus-5
  "Opiskeluoikeus that is in terminal state."
  {:tila {:opiskeluoikeusjaksot [{:tila {:koodiarvo "lasna"}
                                  :alku "2022-08-10"
                                  :loppu "2022-12-31"}
                                 {:tila {:koodiarvo "valiaikaisestikeskeytynyt"}
                                  :alku "2023-01-01"}]}
   :suoritukset     [{:tyyppi          {:koodiarvo "ammatillinentutkinto"}
                      :suorituskieli   {:koodiarvo "fi"}
                      :tutkintonimike  [{:koodiarvo "12345"}
                                        {:koodiarvo "23456"}]
                      :toimipiste      {:oid "1.2.246.562.10.12312312312"}
                      :koulutusmoduuli {:tunniste {:koodiarvo 351407}}}]
   :tyyppi          {:koodiarvo "ammatillinenkoulutus"}
   :koulutustoimija {:oid "1.2.246.562.10.346830761110"}})

(def sqs-msg (atom nil))

(deftest test-initiate?
  (with-redefs [date/now (constantly (LocalDate/of 2023 1 1))]
    (testing "On HOKS creation or update"
      (letfn [(test-not-initiated
                ([hoks opiskeluoikeus log-msg]
                  (test-not-initiated nil hoks opiskeluoikeus log-msg))
                ([kysely hoks opiskeluoikeus log-msg]
                  (doseq [kysely    (if kysely
                                      [kysely]
                                      [:aloituskysely :paattokysely])
                          prev-hoks [nil test-hoks*]]
                    (with-log
                      (is (not (opiskelijapalaute/initiate?
                                 kysely prev-hoks hoks opiskeluoikeus)))
                      (is (logged? 'oph.ehoks.palaute.opiskelija
                                   :info
                                   log-msg))))))]
        (testing "don't initiate kysely if"
          (testing "`osaamisen-hankkimisen-tarve` is missing or is `false`."
            (doseq [hoks (map #(assoc test-hoks
                                      :osaamisen-hankkimisen-tarve %)
                              [false nil])]
              (test-not-initiated
                hoks opiskeluoikeus-1 #"`osaamisen-hankkimisen-tarve` not")))

          (testing "there are no ammatillinen suoritus in opiskeluoikeus"
            (test-not-initiated
              test-hoks opiskeluoikeus-2 #"No ammatillinen suoritus"))

          (testing "heratepvm is invalid"
            (doseq [pvm ["2023-07-01" "2023-09-04" "2024-07-01"]]
              (with-redefs [date/now (constantly (LocalDate/parse pvm))]
                (test-not-initiated
                  :aloituskysely test-hoks opiskeluoikeus-1 #"Herate date")))

            (doseq [pvm ["2024-07-01" "2024-12-31" "2025-01-06"]]
              (with-redefs [date/now (constantly (LocalDate/parse pvm))]
                (test-not-initiated
                  :paattokysely test-hoks opiskeluoikeus-1 #"Herate date"))))

          (testing "HOKS is a TUVA-HOKS or a HOKS related to TUVA-HOKS."
            (doseq [hoks [(assoc test-hoks
                                 :hankittavat-koulutuksen-osat
                                 ["koulutuksen-osa"])
                          (assoc test-hoks
                                 :tuva-opiskeluoikeus-oid
                                 "1.2.246.562.15.88406700034")]]
              (test-not-initiated
                hoks opiskeluoikeus-1 #"HOKS is either TUVA-HOKS")))

          (testing "opiskeluoikeus is linked to another opiskeluoikeus"
            (test-not-initiated
              test-hoks opiskeluoikeus-3 #"linked to another")))

        (testing (str "don't initiate aloituskysely if "
                      "`ensikertainen-hyvaksyminen` is missing.")
          (let [hoks (dissoc test-hoks :ensikertainen-hyvaksyminen)]
            (test-not-initiated
              :aloituskysely hoks opiskeluoikeus-1 #"nen` has not")))

        (testing (str "don't initiate päättökysely if "
                      "`osaamisen-saavuttamisen-pvm` is missing.")
          (let [hoks (dissoc test-hoks :osaamisen-saavuttamisen-pvm)]
            (test-not-initiated
              :paattokysely hoks opiskeluoikeus-1 #"-pvm` has not")))))

    (testing "On HOKS creation"
      (testing
       "initiate aloituskysely if `osaamisen-hankkimisen-tarve` is `true`."
        (is (opiskelijapalaute/initiate?
              :aloituskysely test-hoks opiskeluoikeus-1)))

      (testing
       (str "initiate paattokysely if `osaamisen-hankkimisen-tarve` is "
            "`true` and `osaamisen-saavuttamisen-pvm` is not missing.")
        (is (opiskelijapalaute/initiate?
              :paattokysely test-hoks opiskeluoikeus-1))))

    (testing "On HOKS update"
      (testing (str "initiate aloituskysely if `osaamisen-hankkimisen-tarve` "
                    "is added to HOKS.")
        (is (opiskelijapalaute/initiate?
              :aloituskysely test-hoks* test-hoks opiskeluoikeus-1)))

      (testing (str "initiate aloituskysely if `sahkoposti` is added to HOKS "
                    "and `osaamisen-hankkimisen-tarve` is `true`.")
        (is (opiskelijapalaute/initiate?
              :aloituskysely
              (dissoc test-hoks :sahkoposti)
              test-hoks
              opiskeluoikeus-1)))

      (testing (str "initiate aloituskysely if `puhelinnumero` is added to "
                    "HOKS and `osaamisen-hankkimisen-tarve` is `true`.")
        (is (opiskelijapalaute/initiate?
              :aloituskysely
              (dissoc test-hoks :puhelinnumero)
              test-hoks
              opiskeluoikeus-1)))

      (testing
       (str "initiate päättökysely if `osaamisen-saavuttamisen-pvm` is "
            "added to HOKS.")
        (is (opiskelijapalaute/initiate?
              :paattokysely
              (dissoc test-hoks :osaamisen-saavuttamisen-pvm)
              test-hoks
              opiskeluoikeus-1)))

      (testing "don't initiate aloituskysely if"
        (testing "`sahkoposti` stays unchanged, is changed or is removed."
          (are [old-val new-val]
               (not (opiskelijapalaute/initiate?
                      :aloituskysely
                      {:osaamisen-hankkimisen-tarve true :sahkoposti old-val}
                      {:osaamisen-hankkimisen-tarve true :sahkoposti new-val}
                      opiskeluoikeus-1))
            "testi.testaaja@testidomain.testi"
            "testi.testaaja@testidomain.testi"
            "testi.testaaja@testidomain.testi" "testi.testinen@testi.domain"
            "testi.testaaja@testidomain.testi" nil))

        (testing "`puhelinnumero` stays unchanged, is changed or is removed."
          (are [old-val new-val]
               (not (opiskelijapalaute/initiate?
                      :aloituskysely
                      {:osaamisen-hankkimisen-tarve true
                       :puhelinnumero old-val}
                      {:osaamisen-hankkimisen-tarve true
                       :puhelinnumero new-val}
                      opiskeluoikeus-1))
            "0123456789" "0123456789"
            "0123456789" "0011223344"
            "0123456789" nil)))

      (testing
       (str "don't initiate päättökysely if `osaamisen-saavuttamisen-pvm`"
            " stays unchanged, is changed or is removed.")
        (are [old-val new-val]
             (not (opiskelijapalaute/initiate?
                    :paattokysely
                    {:osaamisen-hankkimisen-tarve true
                     :osaamisen-saavuttamisen-pvm (LocalDate/parse old-val)}
                    {:osaamisen-hankkimisen-tarve true
                     :osaamisen-saavuttamisen-pvm
                     (when new-val (LocalDate/parse new-val))}
                    opiskeluoikeus-1))
          "2023-09-01" "2023-09-01"
          "2023-09-01" "2023-09-02"
          "2023-09-01" nil)))))

(defn expected-msg
  [kysely hoks]
  {:ehoks-id           (:id hoks)
   :kyselytyyppi       (case kysely
                         :aloituskysely "aloittaneet"
                         :paattokysely  "tutkinnon_suorittaneet")
   :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
   :oppija-oid         (:oppija-oid hoks)
   :sahkoposti         (:sahkoposti hoks)
   :puhelinnumero      (:puhelinnumero hoks)
   :alkupvm
   (str (case kysely
          :aloituskysely (:ensikertainen-hyvaksyminen hoks)
          :paattokysely  (:osaamisen-saavuttamisen-pvm hoks)))})

(deftest test-initiate!
  (with-redefs [sqs/send-amis-palaute-message (fn [msg] (reset! sqs-msg msg))
                date/now (constantly (LocalDate/of 2023 4 18))
                oppijaindex/get-hankintakoulutus-oids-by-master-oid
                oppijaindex-test/mock-get-hankintakoulutus-oids-by-master-oid
                koski/get-opiskeluoikeus-info-raw
                koski-test/mock-get-opiskeluoikeus-raw
                organisaatio/get-organisaatio!
                organisaatio-test/mock-get-organisaatio!]
    (db-hoks/insert-hoks! {:id                 (:id test-hoks)
                           :oppija-oid         (:oppija-oid test-hoks)
                           :opiskeluoikeus-oid (:opiskeluoikeus-oid test-hoks)})
    (testing "Testing that function `initiate!`"
      (testing (str "stores kysely info to `palautteet` DB table and "
                    "successfully sends aloituskysely and paattokysely "
                    "herate to SQS queue")
        (are [kysely] (= (expected-msg kysely test-hoks)
                         (do (opiskelijapalaute/initiate!
                               kysely test-hoks opiskeluoikeus-1)
                             @sqs-msg))
          :aloituskysely
          :paattokysely)
        (are [kyselytyyppi herate-basis voimassa-alkupvm voimassa-loppupvm]
             (= (-> (opiskelijapalaute/get-by-hoks-id-and-kyselytyypit!
                      db/spec {:hoks-id      (:id test-hoks)
                               :kyselytyypit  [kyselytyyppi]})
                    first
                    (dissoc :id :created-at :updated-at)
                    (->> (remove-vals nil?)))
                {:tila                           "odottaa_kasittelya"
                 :kyselytyyppi                   kyselytyyppi
                 :hoks-id                        12345
                 :heratepvm                      (get test-hoks herate-basis)
                 :koulutustoimija                "1.2.246.562.10.346830761110"
                 :suorituskieli                  "fi"
                 :toimipiste-oid                 "1.2.246.562.10.12312312312"
                 :tutkintotunnus                 351407
                 :tutkintonimike                 "(\"12345\",\"23456\")"
                 :hankintakoulutuksen-toteuttaja "1.2.246.562.10.10000000009"
                 :voimassa-alkupvm               (LocalDate/parse
                                                   voimassa-alkupvm)
                 :voimassa-loppupvm              (LocalDate/parse
                                                   voimassa-loppupvm)
                 :herate-source                  "ehoks_update"})
          "aloittaneet"  :ensikertainen-hyvaksyminen
          "2023-04-18"   "2023-05-17"
          "valmistuneet" :osaamisen-saavuttamisen-pvm
          "2024-02-05"   "2024-03-05"))
      (testing "doesn't initiate kysely if one already exists for HOKS"
        (are [kysely] (nil? (opiskelijapalaute/initiate!
                              kysely test-hoks opiskeluoikeus-1))
          :aloituskysely :paattokysely)))))
