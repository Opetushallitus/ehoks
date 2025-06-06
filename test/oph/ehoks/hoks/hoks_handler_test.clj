(ns oph.ehoks.hoks.hoks-handler-test
  (:require [clj-time.core :as t]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.external.aws-sqs :as sqs]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.hoks.hoks-parts.parts-test-data :as parts-test-data]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils :refer [base-url]]
            [oph.ehoks.hoks.test-data :as test-data]
            [oph.ehoks.palaute.opiskelija.kyselylinkki :as kyselylinkki]
            [oph.ehoks.test-utils :as test-utils :refer [eq]]
            [ring.mock.request :as mock])
  (:import [java.time LocalDate]))

(use-fixtures :once test-utils/with-clean-database-and-clean-dynamodb)
(use-fixtures :each test-utils/empty-database-after-test)

(defn add-empty-hoks-values [hoks]
  (assoc
    hoks
    :aiemmin-hankitut-ammat-tutkinnon-osat []
    :hankittavat-paikalliset-tutkinnon-osat []
    :hankittavat-ammat-tutkinnon-osat []
    :aiemmin-hankitut-yhteiset-tutkinnon-osat []
    :hankittavat-yhteiset-tutkinnon-osat []
    :hankittavat-koulutuksen-osat []
    :aiemmin-hankitut-paikalliset-tutkinnon-osat []
    :opiskeluvalmiuksia-tukevat-opinnot []))

(deftest get-created-hoks
  (testing "GET newly created HOKS"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                     :oppija-oid "1.2.246.562.24.12312312319"
                     :osaamisen-hankkimisen-tarve true
                     :ensikertainen-hyvaksyminen "2018-12-15"}
          response
          (hoks-utils/mock-st-post
            (hoks-utils/create-app nil) base-url hoks-data)
          body (test-utils/parse-body (:body response))]
      (is (= (:status response) 200))
      (eq body {:data {:uri (format "%s/1" base-url)} :meta {:id 1}})
      (let [hoks
            (-> (get-in body [:data :uri]) hoks-utils/get-authenticated :data)]
        (eq
          hoks
          (assoc (add-empty-hoks-values hoks-data)
                 :id 1
                 :eid (:eid hoks)
                 :manuaalisyotto false))))))

(deftest get-created-hoks-with-tuva-oo
  (testing "GET newly created HOKS with TUVA opiskeluoikeus oid"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                     :tuva-opiskeluoikeus-oid "1.2.246.562.15.20000000008"
                     :oppija-oid "1.2.246.562.24.12312312319"
                     :osaamisen-hankkimisen-tarve true
                     :ensikertainen-hyvaksyminen "2018-12-15"}
          response
          (hoks-utils/mock-st-post
            (hoks-utils/create-app nil) base-url hoks-data)
          body (test-utils/parse-body (:body response))]
      (is (= (:status response) 200))
      (eq body {:data {:uri (format "%s/1" base-url)} :meta {:id 1}})
      (let [hoks
            (-> (get-in body [:data :uri]) hoks-utils/get-authenticated :data)]
        (eq
          hoks
          (assoc (add-empty-hoks-values hoks-data)
                 :id 1
                 :eid (:eid hoks)
                 :manuaalisyotto false))))))

(deftest opiskeluoikeus-type-is-validated
  (testing "Opiskeluoikeus type is validated"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.60000000012"
                     :oppija-oid "1.2.246.562.24.12312312319"
                     :osaamisen-hankkimisen-tarve true
                     :ensikertainen-hyvaksyminen "2025-01-01"}
          response
          (hoks-utils/mock-st-post
            (hoks-utils/create-app nil) base-url hoks-data)
          body (test-utils/parse-body (:body response))]
      (is (= (:status response) 400))
      (is (not (nil? (-> body
                         :errors
                         :opiskeluoikeus-oid)))))))

(deftest tuva-oo-oid-is-validated
  (testing "TUVA opiskeluoikeus oid form is validated as oid"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                     :tuva-opiskeluoikeus-oid "foo"
                     :oppija-oid "1.2.246.562.24.12312312319"
                     :osaamisen-hankkimisen-tarve true
                     :ensikertainen-hyvaksyminen "2018-12-15"}
          response
          (hoks-utils/mock-st-post
            (hoks-utils/create-app nil) base-url hoks-data)
          body (test-utils/parse-body (:body response))]
      (is (= (:status response) 400))
      (is (not (nil? (-> body
                         :errors
                         :tuva-opiskeluoikeus-oid)))))))

(deftest get-last-version-of-hoks
  (testing "GET latest (second) version of HOKS"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                     :oppija-oid "1.2.246.562.24.12312312319"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}]
      (let [response
            (hoks-utils/mock-st-post
              (hoks-utils/create-app nil) base-url hoks-data)
            body (test-utils/parse-body (:body response))]
        (is (= (:status response) 200))
        (eq body
            {:data {:uri (format "%s/1" base-url)} :meta {:id 1}})
        (let [hoks
              (-> (get-in body [:data :uri])
                  hoks-utils/get-authenticated :data)]
          (is (= (count (:eid hoks)) 36))
          (eq
            hoks
            (assoc (add-empty-hoks-values hoks-data)
                   :id 1
                   :eid (:eid hoks)
                   :manuaalisyotto false)))))))

(deftest creating-tuva-hoks-does-not-trigger-heratepalvelu
  (testing "Creating TUVA hoks does not trigger heratepalvelu"
    (let [sqs-call-counter (atom 0)]
      (with-redefs [sqs/send-amis-palaute-message
                    (fn [_] (swap! sqs-call-counter inc))]
        (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.30000000007"
                         :oppija-oid "1.2.246.562.24.12312312319"
                         :ensikertainen-hyvaksyminen "2018-12-15"
                         :osaamisen-hankkimisen-tarve true
                         :hankittavat-koulutuksen-osat
                         [{:koulutuksen-osa-koodi-uri "koulutuksenosattuva_104"
                           :koulutuksen-osa-koodi-versio 1
                           :alku "2022-09-01"
                           :loppu "2022-09-21"
                           :laajuus 10.4}]}
              response (hoks-utils/mock-st-post
                         (hoks-utils/create-app nil) base-url hoks-data)
              body (test-utils/parse-body (:body response))
              hoks-id (get-in body [:meta :id])
              kasittelytilat
              (first
                (db-ops/query
                  [queries/select-amisherate-kasittelytilat-by-hoks-id hoks-id]
                  {:row-fn db-ops/from-sql}))]
          (is (= (:status response) 200))
          (eq body {:data {:uri (format "%s/1" base-url)} :meta {:id 1}})
          (is (= (:aloitusherate-kasitelty kasittelytilat) true))
          (is (= (:paattoherate-kasitelty kasittelytilat) true))
          (is (= @sqs-call-counter 0)))))))

