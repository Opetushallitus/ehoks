(ns oph.ehoks.palaute.lahetys-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.string :as s]
            [oph.ehoks.config :as config]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.dynamodb :as ddb]
            [oph.ehoks.hoks]
            [oph.ehoks.hoks.handler :as hoks-handler]
            [oph.ehoks.hoks-test :as hoks-test]
            [oph.ehoks.oppija.auth-handler-test :refer [mock-get-oppija-raw!]]
            [oph.ehoks.external.http-client :refer [with-mock-responses]]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.koski-test :as koski-test]
            [oph.ehoks.external.oppijanumerorekisteri :as onr]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.external.organisaatio-test :as organisaatio-test]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.opiskeluoikeus-test :as oo-test]
            [oph.ehoks.test-utils :as util]
            [oph.ehoks.utils.date :as date]
            [oph.ehoks.palaute :as palaute]
            [oph.ehoks.palaute.opiskelija :as op]
            [oph.ehoks.palaute.opiskelija.kyselylinkki :as kyselylinkki]
            [oph.ehoks.palaute.handling :as handling]
            [oph.ehoks.palaute.lahetys :as l]
            [oph.ehoks.palaute.vastaajatunnus :as vt])
  (:import (java.time LocalDate ZonedDateTime ZoneId Instant)))

(use-fixtures :once util/migrate-database)
(use-fixtures :each util/empty-both-dbs-after-test)

(def esim-ctx
  {:hoks {:id 1, :sahkoposti "testikayttaja@testi.fi"},
   :suoritus
   {:perusteenDiaarinumero "77/011/2014",
    :suorituskieli {:koodiarvo "SV"},
    :koulutusmoduuli
    {:tunniste
     {:koodiarvo "543496", :nimi {:fi "Sähköalan perustutkinto"}},
     :koodistoUri "koulutus",
     :koodistoVersio 11},
    :alkamispäivä "2016-08-05",
    :osasuoritukset [],
    :toimipiste {:oid "1.2.246.562.10.47353291801"},
    :tyyppi {:koodiarvo "ammatillinentutkinto"},
    :suoritustapa {},
    :perusteenNimi {:fi "Sähköalan perustutkinto"},
    :koulutustyyppi
    {:koodiarvo "1",
     :nimi
     {:fi "Ammatillinen perustutkinto",
      :sv "Yrkesinriktad grundexamen",
      :en "Vocational upper secondary qualification"},
     :lyhytNimi
     {:fi "Ammatillinen perustutkinto", :sv "Yrkesinriktad grundexamen"},
     :koodistoUri "koulutustyyppi",
     :koodistoVersio 2}},
   :existing-palaute
   {:tila "ei_laheteta",
    :kyselytyyppi "aloittaneet",
    :kyselylinkki "http://kysely",
    :voimassa-loppupvm "2017-12-23",
    :heratepvm "2017-10-25",
    :herate-source "ehoks_update",
    :toimipiste-oid "1.2.246.562.10.47353291801",
    :id 1,
    :tutkintotunnus "548313",
    :hoks-id 1,
    :tutkintonimike "(\"\")",
    :voimassa-alkupvm "2026-03-13"}})

(deftest test-send-palaute-initial-email!
  (testing "Test that mail is sent properly for given palaute."
    (let [req (atom nil)]
      (util/with-ticket-auth
        ["1.2.246.562.10.22222222220"
         (fn [_ url options]
           (when (s/ends-with? url "lahetys/v1/viestit")
             (reset! req options)
             {:status 200
              :body {:lahetysTunniste "019cb395-5840-70fa-96c9-918eec8a6f41"
                     :viestiTunniste "019cb395-viestitunniste"}}))]
        (is (= (l/send-palaute-initial-email! esim-ctx)
               "019cb395-5840-70fa-96c9-918eec8a6f41"))
        (is (s/includes?
              (:body @req)
              (str "\"vastaanottajat\":[{\"sahkopostiOsoite\":"
                   "\"testikayttaja@testi.fi\"}]")))
        (is (s/includes? (:body @req) "\"sisalto\":\"<!DOCTYPE html><html"))
        (is (s/includes? (:body @req) "<html lang=\\\"sv\\\">"))
        (is (s/includes?
              (:body @req)
              "<p><a href=\\\"http:\\/\\/kysely?t=e\\\">"))))))

(def business-hours-instant
  (Instant/from (ZonedDateTime/of 2026 4 14 10 0 0 0
                                  (ZoneId/of "Europe/Helsinki"))))

(def non-business-hours-instant
  (Instant/from (ZonedDateTime/of 2026 4 19 10 0 0 0
                                  (ZoneId/of "Europe/Helsinki"))))

(def esim-sms-ctx
  (-> esim-ctx
      (assoc-in [:hoks :puhelinnumero] "040 654 3210")
      (assoc-in [:existing-palaute :kyselytyyppi] "valmistuneet")))

