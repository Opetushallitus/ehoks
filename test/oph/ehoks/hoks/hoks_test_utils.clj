(ns oph.ehoks.hoks.hoks-test-utils
  (:require [ring.mock.request :as mock]
            [oph.ehoks.virkailija.handler :as handler]
            [clojure.test :refer [is]]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.db.db-operations.db-helpers :as db-helpers]
            [oph.ehoks.external.cache :as cache]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.koski-test :as koski-test]
            [oph.ehoks.hoks-test :as hoks-test]
            [oph.ehoks.utils.date :as date]
            [oph.ehoks.test-utils :as test-utils :refer [eq]])
  (:import (java.time LocalDate)
           (java.util UUID)))

(def base-url "/ehoks-virkailija-backend/api/v1/hoks")
(def virkailija-base-url "/ehoks-virkailija-backend/api/v1/virkailija")

(defn create-app [session-store]
  (cache/clear-cache!)
  (common-api/create-app handler/app-routes session-store))

(defn get-authenticated [url]
  (-> (test-utils/with-service-ticket
        (create-app nil)
        (mock/request :get url))
      :body
      test-utils/parse-body))

(defn create-hoks [app]
  (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                   :oppija-oid "1.2.246.562.24.12312312319"
                   :ensikertainen-hyvaksyminen
                   (java.time.LocalDate/of 2019 3 18)
                   :osaamisen-hankkimisen-tarve false}]
    (-> app
        (test-utils/with-service-ticket
          (-> (mock/request :post base-url)
              (mock/json-body hoks-data)))
        :body
        test-utils/parse-body
        (get-in [:data :uri])
        get-authenticated
        :data)))

(defmacro with-hoks-and-app [[hoks app] & body]
  `(let [~app (create-app nil)
         ~hoks (create-hoks ~app)]
     (do ~@body)))

(defn mock-st-request
  ([app full-url method data]
    (let [req (mock/request
                method
                full-url)]
      (test-utils/with-service-ticket
        app
        (if (some? data)
          (mock/json-body req data)
          req))))
  ([app full-url]
    (mock-st-request app full-url :get nil)))

(defn mock-st-post [app full-url data]
  (mock-st-request app full-url :post data))

(defn mock-st-put [app full-url data]
  (mock-st-request app full-url :put data))

(defn mock-st-get [app full-url]
  (mock-st-request app full-url))

(defn mock-st-patch [app full-url data]
  (mock-st-request app full-url :patch data))

(defn create-mock-post-request
  ([path body app hoks]
    (create-mock-post-request (format "%d/%s" (:id hoks) path) body app))
  ([path body app]
    (mock-st-post app (format "%s/%s" base-url path) body)))

(defn create-mock-hoks-put-request [hoks-id updated-data app]
  (mock-st-put app (format "%s/%d" base-url hoks-id) updated-data))

(defn create-mock-hoks-get-request [hoks-id app]
  (mock-st-get app (format "%s/%d" base-url hoks-id)))

(defn create-mock-hoks-patch-request [hoks-id patched-data app]
  (mock-st-patch app (format "%s/%d" base-url hoks-id) patched-data))

(defn assert-partial-put-of-hoks [updated-hoks hoks-part initial-hoks-data]
  (let [app (create-app nil)
        post-response (create-mock-post-request "" initial-hoks-data app)
        put-response (create-mock-hoks-put-request 1 updated-hoks app)
        get-response (create-mock-hoks-get-request 1 app)
        get-response-data (:data (test-utils/parse-body (:body get-response)))]
    (is (= (:status post-response) 200))
    (is (= (:status put-response) 204))
    (is (= (:status get-response) 200))
    (eq (test-utils/dissoc-module-ids (hoks-part get-response-data))
        (test-utils/dissoc-module-ids (hoks-part updated-hoks)))))

(defn create-hoks-in-the-past! [& [transform]]
  (with-redefs [date/now #(LocalDate/of 2023 8 1)]
    (mock-st-post (create-app nil)
                  base-url
                  (dissoc ((or transform identity) hoks-test/hoks-1) :id))))

(defn palautteet []
  (db-helpers/query ["select * from palautteet"]))

(defn kasittelemattomat-palauteet []
  (db-helpers/query
    [(str "select * from palautteet "
          "where arvo_tunniste is null and "
          "tila = 'odottaa_kasittelya' and "
          "kyselytyyppi = 'tyopaikkajakson_suorittaneet'")]))

(defn palautteet-joissa-vastaajatunnus []
  (db-helpers/query
    [(str "select * from palautteet "
          "where arvo_tunniste is not null and "
          "tila = 'vastaajatunnus_muodostettu' and "
          "kyselytyyppi = 'tyopaikkajakson_suorittaneet'")]))

(defn mock-get-opiskeluoikeus! [oid]
  {:oid oid
   :tila {:opiskeluoikeusjaksot
          [{:alku "2010-01-01"
            :tila {:koodiarvo "lasna"
                   :nimi {:fi "Läsnä"}
                   :koodistoUri "koskiopiskeluoikeudentila"
                   :koodistoVersio 1}}]}
   :oppilaitos {:oid "1.2.246.562.10.12944436166"}
   :koulutustoimija {:oid "1.2.246.562.10.346830761110"}
   :suoritukset
   [{:tyyppi        {:koodiarvo "ammatillinentutkinto"}
     :suorituskieli {:koodiarvo "fi"}
     :toimipiste {:oid "1.2.246.562.10.12345678903"}
     :koulutusmoduuli {:tunniste {:koodiarvo "123456"}}
     :osaamisala [{:koodiarvo "test-osaamisala"}]
     :tutkintonimike  [{:koodiarvo "12345"}
                       {:koodiarvo "23456"}]}]
   :tyyppi {:koodiarvo "ammatillinenkoulutus"}})

(defn mock-get-organisaatio! [oid]
  {:oid oid :tyypit #{"organisaatiotyyppi_03"}})

(defn mock-create-jaksotunnus [_]
  {:tunnus (str (UUID/randomUUID))})