(deftest create-new-hoks-with-valid-osa-aikaisuus
  (testing "Create new hoks with valid osa-aikaisuustieto"
    (let [hoks-data test-data/new-hoks-with-valid-osa-aikaisuus
          response  (hoks-utils/mock-st-post
                      (hoks-utils/create-app nil) base-url hoks-data)
          body      (test-utils/parse-body (:body response))]
      (is (= (:status response) 200)))))

(deftest create-new-hoks-without-osa-aikaisuus
  (testing "Create new hoks without osa-aikaisuustieto"
    (let [hoks-data test-data/new-hoks-without-osa-aikaisuus
          response  (hoks-utils/mock-st-post
                      (hoks-utils/create-app nil) base-url hoks-data)
          body      (test-utils/parse-body (:body response))]
      (is (= (:status response) 400))
      (is (eq (:errors body)
              {:hankittavat-ammat-tutkinnon-osat
               [{:osaamisen-hankkimistavat
                 [(str "(not (\"Lisää osa-aikaisuustieto, joka on välillä "
                       "1-100.\" a-clojure.lang.PersistentArrayMap))")]}]})))))

(deftest create-new-telma-hoks-without-osa-aikaisuus
  (testing "Create new hoks without osa-aikaisuustieto"
    (let [hoks-data test-data/new-hoks-without-osa-aikaisuus-telma
          response  (hoks-utils/mock-st-post
                      (hoks-utils/create-app nil) base-url hoks-data)
          body      (test-utils/parse-body (:body response))]
      (is (= (:status response) 200))
      (is (= (get-in body [:data :uri]) (str base-url "/1"))))))

(deftest osaamisen-hankkimistavat-isnt-mandatory
  (testing "Osaamisen hankkimistavat should be optional field in ehoks"
    (let [app (hoks-utils/create-app nil)
          hoks {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                :oppija-oid "1.2.246.562.24.12312312319"
                :ensikertainen-hyvaksyminen "2018-12-15"
                :osaamisen-hankkimisen-tarve false
                :hankittavat-ammat-tutkinnon-osat
                [(dissoc parts-test-data/hao-data :osaamisen-hankkimistavat)]
                :hankittavat-paikalliset-tutkinnon-osat
                [(dissoc parts-test-data/hpto-data :osaamisen-hankkimistavat)]}
          post-response (hoks-utils/create-mock-post-request "" hoks app)
          get-response (hoks-utils/create-mock-hoks-get-request 1 app)
          get-response-data (-> (:body get-response)
                                test-utils/parse-body
                                :data)]
      (is (= (:status post-response) 200))
      (is (= (:status get-response) 200))
      (is (empty?
            (get-in
              get-response-data
              [:hankittavat-ammat-tutkinnon-osat
               :osaamisen-hankkimistavat])))
      (is (empty?
            (get-in
              get-response-data
              [:hankittavat-paikalliset-tutkinnon-osat
               :osaamisen-hankkimistavat]))))))

(def one-value-of-hoks-patched
  {:id 1
   :ensikertainen-hyvaksyminen "2020-01-05"})

(deftest patch-one-value-of-hoks
  (testing "PATCH updates value of created HOKS"
    (let [app (hoks-utils/create-app nil)
          post-response
          (hoks-utils/create-mock-post-request "" test-data/hoks-data app)
          patch-response (hoks-utils/create-mock-hoks-patch-request
                           1 one-value-of-hoks-patched app)
          get-response (hoks-utils/create-mock-hoks-get-request 1 app)
          get-response-data (-> (:body get-response)
                                test-utils/parse-body
                                :data)]
      (is (= (:status post-response) 200))
      (is (= (:status patch-response) 204))
      (is (= (:status get-response) 200))
      (is (= (:ensikertainen-hyvaksyminen get-response-data)
             (:ensikertainen-hyvaksyminen one-value-of-hoks-patched))
          "Patched value should change.")
      (is (= (:kuvaus get-response-data)
             (:kuvaus one-value-of-hoks-patched))
          "Value should stay unchanged"))))

(def osaaminen-saavutettu-patch
  {:id 1
   :osaamisen-saavuttamisen-pvm "2020-01-01"})

(deftest patch-hoks-as-osaaminen-saavutettu
  (testing "PATCH updates value of created HOKS"
    (let [app (hoks-utils/create-app nil)
          post-response
          (hoks-utils/create-mock-post-request "" test-data/hoks-data app)
          patch-response (hoks-utils/create-mock-hoks-patch-request
                           1 osaaminen-saavutettu-patch app)
          get-response (hoks-utils/create-mock-hoks-get-request 1 app)
          get-response-data (-> (:body get-response)
                                test-utils/parse-body
                                :data)]
      ; TODO: Marking hoks with osaaminen-saavuttaminen-pvm triggers sending
      ; a message to herätepalvelu that should be tested also when it's done
      (is (= (:status post-response) 200))
      (is (= (:status patch-response) 204))
      (is (= (:status get-response) 200))
      (is (= (:osaamisen-saavuttamisen-pvm get-response-data)
             (:osaamisen-saavuttamisen-pvm osaaminen-saavutettu-patch))
          "Patched value should change.")
      (is (= (:kuvaus get-response-data)
             (:kuvaus osaaminen-saavutettu-patch))
          "Value should stay unchanged"))))

(def main-level-of-hoks-updated
  {:id 1
   :ensikertainen-hyvaksyminen "2018-12-15"
   :osaamisen-hankkimisen-tarve false})

(deftest hoks-put-removes-parts
  (testing "PUT only main level HOKS values, removes parts"
    (let [app (hoks-utils/create-app nil)
          post-response
          (hoks-utils/create-mock-post-request "" test-data/hoks-data app)
          put-response (hoks-utils/create-mock-hoks-put-request
                         1 main-level-of-hoks-updated app)
          get-response (hoks-utils/create-mock-hoks-get-request 1 app)
          get-response-data (-> (:body get-response)
                                test-utils/parse-body
                                :data)]
      (is (= (:status post-response) 200))
      (is (= (:status put-response) 204))
      (is (= (:status get-response) 200))
      (is (empty? (:opiskeluvalmiuksia-tukevat-opinnot get-response-data)))
      (is (empty? (:hankittavat-ammat-tutkinnon-osat get-response-data)))
      (is (empty? (:hankittavat-paikalliset-tutkinnon-osat get-response-data)))
      (is (empty? (:hankittavat-yhteiset-tutkinnon-osat get-response-data)))
      (is (empty? (:aiemmin-hankitut-ammat-tutkinnon-osat get-response-data)))
      (is (empty?
            (:aiemmin-hankitut-paikalliset-tutkinnon-osat get-response-data)))
      (is (empty?
            (:aiemmin-hankitut-yhteiset-tutkinnon-osat get-response-data))))))

