(ns oph.ehoks.palaute.opiskelija-test
  (:require [clojure.test :refer [are deftest is testing use-fixtures]]
            [medley.core :refer [find-first remove-vals]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.external.organisaatio-test :as organisaatio-test]
            [oph.ehoks.hoks-test :as hoks-test]
            [oph.ehoks.hoks.handler :as hoks-handler]
            [oph.ehoks.opiskeluoikeus-test :as oo-test]
            [oph.ehoks.oppija.auth-handler-test :refer [mock-get-oppija-raw!]]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.oppijaindex-test :as oppijaindex-test]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.opiskelija :as op]
            [oph.ehoks.palaute.opiskelija.kyselylinkki :as kyselylinkki]
            [oph.ehoks.palaute.tapahtuma :as tapahtuma]
            [oph.ehoks.palaute.vastaajatunnus :as vt]
            [oph.ehoks.test-utils :as test-utils]
            [oph.ehoks.utils :refer [map-when]]
            [oph.ehoks.utils.date :as date]
            [taoensso.faraday :as far])
  (:import (java.time LocalDate LocalDateTime)))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-both-dbs-after-test)

(def sqs-msg (atom nil))

(defn test-not-initiated
  ([ctx reason]
    (test-not-initiated nil ctx reason))
  ([kysely-type ctx expected-reason]
    (doseq [kysely-type
            (if kysely-type [kysely-type] [:aloituskysely :paattokysely])]
      (let [[state _ reason]
            (op/initial-palaute-state-and-reason ctx kysely-type)]
        (is (contains? #{:ei-laheteta :heratepalvelussa nil} state))
        (is (= reason expected-reason))))))

(deftest test-initial-palaute-state-and-reason
  (with-redefs [date/now (constantly (LocalDate/of 2023 1 1))]
    (testing "On HOKS creation or update"
      (testing "don't initiate kysely if"
        (testing "there is existing palaute that has been already handled."
          (test-not-initiated
            {:hoks                hoks-test/hoks-1
             :opiskeluoikeus      oo-test/opiskeluoikeus-1
             :existing-palaute    {:tila "kysely_muodostettu"}}
            :jo-lahetetty))

        (testing "there is existing palaute for another HOKS."
          (test-not-initiated
            {:hoks                hoks-test/hoks-1
             :opiskeluoikeus      oo-test/opiskeluoikeus-1
             :existing-palaute    {:tila "odottaa_kasittelya"
                                   :hoks-id 343434}}
            :ei-palautteen-alkuperainen-hoks))

        (testing "there is an existing heräte in herätepalvelu."
          (test-not-initiated
            {:hoks                hoks-test/hoks-1
             :opiskeluoikeus      oo-test/opiskeluoikeus-1
             :existing-ddb-herate (delay {:lahetystila "ei_lahetetty"})}
            :heratepalvelun-vastuulla))

        (testing "`osaamisen-hankkimisen-tarve` is missing or is `false`."
          (doseq [hoks (map #(assoc hoks-test/hoks-1
                                    :osaamisen-hankkimisen-tarve %)
                            [false nil])]
            (test-not-initiated {:hoks           hoks
                                 :opiskeluoikeus oo-test/opiskeluoikeus-1}
                                :ei-ole)))

        (testing (str "`osaamisen-hankkimisen-tarve` is false _and_"
                      "there is no heratepvm.")
          (test-not-initiated {:hoks (assoc hoks-test/hoks-1
                                            :osaamisen-hankkimisen-tarve false
                                            :ensikertainen-hyvaksyminen nil
                                            :osaamisen-saavuttamisen-pvm nil)
                               :opiskeluoikeus oo-test/opiskeluoikeus-1}
                              :ei-ole))

        (testing "there are no ammatillinen suoritus in opiskeluoikeus"
          (test-not-initiated {:hoks           hoks-test/hoks-1
                               :opiskeluoikeus oo-test/opiskeluoikeus-2}
                              :ei-ammatillinen))

        (testing "opiskeluoikeus is in terminal state"
          (test-not-initiated
            {:hoks (assoc
                     hoks-test/hoks-1
                     :ensikertainen-hyvaksyminen (LocalDate/of 2023 9 10)
                     :osaamisen-saavuttamisen-pvm (LocalDate/of 2023 10 10))
             :opiskeluoikeus oo-test/opiskeluoikeus-4} :ulkoisesti-rahoitettu))

        (testing "opiskeluoikeus is externally funded"
          (test-not-initiated {:hoks           hoks-test/hoks-1
                               :opiskeluoikeus oo-test/opiskeluoikeus-5}
                              :opiskelu-paattynyt))

        (testing "heratepvm is invalid"
          (doseq [pvm ["2023-07-01" "2023-09-04" "2024-07-01"]]
            (with-redefs [date/now (constantly (LocalDate/parse pvm))]
              (test-not-initiated :aloituskysely
                                  {:hoks           hoks-test/hoks-1
                                   :opiskeluoikeus oo-test/opiskeluoikeus-1}
                                  :eri-rahoituskaudella)))

          (doseq [pvm ["2024-07-01" "2024-12-31" "2025-01-06"]]
            (with-redefs [date/now (constantly (LocalDate/parse pvm))]
              (test-not-initiated :paattokysely
                                  {:hoks           hoks-test/hoks-1
                                   :opiskeluoikeus oo-test/opiskeluoikeus-1}
                                  :eri-rahoituskaudella))))

        (testing "HOKS is a TUVA-HOKS or a HOKS related to TUVA-HOKS."
          (doseq [hoks [(assoc hoks-test/hoks-1
                               :hankittavat-koulutuksen-osat
                               ["koulutuksen-osa"])
                        (assoc hoks-test/hoks-1
                               :tuva-opiskeluoikeus-oid
                               "1.2.246.562.15.88406700034")]]
            (test-not-initiated
              {:hoks hoks :opiskeluoikeus oo-test/opiskeluoikeus-1}
              :tuva-opiskeluoikeus)))

        (testing "opiskeluoikeus is TUVA related."
          (test-not-initiated {:hoks           hoks-test/hoks-1
                               :opiskeluoikeus (assoc-in
                                                 oo-test/opiskeluoikeus-1
                                                 [:tyyppi :koodiarvo]
                                                 "tuva")}
                              :tuva-opiskeluoikeus))

        (testing "opiskeluoikeus is linked to another opiskeluoikeus"
          (test-not-initiated
            {:hoks hoks-test/hoks-1 :opiskeluoikeus oo-test/opiskeluoikeus-3}
            :liittyva-opiskeluoikeus)))

      (testing (str "don't initiate aloituskysely if "
                    "`ensikertainen-hyvaksyminen` is missing.")
        (let [hoks (dissoc hoks-test/hoks-1 :ensikertainen-hyvaksyminen)]
          (test-not-initiated
            :aloituskysely
            {:hoks hoks :opiskeluoikeus oo-test/opiskeluoikeus-1}
            :ei-ole)))

      (testing (str "don't initiate päättökysely if "
                    "`osaamisen-saavuttamisen-pvm` is missing.")
        (let [hoks (dissoc hoks-test/hoks-1 :osaamisen-saavuttamisen-pvm)]
          (test-not-initiated
            :paattokysely
            {:hoks hoks :opiskeluoikeus oo-test/opiskeluoikeus-1}
            :ei-ole))))

    (testing "On HOKS creation"
      (let [ctx {:hoks hoks-test/hoks-1
                 :opiskeluoikeus oo-test/opiskeluoikeus-1}]
        (testing
         "initiate aloituskysely if `osaamisen-hankkimisen-tarve` is `true`."
          (is (= (op/initial-palaute-state-and-reason ctx :aloituskysely)
                 [:odottaa-kasittelya :ensikertainen-hyvaksyminen
                  :hoks-tallennettu])))

        (testing
         (str "initiate paattokysely if `osaamisen-hankkimisen-tarve` is "
              "`true` and `osaamisen-saavuttamisen-pvm` is not missing.")
          (is (= (op/initial-palaute-state-and-reason ctx :paattokysely)
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

(deftest test-existing-palaute!
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
      (are [kysely-type]
           (= "lahetetty"
              (-> {:hoks hoks-test/hoks-3 :tx db/spec
                   :koulutustoimija "1.2.246.562.10.346830761110"}
                  (op/existing-palaute! kysely-type)
                  :tila))
        :aloituskysely :paattokysely))

    (testing "Kysely is not considered already initiated when"
      (testing "koulutustoimija differs."
        (are [kysely-type]
             (nil? (-> {:hoks hoks-test/hoks-3 :tx db/spec
                        :koulutustoimija "1.2.246.562.10.45678901237"}
                       (op/existing-palaute! kysely-type)))
          :aloituskysely :paattokysely))

      (testing "heratepvm is within different rahoituskausi."
        (are [kysely-type]
             (nil? (-> {:hoks hoks-test/hoks-4 :tx db/spec
                        :koulutustoimija "1.2.246.562.10.346830761110"}
                       (op/existing-palaute! kysely-type)))
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
      (testing "Testing that function `initiate-if-needed!`"
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
                        (tapahtuma/get-all-by-hoks-id-and-kyselytyypit!
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

        (testing "if opiskeluoikeus is not found, will wait for heratepvm"
          (are [kysely-type]
               (= :odottaa-kasittelya
                  (op/initiate-if-needed! (assoc ctx :opiskeluoikeus nil)
                                          kysely-type))
            :aloituskysely :paattokysely))

        (testing "doesn't initiate if it is already handled by herätepalvelu"
          (ddb/sync-amis-herate!
            (op/build-amisherate-record-for-heratepalvelu
              (assoc ctx
                     :koulutustoimija "1.2.246.562.10.346830761110"
                     :hk-toteuttaja (delay nil)
                     :existing-palaute
                     {:kyselytyyppi "aloittaneet"
                      :heratepvm (:ensikertainen-hyvaksyminen
                                   hoks-test/hoks-1)})))
          (ddb/sync-amis-herate!
            (op/build-amisherate-record-for-heratepalvelu
              (assoc ctx
                     :koulutustoimija "1.2.246.562.10.346830761110"
                     :hk-toteuttaja (delay nil)
                     :existing-palaute
                     {:kyselytyyppi "valmistuneet"
                      :heratepvm (:osaamisen-saavuttamisen-pvm
                                   hoks-test/hoks-1)})))
          (are [kysely-type]
               (= :heratepalvelussa (op/initiate-if-needed! ctx kysely-type))
            :aloituskysely :paattokysely))

        (testing "doesn't initiate kysely if one already exists for HOKS"
          (db-ops/query ["UPDATE palautteet SET tila='lahetetty'
                         WHERE hoks_id=12345 RETURNING *"])
          (are [kysely-type] (nil? (op/initiate-if-needed! ctx kysely-type))
            :aloituskysely :paattokysely))))))

(defn create-arvo-kyselylinkki!
  [palaute]
  (-> palaute
      (vt/build-ctx)
      (op/build-kyselylinkki-request-body)
      (arvo/create-kyselytunnus!)))

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
          (palaute/get-palautteet-waiting-for-vastaajatunnus!
            db/spec {:kyselytyypit ["aloittaneet" "valmistuneet"]
                     :hoks-id nil :palaute-id nil})]
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
                   resp (create-arvo-kyselylinkki! herate)]
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
                 (create-arvo-kyselylinkki!))
            (is (= (:tutkinnonosat_hankkimistavoittain @saved-req)
                   {:oppisopimus
                    ["tutkinnonosat_300271" "tutkinnonosat_300268"]
                    :koulutussopimus
                    ["tutkinnonosat_300270" "tutkinnonosat_300269"]
                    :oppilaitosmuotoinenkoulutus
                    ["tutkinnonosat_300270" "tutkinnonosat_300268"]}))
            (is (= ((juxt :koulutustoimija_oid :kyselyn_tyyppi :tutkintotunnus
                          :tutkinnon_suorituskieli :toimipiste_oid) @saved-req)
                   ["1.2.246.562.10.346830761110" "tutkinnon_suorittaneet"
                    "351407" "fi" "1.2.246.562.10.12312312312"]))))))))

(def arvo-error-body
  "{\"error\": \"required-fields-missing\", \"msg\": \"Huonosti menee\"}")

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
          (palaute/get-palautteet-waiting-for-vastaajatunnus!
            db/spec {:kyselytyypit ["aloittaneet" "valmistuneet"]
                     :hoks-id nil :palaute-id nil})]
      (testing "successful Arvo call for amispalaute"
        (client/set-post!
          (fn [^String url options]
            (when (.endsWith url "/api/vastauslinkki/v1")
              (swap! vastauslinkki-counter inc)
              (is (not (empty? (get-in options [:form-params :request_id]))))
              {:status 200
               :body {:tunnus (str "foo" @vastauslinkki-counter)
                      :kysely_linkki (str "https://arvovastaus.csc.fi/v/foo"
                                          @vastauslinkki-counter)
                      :voimassa_loppupvm "2024-10-10"}})))
        (is (->> heratteet
                 (find-first (comp (partial = "valmistuneet") :kyselytyyppi))
                 (vt/handle-palaute-waiting-for-heratepvm!)
                 (some?)))
        (is (= [["odottaa_kasittelya" "aloittaneet"]
                ["kysely_muodostettu" "valmistuneet"]]
               (->> {:hoks-id (:id hoks)
                     :kyselytyypit ["aloittaneet" "valmistuneet"]}
                    (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                    (map (juxt :tila :kyselytyyppi)))))
        (is (= [["odottaa_kasittelya" "odottaa_kasittelya"
                 {:request-id 0
                  :osaamisen-saavuttamisen-pvm "2024-02-05"}]
                ["odottaa_kasittelya" "kysely_muodostettu"
                 {:request-id 36
                  :arvo_response
                  {:tunnus "foo1"
                   :kysely_linkki "https://arvovastaus.csc.fi/v/foo1"
                   :voimassa_loppupvm "2024-10-10"}}]]
               (->> {:hoks-id (:id hoks) :kyselytyypit ["valmistuneet"]}
                    (tapahtuma/get-all-by-hoks-id-and-kyselytyypit!
                      db/spec)
                    (map (juxt :vanha-tila :uusi-tila
                               #(update (:lisatiedot %) :request-id count))))))
        (client/reset-functions!))
      (testing "get kyselylinkit returns linkki from palaute"
        (let [loppupvm (.plusMonths (LocalDateTime/now) 1)]
          (with-redefs [oph.ehoks.external.arvo/get-kyselylinkki-status!
                        (fn [_]
                          {:vastattu false
                           :voimassa-loppupvm (str loppupvm "Z")})]
            (let [kyselylinkit (->> (kyselylinkki/get-by-oppija-oid!
                                      "1.2.246.562.24.12312312319")
                                    (map-when kyselylinkki/active?
                                              kyselylinkki/update-status!)
                                    (filter kyselylinkki/active?))]
              (is (= 1 (count kyselylinkit)))
              (is
                (= (select-keys (first kyselylinkit)
                                [:hoks-id
                                 :oppija-oid
                                 :tyyppi
                                 :kyselylinkki
                                 :vastattu
                                 :sahkoposti
                                 :voimassa-loppupvm])
                   {:hoks-id           12345
                    :oppija-oid        "1.2.246.562.24.12312312319"
                    :tyyppi            "tutkinnon_suorittaneet"
                    :kyselylinkki
                    "https://arvovastaus.csc.fi/v/foo1"
                    :vastattu          false
                    :sahkoposti        "testi.testaaja@testidomain.testi"
                    :voimassa-loppupvm (LocalDate/from loppupvm)}))))))
      (testing "Palaute is synced to herätepalvelu after Arvo call"
        (let [ddb-key {:tyyppi_kausi "tutkinnon_suorittaneet/2023-2024"
                       :toimija_oppija
                       "1.2.246.562.10.346830761110/1.2.246.562.24.12312312319"}
              ddb-item (far/get-item
                         @ddb/faraday-opts @(ddb/tables :amis) ddb-key)]
          (is (= "testi.testaaja@testidomain.testi" (:sahkoposti ddb-item)))
          (is (= "https://arvovastaus.csc.fi/v/foo1" (:kyselylinkki ddb-item)))
          (is (not (empty? (:request-id ddb-item))))
          (is (= 351407 (:tutkintotunnus ddb-item)))))
      (testing "unsuccessful Arvo call for amispalaute"
        (client/set-post!
          (fn [^String url options]
            (when (.endsWith url "/api/vastauslinkki/v1")
              (throw
                (ex-info "bad request" {:status 400 :body arvo-error-body})))))
        (->> heratteet
             (find-first (comp (partial = "aloittaneet") :kyselytyyppi))
             (vt/handle-palaute-waiting-for-heratepvm!))
        (is (= [["odottaa_kasittelya" "aloittaneet"]
                ["kysely_muodostettu" "valmistuneet"]]
               (->> {:hoks-id (:id hoks)
                     :kyselytyypit ["aloittaneet" "valmistuneet"]}
                    (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                    (map (juxt :tila :kyselytyyppi)))))
        (is (= [["odottaa_kasittelya" "odottaa_kasittelya"
                 {:request-id 0
                  :ensikertainen-hyvaksyminen "2023-04-16"}]
                ["odottaa_kasittelya" "odottaa_kasittelya"
                 {:request-id 36
                  :errormsg "HTTP request error: bad request"
                  :body arvo-error-body}]]
               (->> {:hoks-id (:id hoks) :kyselytyypit ["aloittaneet"]}
                    (tapahtuma/get-all-by-hoks-id-and-kyselytyypit!
                      db/spec)
                    (map (juxt :vanha-tila :uusi-tila
                               #(update (:lisatiedot %) :request-id count))))))
        (client/reset-functions!))
      (testing "non-recoverable error in Arvo call"
        (client/set-post!
          (fn [^String url options]
            (when (.endsWith url "/api/vastauslinkki/v1")
              (throw
                (ex-info "not found"
                         {:status 404 :body "{\"error\": \"ei-kyselya\"}"})))))
        (->> heratteet
             (find-first (comp (partial = "aloittaneet") :kyselytyyppi))
             (vt/handle-palaute-waiting-for-heratepvm!))
        (is (= [["odottaa_kasittelya" "odottaa_kasittelya"
                 {:request-id 0
                  :ensikertainen-hyvaksyminen "2023-04-16"}]
                ["odottaa_kasittelya" "odottaa_kasittelya"
                 {:request-id 36
                  :errormsg "HTTP request error: bad request"
                  :body arvo-error-body}]
                ["odottaa_kasittelya" "ei_laheteta"
                 {:heratepvm "2023-04-16"
                  :request-id 36
                  :opiskeluoikeus-oid "1.2.246.562.15.10000000009"}]]
               (->> {:hoks-id (:id hoks) :kyselytyypit ["aloittaneet"]}
                    (tapahtuma/get-all-by-hoks-id-and-kyselytyypit!
                      db/spec)
                    (map (juxt :vanha-tila :uusi-tila
                               #(update (:lisatiedot %) :request-id count))))))
        (client/reset-functions!)))))

(deftest test-handle-amis-palautteet-on-heratepvm!
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
          vastauslinkki-counter (atom 0)
          [aloituskysely paattokysely]
          (palaute/get-by-hoks-id-and-kyselytyypit!
            db/spec
            {:hoks-id (:id hoks) :kyselytyypit ["aloittaneet" "valmistuneet"]})]
      (testing "handle-amis-palautteet-on-heratepvm!"
        (client/set-post!
          (fn [^String url options]
            (when (.endsWith url "/api/vastauslinkki/v1")
              (swap! vastauslinkki-counter inc)
              {:status 200
               :body {:tunnus (str "bar" @vastauslinkki-counter)
                      :kysely_linkki (str "https://arvovastaus.csc.fi/v/bar"
                                          @vastauslinkki-counter)
                      :voimassa_loppupvm "2024-10-10"}})))
        (is (= ["bar1" "bar2"]
               (vt/handle-amis-palautteet-on-heratepvm! {})))
        (is (= [["kysely_muodostettu" "aloittaneet"]
                ["kysely_muodostettu" "valmistuneet"]]
               (->> {:hoks-id (:id hoks)
                     :kyselytyypit ["aloittaneet" "valmistuneet"]}
                    (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                    (map (juxt :tila :kyselytyyppi)))))
        (testing (str "Doesn't create kyselylinkki if herate already exists "
                      "in Heratepalvelu.")
          ; Reset aloituskysely and paattokysely to back to state
          ; before vastaajatunnus creation.
          (palaute/update! db/spec aloituskysely)
          (palaute/update! db/spec paattokysely)
          (is (= 2 @vastauslinkki-counter))
          (vt/handle-amis-palautteet-on-heratepvm! {})
          (is (= 2 @vastauslinkki-counter)) ; shouldn't be incremented
          (is (= [["heratepalvelussa" "aloittaneet"]
                  ["heratepalvelussa" "valmistuneet"]]
                 (->> {:hoks-id (:id hoks)
                       :kyselytyypit ["aloittaneet" "valmistuneet"]}
                      (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                      (map (juxt :tila :kyselytyyppi))))))
        (client/reset-functions!)))))
