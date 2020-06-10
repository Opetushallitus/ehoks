(ns oph.ehoks.mocked-routes.mock-koski-routes)

(def routes
  (GET "/koski/api/oppija/*" []
       (json-response-file
         "dev-routes/koski_api_oppija_1.2.246.562.24.44651722625.json"))

  (GET "/koski/api/opiskeluoikeus/1.2.246.562.15.76811932037" []
       (json-response-file
         "dev-routes/koski_api_opiskeluoikeus_1.2.246.562.15.76811932037.json"))

  (GET "/koski/api/opiskeluoikeus/1.2.246.562.15.60063016194" []
       (json-response-file
         "dev-routes/koski_api_opiskeluoikeus_1.2.246.562.15.60063016194.json"))

  (GET "/koski/api/opiskeluoikeus/:oid" request
       (let [opiskeluoikeus-oid (get-in request [:params :oid])
             oppilaitos-oid
             (if (.startsWith opiskeluoikeus-oid "1.2.246.562.15.76811932")
               "1.2.246.562.10.12424158689"
               (mock-gen/generate-oppilaitos-oid))
             tutkinto (mock-gen/generate-tutkinto)]
         (json-response
           {:oid opiskeluoikeus-oid
            :oppilaitos
            {:oid oppilaitos-oid
             :oppilaitosnumero
             {:koodiarvo "10076"
              :nimi
              {:fi "Testi-yliopisto"
               :sv "Test-universitetet"
               :en "Test University"}
              :lyhytNimi
              {:fi "Testi-yliopisto"
               :sv "Test-universitetet"}
              :koodistoUri "oppilaitosnumero"
              :koodistoVersio 1}
             :nimi
             {:fi "Testi-yliopisto"
              :sv "Testi-universitetet"
              :en "Testi University"}
             :kotipaikka
             {:koodiarvo "091"
              :nimi {:fi "Helsinki", :sv "Helsingfors"}
              :koodistoUri "kunta"
              :koodistoVersio 2}}
            :koulutustoimija {}
            :tila
            {:opiskeluoikeusjaksot
             [{:alku "2018-11-15"
               :tila
               {:koodiarvo "lasna"
                :nimi
                {:fi "Läsnä"
                 :sv "Närvarande"
                 :en "present"}
                :koodistoUri "koskiopiskeluoikeudentila"
                :koodistoVersio 1}}]}
            :suoritukset
            [{:koulutusmoduuli
              {:tunniste
               {:koodiarvo (str (rand-int 999999))
                :nimi
                {:fi tutkinto}}
               :koodistoUri "koulutus"
               :koodistoVersio 11}
              :perusteenDiaarinumero "77/011/2014"
              :perusteenNimi
              {:fi tutkinto}
              :koulutustyyppi
              {:koodiarvo "1"
               :nimi
               {:fi "Ammatillinen perustutkinto"
                :sv "Yrkesinriktad grundexamen"
                :en "Vocational upper secondary qualification"}
               :lyhytNimi
               {:fi "Ammatillinen perustutkinto"
                :sv "Yrkesinriktad grundexamen"}
               :koodistoUri "koulutustyyppi"
               :koodistoVersio 2}
              :suoritustapa {}
              :toimipiste {}
              :alkamispäivä "2016-08-05"
              :suorituskieli {}
              :osasuoritukset []
              :tyyppi {}}]
            :tyyppi
            {:koodiarvo "perusopetus"
             :nimi
             {:fi "Perusopetus"
              :sv "Grundläggande utbildning"}
             :lyhytNimi {:fi "Perusopetus"}
             :koodistoUri "opiskeluoikeudentyyppi"
             :koodistoVersio 1}
            :alkamispäivä "2018-11-15"})))

  (POST "/koski/api/sure/oids" []
        (json-response-file
          "dev-routes/koski_api_sure_oids_1.2.246.562.24.44651722625.json")))
