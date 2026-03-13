(ns oph.ehoks.palaute.lahetys-test
  (:require [clojure.test :refer [deftest is testing]]
            [oph.ehoks.test-utils :as util]
            [clojure.string :as s]
            [oph.ehoks.palaute.lahetys :as l]))

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
              :body {:viestiTunniste "019cb395-5840-70fa-96c9-918eec8a6f41"
                     :lahetysTunniste
                     "019cb395-5840-70fa-96c9-918eec8a6f41"}}))]
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
