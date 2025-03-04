(ns oph.ehoks.palaute.handler-test
  (:require [clojure.string :as string]
            [clojure.test :refer [use-fixtures deftest testing is]]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.palaute.handler :as handler]
            [oph.ehoks.test-utils :as test-utils]
            [oph.ehoks.utils.date :as date]
            [ring.mock.request :as mock])
  (:import (java.time LocalDate)))

(use-fixtures :once test-utils/migrate-database)
(use-fixtures :each test-utils/empty-database-after-test)

(def base-url "/ehoks-palaute-backend/api/v1")

(deftest buildversion
  (testing "GET /buildversion.txt"
    (let [app (common-api/create-app handler/app-routes)
          response (app (mock/request
                          :get "/ehoks-palaute-backend/buildversion.txt"))
          body (slurp (:body response))]
      (is (= (:status response) 200))
      (is (re-find #"^artifactId=" body)))))

(defn post!
  [app uri]
  (-> (mock/request :post (str base-url uri))
      (mock/header "Caller-Id" "test")
      (mock/header "ticket" "ST-testitiketti")
      app))

(defn mock-get-ticket-user
  [_]
  {:oidHenkilo "1.2.246.562.24.11474338834"
   :username "ehoks-test"
   :kayttajaTyyppi "PALVELU"
   :organisaatiot [{:organisaatioOid "1.2.246.562.10.00000000001"
                    :kayttooikeudet [{:palvelu "EHOKS"
                                      :oikeus "CRUD"}]}
                   {:organisaatioOid "1.2.246.562.10.00000000001"
                    :kayttooikeudet [{:palvelu "EHOKS"
                                      :oikeus "OPHPAAKAYTTAJA"}]}]})

(defn mock-validate-ticket
  [_ __]
  {:success? true
   :user {:username "ehoks-test"}})

(deftest test-tyoelamapalaute
  (is (= (:status (hoks-utils/create-hoks-in-the-past!)) 200))
  (with-redefs [oph.ehoks.external.kayttooikeus/get-ticket-user
                mock-get-ticket-user
                oph.ehoks.external.cas/validate-ticket
                mock-validate-ticket]
    (let [palaute-app (common-api/create-app handler/app-routes)]
      (testing (str "POST /tyoelamapalaute/vastaajatunnukset "
                    "without ticket returns 401")
        (let [resp (-> (mock/request
                         :post (str base-url
                                    "/tyoelamapalaute/vastaajatunnukset"))
                       (mock/header "Caller-Id" "test")
                       palaute-app)
              body (test-utils/parse-body (:body resp))]
          (is (= (:status resp) 401))
          (is (= (:error body) "Ticket is missing"))))

      (testing (str "POST /tyoelamapalaute/vastaajatunnukset "
                    "without caller-id returns 401")
        (let [resp (-> (mock/request
                         :post (str base-url
                                    "/tyoelamapalaute/vastaajatunnukset"))
                       (mock/header "ticket" "ST-testitiketti")
                       palaute-app)
              body (test-utils/parse-body (:body resp))]
          (is (= (:status resp) 401))
          (is (= (:error body) "Caller-Id header is missing"))))

      (testing (str "POST /tyoelamapalaute/:palaute-id/vastaajatunnus with "
                    "non-existing palaute id returns empty list")
        (let [resp (post! palaute-app "/tyoelamapalaute/10/vastaajatunnus")
              body (test-utils/parse-body (:body resp))]
          (is (= (:status resp) 200))
          (is (= (get-in body [:data :vastaajatunnukset]) []) body)))

      (testing "Creating vastaajatunnus with"
        (with-redefs [organisaatio/get-organisaatio!
                      hoks-utils/mock-get-organisaatio!
                      koski/get-opiskeluoikeus!
                      hoks-utils/mock-get-opiskeluoikeus!
                      arvo/create-jaksotunnus!
                      hoks-utils/mock-create-jaksotunnus
                      date/now #(LocalDate/of 2024 6 30)]
          (is (= (count (hoks-utils/kasittelemattomat-palauteet)) 5))

          (testing (str "POST /tyoelamapalaute/7/vastaajatunnus creates "
                        "single vastaajatunnus")
            (let [resp (post! palaute-app
                              "/tyoelamapalaute/7/vastaajatunnus")
                  data (:data (test-utils/parse-body (:body resp)))]
              (is (= (:status resp) 200) data)
              (is (not (empty? (:vastaajatunnukset data))))
              ;; TODO: test here that the palaute is synced to DDB with
              ;; hankkimistapa-id
              (is (= (count (hoks-utils/palautteet-joissa-vastaajatunnus)) 1))))

          (testing (str "POST /tyoelamapalaute/vastaajatunnukset creates "
                        "vastaajatunnus for remaining palaute waiting for it")
            (let [resp (post! palaute-app
                              "/tyoelamapalaute/vastaajatunnukset")
                  data (:data (test-utils/parse-body (:body resp)))]
              (is (= (:status resp) 200) data)
              (is (= (count (:vastaajatunnukset data)) 4))
              (is (= (count (hoks-utils/palautteet-joissa-vastaajatunnus)) 5))))

          (testing (str "POST /tyoelamapalaute/vastaajatunnukset is "
                        "successful when there is no palaute waiting for "
                        "vastaajatunnus")
            (let [resp (post! palaute-app
                              "/tyoelamapalaute/vastaajatunnukset")
                  data (:data (test-utils/parse-body (:body resp)))]
              (is (= (:status resp) 200) data)
              (is (= (count (:vastaajatunnukset data)) 0)))))))))
