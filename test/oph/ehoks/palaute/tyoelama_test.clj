(ns oph.ehoks.palaute.tyoelama-test
  (:require [clojure.set :as s]
            [clojure.test :refer [are deftest is testing use-fixtures]]
            [clojure.tools.logging.test :refer [logged? with-log]]
            [medley.core :refer [remove-vals]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.db-helpers :as db-helpers]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.external.organisaatio-test :as organisaatio-test]
            [oph.ehoks.heratepalvelu :as heratepalvelu]
            [oph.ehoks.hoks-test :as hoks-test]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils]
            [oph.ehoks.hoks.osaamisen-hankkimistapa :as oht]
            [oph.ehoks.opiskeluoikeus-test :as oo-test]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.tapahtuma :as tapahtuma]
            [oph.ehoks.palaute.tyoelama :as tep]
            [oph.ehoks.test-utils :as test-utils]
            [oph.ehoks.utils.date :as date]
            [taoensso.faraday :as far])
  (:import [clojure.lang ExceptionInfo]
           (java.time LocalDate)
           (java.util UUID)))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-database-after-test)

;; FIXME: there is some kind of misunderstanding in the format of this
;; data.  It's fed to initiate-if-needed! and
;; initial-palaute-state-and-reason but seems mostly to be in SQS
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
    (are [pvm-str expected] (= (tep/next-niputus-date (LocalDate/parse pvm-str))
                               (LocalDate/parse expected))
      "2021-12-03" "2021-12-16"
      "2021-12-27" "2022-01-01"
      "2021-04-25" "2021-05-01"
      "2022-06-24" "2022-07-01")))

(deftest test-fully-keskeytynyt?
  (testing "fully-keskeytynyt?"
    (let [herate1 {:keskeytymisajanjaksot [{:alku  (LocalDate/of 2021 8 8)
                                            :loppu (LocalDate/of 2021 8 10)}
                                           {:alku  (LocalDate/of 2021 8 1)
                                            :loppu (LocalDate/of 2021 8 4)}]
                   :loppu (LocalDate/of 2021 8 9)}
          herate2 {:keskeytymisajanjaksot [{:alku  (LocalDate/of 2021 8 8)
                                            :loppu (LocalDate/of 2021 8 10)}
                                           {:alku  (LocalDate/of 2021 8 1)
                                            :loppu (LocalDate/of 2021 8 4)}]
                   :loppu (LocalDate/of 2021 8 11)}
          herate3 {}
          herate4 {:keskeytymisajanjaksot [{:alku (LocalDate/of 2021 8 8)}]
                   :loppu (LocalDate/of 2021 8 11)}
          herate5 {:keskeytymisajanjaksot [{:alku (LocalDate/of 2021 8 8)}]}]
      (is (tep/fully-keskeytynyt? herate1))
      (is (not (tep/fully-keskeytynyt? herate2)))
      (is (not (tep/fully-keskeytynyt? herate3)))
      (is (tep/fully-keskeytynyt? herate4))
      (is (not (tep/fully-keskeytynyt? herate5))))))

