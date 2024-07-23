(ns oph.ehoks.palaute.opiskelija-test
  (:require [clojure.test :refer [are deftest is testing use-fixtures]]
            [medley.core :refer [remove-vals]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.koski-test :as koski-test]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.external.organisaatio-test :as organisaatio-test]
            [oph.ehoks.hoks-test :as hoks-test]
            [oph.ehoks.opiskeluoikeus-test :as oo-test]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.oppijaindex-test :as oppijaindex-test]
            [oph.ehoks.palaute.opiskelija :as op]
            [oph.ehoks.test-utils :as test-utils]
            [oph.ehoks.utils.date :as date])
  (:import (java.time LocalDate)))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-database-after-test)

(def sqs-msg (atom nil))

(defn initiate? [kysely prev-hoks hoks oo]
  (let [state-and-reason
        (op/initial-palaute-state-and-reason kysely prev-hoks hoks oo nil)]
    (= :odottaa-kasittelya (first state-and-reason))))

(defn test-not-initiated
  ([hoks opiskeluoikeus reason]
   (test-not-initiated nil hoks opiskeluoikeus reason))
  ([kysely hoks opiskeluoikeus reason]
   (doseq [kysely    (if kysely
                       [kysely]
                       [:aloituskysely :paattokysely])
           prev-hoks [nil hoks-test/hoks-2]]
     (is (not (initiate? kysely prev-hoks hoks opiskeluoikeus)))
     (is (= reason (get (op/initial-palaute-state-and-reason
                          kysely prev-hoks hoks opiskeluoikeus nil) 2))))))