(defn mock-replace-ahyto [& _]
  (throw (Exception. "Failed.")))

(deftest hoks-part-put-fails-whole-operation-is-aborted
  (testing (str "PUT of HOKS should be inside transaction so that when"
                "one part of operation fails, everything is aborted")
    (with-redefs [hoks/replace-ahyto! mock-replace-ahyto]
      (let [app (hoks-utils/create-app nil)
            post-response (hoks-utils/create-mock-post-request
                            "" test-data/hoks-data app)
            put-response (hoks-utils/create-mock-hoks-put-request
                           1 main-level-of-hoks-updated app)
            get-response (hoks-utils/create-mock-hoks-get-request 1 app)
            get-response-data (-> (:body get-response)
                                  test-utils/parse-body
                                  :data)]
        (is (= (:status post-response) 200))
        (is (= (:status put-response) 500))
        (is (= (:status get-response) 200))
        (is (= (:versio get-response-data) 4))
        (is (not-empty (:opiskeluvalmiuksia-tukevat-opinnot
                         get-response-data)))
        (is (not-empty (:hankittavat-ammat-tutkinnon-osat
                         get-response-data)))
        (is (not-empty (:hankittavat-paikalliset-tutkinnon-osat
                         get-response-data)))
        (is (not-empty (:hankittavat-yhteiset-tutkinnon-osat
                         get-response-data)))
        (is (not-empty (:aiemmin-hankitut-ammat-tutkinnon-osat
                         get-response-data)))
        (is (not-empty (:aiemmin-hankitut-paikalliset-tutkinnon-osat
                         get-response-data)))
        (is (not-empty (:aiemmin-hankitut-yhteiset-tutkinnon-osat
                         get-response-data)))))))

(deftest hoks-put-adds-non-existing-part
  (testing "If HOKS part doesn't currently exist, PUT creates it"
    (let [app (hoks-utils/create-app nil)
          post-response
          (hoks-utils/create-mock-post-request
            ""
            (dissoc test-data/hoks-data :opiskeluvalmiuksia-tukevat-opinnot)
            app)
          put-response (hoks-utils/create-mock-hoks-put-request
                         1
                         (-> test-data/hoks-data
                             (assoc :id 1)
                             (dissoc :opiskeluoikeus-oid :oppija-oid))
                         app)
          get-response (hoks-utils/create-mock-hoks-get-request 1 app)
          get-response-data (-> (:body get-response)
                                test-utils/parse-body
                                :data)]
      (is (= (:status post-response) 200))
      (is (= (:status put-response) 204))
      (is (= (:status get-response) 200))
      (eq (test-utils/dissoc-module-ids
            (:opiskeluvalmiuksia-tukevat-opinnot test-data/hoks-data))
          (test-utils/dissoc-module-ids
            (:opiskeluvalmiuksia-tukevat-opinnot get-response-data))))))

(deftest hoks-put-updates-oht-using-yksiloiva-tunniste
  (testing (str "If matching oht yksiloiva-tunniste is found, update oht."
                "If not found, add new oht.")
    (let [app (hoks-utils/create-app nil)
          post-response (hoks-utils/create-mock-post-request
                          "" test-data/hoks-data app)
          put-response
          (hoks-utils/create-mock-hoks-put-request
            1
            (-> test-data/hoks-data
                (assoc :id 1)
                (dissoc :hankittavat-ammat-tutkinnon-osat)
                (dissoc :hankittavat-yhteiset-tutkinnon-osat)
                (assoc
                  :hankittavat-ammat-tutkinnon-osat
                  test-data/hao-data-oht-matching-tunniste)
                (assoc
                  :hankittavat-yhteiset-tutkinnon-osat
                  test-data/hyto-data-oht-matching-and-new-tunniste))
            app)
          get-response (hoks-utils/create-mock-hoks-get-request 1 app)
          get-response-data (:data (test-utils/parse-body (:body get-response)))
          hao-hankkimistavat (:osaamisen-hankkimistavat
                               (first (:hankittavat-ammat-tutkinnon-osat
                                        get-response-data)))
          hyto-hankkimistavat
          (:osaamisen-hankkimistavat
            (first (:osa-alueet
                     (first (:hankittavat-yhteiset-tutkinnon-osat
                              get-response-data)))))]
      (is (= (:status post-response) 200))
      (is (= (:status put-response) 204))
      (is (= (:status get-response) 200))
      (is (= (count hao-hankkimistavat) 1))
      (is (= (count hyto-hankkimistavat) 2))
      ;; hankkijan edustaja poistuu jos puuttuu tulevasta datasta
      (eq (:hankkijan-edustaja (first hao-hankkimistavat))
          (:hankkijan-edustaja
            (first
              (:osaamisen-hankkimistavat
                (first
                  test-data/hao-data-oht-matching-tunniste)))))
      ;; arvo päivittyy uuteen
      (eq (:osa-aikaisuustieto (first hao-hankkimistavat))
          (:osa-aikaisuustieto
            (first
              (:osaamisen-hankkimistavat
                (first
                  test-data/hao-data-oht-matching-tunniste)))))
      ;; sama arvo ei muutu
      (eq (:ajanjakson-tarkenne (first hao-hankkimistavat))
          (:ajanjakson-tarkenne
            (first
              (:osaamisen-hankkimistavat
                (first
                  test-data/hao-data-oht-matching-tunniste)))))
      ;; arvo päivittyy uuteen hyton osa-alueissa
      (eq
        (:loppu (first (filter
                         #(= "qiuewyroqiwuer" (:yksiloiva-tunniste %))
                         hyto-hankkimistavat)))
        (:loppu
          (first
            (filter
              #(= "qiuewyroqiwuer" (:yksiloiva-tunniste %))
              (:osaamisen-hankkimistavat
                (first
                  (:osa-alueet
                    (first
                      test-data/hyto-data-oht-matching-and-new-tunniste))))))))
      ;; puuttuva arvo poistuu
      (is (nil? (:osa-aikaisuustieto
                  (first (filter
                           #(= "qiuewyroqiwuer" (:yksiloiva-tunniste %))
                           hyto-hankkimistavat)))))
      ;; datan mukana tuleva tunniste tallentuu
      (is (some?
            (some
              #(= "uusi-tunniste" (:yksiloiva-tunniste %))
              hyto-hankkimistavat))))))

