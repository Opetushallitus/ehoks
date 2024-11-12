(ns oph.ehoks.palaute.opiskelija-test
  (:require [clojure.test :refer [are deftest is testing use-fixtures]]
            [taoensso.faraday :as far]
            [medley.core :refer [remove-vals find-first]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.external.organisaatio-test :as organisaatio-test]
            [oph.ehoks.hoks.handler :as hoks-handler]
            [oph.ehoks.hoks-test :as hoks-test]
            [oph.ehoks.opiskeluoikeus-test :as oo-test]
            [oph.ehoks.oppija.auth-handler-test :refer [mock-get-oppija-raw!]]
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
  ([kysely-type hoks opiskeluoikeus reason]
    (doseq [kysely-type (if kysely-type
                          [kysely-type]
                          [:aloituskysely :paattokysely])]
      (let [state-and-reason
            (op/initial-palaute-state-and-reason
              {:hoks hoks :opiskeluoikeus opiskeluoikeus} kysely-type nil)]
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
      (let [ctx {:hoks hoks-test/hoks-1
                 :opiskeluoikeus oo-test/opiskeluoikeus-1}]
        (testing
         "initiate aloituskysely if `osaamisen-hankkimisen-tarve` is `true`."
          (is (= (op/initial-palaute-state-and-reason
                   ctx :aloituskysely nil)
                 [:odottaa-kasittelya :ensikertainen-hyvaksyminen
                  :hoks-tallennettu])))

        (testing
         (str "initiate paattokysely if `osaamisen-hankkimisen-tarve` is "
              "`true` and `osaamisen-saavuttamisen-pvm` is not missing.")
          (is (= (op/initial-palaute-state-and-reason ctx :paattokysely nil)
                 [:odottaa-kasittelya :osaamisen-saavuttamisen-pvm
                  :hoks-tallennettu])))))))

(defn expected-msg
  [kysely-type hoks]
  {:ehoks-id           (:id hoks)
   :kyselytyyppi       (case kysely-type
                         :aloituskysely "aloittaneet"
                         :paattokysely  "tutkinnon_suorittaneet")
   :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
   :oppija-oid         (:oppija-oid hoks)
   :sahkoposti         (:sahkoposti hoks)
   :puhelinnumero      (:puhelinnumero hoks)
   :alkupvm
   (str (case kysely-type
          :aloituskysely (:ensikertainen-hyvaksyminen hoks)
          :paattokysely  (:osaamisen-saavuttamisen-pvm hoks)))})

(defn mock-get-opiskeluoikeus-info-raw [oo]
  (if (= oo "1.2.246.562.15.57401181193")
    (throw (ex-info "opiskeluoikeus not found"
                    {:status 404
                     :body (str "[{\"key\": \"notFound.opiskeluoikeuttaEiLöydy"
                                "TaiEiOikeuksia\"}]")}))
    (assoc oo-test/opiskeluoikeus-1 :oid oo)))

(deftest test-existing-heratteet!
  (with-redefs [date/now (constantly (LocalDate/of 2023 4 18))]
    (db-hoks/insert-hoks! hoks-test/hoks-1)
    (with-redefs [organisaatio/get-organisaatio!
                  organisaatio-test/mock-get-organisaatio!]
      (doseq [kysely-type [:aloituskysely :paattokysely]]
        (op/initiate-if-needed! {:hoks           hoks-test/hoks-1
                                 :opiskeluoikeus oo-test/opiskeluoikeus-1}
                                kysely-type)))
    (db-ops/query ["UPDATE palautteet SET tila='lahetetty'
                   WHERE hoks_id=? RETURNING *" (:id hoks-test/hoks-1)])

    (testing
     (str "Kysely is considered already initiated if it is for same "
          "oppija with same koulutustoimija and within same rahoituskausi.")
      (are [kysely-type] (= (:tila (first (op/existing-heratteet!
                                            {:tx   db/spec
                                             :hoks hoks-test/hoks-3
                                             :koulutustoimija
                                             "1.2.246.562.10.346830761110"}
                                            kysely-type)))
                            "lahetetty")
        :aloituskysely :paattokysely))

    (testing "Kysely is not considered already initiated when"
      (testing "koulutustoimija differs."
        (are [kysely-type] (empty? (op/existing-heratteet!
                                     {:tx db/spec
                                      :hoks hoks-test/hoks-3
                                      :koulutustoimija
                                      "1.2.246.562.10.45678901237"}
                                     kysely-type))
          :aloituskysely :paattokysely))

      (testing "heratepvm is within different rahoituskausi."
        (are [kysely-type] (empty? (op/existing-heratteet!
                                     {:tx db/spec
                                      :hoks hoks-test/hoks-4
                                      :koulutustoimija
                                      "1.2.246.562.10.346830761110"}
                                     kysely-type))
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
    (let [ctx {:hoks           hoks-test/hoks-1
               :opiskeluoikeus (mock-get-opiskeluoikeus-info-raw
                                 (:opiskeluoikeus-oid hoks-test/hoks-1))}]
      (testing "Testing that function `initiate!`"
        (testing (str "stores kysely info to `palautteet` DB table and "
                      "successfully sends aloituskysely and paattokysely "
                      "herate to SQS queue")
          (are [kysely-type] (= (expected-msg kysely-type hoks-test/hoks-1)
                                (do (op/initiate-if-needed! ctx kysely-type)
                                    @sqs-msg))
            :aloituskysely
            :paattokysely)
          (is (= (set (map
                        (juxt :kyselytyyppi :uusi-tila :syy)
                        (palautetapahtuma/get-all-by-hoks-id-and-kyselytyypit!
                          db/spec {:hoks-id      (:id hoks-test/hoks-1)
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
          (are [kysely-type] (nil? (op/initiate-if-needed!
                                     (assoc ctx :opiskeluoikeus nil)
                                     kysely-type))
            :aloituskysely :paattokysely))

        (db-ops/query ["UPDATE palautteet SET tila='lahetetty'
                       WHERE hoks_id=12345 RETURNING *"])

        (testing "doesn't initiate kysely if one already exists for HOKS"
          (are [kysely-type] (not= :odottaa-kasittelya
                                   (op/initiate-if-needed! ctx kysely-type))
            :aloituskysely :paattokysely))

        (testing "sends kysely info to AWS SQS when `:resend?` option is given."
          (are [kysely-type] (= :odottaa-kasittelya
                                (op/initiate-if-needed!
                                  ctx kysely-type {:resend? true}))
            :aloituskysely :paattokysely))))))

(deftest test-create-arvo-kyselylinkki!
  (with-redefs [date/now (constantly (LocalDate/of 2023 4 18))
                koski/get-oppija-opiskeluoikeudet
                (fn [_]
                  [{:oid "1.2.246.562.15.55003456345"
                    :oppilaitos {:oid "1.2.246.562.10.12944436166"}}
                   {:oid "1.2.246.562.15.55003456345"
                    :oppilaitos {:oid "1.2.246.562.10.12944436166"}}])
                koski/get-opiskeluoikeus-info-raw
                mock-get-opiskeluoikeus-info-raw
                onr/get-oppija-raw!
                mock-get-oppija-raw!
                organisaatio/get-organisaatio!
                organisaatio-test/mock-get-organisaatio!]
    (oppijaindex/add-hoks-dependents-in-index! hoks-test/hoks-1)
    (let [hoks (hoks-handler/save-hoks-and-initiate-all-palautteet!
                 {:hoks hoks-test/hoks-1
                  :opiskeluoikeus oo-test/opiskeluoikeus-1})
          heratteet
          (palaute/get-amis-palautteet-waiting-for-kyselylinkki!
            db/spec {:heratepvm (LocalDate/of 2024 4 20)})]
      (testing "HOKS creation marks correct palautteet as actionable"
        (is (= [["odottaa_kasittelya" "aloittaneet"]
                ["odottaa_kasittelya" "valmistuneet"]]
               (->> {:hoks-id (:id hoks)
                     :kyselytyypit ["aloittaneet" "valmistuneet"]}
                    (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                    (map (juxt :tila :kyselytyyppi)))))
        (is (= [["odottaa_kasittelya" "aloittaneet"]
                ["odottaa_kasittelya" "valmistuneet"]]
               (map (juxt :tila :kyselytyyppi) heratteet))))
      (testing "Arvo call for amispalaute is done"
        (client/set-post!
          (fn [^String url options]
            (when (.endsWith url "/api/vastauslinkki/v1")
              {:status 200
               :body {:tunnus "foo"
                      :kysely_linkki "https://arvovastaus.csc.fi/v/foo"
                      :voimassa_loppupvm "2024-10-10"}})))
        (are [kysely-type]
             (let [herate (find-first
                            (comp (partial = kysely-type) :kyselytyyppi)
                            heratteet)
                   resp (op/create-arvo-kyselylinkki! herate)]
               (= [:kysely_linkki :tunnus :voimassa_loppupvm]
                  (sort (keys resp))))
          "aloittaneet" "valmistuneet")
        (client/reset-functions!))
      (testing "Arvo is given correct values"
        (let [saved-req (atom nil)]
          (with-redefs [arvo/create-kyselytunnus!
                        (fn [request] (reset! saved-req request) {:tunnus "a"})]
            (->> heratteet
                 (find-first (comp (partial = "valmistuneet") :kyselytyyppi))
                 (op/create-arvo-kyselylinkki!))
            (is (= (:tutkinnonosat_hankkimistavoittain @saved-req)
                   {:oppisopimus
                    ["tutkinnonosat_300268" "tutkinnonosat_300271"]
                    :koulutussopimus
                    ["tutkinnonosat_300269" "tutkinnonosat_300270"]
                    :oppilaitosmuotoinenkoulutus
                    ["tutkinnonosat_300268" "tutkinnonosat_300270"]}))
            (is (= ((juxt :koulutustoimija_oid :kyselyn_tyyppi :tutkintotunnus
                          :tutkinnon_suorituskieli :toimipiste_oid) @saved-req)
                   ["1.2.246.562.10.346830761110" "tutkinnon_suorittaneet"
                    "351407" "fi" "1.2.246.562.10.12312312312"]))))))))

(deftest test-create-and-save-arvo-kyselylinkki!
  (with-redefs [date/now (constantly (LocalDate/of 2023 4 18))
                koski/get-oppija-opiskeluoikeudet
                (fn [_]
                  [{:oid "1.2.246.562.15.55003456345"
                    :oppilaitos {:oid "1.2.246.562.10.12944436166"}}
                   {:oid "1.2.246.562.15.55003456345"
                    :oppilaitos {:oid "1.2.246.562.10.12944436166"}}])
                koski/get-opiskeluoikeus-info-raw
                mock-get-opiskeluoikeus-info-raw
                onr/get-oppija-raw!
                mock-get-oppija-raw!
                organisaatio/get-organisaatio!
                organisaatio-test/mock-get-organisaatio!]
    (oppijaindex/add-hoks-dependents-in-index! hoks-test/hoks-1)
    (let [hoks (hoks-handler/save-hoks-and-initiate-all-palautteet!
                 {:hoks hoks-test/hoks-1
                  :opiskeluoikeus oo-test/opiskeluoikeus-1})
          vastauslinkki-counter (atom 0)
          heratteet
          (palaute/get-amis-palautteet-waiting-for-kyselylinkki!
            db/spec {:heratepvm (LocalDate/of 2024 4 20)})]
      (testing "successful Arvo call for amispalaute"
        (client/set-post!
          (fn [^String url options]
            (when (.endsWith url "/api/vastauslinkki/v1")
              (swap! vastauslinkki-counter inc)
              {:status 200
               :body {:tunnus (str "foo" @vastauslinkki-counter)
                      :kysely_linkki (str "https://arvovastaus.csc.fi/v/foo"
                                          @vastauslinkki-counter)
                      :voimassa_loppupvm "2024-10-10"}})))
        (is (->> heratteet
                 (find-first (comp (partial = "valmistuneet") :kyselytyyppi))
                 (op/create-and-save-arvo-kyselylinkki!)
                 (nil?)))
        (is (= [["odottaa_kasittelya" "aloittaneet"]
                ["kysely_muodostettu" "valmistuneet"]]
               (->> {:hoks-id (:id hoks)
                     :kyselytyypit ["aloittaneet" "valmistuneet"]}
                    (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                    (map (juxt :tila :kyselytyyppi)))))
        (is (= [["odottaa_kasittelya" "odottaa_kasittelya"
                 {:osaamisen-saavuttamisen-pvm "2024-02-05"}]
                ["odottaa_kasittelya" "kysely_muodostettu"
                 {:arvo_response
                  {:tunnus "foo1"
                   :kysely_linkki "https://arvovastaus.csc.fi/v/foo1"
                   :voimassa_loppupvm "2024-10-10"}}]]
               (->> {:hoks-id (:id hoks) :kyselytyypit ["valmistuneet"]}
                    (palautetapahtuma/get-all-by-hoks-id-and-kyselytyypit!
                      db/spec)
                    (map (juxt :vanha-tila :uusi-tila :lisatiedot)))))
        (client/reset-functions!))
      (testing "Palaute is synced to herätepalvelu after Arvo call"
        (let [ddb-key {:tyyppi_kausi "tutkinnon_suorittaneet/2023-2024"
                       :toimija_oppija
                       "1.2.246.562.10.346830761110/1.2.246.562.24.12312312319"}
              ddb-item (far/get-item
                         @ddb/faraday-opts @(ddb/tables :amis) ddb-key)]
          (is (= "testi.testaaja@testidomain.testi" (:sahkoposti ddb-item)))
          (is (= "https://arvovastaus.csc.fi/v/foo1" (:kyselylinkki ddb-item)))
          (is (= 351407 (:tutkintotunnus ddb-item)))))
      (testing "unsuccessful Arvo call for amispalaute"
        (client/set-post!
          (fn [^String url options]
            (when (.endsWith url "/api/vastauslinkki/v1")
              (throw
                (ex-info "not found"
                         {:status 404
                          :body {:error "ei_kyselykertaa"
                                 :msg "Huonosti menee"}})))))
        (is (thrown?
              clojure.lang.ExceptionInfo
              (->> heratteet
                   (find-first (comp (partial = "aloittaneet") :kyselytyyppi))
                   (op/create-and-save-arvo-kyselylinkki!)
                   (keys))))
        (is (= [["odottaa_kasittelya" "aloittaneet"]
                ["kysely_muodostettu" "valmistuneet"]]
               (->> {:hoks-id (:id hoks)
                     :kyselytyypit ["aloittaneet" "valmistuneet"]}
                    (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                    (map (juxt :tila :kyselytyyppi)))))
        (is (= [["odottaa_kasittelya" "odottaa_kasittelya"
                 {:ensikertainen-hyvaksyminen "2023-04-16"}]
                ["odottaa_kasittelya" "odottaa_kasittelya"
                 {:errormsg "HTTP request error: not found"
                  :body {:msg "Huonosti menee", :error "ei_kyselykertaa"}}]]
               (->> {:hoks-id (:id hoks) :kyselytyypit ["aloittaneet"]}
                    (palautetapahtuma/get-all-by-hoks-id-and-kyselytyypit!
                      db/spec)
                    (map (juxt :vanha-tila :uusi-tila :lisatiedot)))))
        (client/reset-functions!)))))

(deftest test-create-and-save-arvo-kyselylinkki-for-all-needed!
  (with-redefs [date/now (constantly (LocalDate/of 2024 12 18))
                koski/get-oppija-opiskeluoikeudet
                (fn [_]
                  [{:oid "1.2.246.562.15.55003456345"
                    :oppilaitos {:oid "1.2.246.562.10.12944436166"}}
                   {:oid "1.2.246.562.15.55003456345"
                    :oppilaitos {:oid "1.2.246.562.10.12944436166"}}])
                koski/get-opiskeluoikeus-info-raw
                mock-get-opiskeluoikeus-info-raw
                onr/get-oppija-raw!
                mock-get-oppija-raw!
                organisaatio/get-organisaatio!
                organisaatio-test/mock-get-organisaatio!]
    (oppijaindex/add-hoks-dependents-in-index! hoks-test/hoks-4)
    (let [hoks (hoks-handler/save-hoks-and-initiate-all-palautteet!
                 {:hoks hoks-test/hoks-4
                  :opiskeluoikeus oo-test/opiskeluoikeus-1})
          vastauslinkki-counter (atom 0)]
      (testing "create-and-save-arvo-kyselylinkki-for-all-needed!"
        (client/set-post!
          (fn [^String url options]
            (when (.endsWith url "/api/vastauslinkki/v1")
              (swap! vastauslinkki-counter inc)
              {:status 200
               :body {:tunnus (str "bar" @vastauslinkki-counter)
                      :kysely_linkki (str "https://arvovastaus.csc.fi/v/bar"
                                          @vastauslinkki-counter)
                      :voimassa_loppupvm "2024-10-10"}})))
        (op/create-and-save-arvo-kyselylinkki-for-all-needed! {})
        (is (= [["kysely_muodostettu" "aloittaneet"]
                ["kysely_muodostettu" "valmistuneet"]]
               (->> {:hoks-id (:id hoks)
                     :kyselytyypit ["aloittaneet" "valmistuneet"]}
                    (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                    (map (juxt :tila :kyselytyyppi)))))
        (client/reset-functions!)))))