(deftest test-tyopaikkajaksot
  (testing (str "The function returns osaamisen hankkimistavat with koodi-uri"
                "\"osaamisenhankkimistapa_koulutussopimus\" or "
                "\"osaamisenhankkimistapa_oppisopimus\"")
    (is (= (map :yksiloiva-tunniste (tep/tyopaikkajaksot hoks-test/hoks-1))
           '("1" "3" "4" "7" "9")))))

(deftest test-initial-palaute-and-reason
  (testing "On HOKS creation or update"
    (with-redefs [date/now #(LocalDate/of 2023 7 1)]
      (testing "don't initiate kysely if"
        (testing "there is already a handled herate for tyopaikkajakso."
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-1
                    :jakso          test-jakso
                    :existing-palaute {:yksiloiva-tunniste "asd"
                                       :tila "vastaajatunnus_muodostettu"}})
                 [nil :yksiloiva-tunniste :jo-lahetetty])))
        (testing "opiskeluoikeus is in terminal state."
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-5
                    :jakso test-jakso})
                 [:ei-laheteta :opiskeluoikeus-oid :opiskelu-paattynyt])))
        (testing "osa-aikaisuus is missing from työpaikkajakso"
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-1
                    :jakso (dissoc test-jakso :osa-aikaisuustieto)})
                 [:ei-laheteta :osa-aikaisuustieto :ei-ole])))
        (testing "workplace information is missing from työpaikkajakso"
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-1
                    :jakso (update test-jakso
                                   :tyopaikalla-jarjestettava-koulutus
                                   dissoc
                                   :tyopaikan-y-tunnus)})
                 [:ei-laheteta :tyopaikalla-jarjestettava-koulutus
                  :puuttuva-yhteystieto])))
        (testing "työpaikkajakso is interrupted on it's end date"
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-1
                    :jakso (assoc-in test-jakso
                                     [:keskeytymisajanjaksot 1]
                                     {:alku  (LocalDate/of 2023 12 1)
                                      :loppu (LocalDate/of 2023 12 15)})})
                 [:ei-laheteta :keskeytymisajanjaksot :jakso-keskeytynyt])))
        (testing "opiskeluoikeus doesn't have any ammatillinen suoritus"
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-2
                    :jakso test-jakso})
                 [:ei-laheteta :opiskeluoikeus-oid :ei-ammatillinen])))
        (testing "there is a feedback preventing code in opiskeluoikeusjakso."
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-4
                    :jakso test-jakso})
                 [:ei-laheteta :opiskeluoikeus-oid :ulkoisesti-rahoitettu])))
        (testing "HOKS is a TUVA-HOKS or a HOKS related to TUVA-HOKS."
          (doseq [test-hoks [(assoc hoks-test/hoks-1
                                    :hankittavat-koulutuksen-osat
                                    ["koulutuksen-osa"])
                             (assoc hoks-test/hoks-1
                                    :tuva-opiskeluoikeus-oid
                                    "1.2.246.562.15.88406700034")]]
            (is (= (tep/initial-palaute-state-and-reason
                     {:hoks           test-hoks
                      :opiskeluoikeus oo-test/opiskeluoikeus-1
                      :jakso test-jakso})
                   [:ei-laheteta
                    :tuva-opiskeluoikeus-oid
                    :tuva-opiskeluoikeus]))))
        (testing "opiskeluoikeus is TUVA related."
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus (assoc-in oo-test/opiskeluoikeus-1
                                              [:tyyppi :koodiarvo] "tuva")
                    :jakso test-jakso})
                 [:ei-laheteta :opiskeluoikeus-oid :tuva-opiskeluoikeus])))
        (testing "opiskeluoikeus is linked to another opiskeluoikeus"
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-3
                    :jakso test-jakso})
                 [:ei-laheteta :opiskeluoikeus-oid :liittyva-opiskeluoikeus]))))
      (testing "initiate kysely if when all of the checks are OK."
        (is (= (tep/initial-palaute-state-and-reason
                 {:hoks           hoks-test/hoks-1
                  :opiskeluoikeus oo-test/opiskeluoikeus-1
                  :jakso test-jakso})
               [:odottaa-kasittelya :loppu :hoks-tallennettu]))))))