(def oto-of-hoks-updated
  {:id 1
   :ensikertainen-hyvaksyminen "2018-12-15"
   :osaamisen-hankkimisen-tarve false
   :opiskeluvalmiuksia-tukevat-opinnot
   [{:nimi "Uusi Nimi"
     :kuvaus "joku kuvaus"
     :alku "2019-06-22"
     :loppu "2021-05-07"}]})

(deftest put-oto-of-hoks
  (testing "PUTs opiskeluvalmiuksia tukevat opinnot of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      oto-of-hoks-updated
      :opiskeluvalmiuksia-tukevat-opinnot
      test-data/hoks-data)))

(def multiple-otos-of-hoks-updated
  {:id 1
   :ensikertainen-hyvaksyminen "2018-12-15"
   :osaamisen-hankkimisen-tarve false
   :opiskeluvalmiuksia-tukevat-opinnot
   [{:nimi "Uusi Nimi"
     :kuvaus "joku kuvaus"
     :alku "2019-06-22"
     :loppu "2021-05-07"}
    {:nimi "Toinen Nimi"
     :kuvaus "eri kuvaus"
     :alku "2018-06-22"
     :loppu "2022-05-07"}]})

(deftest put-multiple-oto-of-hoks
  (testing "PUTs multiple opiskeluvalmiuksia tukevat opinnot of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      multiple-otos-of-hoks-updated
      :opiskeluvalmiuksia-tukevat-opinnot
      test-data/hoks-data)))

(deftest omitted-hoks-fields-are-nullified
  (testing "If HOKS main level value isn't given in PUT, it's nullified"
    (let [app (hoks-utils/create-app nil)
          post-response (hoks-utils/create-mock-post-request
                          "" test-data/hoks-data app)
          put-response (hoks-utils/create-mock-hoks-put-request
                         1 main-level-of-hoks-updated app)
          get-response (hoks-utils/create-mock-hoks-get-request 1 app)
          get-response-data (-> (:body get-response)
                                test-utils/parse-body
                                :data)]
      (is (= (:status post-response) 200))
      (is (= (:status put-response) 204))
      (is (= (:status get-response) 200))
      (is (nil? (:versio get-response-data)))
      (is (nil? (:sahkoposti get-response-data)))
      (is (nil? (:urasuunnitelma-koodi-uri get-response-data)))
      (is (nil? (:osaamisen-saavuttamisen-pvm get-response-data)))
      (is (nil? (:hyvaksytty get-response-data)))
      (is (nil? (:urasuunnitelma-koodi-versio get-response-data)))
      (is (nil? (:paivitetty get-response-data))))))

(deftest put-hato-of-hoks
  (testing "PUTs hankittavat ammatilliset tutkinnon osat of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      test-data/hato-of-hoks-updated
      :hankittavat-ammat-tutkinnon-osat
      test-data/hoks-data)))

(deftest put-hpto-of-hoks
  (testing "PUTs hankittavat paikalliset tutkinnon osat of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      test-data/hpto-of-hoks-updated
      :hankittavat-paikalliset-tutkinnon-osat
      test-data/hoks-data)))

(deftest put-hyto-of-hoks
  (testing "PUTs hankittavat yhteiset tutkinnon osat of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      test-data/hyto-of-hoks-updated
      :hankittavat-yhteiset-tutkinnon-osat
      test-data/hoks-data)))

(deftest get-hoks-by-opiskeluoikeus-oid
  (testing "GET HOKS by opiskeluoikeus-oid"
    (let [opiskeluoikeus-oid "1.2.246.562.15.10000000009"
          hoks-data {:opiskeluoikeus-oid opiskeluoikeus-oid
                     :oppija-oid "1.2.246.562.24.12312312319"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}
          app (hoks-utils/create-app nil)]
      (let [response
            (hoks-utils/mock-st-get
              app
              (format "%s/opiskeluoikeus/%s"
                      base-url opiskeluoikeus-oid))]
        (is (= (:status response) 404)))
      (hoks-utils/mock-st-post app base-url hoks-data)
      (let [response
            (hoks-utils/mock-st-get
              app
              (format "%s/opiskeluoikeus/%s"
                      base-url opiskeluoikeus-oid))
            body (test-utils/parse-body (:body response))]
        (is (= (:status response) 200))
        (is (= (-> body
                   :data
                   :opiskeluoikeus-oid)
               opiskeluoikeus-oid))))))

(deftest non-service-user-test
  (testing "Deny access from non-service user"
    (client/with-mock-responses
      [(fn [^String url options]
         (cond (.endsWith
                 url "/koski/api/opiskeluoikeus/1.2.246.562.15.10000000009")
               {:status 200
                :body {:oppilaitos {:oid "1.2.246.562.10.12944436166"}}}
               (.endsWith url "/serviceValidate")
               {:status 200
                :body
                (str "<cas:serviceResponse"
                     "  xmlns:cas='http://www.yale.edu/tp/cas'>"
                     "<cas:authenticationSuccess><cas:user>ehoks</cas:user>"
                     "<cas:attributes>"
                     "<cas:longTermAuthenticationRequestTokenUsed>false"
                     "</cas:longTermAuthenticationRequestTokenUsed>"
                     "<cas:isFromNewLogin>false</cas:isFromNewLogin>"
                     "<cas:authenticationDate>2019-02-20T10:14:24.046+02:00"
                     "</cas:authenticationDate></cas:attributes>"
                     "</cas:authenticationSuccess></cas:serviceResponse>")}
               (.endsWith url "/kayttooikeus-service/kayttooikeus/kayttaja")
               {:status 200
                :body [{:oidHenkilo "1.2.246.562.24.11474338834"
                        :username "ehoks"
                        :kayttajaTyyppi "VIRKAILIJA"
                        :organisaatiot
                        [{:organisaatioOid "1.2.246.562.10.12944436166"
                          :kayttooikeudet [{:palvelu "EHOKS"
                                            :oikeus "CRUD"}]}]}]}))
       (fn [^String url options]
         (cond
           (.endsWith url "/v1/tickets")
           {:status 201
            :headers {"location" "http://test.ticket/1234"}}
           (= url "http://test.ticket/1234")
           {:status 200
            :body "ST-1234-testi"}))]
      (let [app (hoks-utils/create-app nil)
            hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                       :oppija-oid "1.2.246.562.24.12312312319"
                       :laatija {:nimi "Teppo Tekijä"}
                       :paivittaja {:nimi "Pekka Päivittäjä"}
                       :hyvaksyja {:nimi "Heikki Hyväksyjä"}
                       :osaamisen-hankkimisen-tarve false
                       :ensikertainen-hyvaksyminen "2018-12-15"}
            response (app (-> (mock/request :post base-url)
                              (mock/json-body hoks-data)
                              (mock/header "Caller-Id" "test")
                              (mock/header "ticket" "ST-testitiketti")))]
        (is (= (:status response) 403))
        (is (= (test-utils/parse-body (:body response))
               {:error "User type 'PALVELU' is required"}))))))

