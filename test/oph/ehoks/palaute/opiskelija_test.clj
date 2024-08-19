(ns oph.ehoks.palaute.opiskelija-test
  (:require [clojure.test :refer [are deftest is testing use-fixtures]]
            [medley.core :refer [remove-vals]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.external.organisaatio-test :as organisaatio-test]
            [oph.ehoks.hoks-test :as hoks-test]
            [oph.ehoks.opiskeluoikeus-test :as oo-test]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.oppijaindex-test :as oppijaindex-test]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.tapahtuma :as palautetapahtuma]
            [oph.ehoks.palaute.opiskelija :as op]
            [oph.ehoks.test-utils :as test-utils]
            [oph.ehoks.utils.date :as date])
  (:import (java.time LocalDate)))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-database-after-test)

(def sqs-msg (atom nil))

(defn test-not-initiated
  ([hoks opiskeluoikeus reason]
    (test-not-initiated nil hoks opiskeluoikeus reason))
  ([kysely hoks opiskeluoikeus reason]
    (doseq [kysely (if kysely
                     [kysely]
                     [:aloituskysely :paattokysely])]
      (let [state-and-reason
            (op/initial-palaute-state-and-reason
              kysely hoks opiskeluoikeus nil)]
        (is (contains? #{:ei-laheteta nil} (first state-and-reason)))
        (is (= (last state-and-reason) reason))))))

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

        (testing "opiskeluoikeus is in terminal state"
          (test-not-initiated
            (assoc hoks-test/hoks-1
                   :ensikertainen-hyvaksyminen (LocalDate/of 2023 9 10)
                   :osaamisen-saavuttamisen-pvm (LocalDate/of 2023 10 10))
            oo-test/opiskeluoikeus-4 :ulkoisesti-rahoitettu))

        (testing "opiskeluoikeus is externally funded"
          (test-not-initiated hoks-test/hoks-1
                              oo-test/opiskeluoikeus-5
                              :opiskelu-paattynyt))

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

        (testing "opiskeluoikeus is TUVA related."
          (test-not-initiated
            hoks-test/hoks-1
            (assoc-in oo-test/opiskeluoikeus-1 [:tyyppi :koodiarvo] "tuva")
            :tuva-opiskeluoikeus))

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
        (is (= (op/initial-palaute-state-and-reason
                 :aloituskysely hoks-test/hoks-1 oo-test/opiskeluoikeus-1)
               [:odottaa-kasittelya nil :hoks-tallennettu])))

      (testing
       (str "initiate paattokysely if `osaamisen-hankkimisen-tarve` is "
            "`true` and `osaamisen-saavuttamisen-pvm` is not missing.")
        (is (= (op/initial-palaute-state-and-reason
                 :paattokysely hoks-test/hoks-1 oo-test/opiskeluoikeus-1)
               [:odottaa-kasittelya nil :hoks-tallennettu]))))))

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

(defn mock-get-opiskeluoikeus-info-raw [oo]
  (if (= oo "1.2.246.562.15.57401181193")
    (throw (ex-info "opiskeluoikeus not found"
                    {:status 404
                     :body (str "[{\"key\": \"notFound.opiskeluoikeuttaEiLöydy"
                                "TaiEiOikeuksia\"}]")}))
    oo-test/opiskeluoikeus-1))

(deftest test-existing-heratteet!
  (with-redefs [date/now (constantly (LocalDate/of 2023 4 18))
                organisaatio/get-organisaatio!
                organisaatio-test/mock-get-organisaatio!
                koski/get-opiskeluoikeus-info-raw
                mock-get-opiskeluoikeus-info-raw]
    (db-hoks/insert-hoks!
      {:id                 (:id hoks-test/hoks-1)
       :oppija-oid         (:oppija-oid hoks-test/hoks-1)
       :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks-test/hoks-1)})
    (doseq [kysely [:aloituskysely :paattokysely]]
      (op/initiate-if-needed! kysely hoks-test/hoks-1))
    (db-ops/query ["UPDATE palautteet SET tila='lahetetty'
                   WHERE hoks_id=? RETURNING *" (:id hoks-test/hoks-1)])

    (testing
     (str "Kysely is considered already initiated if it is for same "
          "oppija with same koulutustoimija and within same rahoituskausi.")
      (are [kysely] (= (:tila (first (op/existing-heratteet!
                                       kysely
                                       hoks-test/hoks-3
                                       "1.2.246.562.10.346830761110"
                                       db/spec)))
                       "lahetetty")
        :aloituskysely :paattokysely))

    (testing "Kysely is not considered already initiated when"
      (testing "koulutustoimija differs."
        (are [kysely] (empty? (op/existing-heratteet!
                                kysely
                                hoks-test/hoks-3
                                "1.2.246.562.10.45678901237"
                                db/spec))
          :aloituskysely :paattokysely))

      (testing "heratepvm is within different rahoituskausi."
        (are [kysely] (empty? (op/existing-heratteet!
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
                         (do (op/initiate-if-needed! kysely hoks-test/hoks-1)
                             @sqs-msg))
          :aloituskysely
          :paattokysely)
        (is (= (set (map (juxt :kyselytyyppi :uusi-tila :syy)
                         (palautetapahtuma/get-all-by-hoks-id-and-kyselytyypit!
                           db/spec {:hoks-id (:id hoks-test/hoks-1)
                                    :kyselytyypit op/kyselytyypit})))
               #{["aloittaneet" "odottaa_kasittelya" "hoks_tallennettu"]
                 ["valmistuneet" "odottaa_kasittelya" "hoks_tallennettu"]}))
        (are [kyselytyyppi herate-basis voimassa-alkupvm voimassa-loppupvm]
             (= (-> (palaute/get-by-hoks-id-and-kyselytyypit!
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

      (testing "doesn't initiate kysely if opiskeluoikeus is not found"
        (are [kysely] (nil? (op/initiate-if-needed!
                              kysely
                              (assoc hoks-test/hoks-1
                                     :opiskeluoikeus-oid
                                     "1.2.246.562.15.57401181193")))
          :aloituskysely :paattokysely))

      (db-ops/query ["UPDATE palautteet SET tila='lahetetty'
                     WHERE hoks_id=12345 RETURNING *"])

      (testing "doesn't initiate kysely if one already exists for HOKS"
        (are [kysely] (not= :odottaa-kasittelya
                            (op/initiate-if-needed! kysely hoks-test/hoks-1))
          :aloituskysely :paattokysely))

      (testing "sends kysely info to AWS SQS when `:resend?` option is given."
        (are [kysely] (= :odottaa-kasittelya
                         (op/initiate-if-needed!
                           kysely hoks-test/hoks-1 {:resend? true}))
          :aloituskysely :paattokysely)))))
