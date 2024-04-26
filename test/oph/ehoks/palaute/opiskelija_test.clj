(ns oph.ehoks.palaute.opiskelija-test
  (:require [clojure.test :refer [are deftest is testing use-fixtures]]
            [clojure.tools.logging.test :refer [logged? with-log]]
            [medley.core :refer [remove-vals]]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.hoks.test-data :as test-data]
            [oph.ehoks.palaute.opiskelija :as opiskelijapalaute]
            [oph.ehoks.test-utils :as test-utils]
            [oph.ehoks.utils.date :as date])
  (:import (java.time LocalDate)))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-database-after-test)

(def test-hoks
  {:id                          12345
   :osaamisen-hankkimisen-tarve true
   :ensikertainen-hyvaksyminen  (LocalDate/of 2023 04 16)
   :osaamisen-saavuttamisen-pvm (LocalDate/of 2024 02 05)
   :sahkoposti                  "testi.testaaja@testidomain.testi"
   :puhelinnumero               "0123456789"})

(def test-hoks*
  (dissoc test-hoks :osaamisen-hankkimisen-tarve :osaamisen-saavuttamisen-pvm))

(def opiskeluoikeus-1
  "Opiskeluoikeus with ammatillinen suoritus"
  {:suoritukset [{:tyyppi {:koodiarvo "ammatillinentutkinto"}}]
   :tyyppi      {:koodiarvo "ammatillinenkoulutus"}})

(def opiskeluoikeus-2
  "Opiskeluoikeus without ammatillinen suoritus"
  {:suoritukset [{:tyyppi {:koodiarvo "joku_muu"}}]
   :tyyppi      {:koodiarvo "ammatillinenkoulutus"}})

(def opiskeluoikeus-3
  "Opiskeluoikeus that is linked to another opiskeluoikeus"
  {:suoritukset               [{:tyyppi {:koodiarvo "ammatillinentutkinto"}}]
   :tyyppi                    {:koodiarvo "ammatillinenkoulutus"}
   :sisältyyOpiskeluoikeuteen {:oid "1.2.246.562.15.10000000009"}})

(def sqs-msg (atom nil))

(defn mock-get-opiskeluoikeus-info-raw
  [oid]
  (case oid
    "1.2.246.562.15.10000000009" {:suoritukset
                                  [{:tyyppi
                                    {:koodiarvo "ammatillinentutkinto"}}]
                                  :tyyppi {:koodiarvo "ammatillinenkoulutus"}
                                  :koulutustoimija
                                  {:oid "1.2.246.562.10.346830761110"}}
    "1.2.246.562.15.20000000008" {:suoritukset
                                  [{:tyyppi {:koodiarvo "joku_muu"}}]
                                  :tyyppi {:koodiarvo "ammatillinenkoulutus"}}
    "1.2.246.562.15.30000000007" {:suoritukset
                                  [{:tyyppi
                                    {:koodiarvo "ammatillinentutkinto"}}]
                                  :tyyppi {:koodiarvo "ammatillinenkoulutus"}
                                  :sisältyyOpiskeluoikeuteen
                                  {:oid "1.2.246.562.15.10000000009"}}
    (throw (ex-info "Opiskeluoikeus not found"
                    {:status 404
                     :body (str "[{\"key\": \"notFound.opiskeluoikeutta"
                                "EiLöydyTaiEiOikeuksia\"}]")}))))

(deftest test-initiate?
  (with-redefs [date/now (constantly (LocalDate/of 2023 1 1))]
    (let [opiskeluoikeus (mock-get-opiskeluoikeus-info-raw
                           "1.2.246.562.15.10000000009")]
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
                    :aloituskysely test-hoks opiskeluoikeus #"Herate date")))

              (doseq [pvm ["2024-07-01" "2024-12-31" "2025-01-06"]]
                (with-redefs [date/now (constantly (LocalDate/parse pvm))]
                  (test-not-initiated
                    :paattokysely test-hoks opiskeluoikeus #"Herate date"))))

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
                      opiskeluoikeus))
            "2023-09-01" "2023-09-01"
            "2023-09-01" "2023-09-02"
            "2023-09-01" nil))))))

(defn expected-msg
  [kysely hoks]
  {:ehoks-id (:id hoks)
   :kyselytyyppi (case kysely
                   :aloituskysely "aloittaneet"
                   :paattokysely  "tutkinnon_suorittaneet")
   :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
   :oppija-oid (:oppija-oid hoks)
   :sahkoposti (:sahkoposti hoks)
   :puhelinnumero (:puhelinnumero hoks)
   :alkupvm
   (case kysely
     :aloituskysely (:ensikertainen-hyvaksyminen hoks)
     :paattokysely  (:osaamisen-saavuttamisen-pvm hoks))})

(deftest test-initiate!
  (with-redefs [sqs/send-amis-palaute-message (fn [msg] (reset! sqs-msg msg))]
    (let [hoks (assoc test-data/hoks-data :id 1)
          opiskeluoikeus (mock-get-opiskeluoikeus-info-raw
                           (:opiskeluoikeus-oid hoks))]
      (db-hoks/insert-hoks! {:oppija-oid         (:oppija-oid hoks)
                             :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)})
      (testing "Testing that function `initiate!`"
        (testing (str "stores kysely info to `palautteet` DB table and "
                      "successfully sends aloituskysely and paattokysely "
                      "herate to SQS queue")
          (are [kysely] (= (expected-msg kysely hoks)
                           (do (opiskelijapalaute/initiate!
                                 kysely hoks opiskeluoikeus)
                               @sqs-msg))
            :aloituskysely
            :paattokysely)
          (are [kyselytyyppi heratepvm]
               (= (-> (opiskelijapalaute/get-by-hoks-id-and-kyselytyypit!
                        db/spec {:hoks-id (:id hoks)
                                 :kyselytyypit [kyselytyyppi]})
                      first
                      (dissoc :id :created-at :updated-at)
                      (->> (remove-vals nil?)))
                  {:tila "odottaa_kasittelya"
                   :kyselytyyppi kyselytyyppi
                   :hoks-id 1
                   :heratepvm heratepvm
                   :koulutustoimija "1.2.246.562.10.346830761110"
                   :herate-source "ehoks_update"})
            "aloittaneet"  (LocalDate/parse (:ensikertainen-hyvaksyminen hoks))
            "valmistuneet" (LocalDate/parse
                             (:osaamisen-saavuttamisen-pvm hoks))))
        (testing "doesn't initiate kysely if one already exists for HOKS"
          (are [kysely] (nil? (opiskelijapalaute/initiate!
                                kysely hoks opiskeluoikeus))
            :aloituskysely :paattokysely))))))