(deftest post-kyselylinkki
  (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                   :oppija-oid "1.2.246.562.24.12312312319"
                   :osaamisen-hankkimisen-tarve true
                   :ensikertainen-hyvaksyminen "2018-12-15"}
        app (hoks-utils/create-app nil)
        hoks-resp (hoks-utils/mock-st-post
                    app base-url hoks-data)
        req (mock/request
              :post
              (str base-url "/1/kyselylinkki"))
        data {:kyselylinkki "https://palaute.fi/abc123"
              :alkupvm (str (t/today))
              :tyyppi "aloittaneet"
              :lahetystila "ei_lahetetty"}]

    (test-utils/with-service-ticket
      app
      (mock/json-body req data)
      "1.2.246.562.10.00000000001")

    (is (= "https://palaute.fi/abc123"
           (:kyselylinkki (first (kyselylinkki/get-by-oppija-oid!
                                   "1.2.246.562.24.12312312319")))))))

(deftest put-kyselylinkki-lahetysdata
  (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                   :oppija-oid "1.2.246.562.24.12312312319"
                   :osaamisen-hankkimisen-tarve true
                   :ensikertainen-hyvaksyminen "2018-12-15"}
        app (hoks-utils/create-app nil)
        hoks-resp (hoks-utils/mock-st-post
                    app base-url hoks-data)
        req1 (mock/request
               :post
               (str base-url "/1/kyselylinkki"))
        req2 (mock/request
               :patch
               (str base-url "/kyselylinkki"))
        data-post {:kyselylinkki "https://palaute.fi/abc123"
                   :alkupvm (str (t/today))
                   :tyyppi "aloittaneet"
                   :lahetystila "ei_lahetetty"}
        data-patch {:kyselylinkki "https://palaute.fi/abc123"
                    :lahetyspvm (str (t/today))
                    :sahkoposti "testi@testi.fi"
                    :lahetystila "viestintapalvelussa"}]

    (test-utils/with-service-ticket
      app
      (mock/json-body req1 data-post)
      "1.2.246.562.10.00000000001")

    (is (nil? (:sahkoposti (first (kyselylinkki/get-by-oppija-oid!
                                    "1.2.246.562.24.12312312319")))))

    (test-utils/with-service-ticket
      app
      (mock/json-body req2 data-patch)
      "1.2.246.562.10.00000000001")

    (is (= "testi@testi.fi"
           (:sahkoposti (first (kyselylinkki/get-by-oppija-oid!
                                 "1.2.246.562.24.12312312319")))))
    (is (= "viestintapalvelussa"
           (:lahetystila (first (kyselylinkki/get-by-oppija-oid!
                                  "1.2.246.562.24.12312312319")))))))

(deftest get-paged-vipunen-data
  (testing "GET paged HOKSes for Vipunen"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                     :oppija-oid "1.2.246.562.24.12312312319"
                     :sahkoposti "tyyppi@mesta.fi"
                     :osaamisen-hankkimisen-tarve true
                     :ensikertainen-hyvaksyminen "2018-12-15"}
          app (hoks-utils/create-app nil)
          post-response (hoks-utils/mock-st-post app base-url hoks-data)]
      (is (= (:status post-response) 200))
      (let [hoks-uri (-> (test-utils/parse-body (:body post-response))
                         :data
                         :uri)
            get-response (hoks-utils/mock-st-get app hoks-uri)]
        (is (= (:status get-response) 200))
        (let [hoks (-> (test-utils/parse-body (:body get-response))
                       :data)
              paged-response (hoks-utils/mock-st-get
                               app (format "%s/paged" base-url))]
          (is (= (:status paged-response) 200))
          (let [body (test-utils/parse-body (:body paged-response))]
            (eq (-> (get-in body [:data :result 0])
                    (select-keys (keys hoks-data)))
                (dissoc hoks-data :sahkoposti))
            (is (= (get-in body [:data :result 0 :id])
                   (:id hoks)))))))))

(deftest get-paged-deleted
  (testing "GET paged HOKSes with deleted item"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                     :oppija-oid "1.2.246.562.24.12312312319"
                     :osaamisen-hankkimisen-tarve true
                     :ensikertainen-hyvaksyminen "2018-12-15"}
          app (hoks-utils/create-app nil)
          post-response (hoks-utils/mock-st-post app base-url hoks-data)]
      (is (= (:status post-response) 200))
      (let [hoks-uri (-> (test-utils/parse-body (:body post-response))
                         :data
                         :uri)
            get-response (hoks-utils/mock-st-get app hoks-uri)]
        (is (= (:status get-response) 200))
        (let [hoks (-> (test-utils/parse-body (:body get-response))
                       :data)
              hoks-id (-> hoks :id)]
          (db-hoks/soft-delete-hoks-by-hoks-id hoks-id)
          (let [paged-response (hoks-utils/mock-st-get
                                 app (format "%s/paged" base-url))
                paged-body (test-utils/parse-body (:body paged-response))]
            (is (= (:status paged-response) 200))
            (is (= (-> paged-body
                       :data
                       :result
                       first
                       :id)
                   hoks-id))
            (is (empty? (-> paged-body
                            :data
                            :failed-ids)))
            (is (not (nil? (-> paged-body
                               :data
                               :result
                               first
                               :poistettu))))))))))

(defn make-timestamp
  ([plus-millis]
    (let [tz (java.util.TimeZone/getTimeZone "UTC")
          df (new java.text.SimpleDateFormat "yyyy-MM-dd'T'HH:mm:ss'Z'")
          ^long millis (+ (.getTime (new java.util.Date))
                          plus-millis)]
      (.setTimeZone df tz)
      (.format df (new java.util.Date millis))))
  ([] (make-timestamp 0)))

