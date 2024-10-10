(ns oph.ehoks.palaute.tyoelama-test
  (:require [clojure.test :refer [are deftest is testing use-fixtures]]
            [clojure.tools.logging.test :refer [logged? with-log]]
            [clojure.set :as s]
            [medley.core :refer [remove-vals]]
            [taoensso.faraday :as far]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.db-helpers :as db-helpers]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.external.organisaatio-test :as organisaatio-test]
            [oph.ehoks.hoks-test :as hoks-test]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils]
            [oph.ehoks.opiskeluoikeus-test :as oo-test]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.tapahtuma :as palautetapahtuma]
            [oph.ehoks.palaute.tyoelama :as tep]
            [oph.ehoks.test-utils :as test-utils]
            [oph.ehoks.utils.date :as date])
  (:import (java.time LocalDate)))

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
        (testing "there is already herate for tyopaikkajakso."
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-1
                    :jakso          test-jakso}
                   {:existing-heratteet [{:yksiloiva-tunniste "asd"}]})
                 [nil :yksiloiva-tunniste :jo-lahetetty])))
        (testing "opiskeluoikeus is in terminal state."
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-5
                    :jakso test-jakso}
                   {})
                 [:ei-laheteta :opiskeluoikeus-oid :opiskelu-paattynyt])))
        (testing "osa-aikaisuus is missing from työpaikkajakso"
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-1
                    :jakso (dissoc test-jakso :osa-aikaisuustieto)}
                   {})
                 [:ei-laheteta :osa-aikaisuustieto :ei-ole])))
        (testing "workplace information is missing from työpaikkajakso"
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-1
                    :jakso (update test-jakso
                                   :tyopaikalla-jarjestettava-koulutus
                                   dissoc
                                   :tyopaikan-y-tunnus)}
                   {})
                 [:ei-laheteta :tyopaikalla-jarjestettava-koulutus
                  :puuttuva-yhteystieto])))
        (testing "työpaikkajakso is interrupted on it's end date"
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-1
                    :jakso (assoc-in test-jakso
                                     [:keskeytymisajanjaksot 1]
                                     {:alku  (LocalDate/of 2023 12 1)
                                      :loppu (LocalDate/of 2023 12 15)})}
                   {})
                 [:ei-laheteta :keskeytymisajanjaksot :jakso-keskeytynyt])))
        (testing "opiskeluoikeus doesn't have any ammatillinen suoritus"
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-2
                    :jakso test-jakso}
                   {})
                 [:ei-laheteta :opiskeluoikeus-oid :ei-ammatillinen])))
        (testing "there is a feedback preventing code in opiskeluoikeusjakso."
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-4
                    :jakso test-jakso}
                   {})
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
                      :jakso test-jakso}
                     {})
                   [:ei-laheteta
                    :tuva-opiskeluoikeus-oid
                    :tuva-opiskeluoikeus]))))
        (testing "opiskeluoikeus is TUVA related."
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus (assoc-in oo-test/opiskeluoikeus-1
                                              [:tyyppi :koodiarvo] "tuva")
                    :jakso test-jakso}
                   {})
                 [:ei-laheteta :opiskeluoikeus-oid :tuva-opiskeluoikeus])))
        (testing "opiskeluoikeus is linked to another opiskeluoikeus"
          (is (= (tep/initial-palaute-state-and-reason
                   {:hoks           hoks-test/hoks-1
                    :opiskeluoikeus oo-test/opiskeluoikeus-3
                    :jakso test-jakso}
                   {})
                 [:ei-laheteta :opiskeluoikeus-oid :liittyva-opiskeluoikeus]))))
      (testing "initiate kysely if when all of the checks are OK."
        (is (= (tep/initial-palaute-state-and-reason
                 {:hoks           hoks-test/hoks-1
                  :opiskeluoikeus oo-test/opiskeluoikeus-1
                  :jakso test-jakso}
                 {})
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
                       (first)
                       (dissoc :id :created-at :updated-at)
                       (->> (remove-vals nil?)))
              expected (build-expected-herate test-jakso hoks-test/hoks-1)]
          (is (= real expected)
              ["diff: " (clojure.data/diff real expected)]))
        (let [tapahtumat (palautetapahtuma/get-all-by-hoks-id-and-kyselytyypit!
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
                (->> {:hoks-id (:id hoks-test/hoks-1)
                      :yksiloiva-tunniste (:yksiloiva-tunniste test-jakso)}
                     (palaute/get-by-hoks-id-and-yksiloiva-tunniste! db/spec)
                     (first))]
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
                       (first)
                       (dissoc :id :created-at :updated-at)
                       (->> (remove-vals nil?)))
              expected (build-expected-herate jakso hoks-test/hoks-1)]
          (is (= real expected)
              ["diff: " (clojure.data/diff real expected)]))))))

