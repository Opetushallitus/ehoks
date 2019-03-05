(ns oph.ehoks.oppija.handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.handler :refer [create-app]]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils :refer [eq]]
            [oph.ehoks.db.memory :as db]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.session-store :refer [test-session-store]]))

(def url "/ehoks-backend/api/v1/oppija/oppijat")

(def hoks
  {:puuttuvat-paikalliset-tutkinnon-osat
   [{:koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
     :hankitun-osaamisen-naytto []
     :osaamisen-hankkimistavat []
     :tavoitteet-ja-sisallot ""
     :nimi "Orientaatio alaan"}
    {:koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
     :hankitun-osaamisen-naytto []
     :osaamisen-hankkimistavat []
     :nimi "Infotilaisuus"}
    {:koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
     :hankitun-osaamisen-naytto []
     :osaamisen-hankkimistavat []
     :nimi "Opintojen ohjaus"}
    {:koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
     :hankitun-osaamisen-naytto []
     :osaamisen-hankkimistavat []
     :nimi "Tutkintotilaisuus"}
    {:koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
     :hankitun-osaamisen-naytto []
     :osaamisen-hankkimistavat []
     :nimi "Työpaikalla oppiminen "}]
   :ensikertainen-hyvaksyminen (java.util.Date.)
   :luotu (java.util.Date.)
   :urasuunnitelma-koodi-uri "urasuunnitelma_0001"
   :hyvaksytty (java.util.Date.)
   :olemassa-olevat-ammatilliset-tutkinnon-osat []
   :olemassa-olevat-yhteiset-tutkinnon-osat []
   :puuttuvat-ammatilliset-tutkinnon-osat
   [{:tutkinnon-osa-koodi-uri "tutkinnonosat_103590"
     :tutkinnon-osa-koodisto-koodi
     {:koodi-arvo "103590"
      :koodi-uri "tutkinnonosat_103590"
      :versio 2
      :metadata
      [{:nimi "Lähiesimiehenä toimiminen"
        :lyhyt-nimi "Lähiesimiehenä toimiminen"
        :kuvaus "Lähiesimiehenä toimiminen"
        :kieli "FI"}]}
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
     :hankitun-osaamisen-naytto
     [{:alku (java.time.LocalDate/now)
       :loppu (java.time.LocalDate/now)
       :jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921329"}
       :nayttoymparisto {:nimi "" :y-tunnus ""}
       :koulutuksenjarjestaja-arvioijat
       [{:nimi "Olson,Wava"
         :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921329"}}]}]
     :osaamisen-hankkimistavat []}
    {:tutkinnon-osa-koodi-uri "tutkinnonosat_103590"
     :tutkinnon-osa-koodisto-koodi
     {:koodi-arvo "103590"
      :koodi-uri "tutkinnonosat_103590"
      :versio 2
      :metadata
      [{:nimi "Lähiesimiehenä toimiminen"
        :lyhyt-nimi "Lähiesimiehenä toimiminen"
        :kuvaus "Lähiesimiehenä toimiminen"
        :kieli "FI"}]}
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
     :hankitun-osaamisen-naytto
     [{:alku (java.time.LocalDate/now)
       :loppu (java.time.LocalDate/now)
       :jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921329"}
       :nayttoymparisto {:nimi "", :y-tunnus ""}
       :koulutuksenjarjestaja-arvioijat
       [{:nimi "Moen,Pearl"
         :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921329"}}]}]
     :osaamisen-hankkimistavat []}]
   :puuttuvat-yhteiset-tutkinnon-osat
   [{:tutkinnon-osa-koodi-uri "tutkinnonosat_103596"
     :tutkinnon-osa-koodisto-koodi
     {:koodi-arvo "103596"
      :koodi-uri "tutkinnonosat_103596"
      :versio 3
      :metadata [{:kuvaus "Esimerkki tutkinnon osa"
                  :lyhyt-nimi "Esimerkki tutkinnon osa"
                  :kieli "FI"
                  :nimi "Esimerkki tutkinnon osa"}]}
     :osa-alueet
     [{:osa-alue-koodi-uri "ammatillisenoppiaineet_fk"
       :osa-alue-koodisto-koodi
       {:koodi-arvo "fk"
        :koodi-uri "ammatillisenoppiaineet_fk"
        :versio 1
        :metadata [{:nimi "Fysiikka ja kemia"
                    :lyhyt-nimi "Fysiikka ja kemia"
                    :kuvaus "Fysiikka ja kemia"
                    :kieli "FI"}]}}]
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"}]
   :opiskeluoikeus-oid "1.2.246.562.15.76811932037"
   :laatija {:nimi "Simonis,Hollie"}
   :versio 0
   :paivitetty (java.util.Date.)
   :eid "0000"
   :paivittaja {:nimi "Ei tietoa"}
   :oppija-oid "1.2.246.562.24.29790141661"
   :hyvaksyja {:nimi "Ei tietoa"}
   :opiskeluvalmiuksia-tukevat-opinnot []})

(defn set-hoks-data! [h]
  (reset!
    db/hoks-store
    [(assoc h :versio 1 :paivittaja {:nimi "Tapio Testaaja"})
     h]))

(defn with-cleaning [f]
  (f)
  (client/reset-functions!)
  (reset! oph.ehoks.external.cache/cache {}))

(defn clean-koodistot [h]
  (-> h
      (update
        :puuttuvat-ammatilliset-tutkinnon-osat
        (fn [t] (mapv #(dissoc % :tutkinnon-osa-koodisto-koodi) t)))
      (update
        :puuttuvat-yhteiset-tutkinnon-osat
        (fn [tc]
          (mapv
            (fn [t]
              (-> t
                  (dissoc :tutkinnon-osa-koodisto-koodi)
                  (update
                    :osa-alueet
                    (fn [oc]
                      (mapv
                        (fn [o] (dissoc o :osa-alue-koodisto-koodi))
                        oc)))))
            tc)))))

(use-fixtures :each with-cleaning)

(deftest get-koodisto-enriched-hoks
  (testing "GET koodisto enriched HOKS"
    (set-hoks-data! (clean-koodistot hoks))
    (client/set-get!
      (fn [p _]
        (cond
          (.endsWith p "urasuunnitelma_0001")
          {:body {:koodiArvo "jatkokoulutus"
                  :koodiUri "urasuunnitelma_0001"
                  :versio 1
                  :metadata
                  [{:kuvaus "Jatko-opinnot ja lisäkoulutus"
                    :kasite ""
                    :lyhytNimi "Jatkokoulutus"
                    :eiSisallaMerkitysta ""
                    :kieli "FI"
                    :nimi "Jatkokoulutus"
                    :sisaltaaMerkityksen ""
                    :huomioitavaKoodi ""
                    :kayttoohje ""
                    :sisaltaaKoodiston ""}]}}
          (.endsWith p "tutkinnonosat_103596")
          {:body {:koodiArvo "103596"
                  :versio 3
                  :koodiUri "tutkinnonosat_103596"
                  :metadata [{:kuvaus "Esimerkki tutkinnon osa"
                              :kasite nil
                              :lyhytNimi "Esimerkki tutkinnon osa"
                              :eiSisallaMerkitysta nil
                              :kieli "FI"
                              :nimi "Esimerkki tutkinnon osa"
                              :sisaltaaMerkityksen nil
                              :huomioitavaKoodi nil
                              :kayttoohje nil
                              :sisaltaaKoodiston nil}]}}
          (.endsWith p "tutkinnonosat_103590")
          {:body {:koodiArvo "103590"
                  :versio 2
                  :koodiUri "tutkinnonosat_103590"
                  :metadata [{:kuvaus "Lähiesimiehenä toimiminen"
                              :kasite nil
                              :lyhytNimi "Lähiesimiehenä toimiminen"
                              :eiSisallaMerkitysta nil
                              :kieli "FI"
                              :nimi "Lähiesimiehenä toimiminen"
                              :sisaltaaMerkityksen nil
                              :huomioitavaKoodi nil
                              :kayttoohje nil
                              :sisaltaaKoodiston nil}]}}
          (.endsWith p "ammatillisenoppiaineet_fk")
          {:body {:koodiArvo "fk"
                  :versio 1
                  :koodiUri "ammatillisenoppiaineet_fk"
                  :metadata [{:kuvaus "Fysiikka ja kemia"
                              :kasite nil
                              :lyhytNimi "Fysiikka ja kemia"
                              :eiSisallaMerkitysta nil
                              :kieli "FI"
                              :nimi "Fysiikka ja kemia"
                              :sisaltaaMerkityksen nil
                              :huomioitavaKoodi nil
                              :kayttoohje nil
                              :sisaltaaKoodiston nil}]}})))
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
            dissoc
            :luotu :paivitetty :hyvaksytty :ensikertainen-hyvaksyminen)
          {:data
           [(-> hoks
                (dissoc
                  :luotu
                  :paivitetty
                  :hyvaksytty
                  :ensikertainen-hyvaksyminen)
                (assoc
                  :paivittaja {:nimi "Tapio Testaaja"}
                  :versio 1
                  :urasuunnitelma
                  {:koodi-arvo "jatkokoulutus"
                   :koodi-uri "urasuunnitelma_0001"
                   :versio 1
                   :metadata
                   [{:kuvaus "Jatko-opinnot ja lisäkoulutus"
                     :lyhyt-nimi "Jatkokoulutus"
                     :kieli "FI"
                     :nimi "Jatkokoulutus"}]})
                (update
                  :puuttuvat-ammatilliset-tutkinnon-osat
                  (fn [oc]
                    (mapv
                      (fn [o]
                        (update
                          o
                          :hankitun-osaamisen-naytto
                          (fn [c]
                            (mapv
                              #(update
                                 (update %  :alku str)
                                 :loppu str)
                              c))))
                      oc))))]
           :meta {:errors []}})))))

(deftest enrich-koodisto-not-found
  (testing "GET not found koodisto enriched HOKS"
    (set-hoks-data! (dissoc hoks :puuttuvat-ammatilliset-tutkinnon-osat))
    (client/set-get!
      (fn [p _]
        (is (.endsWith p "urasuunnitelma_0001"))
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
            dissoc
            :luotu
            :paivitetty
            :hyvaksytty
            :ensikertainen-hyvaksyminen
            :paivittaja
            :versio
            :puuttuvat-ammatilliset-tutkinnon-osat)
          {:data [(dissoc
                    hoks
                    :luotu
                    :paivitetty
                    :hyvaksytty
                    :ensikertainen-hyvaksyminen
                    :paivittaja
                    :versio
                    :puuttuvat-ammatilliset-tutkinnon-osat)]
           :meta {:errors
                  [{:error-type "not-found"
                    :keys ["urasuunnitelma"]
                    :path "rest/codeelement/latest/urasuunnitelma_0001"}]}})))))