(deftest test-initial-palaute-state-and-reason
  (with-redefs [date/now (constantly (LocalDate/of 2023 1 1))]
    (testing "On HOKS creation or update"
      (testing "don't initiate kysely if"
        (testing "`osaamisen-hankkimisen-tarve` is missing or is `false`."
          (doseq [hoks (map #(assoc hoks-test/hoks-1
                                    :osaamisen-hankkimisen-tarve %)
                            [false nil])]
            (test-not-initiated hoks
                                oo-test/opiskeluoikeus-1
                                :ei-ole)))

        (testing "there are no ammatillinen suoritus in opiskeluoikeus"
          (test-not-initiated hoks-test/hoks-1
                              oo-test/opiskeluoikeus-2
                              :ei-ammatillinen))

        (testing "heratepvm is invalid"
          (doseq [pvm ["2023-07-01" "2023-09-04" "2024-07-01"]]
            (with-redefs [date/now (constantly (LocalDate/parse pvm))]
              (test-not-initiated :aloituskysely
                                  hoks-test/hoks-1
                                  oo-test/opiskeluoikeus-1
                                  :eri-rahoituskaudella)))

          (doseq [pvm ["2024-07-01" "2024-12-31" "2025-01-06"]]
            (with-redefs [date/now (constantly (LocalDate/parse pvm))]
              (test-not-initiated :paattokysely
                                  hoks-test/hoks-1
                                  oo-test/opiskeluoikeus-1
                                  :eri-rahoituskaudella))))

        (testing "HOKS is a TUVA-HOKS or a HOKS related to TUVA-HOKS."
          (doseq [hoks [(assoc hoks-test/hoks-1
                               :hankittavat-koulutuksen-osat
                               ["koulutuksen-osa"])
                        (assoc hoks-test/hoks-1
                               :tuva-opiskeluoikeus-oid
                               "1.2.246.562.15.88406700034")]]
            (test-not-initiated
              hoks oo-test/opiskeluoikeus-1 :tuva-opiskeluoikeus)))

        (testing "opiskeluoikeus is linked to another opiskeluoikeus"
          (test-not-initiated
            hoks-test/hoks-1 oo-test/opiskeluoikeus-3
            :liittyva-opiskeluoikeus)))

      (testing (str "don't initiate aloituskysely if "
                    "`ensikertainen-hyvaksyminen` is missing.")
        (let [hoks (dissoc hoks-test/hoks-1 :ensikertainen-hyvaksyminen)]
          (test-not-initiated
            :aloituskysely hoks oo-test/opiskeluoikeus-1 :ei-ole)))

      (testing (str "don't initiate päättökysely if "
                    "`osaamisen-saavuttamisen-pvm` is missing.")
        (let [hoks (dissoc hoks-test/hoks-1 :osaamisen-saavuttamisen-pvm)]
          (test-not-initiated
            :paattokysely hoks oo-test/opiskeluoikeus-1 :ei-ole))))

    (testing "On HOKS creation"
      (testing
        "initiate aloituskysely if `osaamisen-hankkimisen-tarve` is `true`."
        (is (initiate?
              :aloituskysely nil hoks-test/hoks-1 oo-test/opiskeluoikeus-1)))

      (testing
        (str "initiate paattokysely if `osaamisen-hankkimisen-tarve` is "
             "`true` and `osaamisen-saavuttamisen-pvm` is not missing.")
        (is (initiate?
              :paattokysely nil hoks-test/hoks-1 oo-test/opiskeluoikeus-1))))

    (testing "On HOKS update"
      (testing (str "initiate aloituskysely if `osaamisen-hankkimisen-tarve` "
                    "is added to HOKS.")
        (is (initiate? :aloituskysely
                          hoks-test/hoks-2
                          hoks-test/hoks-1
                          oo-test/opiskeluoikeus-1)))

      (testing (str "initiate aloituskysely if `sahkoposti` is added to HOKS "
                    "and `osaamisen-hankkimisen-tarve` is `true`.")
        (is (initiate?
              :aloituskysely
              (dissoc hoks-test/hoks-1 :sahkoposti)
              hoks-test/hoks-1
              oo-test/opiskeluoikeus-1)))

      (testing (str "initiate aloituskysely if `puhelinnumero` is added to "
                    "HOKS and `osaamisen-hankkimisen-tarve` is `true`.")
        (is (initiate?
              :aloituskysely
              (dissoc hoks-test/hoks-1 :puhelinnumero)
              hoks-test/hoks-1
              oo-test/opiskeluoikeus-1)))

      (testing
       (str "initiate päättökysely if `osaamisen-saavuttamisen-pvm` is "
            "added to HOKS.")
        (is (initiate?
              :paattokysely
              (dissoc hoks-test/hoks-1 :osaamisen-saavuttamisen-pvm)
              hoks-test/hoks-1
              oo-test/opiskeluoikeus-1)))

      (testing "don't initiate aloituskysely if"
        (testing "`sahkoposti` stays unchanged, is changed or is removed."
          (are [old-val new-val]
               (not (initiate?
                      :aloituskysely
                      {:osaamisen-hankkimisen-tarve true :sahkoposti old-val}
                      {:osaamisen-hankkimisen-tarve true :sahkoposti new-val}
                      oo-test/opiskeluoikeus-1))
            "testi.testaaja@testidomain.testi"
            "testi.testaaja@testidomain.testi"
            "testi.testaaja@testidomain.testi" "testi.testinen@testi.domain"
            "testi.testaaja@testidomain.testi" nil))

        (testing "`puhelinnumero` stays unchanged, is changed or is removed."
          (are [old-val new-val]
               (not (initiate?
                      :aloituskysely
                      {:osaamisen-hankkimisen-tarve true
                       :puhelinnumero old-val}
                      {:osaamisen-hankkimisen-tarve true
                       :puhelinnumero new-val}
                      oo-test/opiskeluoikeus-1))
            "0123456789" "0123456789"
            "0123456789" "0011223344"
            "0123456789" nil))))))

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

(defn already-initiated?!
  [kysely hoks koulutustoimija tx]
  (op/already-initiated?
    kysely hoks
    (op/existing-heratteet!
      kysely hoks koulutustoimija tx)))

(defn mock-get-opiskeluoikeus-info-raw [oo]
  oo-test/opiskeluoikeus-1)