(deftest test-check-palaute-for-sending
  (let [viestit (atom {})]
    (with-redefs [date/now-with-time (constantly business-hours-instant)
                  palaute/ehoks-sends-sms? (constantly true)
                  l/palauteviesti-tila (fn [_ viestityyppi]
                                         (get @viestit viestityyppi))]
      (testing "email:"
        (testing "returns go for example context"
          (is (= [:odottaa-lahetysta nil :voimassa-alkupvm :viesti-lahetys]
                 (l/check-palaute-for-sending esim-ctx "email"))))
        (testing "doesn't handle anything already in heratepalvelu"
          (is (= [nil :heratepalvelussa :heratepvm :heratepalvelun-vastuulla]
                 (l/check-palaute-for-sending
                   (assoc esim-ctx :existing-ddb-herate
                          (delay {:toimija_oppija "foo/bar"}))
                   "email"))))
        (testing "no action if email is missing"
          (is (= [:lahetys-epaonnistunut nil :sahkoposti :ei-ole]
                 (l/check-palaute-for-sending
                   (assoc-in esim-ctx [:hoks :sahkoposti] "") "email"))))
        (testing "don't send messages for overdue palautekysely"
          (is (= [nil :vastausaika-loppunut :voimassa-loppupvm :arvo-paivitys]
                 (l/check-palaute-for-sending
                   (assoc esim-ctx :arvo-status
                          (delay {:vastattu false
                                  :voimassa-loppupvm "2021-01-01T00:00:00Z"}))
                   "email"))))
        (testing "don't send messages for already answered palautekysely"
          (is (= [nil :vastattu :heratepvm :arvo-paivitys]
                 (l/check-palaute-for-sending
                   (assoc esim-ctx :arvo-status
                          (delay {:vastattu true
                                  :voimassa-loppupvm "2051-01-01T00:00:00Z"}))
                   "email"))))
        (testing "Herätepalvelu record doesn't matter after SMS handling"
          (reset! viestit {"sms" "lahetetty"})
          (is (= [:odottaa-lahetysta nil :voimassa-alkupvm :viesti-lahetys]
                 (l/check-palaute-for-sending
                   (assoc esim-ctx :existing-ddb-herate
                          (delay {:toimija_oppija "foo/bar"}))
                   "email"))))
        (testing "don't send the same message twice"
          (reset! viestit {"email" "odottaa_lahetysta"})
          (is (= [nil nil :viestityyppi :jo-kasitelty]
                 (l/check-palaute-for-sending esim-ctx "email")))))

      (testing "sms:"
        (reset! viestit {})
        (testing "returns go for example context"
          (is (= [:odottaa-lahetysta nil :voimassa-alkupvm :viesti-lahetys]
                 (l/check-palaute-for-sending esim-sms-ctx "sms"))))
        (testing "doesn't handle anything already in heratepalvelu"
          (is (= [nil :heratepalvelussa :heratepvm :heratepalvelun-vastuulla]
                 (l/check-palaute-for-sending
                   (assoc esim-sms-ctx :existing-ddb-herate
                          (delay {:toimija_oppija "foo/bar"}))
                   "sms"))))
        (testing "Herätepalvelu record doesn't matter after email handling"
          (reset! viestit {"email" "odottaa_lahetysta"})
          (is (= [:odottaa-lahetysta nil :voimassa-alkupvm :viesti-lahetys]
                 (l/check-palaute-for-sending
                   (assoc esim-sms-ctx :existing-ddb-herate
                          (delay {:toimija_oppija "foo/bar"}))
                   "sms"))))
        (testing "no SMS for kyselytyypit that don't get one"
          (is (= [nil nil :kyselytyyppi :ei-tekstiviestia]
                 (l/check-palaute-for-sending esim-ctx "sms"))))
        (testing "fails if phone number is missing"
          (is (= [:lahetys-epaonnistunut nil :puhelinnumero :ei-ole]
                 (l/check-palaute-for-sending
                   (assoc-in esim-sms-ctx [:hoks :puhelinnumero] nil)
                   "sms"))))
        (testing "fails if phone number is not a Finnish mobile number"
          (is (= [:lahetys-epaonnistunut nil :puhelinnumero :virheellinen]
                 (l/check-palaute-for-sending
                   (assoc-in esim-sms-ctx [:hoks :puhelinnumero]
                             "+1 517 987 5432")
                   "sms"))))
        (testing "don't send messages for overdue palautekysely"
          (is (= [nil :vastausaika-loppunut :voimassa-loppupvm :arvo-paivitys]
                 (l/check-palaute-for-sending
                   (assoc esim-sms-ctx :arvo-status
                          (delay {:vastattu false
                                  :voimassa-loppupvm "2021-01-01T00:00:00Z"}))
                   "sms"))))
        (testing "don't send the same SMS twice"
          (reset! viestit {"email" "lahetetty" "sms" "lahetetty"})
          (is (= [nil nil :viestityyppi :jo-kasitelty]
                 (l/check-palaute-for-sending esim-sms-ctx "sms"))))
        (testing "no SMS unless eHOKS is responsible for them"
          (with-redefs [palaute/ehoks-sends-sms? (constantly false)]
            (is (= [nil nil :viestityyppi :ei-ehoksin-vastuulla]
                   (l/check-palaute-for-sending esim-sms-ctx "sms"))))))))
  (with-redefs [date/now-with-time (constantly non-business-hours-instant)
                palaute/ehoks-sends-sms? (constantly true)
                l/palauteviesti-tila (constantly nil)]
    (testing "no action outside Finnish business hours"
      (is (= [nil nil :heratepvm :kasittely-odottaa-toimistoaikaa]
             (l/check-palaute-for-sending esim-ctx "email")))
      (is (= [nil nil :heratepvm :kasittely-odottaa-toimistoaikaa]
             (l/check-palaute-for-sending esim-sms-ctx "sms"))))))