(deftest get-paged-delta-with-deleted
  (testing "GET paged delta stream of HOKSes"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                     :oppija-oid "1.2.246.562.24.12312312319"
                     :osaamisen-hankkimisen-tarve true
                     :ensikertainen-hyvaksyminen "2018-12-15"}
          app (hoks-utils/create-app nil)
          post-response (hoks-utils/mock-st-post app base-url hoks-data)]
      (is (= (:status post-response) 200))
      (let [hoks-uri (-> (test-utils/parse-body (:body post-response))
                         :data
                         :uri)
            get-response (hoks-utils/mock-st-get app hoks-uri)]
        (is (= (:status get-response) 200))
        (let [hoks (-> (test-utils/parse-body (:body get-response))
                       :data)
              hoks-id (-> hoks :id)
              paged-response (hoks-utils/mock-st-get
                               app (format "%s/paged" base-url))
              paged-body (test-utils/parse-body (:body paged-response))
              before-delete-ts (make-timestamp)]
          (is (= (:status paged-response) 200))
          (is (= (-> paged-body
                     :data
                     :result
                     first
                     :id)
                 hoks-id))
          (is (not (contains? (-> paged-body
                                  :data
                                  :result
                                  first)
                              :poistettu)))
          (db-hoks/soft-delete-hoks-by-hoks-id hoks-id)
          (let [before-delete-resp (hoks-utils/mock-st-get
                                     app (format "%s/paged?updated-after=%s"
                                                 base-url
                                                 before-delete-ts))
                before-delete-body (test-utils/parse-body
                                     (:body before-delete-resp))
                after-delete-ts (make-timestamp 2000)
                after-delete-resp (hoks-utils/mock-st-get
                                    app (format "%s/paged?updated-after=%s"
                                                base-url
                                                after-delete-ts))
                after-delete-body (test-utils/parse-body
                                    (:body after-delete-resp))]
            (is (= (:status before-delete-resp) 200))
            (is (= (-> before-delete-body
                       :data
                       :result
                       first
                       :id)
                   hoks-id))
            (is (not (nil? (-> before-delete-body
                               :data
                               :result
                               first
                               :poistettu))))
            (is (= (:status after-delete-resp) 200))
            (is (empty? (-> after-delete-body
                            :data
                            :result)))))))))

(defn select-deleted-at-from
  [table where arg]
  (-> (db-ops/query [(str "SELECT deleted_at FROM " table " WHERE " where) arg])
      first
      :deleted_at))

(deftest test-hoks-soft-delete-and-undo
  (testing "HOKS soft delete and undo"
    (let [app (hoks-utils/create-app nil)
          hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                     :oppija-oid "1.2.246.562.24.12312312319"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false
                     :hankittavat-ammat-tutkinnon-osat
                     [parts-test-data/hao-data]
                     :hankittavat-paikalliset-tutkinnon-osat
                     [parts-test-data/hpto-data]
                     :hankittavat-yhteiset-tutkinnon-osat
                     [parts-test-data/hyto-data]
                     :aiemmin-hankitut-ammat-tutkinnon-osat
                     [parts-test-data/ahato-data]
                     :aiemmin-hankitut-paikalliset-tutkinnon-osat
                     [parts-test-data/ahpto-data]
                     :aiemmin-hankitut-yhteiset-tutkinnon-osat
                     [parts-test-data/ahyto-data]}
          post-response (hoks-utils/mock-st-post app base-url hoks-data)]
      (is (= (:status post-response) 200))
      (let [hoks-uri (-> post-response :body (test-utils/parse-body) :data :uri)
            get-response (hoks-utils/mock-st-get app hoks-uri)
            hoks (-> get-response :body (test-utils/parse-body) :data)
            hoks-id (:id hoks)]
        (is (= (:status get-response) 200))
        (is (some? (seq (:hankittavat-ammat-tutkinnon-osat hoks))))
        (is (some? (seq (:hankittavat-paikalliset-tutkinnon-osat hoks))))
        (is (some? (seq (:hankittavat-yhteiset-tutkinnon-osat hoks))))
        (db-hoks/soft-delete-hoks-by-hoks-id hoks-id)
        (let [get-resp (hoks-utils/mock-st-get app hoks-uri)]
          (is (= (:status get-resp) 404)))
        (let [hoks (select-deleted-at-from
                     "hoksit"
                     "id = ?" hoks-id)
              hato (select-deleted-at-from
                     "hankittavat_ammat_tutkinnon_osat"
                     "hoks_id = ?" hoks-id)
              hpto (select-deleted-at-from
                     "hankittavat_paikalliset_tutkinnon_osat"
                     "hoks_id = ?" hoks-id)
              hyto (select-deleted-at-from
                     "hankittavat_yhteiset_tutkinnon_osat"
                     "hoks_id = ?" hoks-id)
              ahato (select-deleted-at-from
                      "aiemmin_hankitut_ammat_tutkinnon_osat"
                      "hoks_id = ?" hoks-id)
              ahpto (select-deleted-at-from
                      "aiemmin_hankitut_paikalliset_tutkinnon_osat"
                      "hoks_id = ?" hoks-id)
              ahyto (select-deleted-at-from
                      "aiemmin_hankitut_yhteiset_tutkinnon_osat"
                      "hoks_id = ?" hoks-id)]
          (is (not (nil? hoks)))
          (is (= hoks hato))
          (is (= hoks hpto))
          (is (= hoks hyto))
          (is (= hoks ahato))
          (is (= hoks ahpto))
          (is (= hoks ahyto)))
        (db-hoks/undo-soft-delete hoks-id)
        (let [get-resp (hoks-utils/mock-st-get app hoks-uri)]
          (is (= (:status get-resp) 200))
          (is (= (-> (test-utils/parse-body (:body get-resp))
                     :data) hoks)))))))