(defn- build-expected-herate
  [jakso hoks]
  (let [heratepvm (:loppu jakso)
        voimassa-alkupvm (tep/next-niputus-date heratepvm)]
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
        (tep/initiate-if-needed! {:hoks           hoks-test/hoks-1
                                  :opiskeluoikeus oo-test/opiskeluoikeus-1}
                                 test-jakso)
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
                  :lisatiedot   {:loppu "2023-12-15"}}))))
      (testing
       "doesn't initiate tyoelamapalaute if it has already been initiated"
        (with-log
          (let [existing
                (palaute/get-by-hoks-id-and-yksiloiva-tunniste!
                  db/spec
                  {:hoks-id (:id hoks-test/hoks-1)
                   :yksiloiva-tunniste (:yksiloiva-tunniste test-jakso)})]
            (palaute/update! db/spec (assoc existing :tila "lahetetty"))
            (tep/initiate-if-needed! {:hoks           hoks-test/hoks-1
                                      :opiskeluoikeus oo-test/opiskeluoikeus-1}
                                     test-jakso)
            (is (logged? 'oph.ehoks.palaute.tyoelama
                         :info
                         #":jo-lahetetty"))))))))

(deftest test-initiate-all-uninitiated!
  (with-redefs [date/now (constantly (LocalDate/of 2023 10 18))
                organisaatio/get-organisaatio!
                organisaatio-test/mock-get-organisaatio!]
    (db-hoks/insert-hoks!
      {:id                 (:id hoks-test/hoks-1)
       :oppija-oid         (:oppija-oid hoks-test/hoks-1)
       :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks-test/hoks-1)})
    (testing (str "The function successfully initiates kyselys for every "
                  "tyopaikkajakso in HOKS.")
      (tep/initiate-all-uninitiated! {:hoks hoks-test/hoks-1
                                      :opiskeluoikeus oo-test/opiskeluoikeus-1})
      (doseq [jakso (tep/tyopaikkajaksot hoks-test/hoks-1)]
        (let [real (-> (palaute/get-by-hoks-id-and-yksiloiva-tunniste!
                         db/spec
                         {:hoks-id            (:id hoks-test/hoks-1)
                          :yksiloiva-tunniste (:yksiloiva-tunniste jakso)})
                       (dissoc :id :created-at :updated-at)
                       (->> (remove-vals nil?)))
              expected (build-expected-herate jakso hoks-test/hoks-1)]
          (is (= real expected)
              ["diff: " (clojure.data/diff real expected)]))))))

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
    :hoks_id
    :hankkimistapa_tyyppi
    :tyopaikan_nimi
    :tyopaikan_ytunnus
    :jakso_loppupvm
    :ohjaaja_puhelinnumero
    :osaamisala
    :osa_aikaisuus
    :tutkinnonosa_tyyppi
    :tpk-niputuspvm
    :tallennuspvm
    :oppilaitos
    :tunnus
    :ohjaaja_ytunnus_kj_tutkinto
    :kasittelytila
    :tutkinnonosa_id
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
    :oppisopimuksen_perusta})

(deftest test-handle-all-palautteet-waiting-for-vastaajatunnus!
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
      (tep/handle-all-palautteet-waiting-for-vastaajatunnus! {})
      (let [palautteet (hoks-utils/palautteet-joissa-vastaajatunnus)
            ddb-jaksot (far/scan @ddb/faraday-opts @(ddb/tables :jakso) {})
            ddb-niput  (far/scan @ddb/faraday-opts @(ddb/tables :nippu) {})
            tapahtumat (db-helpers/query
                         [(str "select * from palaute_tapahtumat "
                               "where uusi_tila = "
                               "'vastaajatunnus_muodostettu'")])]
        (is (= (count palautteet) 5))
        (is (= (count ddb-jaksot) 5))
        (is (= ddb-niput expected-ddb-niput))
        (is (= (count tapahtumat) 5))
        (is (= (set (map :arvo_tunniste palautteet))
               (set (map :tunnus ddb-jaksot))))
        (doseq [jakso ddb-jaksot]
          (is (every? some? (map #(get jakso %) required-jakso-keys)))
          (is (empty? (s/difference
                        (set (keys jakso))
                        (set (concat required-jakso-keys
                                     optional-jakso-keys))))))))))

(deftest test-handle-palaute-waiting-for-vastaajatunnus!-error-handling
  (clear-ddb-jakso-table!)
  (clear-ddb-tpo-nippu-table!)
  ; Test initialization
  (is (= (:status (hoks-utils/create-hoks-in-the-past!)) 200))

  (let [initial-palautteet  (hoks-utils/palautteet)
        tep-palaute (nth
                      (palaute/get-tep-palautteet-waiting-for-vastaajatunnus!
                        db/spec {:heratepvm (str (date/now))}) 1)
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
            (is (thrown-with-msg?
                  ExceptionInfo
                  #"Arvo error"
                  (tep/handle-palaute-waiting-for-vastaajatunnus!
                    tep-palaute))))
          (check-current-state-is-same-as-initial-state))
        (testing "jakso sync to Herätepalvelu fails."
          (with-redefs [heratepalvelu/sync-jakso!*
                        (fn [_] (throw (ex-info "Jakso sync error" {})))]
            (is (thrown-with-msg?
                  ExceptionInfo
                  #"Failed to sync jakso"
                  (tep/handle-palaute-waiting-for-vastaajatunnus!
                    tep-palaute))))
          (check-current-state-is-same-as-initial-state))
        (testing "nippu sync to Herätepalvelu fails."
          (with-redefs
           [heratepalvelu/sync-tpo-nippu!*
            (fn [_] (throw (ex-info "TPO-nippu sync failed" {})))]
            (is (thrown-with-msg?
                  ExceptionInfo
                  #"Failed to sync TPO-nippu"
                  (tep/handle-palaute-waiting-for-vastaajatunnus!
                    tep-palaute))))
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
            (is (thrown-with-msg?
                  ExceptionInfo
                  #"Error while fetching opiskeluoikeus"
                  (tep/handle-palaute-waiting-for-vastaajatunnus! tep-palaute)))
            (is (= counter-value-before-fn-call @create-jaksotunnus-counter)))
          (check-current-state-is-same-as-initial-state))
        (testing (str "When opiskeluoikeus for palaute is not found from from "
                      "Koski, `tila` for it should be marked as `ei_laheteta`. "
                      "No call to Arvo should be made.")
          (with-redefs [koski/get-opiskeluoikeus! (fn [_] nil)]
            (tep/handle-palaute-waiting-for-vastaajatunnus! tep-palaute)
            (is (= counter-value-before-fn-call @create-jaksotunnus-counter))
            (is (= (:tila (palaute/get-by-id! db/spec {:id (:id tep-palaute)}))
                   "ei_laheteta"))))
        (testing (str "Palaute should be marked as \"ei_laheteta\" when there "
                      "are one or more open keskeytymisajanjakso. No call to "
                      "Arvo should be made.")
          (with-redefs [oht/get-keskeytymisajanjaksot!
                        (fn [_ __] [{:alku  (LocalDate/of 2023 11 1)
                                     :loppu (LocalDate/of 2023 11 16)}
                                    {:alku  (LocalDate/of 2024 02 5)}])]
            (tep/handle-palaute-waiting-for-vastaajatunnus! tep-palaute)
            (is (= counter-value-before-fn-call @create-jaksotunnus-counter))
            (is (= (:tila (palaute/get-by-id! db/spec {:id (:id tep-palaute)}))
                   "ei_laheteta"))))))))
