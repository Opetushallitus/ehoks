(ns oph.ehoks.oppija.handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.handler :refer [create-app]]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils :refer [eq]]
            [oph.ehoks.db.memory :as db]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.session-store :refer [test-session-store]]
            [oph.ehoks.external.connection :as c]
            [clj-time.core :as t]))

(def url "/ehoks-backend/api/v1/oppijat")

(def hoks
  {:urasuunnitelma {:koodi-arvo "jatkokoulutus"
                    :koodi-uri "urasuunnitelma_1"
                    :versio 1}
   :opiskeluoikeus-oid "1.2.444.333.55.76666666666"
   :tutkinto {:laajuus 35
              :nimi "Audiovisuaalisen sisällön tuottamisen perustutkinto"}
   :oppija-oid "1.2.333.444.55.76666666666"
   :luotu (java.util.Date.)
   :luonut "Olli Opettaja"
   :hyvaksytty (java.util.Date.)
   :hyvaksynyt "Heikki Hyväksyjä"
   :paivitetty (java.util.Date.)
   :paivittanyt "Päivi Päivittäjä"
   :versio 2
   :id 1})

(defn set-hoks-data! []
  (reset!
    db/hoks-store
    [(assoc hoks :versio 1 :paivittanyt "Tapio Testaaja")
     hoks]))

(defn with-cleaning [f]
  (set-hoks-data!)
  (f)
  (client/reset-functions!)
  (reset! c/cache {}))

(use-fixtures :each with-cleaning)

(deftest get-koodisto-enriched-hoks
  (testing "GET koodisto enriched HOKS"
    (client/set-get!
      (fn [p _]
        (is (.endsWith p "urasuunnitelma_1/1"))
        {:body {:metadata
                [{:kuvaus "Jatko-opinnot ja lisäkoulutus"
                  :kasite ""
                  :lyhytNimi "Jatkokoulutus"
                  :eiSisallaMerkitysta ""
                  :kieli "FI"
                  :nimi "Jatkokoulutus"
                  :sisaltaaMerkityksen ""
                  :huomioitavaKoodi ""
                  :kayttoohje ""
                  :sisaltaaKoodiston ""}]}}))
    (let [store (atom {})
          app (create-app (test-session-store store))
          response
          (utils/with-authenticated-oid
            store
            (:oppija-oid hoks)
            app
            (mock/request
              :get
              (format "%s/%s/hoks" url (:oppija-oid hoks))))]
      (is (= (:status response) 200))
      (let [body (utils/parse-body (:body response))]
        (eq
          (update-in
            body
            [:data 0]
            dissoc :luotu :paivitetty :hyvaksytty)
          {:data [(update
                    (dissoc hoks :luotu :paivitetty :hyvaksytty)
                    :urasuunnitelma
                    assoc
                    :metadata
                    [{:kuvaus "Jatko-opinnot ja lisäkoulutus"
                      :lyhyt-nimi "Jatkokoulutus"
                      :kieli "FI"
                      :nimi "Jatkokoulutus"}])]
           :meta {:errors []}})))))

(deftest enrich-koodisto-not-found
  (testing "GET not found koodisto enriched HOKS"
    (set-hoks-data!)
    (client/set-get!
      (fn [p _]
        (is (.endsWith p "urasuunnitelma_1/1"))
        ; Return Koodisto Koodi Not found exception (see Koodisto.clj)
        (throw
          (ex-info
            "Internal Server Error"
            {:body "error.codeelement.not.found"
             :status 500}))))
    (let [store (atom {})
          app (create-app (test-session-store store))
          response
          (utils/with-authenticated-oid
            store
            (:oppija-oid hoks)
            app
            (mock/request
              :get
              (format "%s/%s/hoks" url (:oppija-oid hoks))))]
      (is (= (:status response) 200))
      (let [body (utils/parse-body (:body response))]
        (eq
          (update-in
            body
            [:data 0]
            dissoc :luotu :paivitetty :hyvaksytty)
          {:data [(dissoc hoks :luotu :paivitetty :hyvaksytty)]
           :meta {:errors [{:error-type "not-found"
                            :keys ["urasuunnitelma"]
                            :uri "urasuunnitelma_1"
                            :version 1}]}})))))
