(ns oph.ehoks.palaute.opiskelija-test
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.tools.logging.test :refer [logged? with-log]]
            [medley.core :refer [assoc-some]]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.hoks.test-data :as test-data]
            [oph.ehoks.palaute.opiskelija :as opiskelijapalaute]))

(def hoksit
  (for [tarve  [true false nil]
        pvm    ["2023-09-01" nil]
        sposti ["testi.testaaja@testidomain.testi" nil]
        puh    ["0123456789" nil]]
    (assoc-some {}
                :osaamisen-hankkimisen-tarve tarve
                :osaamisen-saavuttamisen-pvm pvm
                :sahkoposti sposti
                :puhelinnumero puh)))

(def tuva-hoksit
  (for [tarve  [true false nil]
        pvm    ["2023-09-01" nil]
        sposti ["testi.testaaja@testidomain.testi" nil]
        puh    ["0123456789" nil]]
    (assoc-some {}
                :osaamisen-hankkimisen-tarve tarve
                :osaamisen-saavuttamisen-pvm pvm
                :sahkoposti sposti
                :puhelinnumero puh
                :hankittavat-koulutuksen-osat ["koulutuksen-osa"])))

(def tuva-rinnakkaiset-ammat-hoksit
  (for [tarve  [true false nil]
        pvm    ["2023-09-01" nil]
        sposti ["testi.testaaja@testidomain.testi" nil]
        puh    ["0123456789" nil]]
    (assoc-some {}
                :osaamisen-hankkimisen-tarve tarve
                :osaamisen-saavuttamisen-pvm pvm
                :sahkoposti sposti
                :puhelinnumero puh
                :tuva-opiskeluoikeus-oid "1.2.246.562.15.88406700034")))

