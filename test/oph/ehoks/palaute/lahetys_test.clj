(ns oph.ehoks.palaute.lahetys-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.string :as s]
            [oph.ehoks.db :as db]
            [oph.ehoks.db.dynamodb :as ddb]
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
            [oph.ehoks.palaute.lahetys :as l]
            [oph.ehoks.palaute.vastaajatunnus :as vt])
  (:import (java.time LocalDate)))

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

(deftest test-check-palaute-for-sending
  (testing "check-palaute-for-sending"
    (testing "returns go for example context"
      (is (= [:odottaa-lahetysta nil :voimassa-alkupvm :viesti-lahetys]
             (l/check-palaute-for-sending esim-ctx))))
    (testing "doesn't handle anything already in heratepalvelu"
      (is (= [nil :heratepalvelussa :heratepvm :heratepalvelun-vastuulla]
             (l/check-palaute-for-sending
               (assoc esim-ctx :existing-ddb-herate
                      (delay {:toimija_oppija "foo/bar"}))))))
    (testing "no action if email is missing"
      (is (= [:lahetys-epaonnistunut nil :sahkoposti :ei-ole]
             (l/check-palaute-for-sending
               (assoc-in esim-ctx [:hoks :sahkoposti] "")))))
    (testing "don't send messages for overdue palautekysely"
      (is (= [nil :vastausaika-loppunut :voimassa-loppupvm :arvo-paivitys]
             (l/check-palaute-for-sending
               (assoc esim-ctx :arvo-status
                      (delay {:vastattu false
                              :voimassa-loppupvm "2021-01-01T00:00:00Z"}))))))
    (testing "don't send messages for already answered palautekysely"
      (is (= [nil :vastattu :heratepvm :arvo-paivitys]
             (l/check-palaute-for-sending
               (assoc esim-ctx :arvo-status
                      (delay {:vastattu true
                              :voimassa-loppupvm "2051-01-01T00:00:00Z"}))))))))

(deftest test-handle-unsent-palaute!
  (with-redefs [date/now (constantly (LocalDate/of 2023 4 18))
                koski/get-oppija-opiskeluoikeudet
                koski-test/mock-get-oppija-opiskeluoikeudet
                koski/get-opiskeluoikeus-info-raw
                koski-test/mock-get-opiskeluoikeus-raw
                onr/get-oppija-raw!
                mock-get-oppija-raw!
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
                   (->> {:viestityypit ["email"] :tila "lahetys_epaonnistunut"}
                        (l/get-by-tila-and-viestityypit! db/spec)
                        (map (juxt :viesti-tila :ulkoinen-tunniste))))))

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
              (is (= "brymir" (l/handle-unsent-palaute! (first heratteet)))))
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

          (testing "with expired kyselylinkki"
            (with-mock-responses
              [(fn [url _]
                 (when (s/ends-with? url "/vastauslinkki/v1/status/testivain")
                   {:status 200
                    :body {:tunnus "test"
                           :voimassa_loppupvm "2022-04-14"
                           :vastattu false}}))
               (fn [_ __] {})]
              (is (nil? (l/handle-unsent-palaute! (first heratteet)))))
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
              (is (nil? (l/handle-unsent-palaute! (first heratteet)))))
            (is (= [["vastattu" "aloittaneet"
                     "https://arvovastaus.csc.fi/v/test"]]
                   (->> {:hoks-id (:id hoks) :kyselytyypit ["aloittaneet"]}
                        (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                        (map (juxt :tila :kyselytyyppi :kyselylinkki))))))

          (testing "with a record already in Herätepalvelu"
            (ddb/sync-amis-herate!
              (op/build-amisherate-record-for-heratepalvelu
                (assoc ctx
                       :existing-palaute (first heratteet)
                       :koulutustoimija "1.2.246.562.10.10000000009"
                       :hk-toteuttaja (delay nil)))
              :after-all-processing)
            (is (nil? (l/handle-unsent-palaute! (first heratteet))))
            (is (= [["heratepalvelussa" "aloittaneet"]]
                   (->> {:hoks-id (:id hoks) :kyselytyypit ["aloittaneet"]}
                        (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                        (map (juxt :tila :kyselytyyppi))))))
          nil)))))

(deftest test-update-delivery-status!
  (with-redefs [date/now (constantly (LocalDate/of 2023 4 18))
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
            (is (= "test-message-id"
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
                          (map (juxt :tila :kyselytyyppi)))))))))

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
            (is (= "test-message-id-2"
                   (l/handle-unsent-palaute! (first heratteet))))))

        (let [viestit (->> {:viestityypit ["email"] :tila "odottaa_lahetysta"}
                           (l/get-by-tila-and-viestityypit! db/spec))
              viesti (first viestit)]

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
               (fn [_ __] {})]
              (l/handle-palaute-waiting-for-sending-status! viesti))

            (is (= [["kysely_muodostettu" "valmistuneet"]]
                   (->> {:hoks-id (:id hoks) :kyselytyypit ["valmistuneet"]}
                        (palaute/get-by-hoks-id-and-kyselytyypit! db/spec)
                        (map (juxt :tila :kyselytyyppi)))))
            (is (= [["lahetys_epaonnistunut" "test-message-id-2"]]
                   (->> {:viestityypit ["email"] :tila "lahetys_epaonnistunut"}
                        (l/get-by-tila-and-viestityypit! db/spec)
                        (map (juxt :viesti-tila :ulkoinen-tunniste)))))))))))

(deftest test-handle-palautteet-waiting-for-sending-status!
  (with-redefs [date/now (constantly (LocalDate/of 2023 4 18))
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
                        (count)))))))))