(defn clear-ddb-jakso-table! []
  (doseq [jakso (far/scan @ddb/faraday-opts @(ddb/tables :jakso) {})]
    (far/delete-item @ddb/faraday-opts @(ddb/tables :jakso)
                     {:hankkimistapa_id (:hankkimistapa_id jakso)})))

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

(deftest test-create-and-save-arvo-vastaajatunnus-for-all-needed!
  (clear-ddb-jakso-table!)
  (testing (str "create-and-save-arvo-vastaajatunnus-for-all-needed! "
                "calls Arvo, saves vastaajatunnus to db, "
                "and syncs palaute to heratepalvelu")
    (with-redefs [organisaatio/get-organisaatio!
                  hoks-utils/mock-get-organisaatio!
                  koski/get-opiskeluoikeus! hoks-utils/mock-get-opiskeluoikeus!
                  arvo/create-jaksotunnus hoks-utils/mock-create-jaksotunnus
                  date/now #(LocalDate/of 2024 6 30)]
      (is (= (:status (hoks-utils/create-hoks-in-the-past!)) 200))
      (tep/create-and-save-arvo-vastaajatunnus-for-all-needed! {})
      (let [palautteet (hoks-utils/palautteet-joissa-vastaajatunnus)
            ddb-jaksot
            (far/scan @ddb/faraday-opts @(ddb/tables :jakso) {})
            tapahtumat
            (db-helpers/query
              [(str "select * from palaute_tapahtumat "
                    "where uusi_tila = 'vastaajatunnus_muodostettu'")])]
        (is (= (count palautteet) 5))
        (is (= (count ddb-jaksot) 5))
        (is (= (count tapahtumat) 5))
        (is (= (set (map :arvo_tunniste palautteet))
               (set (map :tunnus ddb-jaksot))))
        (doseq [jakso ddb-jaksot]
          (is (every? some? (map #(get jakso %) required-jakso-keys)))
          (is (empty? (s/difference
                        (set (keys jakso))
                        (set (concat required-jakso-keys
                                     optional-jakso-keys))))))))))

(deftest test-create-and-save-arvo-vastaajatunnus-for-all-needed!-error-handling
  (testing (str "create-and-save-arvo-vastaajatunnus-for-all-needed! "
                "error handling when error occurs in")
    (with-redefs [organisaatio/get-organisaatio!
                  hoks-utils/mock-get-organisaatio!
                  koski/get-opiskeluoikeus! hoks-utils/mock-get-opiskeluoikeus!
                  arvo/create-jaksotunnus hoks-utils/mock-create-jaksotunnus
                  date/now #(LocalDate/of 2024 6 30)]
      (is (= (:status (hoks-utils/create-hoks-in-the-past!)) 200))
      (is (= (count (hoks-utils/kasittelemattomat-palauteet)) 5))

      (testing "Arvo call it should rollback palaute to earlier state"
        (with-redefs [arvo/create-jaksotunnus
                      (fn [_] (throw (ex-info "Arvo error" {})))]
          (tep/create-and-save-arvo-vastaajatunnus-for-all-needed! {})
          (is (= (count (hoks-utils/kasittelemattomat-palauteet)) 5))))

      (testing (str "DynamoDB sync it should rollback palaute to earlier "
                    "state and try to delete vastaajatunnus from Arvo")
        (let [delete-count (atom 0)]
          (with-redefs [tep/sync-jakso-to-heratepalvelu!
                        (fn [_ __ ___ ____ _____]
                          (throw (ex-info "DDB sync error" {})))
                        arvo/delete-jaksotunnus
                        (fn [tunnus]
                          (assert tunnus)
                          (swap! delete-count inc))]
            (tep/create-and-save-arvo-vastaajatunnus-for-all-needed! {})
            (is (= @delete-count 5))
            (is (= (count (hoks-utils/kasittelemattomat-palauteet)) 5)))))

      (testing (str "getting opiskeluoikeus from Koski (not found) it "
                    "should skip getting vastaajatunnus for that "
                    "jakso/hoks")
        (with-redefs [koski/get-opiskeluoikeus! (fn [_] nil)]
          (tep/create-and-save-arvo-vastaajatunnus-for-all-needed! {})
          (is (= (count (hoks-utils/kasittelemattomat-palauteet)) 5))))
      (testing (str "getting opiskeluoikeus from Koski (other http error) "
                    "it should skip getting vastaajatunnus for that "
                    "jakso/hoks")
        (with-redefs [koski/get-opiskeluoikeus!
                      (fn [oid]
                        (throw (ex-info "Koski error" {:status 500})))]
          (tep/create-and-save-arvo-vastaajatunnus-for-all-needed! {})
          (is (= (count (hoks-utils/kasittelemattomat-palauteet)) 5)))))))