(deftest test-initiate?
  (testing "On HOKS creation or update"
    (testing "don't initiate kysely if"
      (testing "`osaamisen-hankkimisen-tarve` is missing or is `false`."
        (doseq [hoks (filter #(not (:osaamisen-hankkimisen-tarve %)) hoksit)]
          (is (not (opiskelijapalaute/initiate? :aloituskysely hoks)))
          (is (not (opiskelijapalaute/initiate? :paattokysely  hoks))))
        (doseq [current-hoks hoksit
                updated-hoks (filter #(not (:osaamisen-hankkimisen-tarve %))
                                     hoksit)]
          (is (not (opiskelijapalaute/initiate? :aloituskysely
                                                current-hoks
                                                updated-hoks)))
          (is (not (opiskelijapalaute/initiate? :paattokysely
                                                current-hoks
                                                updated-hoks)))))

      (testing "HOKS is a TUVA-HOKS or a HOKS related to TUVA-HOKS."
        (doseq [hoks (concat tuva-hoksit tuva-rinnakkaiset-ammat-hoksit)]
          (is (not (opiskelijapalaute/initiate? :aloituskysely hoks)))
          (is (not (opiskelijapalaute/initiate? :paattokysely  hoks))))
        (doseq [current-hoks hoksit
                updated-hoks (concat tuva-hoksit
                                     tuva-rinnakkaiset-ammat-hoksit)]
          (is (not (opiskelijapalaute/initiate? :aloituskysely
                                                current-hoks
                                                updated-hoks)))
          (is (not (opiskelijapalaute/initiate? :paattokysely
                                                current-hoks
                                                updated-hoks)))))

      (testing (str "don't initiate päättökysely if "
                    "`osaamisen-saavuttamisen-pvm` is missing.")
        (doseq [hoks (filter #(not (:osaamisen-saavuttamisen-pvm %)) hoksit)]
          (is (not (opiskelijapalaute/initiate? :paattokysely  hoks))))
        (doseq [current-hoks hoksit
                updated-hoks (filter #(not (:osaamisen-saavuttamisen-pvm %))
                                     hoksit)]
          (is (not (opiskelijapalaute/initiate? :paattokysely
                                                current-hoks
                                                updated-hoks)))))))

  (testing "On HOKS creation"
    (testing
     "initiate aloituskysely if `osaamisen-hankkimisen-tarve` is `true`."
      (doseq [hoks (filter :osaamisen-hankkimisen-tarve hoksit)]
        (is (opiskelijapalaute/initiate? :aloituskysely hoks))))

    (testing (str "initiate paattokysely if `osaamisen-hankkimisen-tarve` is "
                  "`true` and `osaamisen-saavuttamisen-pvm` is not missing.")
      (doseq [hoks (filter #(and (:osaamisen-hankkimisen-tarve %)
                                 (:osaamisen-saavuttamisen-pvm %))
                           hoksit)]
        (is (opiskelijapalaute/initiate? :paattokysely hoks)))))

  (testing "On HOKS update"
    (testing
     "initiate aloituskysely if `osaamisen-hankkimisen-tarve` is added to HOKS."
      (doseq [current-hoks (filter #(not (:osaamisen-hankkimisen-tarve %))
                                   hoksit)
              updated-hoks (filter :osaamisen-hankkimisen-tarve hoksit)]
        (is (opiskelijapalaute/initiate? :aloituskysely
                                         current-hoks
                                         updated-hoks))))

    (testing (str "initiate aloituskysely if `sahkoposti` is added to HOKS and "
                  "`osaamisen-hankkimisen-tarve` is `true`.")
      (doseq [current-hoks (filter #(not (:sahkoposti %)) hoksit)
              updated-hoks (filter #(and (:sahkoposti %)
                                         (:osaamisen-hankkimisen-tarve %))
                                   hoksit)]
        (is (opiskelijapalaute/initiate? :aloituskysely
                                         current-hoks
                                         updated-hoks))))

    (testing (str "initiate aloituskysely if `puhelinnumero` is added to HOKS "
                  "and `osaamisen-hankkimisen-tarve` is `true`.")
      (doseq [current-hoks (filter #(not (:puhelinnumero %)) hoksit)
              updated-hoks (filter #(and (:puhelinnumero %)
                                         (:osaamisen-hankkimisen-tarve %))
                                   hoksit)]
        (is (opiskelijapalaute/initiate? :aloituskysely
                                         current-hoks
                                         updated-hoks))))

    (testing
     "initiate päättökysely if `osaamisen-saavuttamisen-pvm` is added to HOKS."
      (doseq [current-hoks (filter #(and (:osaamisen-hankkimisen-tarve %)
                                         (not (:osaamisen-saavuttamisen-pvm %)))
                                   hoksit)
              updated-hoks (filter #(and (:osaamisen-hankkimisen-tarve %)
                                         (:osaamisen-saavuttamisen-pvm %))
                                   hoksit)]
        (is (opiskelijapalaute/initiate? :paattokysely
                                         current-hoks
                                         updated-hoks))))

    (testing "don't initiate aloituskysely if"
      (testing "`sahkoposti` stays unchanged, is changed or is removed."
        (are [old-val new-val]
             (not (opiskelijapalaute/initiate?
                    :aloituskysely
                    {:osaamisen-hankkimisen-tarve true :sahkoposti old-val}
                    {:osaamisen-hankkimisen-tarve true :sahkoposti new-val}))
          "testi.testaaja@testidomain.testi" "testi.testaaja@testidomain.testi"
          "testi.testaaja@testidomain.testi" "testi.testinen@testi.domain"
          "testi.testaaja@testidomain.testi" nil))

      (testing "`puhelinnumero` stays unchanged, is changed or is removed."
        (are [old-val new-val]
             (not (opiskelijapalaute/initiate?
                    :aloituskysely
                    {:osaamisen-hankkimisen-tarve true
                     :puhelinnumero old-val}
                    {:osaamisen-hankkimisen-tarve true
                     :puhelinnumero new-val}))
          "0123456789" "0123456789"
          "0123456789" "0011223344"
          "0123456789" nil)))

    (testing (str "don't initiate päättökysely if `osaamisen-saavuttamisen-pvm`"
                  " stays unchanged, is changed or is removed.")
      (are [old-val new-val]
           (not (opiskelijapalaute/initiate?
                  :paattokysely
                  {:osaamisen-hankkimisen-tarve true
                   :osaamisen-saavuttamisen-pvm old-val}
                  {:osaamisen-hankkimisen-tarve true
                   :osaamisen-saavuttamisen-pvm new-val}))
        "2023-09-01" "2023-09-01"
        "2023-09-01" "2023-09-02"
        "2023-09-01" nil))))

(def sqs-msg (atom nil))

(defn mock-get-opiskeluoikeus-info-raw
  [oid]
  (case oid
    "1.2.246.562.15.10000000009" {:suoritukset
                                  [{:tyyppi
                                    {:koodiarvo "ammatillinentutkinto"}}]
                                  :tyyppi {:koodiarvo "ammatillinenkoulutus"}}
    "1.2.246.562.15.20000000008" {:suoritukset
                                  [{:tyyppi {:koodiarvo "joku_muu"}}]
                                  :tyyppi {:koodiarvo "ammatillinenkoulutus"}}
    (throw (ex-info "Opiskeluoikeus not found"
                    {:status 404
                     :body (str "[{\"key\": \"notFound.opiskeluoikeutta"
                                "EiLöydyTaiEiOikeuksia\"}]")}))))

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
  (with-redefs [sqs/send-amis-palaute-message (fn [msg] (reset! sqs-msg msg))
                k/get-opiskeluoikeus-info-raw mock-get-opiskeluoikeus-info-raw]
    (let [hoks (assoc test-data/hoks-data :id 1)]
      (testing "Testing that function `initiate!`"
        (testing "successfully initiates aloituskysely and paattokysely"
          (are [kysely] (= (expected-msg kysely hoks)
                           (do (opiskelijapalaute/initiate! kysely hoks)
                               @sqs-msg))
            :aloituskysely
            :paattokysely))
        (testing "logs appropriately when kyselys could not be initiated."
          (with-log
            (opiskelijapalaute/initiate!
              :paattokysely
              (assoc hoks :opiskeluoikeus-oid "1.2.246.562.15.20000000008"))
            (is (logged? 'oph.ehoks.palaute.opiskelija
                         :info
                         #"No ammatillinen suoritus"))
            (opiskelijapalaute/initiate!
              :paattokysely
              (assoc hoks :opiskeluoikeus-oid "1.2.246.562.15.30000000007"))
            (is (logged? 'oph.ehoks.palaute.opiskelija
                         :warn
                         #"not found in Koski"))))))))