(deftest test-handle-unsent-palaute!
  (with-redefs [date/now (constantly (LocalDate/of 2023 4 18))
                date/now-with-time (constantly business-hours-instant)
                onr/get-oppija-raw! mock-get-oppija-raw!
                koski/get-oppija-opiskeluoikeudet
                koski-test/mock-get-oppija-opiskeluoikeudet
                koski/get-opiskeluoikeus-info-raw
                koski-test/mock-get-opiskeluoikeus-raw
                organisaatio/get-organisaatio!
                organisaatio-test/mock-get-organisaatio!]
    (oppijaindex/add-hoks-dependents-in-index! hoks-test/hoks-1)
    (let [ctx {:hoks hoks-test/hoks-1 :opiskeluoikeus oo-test/opiskeluoikeus-1}
          hoks (hoks-handler/save-hoks-and-initiate-all-palautteet! ctx)
          sent-message (atom {})]
      (testing "handle-unsent-palaute! sending messages"
        (with-mock-responses
          [(fn [_ __] {})
           (fn [^String url _]
             (when (s/ends-with? url "/api/vastauslinkki/v1")
               {:status 200
                :body {:tunnus "testivain"
                       :kysely_linkki "https://arvovastaus.csc.fi/v/test"
                       :voimassa_loppupvm "2024-10-10"}}))]
          (->> {:kyselytyypit ["aloittaneet"]}
               (palaute/get-palautteet-waiting-for-vastaajatunnus! db/spec)
               (first)
               (vt/create-vastaajatunnus!)))
        (let [heratteet
              (->> {:kyselytyypit ["aloittaneet"] :viestityyppi "email"}
                   (palaute/get-unsent-palautteet! db/spec))]
          (is (= [["kysely_muodostettu" "aloittaneet" "testivain"
                   "https://arvovastaus.csc.fi/v/test"]]
                 (map (juxt :tila :kyselytyyppi :arvo-tunniste :kyselylinkki)
                      heratteet)))

          (testing "with successful arvo-status and sending"
            (with-mock-responses
              [(fn [url _]
                 (when (s/ends-with? url "/vastauslinkki/v1/status/testivain")
                   {:status 200
                    :body {:tunnus "test"
                           :voimassa_loppupvm "2026-04-14"
                           :vastattu false}}))
               (fn [^String url options]
                 (when (s/ends-with? url "/lahetys/v1/viestit")
                   (reset! sent-message (:body options))
                   {:status 200
                    :body {:viestiTunniste "brymir"
                           :lahetysTunniste "brymir"}}))]
              (is (= {:email "brymir" :sms nil}
                     (l/handle-unsent-palaute! (first heratteet)))))
            (is (s/includes?
                  @sent-message
                  "\"sahkopostiOsoite\":\"testi.testaaja@testidomain.testi\""))
            (is (s/includes?
                  @sent-message
                  "https:\\/\\/arvovastaus.csc.fi\\/v\\/test?t=e<\\/a>"))
            (is (= [] (->> {:kyselytyypit ["aloittaneet"] :viestityyppi "email"}
                           (palaute/get-unsent-palautteet! db/spec)
                           (map (juxt :tila :kyselytyyppi :kyselylinkki)))))
            (is (= [["kysely_muodostettu" "aloittaneet"
                     "https://arvovastaus.csc.fi/v/test"]]
                   (->> {:hoks-id (:id hoks) :kyselytyypit ["aloittaneet"]}
                        (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                        (map (juxt :tila :kyselytyyppi :kyselylinkki)))))
            (is (= [["odottaa_lahetysta" "brymir"]]
                   (->> {:viestityypit ["email"] :tila "odottaa_lahetysta"}
                        (l/get-by-tila-and-viestityypit! db/spec)
                        (map (juxt :viesti-tila :ulkoinen-tunniste))))))

          (db-ops/query
            ["DELETE FROM palaute_viestit WHERE palaute_id=? RETURNING id"
             (:id (first heratteet))])

          (testing "with expired kyselylinkki"
            (with-mock-responses
              [(fn [url _]
                 (when (s/ends-with? url "/vastauslinkki/v1/status/testivain")
                   {:status 200
                    :body {:tunnus "test"
                           :voimassa_loppupvm "2022-04-14"
                           :vastattu false}}))
               (fn [_ __] {})]
              (is (= {:email nil :sms nil}
                     (l/handle-unsent-palaute! (first heratteet)))))
            (is (= [["vastausaika_loppunut" "aloittaneet"
                     "https://arvovastaus.csc.fi/v/test"]]
                   (->> {:hoks-id (:id hoks) :kyselytyypit ["aloittaneet"]}
                        (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                        (map (juxt :tila :kyselytyyppi :kyselylinkki))))))

          (testing "with already vastattu kysely"
            (with-mock-responses
              [(fn [url _]
                 (when (s/ends-with? url "/vastauslinkki/v1/status/testivain")
                   {:status 200
                    :body {:tunnus "test"
                           :voimassa_loppupvm "2026-04-14"
                           :vastattu true}}))
               (fn [_ __] {})]
              (is (= {:email nil :sms nil}
                     (l/handle-unsent-palaute! (first heratteet)))))
            (is (= [["vastattu" "aloittaneet"
                     "https://arvovastaus.csc.fi/v/test"]]
                   (->> {:hoks-id (:id hoks) :kyselytyypit ["aloittaneet"]}
                        (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                        (map (juxt :tila :kyselytyyppi :kyselylinkki)))))
            (is (= ["vastattu"]
                   (->> "1.2.246.562.24.12312312319"
                        (kyselylinkki/get-by-oppija-oid!)
                        (map :lahetystila)))))

          (testing "with unsuccessful sending"
            (with-mock-responses
              [(fn [url _]
                 (when (s/ends-with? url "/vastauslinkki/v1/status/testivain")
                   {:status 200
                    :body {:tunnus "test"
                           :voimassa_loppupvm "2026-04-14"
                           :vastattu false}}))
               (fn [^String url _]
                 (when (s/ends-with? url "/lahetys/v1/viestit")
                   (throw (ex-info
                            "clj-http: status 400"
                            {:status 400
                             :body (str "{\"validointiVirheet\":["
                                        "\"jokin on pakollinen\"]}")}))))]
              (l/handle-unsent-palaute! (first heratteet)))
            (is (= [["kysely_muodostettu" "aloittaneet"
                     "https://arvovastaus.csc.fi/v/test"]]
                   (->> {:hoks-id (:id hoks) :kyselytyypit ["aloittaneet"]}
                        (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                        (map (juxt :tila :kyselytyyppi :kyselylinkki)))))
            (is (= [["lahetys_epaonnistunut" nil]]
                   (->> {:viestityypit ["email"]
                         :palaute-id (:id (first heratteet))}
                        (l/get-by-palaute-and-viestityypit! db/spec)
                        (map (juxt :tila :ulkoinen-tunniste)))))
            (let [ddb-item
                  (ddb/get-item!
                    :amis
                    {:toimija_oppija
                     "1.2.246.562.10.10000000009/1.2.246.562.24.12312312319"
                     :tyyppi_kausi "aloittaneet/2022-2023"})]
              (is (= "testi.testaaja@testidomain.testi"
                     (:sahkoposti ddb-item)))
              (is (= "https://arvovastaus.csc.fi/v/test"
                     (:kyselylinkki ddb-item)))
              (is (= 0 (:muistutukset ddb-item)))
              (is (= "lahetys_epaonnistunut" (:lahetystila ddb-item)))
              (is (= "ei_laheteta" (:sms-lahetystila ddb-item)))))

          ;; a record in Herätepalvelu only matters if eHOKS hasn't
          ;; handled any messages for the palaute
          (db-ops/query
            ["DELETE FROM palaute_viestit WHERE palaute_id=? RETURNING id"
             (:id (first heratteet))])

          (testing "with a record already in Herätepalvelu"
            (ddb/sync-amis-herate!
              (op/build-amisherate-record-for-heratepalvelu
                (assoc ctx
                       :existing-palaute (first heratteet)
                       :koulutustoimija "1.2.246.562.10.10000000009"
                       :hk-toteuttaja (delay nil)))
              :after-all-processing)
            (is (= {:email nil :sms nil}
                   (l/handle-unsent-palaute! (first heratteet))))
            (is (= [["heratepalvelussa" "aloittaneet"]]
                   (->> {:hoks-id (:id hoks) :kyselytyypit ["aloittaneet"]}
                        (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                        (map (juxt :tila :kyselytyyppi))))))
          nil)))))

(deftest test-update-delivery-status!
  (with-redefs [date/now (constantly (LocalDate/of 2023 4 18))
                date/now-with-time (constantly business-hours-instant)
                koski/get-oppija-opiskeluoikeudet
                koski-test/mock-get-oppija-opiskeluoikeudet
                koski/get-opiskeluoikeus-info-raw
                koski-test/mock-get-opiskeluoikeus-raw
                onr/get-oppija-raw!
                mock-get-oppija-raw!
                organisaatio/get-organisaatio!
                organisaatio-test/mock-get-organisaatio!]
    (oppijaindex/add-hoks-dependents-in-index! hoks-test/hoks-1)
    (let [ctx {:hoks (assoc
                       hoks-test/hoks-1
                       :osaamisen-saavuttamisen-pvm (LocalDate/of 2023 4 17))
               :opiskeluoikeus oo-test/opiskeluoikeus-1}
          hoks (hoks-handler/save-hoks-and-initiate-all-palautteet! ctx)]

      (testing "update-delivery-status! with LAHETETTY status"
        (with-mock-responses
          [(fn [_ __] {})
           (fn [url _]
             (when (s/ends-with? url "/api/vastauslinkki/v1")
               {:status 200
                :body {:tunnus "testivain"
                       :kysely_linkki "https://arvovastaus.csc.fi/v/test"
                       :voimassa_loppupvm "2024-10-10"}}))]
          (->> {:kyselytyypit ["aloittaneet"]}
               (palaute/get-palautteet-waiting-for-vastaajatunnus! db/spec)
               (first)
               (vt/create-vastaajatunnus!)))

        (let [heratteet
              (->> {:kyselytyypit ["aloittaneet"] :viestityyppi "email"}
                   (palaute/get-unsent-palautteet! db/spec))]
          (with-mock-responses
            [(fn [url _]
               (when (s/ends-with? url "/vastauslinkki/v1/status/testivain")
                 {:status 200
                  :body {:tunnus "test"
                         :voimassa_loppupvm "2026-04-14"
                         :vastattu false}}))
             (fn [^String url _]
               (when (s/ends-with? url "/lahetys/v1/viestit")
                 {:status 200
                  :body {:viestiTunniste "test-message-id"
                         :lahetysTunniste "test-message-id"}}))]
            (is (= {:email "test-message-id" :sms nil}
                   (l/handle-unsent-palaute! (first heratteet)))))

          (let [viestit (->> {:viestityypit ["email"] :tila "odottaa_lahetysta"}
                             (l/get-by-tila-and-viestityypit! db/spec))
                viesti (first viestit)]
            (testing "message was created with odottaa_lahetysta status"
              (is (= 1 (count viestit)))
              (is (= "test-message-id" (:ulkoinen-tunniste viesti))))

            (testing "updates status to lahetetty when VVP reports LAHETETTY"
              (with-mock-responses
                [(fn [url _]
                   (cond
                     (s/ends-with? url "/vastauslinkki/v1/status/testivain")
                     {:status 200
                      :body {:tunnus "test"
                             :voimassa_loppupvm "2026-04-14"
                             :vastattu false}}
                     (s/ends-with?
                       url "/lahetykset/test-message-id/vastaanottajat")
                     {:status 200
                      :body {:vastaanottajat [{:tila "LAHETETTY"}]}}))
                 (fn [_ _])
                 (fn [url options]
                   (when (s/ends-with? url "/vastauslinkki/v1/testivain")
                     {:status 200
                      :body (:body options)}))]
                (l/handle-palaute-waiting-for-sending-status! viesti))

              (is (= [] (->> {:viestityypit ["email"] :tila "odottaa_lahetysta"}
                             (l/get-by-tila-and-viestityypit! db/spec))))
              (is (= [["lahetetty" "test-message-id"]]
                     (->> {:viestityypit ["email"] :tila "lahetetty"}
                          (l/get-by-tila-and-viestityypit! db/spec)
                          (map (juxt :viesti-tila :ulkoinen-tunniste)))))
              (is (= [["lahetetty" "aloittaneet"]]
                     (->> {:hoks-id (:id hoks) :kyselytyypit ["aloittaneet"]}
                          (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                          (map (juxt :tila :kyselytyyppi)))))
              (let [ddb-item
                    (ddb/get-item!
                      :amis
                      {:toimija_oppija
                       "1.2.246.562.10.10000000009/1.2.246.562.24.12312312319"
                       :tyyppi_kausi "aloittaneet/2022-2023"})]
                (is (= "testi.testaaja@testidomain.testi"
                       (:sahkoposti ddb-item)))
                (is (= "https://arvovastaus.csc.fi/v/test"
                       (:kyselylinkki ddb-item)))
                (is (= "lahetetty" (:lahetystila ddb-item)))
                (is (= 0 (:muistutukset ddb-item)))
                (is (= "ei_laheteta" (:sms-lahetystila ddb-item))))))))

      (testing "update-delivery-status! with VIRHE status"
        (with-mock-responses
          [(fn [_ __] {})
           (fn [url _]
             (when (s/ends-with? url "/api/vastauslinkki/v1")
               {:status 200
                :body {:tunnus "testivain2"
                       :kysely_linkki "https://arvovastaus.csc.fi/v/test2"
                       :voimassa_loppupvm "2024-10-10"}}))]
          (let [palautteet (palaute/get-palautteet-waiting-for-vastaajatunnus!
                             db/spec {:kyselytyypit ["valmistuneet"]})]
            (is (= 1 (count palautteet)))
            (is (= "testivain2"
                   (vt/create-vastaajatunnus! (first palautteet))))))

        (let [heratteet
              (->> {:kyselytyypit ["valmistuneet"] :viestityyppi "email"}
                   (palaute/get-unsent-palautteet! db/spec))]
          (is (= 1 (count heratteet)))
          (with-mock-responses
            [(fn [url _]
               (when (s/ends-with? url "/vastauslinkki/v1/status/testivain2")
                 {:status 200
                  :body {:tunnus "test2"
                         :voimassa_loppupvm "2026-04-14"
                         :vastattu false}}))
             (fn [^String url _]
               (when (s/ends-with? url "/lahetys/v1/viestit")
                 {:status 200
                  :body {:viestiTunniste "test-message-id-2"
                         :lahetysTunniste "test-message-id-2"}}))]
            (is (= {:email "test-message-id-2" :sms nil}
                   (l/handle-unsent-palaute! (first heratteet))))))

        (let [viestit (->> {:viestityypit ["email"] :tila "odottaa_lahetysta"}
                           (l/get-by-tila-and-viestityypit! db/spec))
              viesti (first viestit)
              arvo-lahetys (atom {})]

          (testing "päättökyselyn viesti was created"
            (is (= 1 (count viestit)))
            (is (= "test-message-id-2" (:ulkoinen-tunniste viesti))))

          (testing (str "updates status to lahetys-epaonnistunut "
                        "when VVP reports VIRHE")
            (with-mock-responses
              [(fn [url _]
                 (when (s/ends-with?
                         url "/lahetykset/test-message-id-2/vastaanottajat")
                   {:status 200
                    :body {:vastaanottajat [{:tila "VIRHE"}]}}))
               (fn [_ __] {})
               (fn [url options]
                 (when (s/ends-with? url "/vastauslinkki/v1/testivain2")
                   (reset! arvo-lahetys (:form-params options))
                   {:status 200
                    :body (:form-params options)}))]
              (l/handle-palaute-waiting-for-sending-status! viesti))

            (is (= {:metatiedot {:tila "lahetys_epaonnistunut"}} @arvo-lahetys))
            (is (= [["kysely_muodostettu" "valmistuneet"]]
                   (->> {:hoks-id (:id hoks) :kyselytyypit ["valmistuneet"]}
                        (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                        (map (juxt :tila :kyselytyyppi)))))
            (is (= [["lahetys_epaonnistunut" "test-message-id-2"]]
                   (->> {:viestityypit ["email"] :tila "lahetys_epaonnistunut"}
                        (l/get-by-tila-and-viestityypit! db/spec)
                        (map (juxt :viesti-tila :ulkoinen-tunniste)))))
            (let [herate
                  (ddb/get-item!
                    :amis
                    {:toimija_oppija
                     "1.2.246.562.10.10000000009/1.2.246.562.24.12312312319"
                     :tyyppi_kausi "tutkinnon_suorittaneet/2022-2023"})]
              (is (instance? java.lang.String (:lahetyspvm herate)))
              (is (= "lahetys_epaonnistunut" (:lahetystila herate)))
              (is (= "ei_lahetetty" (:sms-lahetystila herate)))
              (is (= "test-message-id-2" (:viestintapalvelu-id herate)))
              (is (= "2023-05-17" (:voimassa-loppupvm herate))))))))))

(deftest test-handle-palautteet-waiting-for-sending-status!
  (with-redefs [date/now (constantly (LocalDate/of 2023 4 18))
                date/now-with-time (constantly business-hours-instant)
                koski/get-oppija-opiskeluoikeudet
                koski-test/mock-get-oppija-opiskeluoikeudet
                koski/get-opiskeluoikeus-info-raw
                koski-test/mock-get-opiskeluoikeus-raw
                onr/get-oppija-raw!
                mock-get-oppija-raw!
                organisaatio/get-organisaatio!
                organisaatio-test/mock-get-organisaatio!]
    (oppijaindex/add-hoks-dependents-in-index! hoks-test/hoks-1)
    (let [ctx {:hoks hoks-test/hoks-1 :opiskeluoikeus oo-test/opiskeluoikeus-1}]
      (hoks-handler/save-hoks-and-initiate-all-palautteet! ctx)

      (with-mock-responses
        [(fn [_ __] {})
         (fn [url _]
           (when (s/ends-with? url "/api/vastauslinkki/v1")
             {:status 200
              :body {:tunnus "testivain"
                     :kysely_linkki "https://arvovastaus.csc.fi/v/test"
                     :voimassa_loppupvm "2024-10-10"}}))]
        (->> {:kyselytyypit ["aloittaneet"]}
             (palaute/get-palautteet-waiting-for-vastaajatunnus! db/spec)
             (first)
             (vt/create-vastaajatunnus!)))

      (let [heratteet
            (->> {:kyselytyypit ["aloittaneet"] :viestityyppi "email"}
                 (palaute/get-unsent-palautteet! db/spec))]
        (with-mock-responses
          [(fn [url _]
             (when (s/ends-with? url "/vastauslinkki/v1/status/testivain")
               {:status 200
                :body {:tunnus "test"
                       :voimassa_loppupvm "2026-04-14"
                       :vastattu false}}))
           (fn [^String url _]
             (when (s/ends-with? url "/lahetys/v1/viestit")
               {:status 200
                :body {:viestiTunniste "test-message-id"
                       :lahetysTunniste "test-message-id"}}))]
          (l/handle-unsent-palaute! (first heratteet)))

        (testing "processes all messages waiting for sending status"
          (is (->> {:viestityypit ["email"] :tila "odottaa_lahetysta"}
                   (l/get-by-tila-and-viestityypit! db/spec)
                   (count)
                   (= 1)))

          (with-mock-responses
            [(fn [url _]
               (cond
                 (s/ends-with? url "/vastauslinkki/v1/status/testivain")
                 {:status 200
                  :body {:tunnus "test"
                         :voimassa_loppupvm "2026-04-14"
                         :vastattu false}}
                 (s/ends-with?
                   url "/lahetykset/test-message-id/vastaanottajat")
                 {:status 200
                  :body {:vastaanottajat [{:tila "LAHETETTY"}]}}))
             (fn [_ _])
             (fn [url options]
               (when (s/ends-with? url "/vastauslinkki/v1/testivain")
                 {:status 200
                  :body (:body options)}))]
            (l/handle-palautteet-waiting-for-sending-status!))

          (is (= 0 (->> {:viestityypit ["email"] :tila "odottaa_lahetysta"}
                        (l/get-by-tila-and-viestityypit! db/spec)
                        (count))))
          (is (= 1 (->> {:viestityypit ["email"] :tila "lahetetty"}
                        (l/get-by-tila-and-viestityypit! db/spec)
                        (count))))
          (let [ddb-item
                (ddb/get-item!
                  :amis
                  {:toimija_oppija
                   "1.2.246.562.10.10000000009/1.2.246.562.24.12312312319"
                   :tyyppi_kausi "aloittaneet/2022-2023"})]
            (is (some? ddb-item))
            (is (= "testi.testaaja@testidomain.testi" (:sahkoposti ddb-item)))
            (is (= "https://arvovastaus.csc.fi/v/test"
                   (:kyselylinkki ddb-item)))
            (is (= "lahetetty" (:lahetystila ddb-item)))))))))

(deftest test-vastausaika-updated-on-confirmed-delivery!
  (testing (str "voimassa_alkupvm and voimassa_loppupvm are updated to reflect "
                "the actual sending date, not the original heratepvm")
    (let [initial-date (LocalDate/of 2023 4 18)
          sending-date (LocalDate/of 2023 4 21)]
      (with-redefs [date/now (constantly initial-date)
                    date/now-with-time (constantly business-hours-instant)
                    koski/get-oppija-opiskeluoikeudet
                    koski-test/mock-get-oppija-opiskeluoikeudet
                    koski/get-opiskeluoikeus-info-raw
                    koski-test/mock-get-opiskeluoikeus-raw
                    onr/get-oppija-raw!
                    mock-get-oppija-raw!
                    organisaatio/get-organisaatio!
                    organisaatio-test/mock-get-organisaatio!]
        (oppijaindex/add-hoks-dependents-in-index! hoks-test/hoks-1)
        (hoks-handler/save-hoks-and-initiate-all-palautteet!
          {:hoks hoks-test/hoks-1 :opiskeluoikeus oo-test/opiskeluoikeus-1})

        (with-mock-responses
          [(fn [_ __] {})
           (fn [url _]
             (when (s/ends-with? url "/api/vastauslinkki/v1")
               {:status 200
                :body {:tunnus "testivain"
                       :kysely_linkki "https://arvovastaus.csc.fi/v/test"
                       :voimassa_loppupvm "2024-10-10"}}))]
          (->> {:kyselytyypit ["aloittaneet"]}
               (palaute/get-palautteet-waiting-for-vastaajatunnus! db/spec)
               (first)
               (vt/create-vastaajatunnus!)
               (= "testivain")
               (is)))

        (let [palautteet (palaute/get-unsent-palautteet!
                           db/spec {:kyselytyypit ["aloittaneet"]
                                    :viestityyppi "email"})
              palaute (first palautteet)]
          (is (= 1 (count palautteet)))
          (is (= "2023-04-18" (str (:voimassa-alkupvm palaute))))

          (with-mock-responses
            [(fn [url _]
               (when (s/ends-with? url "/vastauslinkki/v1/status/testivain")
                 {:status 200
                  :body {:tunnus "test"
                         :voimassa_loppupvm "2026-04-14"
                         :vastattu false}}))
             (fn [url _]
               (when (s/ends-with? url "/lahetys/v1/viestit")
                 {:status 200
                  :body {:viestiTunniste "test-message-id"
                         :lahetysTunniste "test-message-id"}}))]
            (is (= {:email "test-message-id" :sms nil}
                   (l/handle-unsent-palaute! palaute)))))

        (let [viesti (first (l/get-by-tila-and-viestityypit!
                              db/spec {:viestityypit ["email"]
                                       :tila "odottaa_lahetysta"}))]
          (is (= "odottaa_lahetysta" (:viesti-tila viesti)))
          (is (= "test-message-id" (:ulkoinen-tunniste viesti)))
          (let [arvo-patch-options (atom nil)]
            (with-redefs [date/now (constantly sending-date)]
              (with-mock-responses
                [(fn [url _]
                   (when (s/ends-with?
                           url "/lahetykset/test-message-id/vastaanottajat")
                     {:status 200
                      :body {:vastaanottajat [{:tila "LAHETETTY"}]}}))
                 (fn [_ _])
                 (fn [url options]
                   (when (s/ends-with? url "/vastauslinkki/v1/testivain")
                     (reset! arvo-patch-options options)
                     {:status 200 :body (:body options)}))]
                (l/handle-palaute-waiting-for-sending-status! viesti)))
            (is (= (str sending-date)
                   (str (get-in @arvo-patch-options
                                [:form-params :voimassa_alkupvm]))))
            (is (= (str (.plusDays sending-date 29))
                   (str (get-in @arvo-patch-options
                                [:form-params :voimassa_loppupvm])))))

          (let [palautteet (palaute/get-by-hoks-id-and-kyselytyypit!
                             db/spec {:hoks-id (:hoks-id viesti)
                                      :kyselytyypit ["aloittaneet"]})
                palaute (first palautteet)]
            (is (= 1 (count palautteet)))
            (is (= (str sending-date) (str (:voimassa-alkupvm palaute))))
            (is (= (str (.plusDays sending-date 29))
                   (str (:voimassa-loppupvm palaute))))))))))

(deftest test-handle-unsent-palaute!-sms-option
  (with-redefs [l/send-invitation! (constantly "email-id")
                palaute/get-by-id! (constantly {:id 1})
                l/send-sms-invitation! (constantly :lahetetty)]
    (testing "SMS is sent along with email by default"
      (is (= {:email "email-id" :sms :lahetetty}
             (l/handle-unsent-palaute! {:id 1}))))
    (testing "SMS is not sent with :sms? false"
      (is (= {:email "email-id" :sms nil}
             (l/handle-unsent-palaute! {:id 1} {:sms? false}))))))

(defn- mock-arvo-status
  [url _]
  (when (s/ends-with? url "/vastauslinkki/v1/status/testivain")
    {:status 200
     :body {:tunnus "testivain"
            :voimassa_loppupvm "2026-04-14"
            :vastattu false}}))

(defn- mock-elisa-response
  [status sent-sms]
  (fn [^String url options]
    (when (s/starts-with? url "https://viestipalvelu-api.elisa.fi/")
      (reset! sent-sms (:body options))
      {:status 200
       :body {:messages {:0406543210 {:status status
                                      :converted "+358406543210"}}}})))

(defn- sms-viestit
  [palaute-id]
  (->> {:viestityypit ["sms"] :palaute-id palaute-id}
       (l/get-by-palaute-and-viestityypit! db/spec)
       (map (juxt :tila :vastaanottaja))))

(defn- valmistuneet-tilat
  [hoks-id]
  (->> {:hoks-id hoks-id :kyselytyypit ["valmistuneet"]}
       (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
       (map :tila)))

(defn- valmistuneet-ddb-item []
  (ddb/get-item!
    :amis
    {:toimija_oppija "1.2.246.562.10.10000000009/1.2.246.562.24.12312312319"
     :tyyppi_kausi "tutkinnon_suorittaneet/2022-2023"}))

(deftest test-sms-sending
  (testing "does nothing if eHOKS is not responsible for sending SMS's"
    (is (nil? (l/handle-palautteet-without-sms! ["valmistuneet"]))))
  (with-redefs [config/config (-> config/config
                                  (update :heratepalvelu-responsibities
                                          conj :send-initial-sms)
                                  (assoc :elisa-sms-apikey "test-key"))
                date/now (constantly (LocalDate/of 2023 4 18))
                date/now-with-time (constantly business-hours-instant)
                ;; avoid HOKS cached by earlier tests (with other phone)
                handling/get-hoks-by-id! oph.ehoks.hoks/get-by-id
                koski/get-oppija-opiskeluoikeudet
                koski-test/mock-get-oppija-opiskeluoikeudet
                koski/get-opiskeluoikeus-info-raw
                koski-test/mock-get-opiskeluoikeus-raw
                onr/get-oppija-raw! mock-get-oppija-raw!
                organisaatio/get-organisaatio!
                (fn [oid]
                  (assoc (organisaatio-test/mock-get-organisaatio! oid)
                         :nimi {:fi "Testioppilaitos"}))]
    (oppijaindex/add-hoks-dependents-in-index! hoks-test/hoks-1)
    (let [ctx {:hoks (assoc hoks-test/hoks-1
                            :puhelinnumero "0406543210"
                            :osaamisen-saavuttamisen-pvm (LocalDate/of 2023 4 17))
               :opiskeluoikeus oo-test/opiskeluoikeus-1}
          hoks (hoks-handler/save-hoks-and-initiate-all-palautteet! ctx)
          sent-sms (atom nil)
          arvo-lahetys (atom nil)
          mock-arvo-patch (fn [url options]
                            (when (s/ends-with? url "/vastauslinkki/v1/testivain")
                              (reset! arvo-lahetys (:form-params options))
                              {:status 200 :body (:form-params options)}))]
      (with-mock-responses
        [(fn [_ __] {})
         (fn [url _]
           (when (s/ends-with? url "/api/vastauslinkki/v1")
             {:status 200
              :body {:tunnus "testivain"
                     :kysely_linkki "https://arvovastaus.csc.fi/v/test"
                     :voimassa_loppupvm "2024-10-10"}}))]
        (->> {:kyselytyypit ["valmistuneet"]}
             (palaute/get-palautteet-waiting-for-vastaajatunnus! db/spec)
             (first)
             (vt/create-vastaajatunnus!)))

      (testing "SMS can be handled before email has been handled"
        (is (= 1 (count (palaute/get-palautteet-without-sms!
                          db/spec {:kyselytyypit ["valmistuneet"]})))))

      (let [palaute (first (palaute/get-unsent-palautteet!
                             db/spec {:kyselytyypit ["valmistuneet"]
                                      :viestityyppi "email"}))
            mock-send (fn [elisa-status]
                        (let [elisa (mock-elisa-response elisa-status sent-sms)]
                          (fn [^String url options]
                            (if (s/ends-with? url "/lahetys/v1/viestit")
                              {:status 200
                               :body {:viestiTunniste "email-id"
                                      :lahetysTunniste "email-id"}}
                              (elisa url options)))))]
        (is (some? palaute))

        (testing "nothing is sent outside business hours"
          (with-redefs [date/now-with-time
                        (constantly non-business-hours-instant)]
            (with-mock-responses
              [mock-arvo-status (mock-send "CREATED")]
              (is (= {:email nil :sms nil}
                     (l/handle-unsent-palaute! palaute)))))
          (is (nil? @sent-sms))
          (is (empty? (sms-viestit (:id palaute)))))

        (testing "SMS is sent along with email; failure is recorded and
                 synced to Herätepalvelu"
          (with-mock-responses
            [mock-arvo-status (mock-send "FAILED") mock-arvo-patch]
            (is (= {:email "email-id" :sms :lahetys-epaonnistunut}
                   (l/handle-unsent-palaute! palaute))))
          (is (s/includes? @sent-sms "Testioppilaitos: P\u00e4\u00e4tt"))
          (is (s/includes? @sent-sms "https://arvovastaus.csc.fi/v/test?t=s"))
          (is (= [["lahetys_epaonnistunut" "+358406543210"]]
                 (sms-viestit (:id palaute))))
          (is (= ["kysely_muodostettu"] (valmistuneet-tilat (:id hoks))))
          (testing "but not reported to Arvo while email is still pending"
            (is (nil? @arvo-lahetys)))
          (is (= "lahetys_epaonnistunut"
                 (:sms-lahetystila (valmistuneet-ddb-item))))
          (is (empty? (palaute/get-palautteet-without-sms!
                        db/spec {:kyselytyypit ["valmistuneet"]}))))

        (testing "failure is reported to Arvo when email also fails"
          (with-mock-responses
            [(fn [url _]
               (when (s/ends-with? url "/lahetykset/email-id/vastaanottajat")
                 {:status 200 :body {:vastaanottajat [{:tila "VIRHE"}]}}))
             (fn [_ __] {})
             mock-arvo-patch]
            (->> {:viestityypit ["email"] :tila "odottaa_lahetysta"}
                 (l/get-by-tila-and-viestityypit! db/spec)
                 (first)
                 (l/handle-palaute-waiting-for-sending-status!)))
          (is (= {:metatiedot {:tila "lahetys_epaonnistunut"}}
                 @arvo-lahetys)))

        (db-ops/query
          ["DELETE FROM palaute_viestit WHERE palaute_id=? AND viestityyppi='sms'
            RETURNING id" (:id palaute)])
        (reset! arvo-lahetys nil)

        (testing "unsent SMS is sent later; success marks palaute sent"
          (with-mock-responses
            [mock-arvo-status
             (mock-elisa-response "CREATED" sent-sms)
             mock-arvo-patch]
            (is (= [:lahetetty]
                   (l/handle-palautteet-without-sms! ["valmistuneet"]))))
          (is (= [["lahetetty" "+358406543210"]]
                 (sms-viestit (:id palaute))))
          (is (= ["lahetetty"] (valmistuneet-tilat (:id hoks))))
          (is (= "lahetetty" (get-in @arvo-lahetys [:metatiedot :tila])))
          (is (= "lahetetty" (:sms-lahetystila (valmistuneet-ddb-item))))
          (is (empty? (palaute/get-palautteet-without-sms!
                        db/spec {:kyselytyypit ["valmistuneet"]}))))))))