(deftest test-hoks-delete-undo-patch-hoks-delete-undo
  (testing "HOKS soft delete, undo, patch hoks, delete & undo"
    ; luo hoks
    (let [app (hoks-utils/create-app nil)
          hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                     :oppija-oid "1.2.246.562.24.12312312319"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false
                     :hankittavat-ammat-tutkinnon-osat
                     [parts-test-data/hao-data]
                     :hankittavat-paikalliset-tutkinnon-osat
                     [parts-test-data/hpto-data]
                     :hankittavat-yhteiset-tutkinnon-osat
                     [parts-test-data/hyto-data]
                     :aiemmin-hankitut-ammat-tutkinnon-osat
                     [parts-test-data/ahato-data]
                     :aiemmin-hankitut-paikalliset-tutkinnon-osat
                     [parts-test-data/ahpto-data]
                     :aiemmin-hankitut-yhteiset-tutkinnon-osat
                     [parts-test-data/ahyto-data]}
          post-response (hoks-utils/mock-st-post app base-url hoks-data)]
      (is (= (:status post-response) 200))
      (let [hoks-uri (-> post-response :body (test-utils/parse-body) :data :uri)
            get-response (hoks-utils/mock-st-get app hoks-uri)
            hoks (-> get-response :body (test-utils/parse-body) :data)
            hoks-id (:id hoks)]
        (is (= (:status get-response) 200))
        ; poista ja palauta
        (db-hoks/soft-delete-hoks-by-hoks-id hoks-id)
        (db-hoks/undo-soft-delete hoks-id)
        (let [get-resp (hoks-utils/mock-st-get app hoks-uri)]
          (is (= (:status get-resp) 200))
          (is (= (-> (test-utils/parse-body (:body get-resp))
                     :data) hoks)))
        ; päivitä hoksia
        (let [patch-response
              (hoks-utils/create-mock-hoks-patch-request
                hoks-id
                {:id hoks-id
                 :osaamisen-saavuttamisen-pvm "2023-12-31"}
                app)]
          (is (= (:status patch-response) 204))
          (let [get-hoks-after-patch (hoks-utils/mock-st-get app hoks-uri)
                hoks-after-patch
                (-> get-hoks-after-patch :body (test-utils/parse-body) :data)]
            (is (= (:status get-hoks-after-patch) 200))
            (is (= "2023-12-31"
                   (:osaamisen-saavuttamisen-pvm hoks-after-patch)))
            ; poista ja palauta
            (db-hoks/soft-delete-hoks-by-hoks-id hoks-id)
            (db-hoks/undo-soft-delete hoks-id)
            (let [get-resp (hoks-utils/mock-st-get app hoks-uri)]
              (is (= (:status get-resp) 200))
              (is (= (-> (test-utils/parse-body (:body get-resp))
                         :data) hoks-after-patch)))))))))

(deftest test-hoks-delete-undo-update-hato-delete-undo
  (testing "HOKS soft delete, undo, patch hato, delete & undo"
    ; luo hoks
    (let [app (hoks-utils/create-app nil)
          hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                     :oppija-oid "1.2.246.562.24.12312312319"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false
                     :hankittavat-ammat-tutkinnon-osat
                     [parts-test-data/hao-data]
                     :hankittavat-paikalliset-tutkinnon-osat
                     [parts-test-data/hpto-data]
                     :hankittavat-yhteiset-tutkinnon-osat
                     [parts-test-data/hyto-data]
                     :aiemmin-hankitut-ammat-tutkinnon-osat
                     [parts-test-data/ahato-data]
                     :aiemmin-hankitut-paikalliset-tutkinnon-osat
                     [parts-test-data/ahpto-data]
                     :aiemmin-hankitut-yhteiset-tutkinnon-osat
                     [parts-test-data/ahyto-data]}
          post-response (hoks-utils/mock-st-post app base-url hoks-data)]
      (is (= (:status post-response) 200))
      (let [hoks-uri (-> post-response :body (test-utils/parse-body) :data :uri)
            get-response (hoks-utils/mock-st-get app hoks-uri)
            hoks (-> get-response :body (test-utils/parse-body) :data)
            hoks-id (:id hoks)]
        (is (= (:status get-response) 200))
        ; poista ja palauta
        (db-hoks/soft-delete-hoks-by-hoks-id hoks-id)
        (db-hoks/undo-soft-delete hoks-id)
        (let [get-resp (hoks-utils/mock-st-get app hoks-uri)]
          (is (= (:status get-resp) 200))
          (is (= (-> (test-utils/parse-body (:body get-resp))
                     :data) hoks)))
        ; päivitä hankittavaa ammatillista tutkinnon osaa
        (let [put-response (hoks-utils/mock-st-put
                             app
                             (format "%s/1" base-url)
                             (assoc-in (assoc hoks-data :id 1)
                                       [:hankittavat-ammat-tutkinnon-osat 0]
                                       (assoc parts-test-data/hao-data
                                              :opetus-ja-ohjaus-maara 10.5)))]
          (is (= (:status put-response) 204))
          (let [get-hoks-after-patch (hoks-utils/mock-st-get app hoks-uri)
                hoks-after-patch
                (-> get-hoks-after-patch :body (test-utils/parse-body) :data)]
            (is (= (:status get-hoks-after-patch) 200))
            (is (= (-> hoks-after-patch
                       :hankittavat-ammat-tutkinnon-osat
                       first
                       :opetus-ja-ohjaus-maara)
                   10.5))
            ; poista ja palauta
            (db-hoks/soft-delete-hoks-by-hoks-id hoks-id)
            (db-hoks/undo-soft-delete hoks-id)
            (let [get-resp (hoks-utils/mock-st-get app hoks-uri)]
              (is (= (:status get-resp) 200))
              (is (= (-> (test-utils/parse-body (:body get-resp))
                         :data) hoks-after-patch)))))))))