(deftest test-already-initiated?
  (with-redefs [date/now (constantly (LocalDate/of 2023 4 18))
                organisaatio/get-organisaatio!
                organisaatio-test/mock-get-organisaatio!
                koski/get-opiskeluoikeus-info-raw
                mock-get-opiskeluoikeus-info-raw]
    (db-hoks/insert-hoks!
      {:id                  (:id hoks-test/hoks-1)
       :oppija-oid         (:oppija-oid hoks-test/hoks-1)
       :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks-test/hoks-1)})
    (doseq [kysely [:aloituskysely :paattokysely]]
      (op/initiate-if-needed! kysely nil hoks-test/hoks-1))

    (testing
     (str "Kysely is considered already initiated if it is for same "
          "oppija with same koulutustoimija and within same rahoituskausi.")
      (are [kysely] (already-initiated?! kysely
                                         hoks-test/hoks-3
                                         "1.2.246.562.10.346830761110"
                                         db/spec)
        :aloituskysely :paattokysely))

    (testing "Kysely is not considered already initiated when"
      (testing "koulutustoimija differs."
        (are [kysely] (not (already-initiated?!
                             kysely
                             hoks-test/hoks-3
                             "1.2.246.562.10.45678901237"
                             db/spec))
          :aloituskysely :paattokysely))

      (testing "heratepvm is within different rahoituskausi."
        (are [kysely] (not (already-initiated?!
                             kysely
                             hoks-test/hoks-4
                             "1.2.246.562.10.346830761110"
                             db/spec))
          :aloituskysely :paattokysely)))))

(deftest test-initiate-if-needed!
  (with-redefs [sqs/send-amis-palaute-message (fn [msg] (reset! sqs-msg msg))
                date/now (constantly (LocalDate/of 2023 4 18))
                oppijaindex/get-hankintakoulutus-oids-by-master-oid
                oppijaindex-test/mock-get-hankintakoulutus-oids-by-master-oid
                koski/get-opiskeluoikeus-info-raw
                mock-get-opiskeluoikeus-info-raw
                organisaatio/get-organisaatio!
                organisaatio-test/mock-get-organisaatio!]
    (db-hoks/insert-hoks!
      {:id                 (:id hoks-test/hoks-1)
       :oppija-oid         (:oppija-oid hoks-test/hoks-1)
       :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks-test/hoks-1)})
    (testing "Testing that function `initiate!`"
      (testing (str "stores kysely info to `palautteet` DB table and "
                    "successfully sends aloituskysely and paattokysely "
                    "herate to SQS queue")
        (are [kysely] (= (expected-msg kysely hoks-test/hoks-1)
                         (do (op/initiate-if-needed!
                               kysely nil hoks-test/hoks-1)
                             @sqs-msg))
          :aloituskysely
          :paattokysely)
        (are [kyselytyyppi herate-basis voimassa-alkupvm voimassa-loppupvm]
             (= (-> (op/get-by-hoks-id-and-kyselytyypit!
                      db/spec {:hoks-id      (:id hoks-test/hoks-1)
                               :kyselytyypit  [kyselytyyppi]})
                    first
                    (dissoc :id :created-at :updated-at)
                    (->> (remove-vals nil?)))
                {:tila                           "odottaa_kasittelya"
                 :kyselytyyppi                   kyselytyyppi
                 :hoks-id                        12345
                 :heratepvm                      (get hoks-test/hoks-1
                                                      herate-basis)
                 :koulutustoimija                "1.2.246.562.10.346830761110"
                 :suorituskieli                  "fi"
                 :toimipiste-oid                 "1.2.246.562.10.12312312312"
                 :tutkintotunnus                 351407
                 :tutkintonimike                 "(\"12345\",\"23456\")"
                 :hankintakoulutuksen-toteuttaja "1.2.246.562.10.346830761110"
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
        (are [kysely] (nil? (op/initiate-if-needed!
                              kysely nil hoks-test/hoks-1))
          :aloituskysely :paattokysely))
      (testing "sends kysely info to AWS SQS when `:resend?` option is given."
        (are [kysely] (some? (op/initiate-if-needed!
                               kysely nil hoks-test/hoks-1
                               {:resend? true}))
          :aloituskysely :paattokysely)))))