(deftest test-hoks-delete-undo-update-part-delete-undo
  (testing "HOKS soft delete, undo, update hato, delete & undo"
    ; luo hoks
    (let [app (hoks-utils/create-app nil)
          hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
                     :oppija-oid "1.2.246.562.24.12312312319"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false
                     :hankittavat-ammat-tutkinnon-osat
                     [parts-test-data/hao-data]
                     :hankittavat-paikalliset-tutkinnon-osat
                     [parts-test-data/hpto-data]
                     :hankittavat-yhteiset-tutkinnon-osat
                     [parts-test-data/hyto-data]
                     :aiemmin-hankitut-ammat-tutkinnon-osat
                     [parts-test-data/ahato-data]
                     :aiemmin-hankitut-paikalliset-tutkinnon-osat
                     [parts-test-data/ahpto-data]
                     :aiemmin-hankitut-yhteiset-tutkinnon-osat
                     [parts-test-data/ahyto-data]}
          post-response (hoks-utils/mock-st-post app base-url hoks-data)]
      (is (= (:status post-response) 200))
      (let [hoks-uri (-> post-response :body (test-utils/parse-body) :data :uri)
            get-response (hoks-utils/mock-st-get app hoks-uri)
            hoks (-> get-response :body (test-utils/parse-body) :data)
            hoks-id (:id hoks)]
        (is (= (:status get-response) 200))
        ; poista ja palauta
        (db-hoks/soft-delete-hoks-by-hoks-id hoks-id)
        (db-hoks/undo-soft-delete hoks-id)
        (let [get-resp (hoks-utils/mock-st-get app hoks-uri)]
          (is (= (:status get-resp) 200))
          (is (= (-> (test-utils/parse-body (:body get-resp))
                     :data) hoks)))
        (let [hoks-update
              (assoc hoks-data
                     :hankittavat-ammat-tutkinnon-osat
                     [parts-test-data/hao-data-with-valid-osa-aikaisuus]
                     :id hoks-id)
              ; päivitä olemassa oleva hato toiseksi
              update-response (hoks-utils/create-mock-hoks-put-request
                                hoks-id hoks-update app)]
          (is (= (:status update-response) 204))
          (let [get-hoks-after-update (hoks-utils/mock-st-get app hoks-uri)
                hoks-after-update
                (-> get-hoks-after-update :body (test-utils/parse-body) :data)]
            (is (= (:status get-hoks-after-update) 200))
            ; poista ja palauta
            (db-hoks/soft-delete-hoks-by-hoks-id hoks-id)
            (db-hoks/undo-soft-delete hoks-id)
            (let [get-hoks-after-undo (hoks-utils/mock-st-get app hoks-uri)
                  hoks-after-undo
                  (-> get-hoks-after-undo :body (test-utils/parse-body) :data)]
              (is (= (:status get-hoks-after-undo) 200))
              (is (= hoks-after-update hoks-after-undo)))
            (let [hatos (db-ops/query
                          [(str "SELECT id, deleted_at FROM "
                                "hankittavat_ammat_tutkinnon_osat WHERE "
                                "hoks_id = ?") hoks-id])]
              (is (= (count hatos) 2))
              ; aiempi hato on merkitty poistetuksi, jälkimmäinen ei
              (is (= (map #(assoc % :deleted_at (some? (:deleted_at %)))
                          hatos)
                     [{:id 1 :deleted_at true}
                      {:id 2 :deleted_at false}])))))))))

(deftest no-internal-server-error-in-response-validation-failure
  (testing (str "Response validation failure shouldn't give "
                "`internal-server-error` in response")
    (let [invalid-hoks
          (-> {:opiskeluoikeus-oid "1.2.246.562.15.10000000009"
               :oppija-oid "1.2.246.562.24.12312312319"
               :osaamisen-hankkimisen-tarve true
               :ensikertainen-hyvaksyminen (LocalDate/parse "2018-12-15")}
              add-empty-hoks-values
              (assoc :hankittavat-ammat-tutkinnon-osat
                     [{:tutkinnon-osa-koodi-uri "tutkinnonosat_300268"
                       :tutkinnon-osa-koodi-versio 1
                       :vaatimuksista-tai-tavoitteista-poikkeaminen
                       "Ei poikkeamia."
                       :opetus-ja-ohjaus-maara 10.1
                       :osaamisen-osoittaminen
                       [{:jarjestaja
                         {:oppilaitos-oid "1.2.246.562.10.54453924331"}
                         :nayttoymparisto {:nimi "Testiympäristö"
                                           :y-tunnus "invalid"
                                           :kuvaus "Testi test"}
                         :sisallon-kuvaus ["Testaus"]
                         :koulutuksen-jarjestaja-osaamisen-arvioijat []
                         :osa-alueet []
                         :tyoelama-osaamisen-arvioijat []
                         :yksilolliset-kriteerit []}]
                       :osaamisen-hankkimistavat []}]))
          app (hoks-utils/create-app nil)]
      (hoks-utils/mock-st-post
        app base-url (dissoc invalid-hoks :hankittavat-ammat-tutkinnon-osat))
      ; Päivitetään hoks suoraan kantaan ei-validilla datalla.
      (hoks/replace! (assoc invalid-hoks :id 1))
      (let [response (hoks-utils/mock-st-get app (format "%s/1" base-url))
            body     (test-utils/parse-body (:body response))]
        (is (= (:status response) 200))
        (is (= (-> (:data body)
                   (dissoc :id :eid :manuaalisyotto)
                   (update-in [:hankittavat-ammat-tutkinnon-osat 0]
                              dissoc
                              :module-id)
                   (update-in [:hankittavat-ammat-tutkinnon-osat 0
                               :osaamisen-osoittaminen 0] dissoc :module-id)
                   (update-in [:ensikertainen-hyvaksyminen]
                              #(LocalDate/parse %)))
               invalid-hoks))))))

(deftest test-handles-unauthorized
  (testing "Responds with unauthorized when not able to get ST for käyttöoikeus"
    (client/set-post!
      (fn [^String url _]
        (cond
          (.endsWith url "/v1/tickets")
          {:status 201
           :headers {"location" "http://localhost/TGT-1234"}}
          (= url "http://localhost/TGT-1234")
          (throw
            (ex-info
              "Test HTTP Exception"
              {:status 404
               :body
               "TGT-1234 could not be found or is considered invalid"})))))
    (client/set-get!
      (fn [^String url options]
        (cond
          (.endsWith url "/serviceValidate")
          {:status 200
           :body
           (str "<cas:serviceResponse"
                "  xmlns:cas='http://www.yale.edu/tp/cas'>"
                "<cas:authenticationSuccess><cas:user>ehoks</cas:user>"
                "<cas:attributes>"
                "<cas:longTermAuthenticationRequestTokenUsed>false"
                "</cas:longTermAuthenticationRequestTokenUsed>"
                "<cas:isFromNewLogin>false</cas:isFromNewLogin>"
                "<cas:authenticationDate>2019-02-20T10:14:24.046+02:00"
                "</cas:authenticationDate></cas:attributes>"
                "</cas:authenticationSuccess></cas:serviceResponse>")})))
    (let [app (hoks-utils/create-app nil)
          response (app (-> (mock/request
                              :get
                              (str hoks-utils/base-url "/1"))
                            (mock/header "Caller-Id" "test")
                            (mock/header "ticket" "ST-testitiketti")))]
      (is (= (:status response) 401))
      (is (= (test-utils/parse-body (:body response))
             {:reason "Unable to check access rights"})))))

(deftest test-bypasses-oht-date-checks-when-eronnut
  (testing "Bypasses OHT date check when opiskeluoikeus tila is eronnut"
    (let [app (hoks-utils/create-app nil)
          data
          (assoc test-data/hoks-data
                 :opiskeluoikeus-oid
                 "1.2.246.562.15.60000000004")
          post-response
          (hoks-utils/create-mock-post-request "" data app)]
      (is (= (:status post-response) 200)))))
