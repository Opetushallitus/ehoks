(ns oph.ehoks.mock-routes
  (:require [compojure.core :refer [GET POST defroutes routes]]
            [ring.util.http-response :as response]
            [oph.ehoks.config :refer [config]]
            [clojure.java.io :as io]
            [cheshire.core :as cheshire]
            [clj-http.client :as client]
            [oph.ehoks.mock-gen :as mock-gen]))

(defn- json-response [value]
  (assoc-in
    (response/ok
      (cheshire/generate-string
        value))
    [:headers "Content-Type"] "application/json"))

(defn- json-response-file [f]
  (-> (io/resource f)
      slurp
      (cheshire/parse-string true)
      json-response))

(defroutes mock-routes
  (routes
    (GET "/auth-dev/opintopolku-login/" request
      (let [result
            (client/get
              (:opintopolku-return-url config)
              {:redirect-strategy :none
               :headers {"firstname" "Aarto Maurits"
                         "cn" "Aarto"
                         "givenname" "Aarto"
                         "hetu" "250103-5360"
                         "sn" "Väisänen-perftest"}})
            cookie (-> (get-in result [:cookies "ring-session"])
                       (update :expires str)
                       (dissoc :version :discard))]
        (assoc
          (response/see-other (get-in result [:headers "Location"]))
          :cookies
          {"ring-session" cookie})))

    (GET "/auth-dev/opintopolku-tt-login/" request
      (response/see-other
        (format "%s/%s/%s"
                (:frontend-url config)
                (:frontend-url-path config)
                (:tyopaikan-toimija-frontend-path config))))

    (GET "/auth-dev/opintopolku-logout/" request
      (response/see-other (get-in request [:query-params "return"])))

    (POST "/cas/v1/tickets" request
      (response/created
        (format
          "http://localhost:%d/cas/v1/tickets/TGT-1234-Example-cas.1234567890abc"
          (:port config))))

    (POST "/cas/v1/tickets/TGT-1234-Example-cas.1234567890abc" []
      (response/ok "ST-1234-aBcDeFgHiJkLmN123456-cas.1234567890ab"))

    (GET "/cas/p3/serviceValidate" request
      (let [username (if (= (get-in request [:query-params "ticket"])
                            "ST-6777-aBcDeFgHiJkLmN123456-cas.1234567890ac")
                       "ehoksvirkailija"
                       "ehoks")]
        (response/ok
          (format
            (str "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"
                 "<cas:authenticationSuccess><cas:user>%s</cas:user>"
                 "<cas:attributes>"
                 "<cas:longTermAuthenticationRequestTokenUsed>false"
                 "</cas:longTermAuthenticationRequestTokenUsed>"
                 "<cas:isFromNewLogin>false</cas:isFromNewLogin>"
                 "<cas:authenticationDate>2019-02-20T10:14:24.046+02:00"
                 "</cas:authenticationDate></cas:attributes>"
                 "</cas:authenticationSuccess></cas:serviceResponse>")
            username))))

    (GET "/cas/login" request
      (response/see-other
        (format
          "%s?ticket=ST-6777-aBcDeFgHiJkLmN123456-cas.1234567890ac"
          (get-in request [:query-params "service"]))))

    (GET "/oppijanumerorekisteri-service/henkilo" request
      (json-response
        {:results
         [{:oidHenkilo "1.2.246.562.24.44651722625"
           :hetu "250103-5360"
           :etunimet "Aarto Maurits"
           :kutsumanimi "Aarto"
           :sukunimi "Väisänen-perftest"}]}))

    (GET "/oppijanumerorekisteri-service/henkilo/1.2.246.562.24.44651722625" []
      (json-response
        {:oidHenkilo "1.2.246.562.24.44651722625"
         :hetu "250103-5360"
         :etunimet "Aarto Maurits"
         :kutsumanimi "Aarto"
         :sukunimi "Väisänen-perftest"
         :yhteystiedotRyhma
         '({:id 0
            :readOnly true
            :ryhmaAlkuperaTieto "testiservice"
            :ryhmaKuvaus "testiryhmä"
            :yhteystieto
            [{:yhteystietoArvo "kayttaja@domain.local"
              :yhteystietoTyyppi "YHTEYSTIETO_SAHKOPOSTI"}]})}))

    (GET "/oppijanumerorekisteri-service/henkilo/1.2.246.562.24.00000000000" []
      (response/not-found))

    (GET "/oppijanumerorekisteri-service/henkilo/:oid" request
      (let [first-name (mock-gen/generate-first-name)]
        (json-response
          {:oidHenkilo (get-in request [:params :oid])
           :hetu "250103-5360"
           :etunimet (format "%s %s" first-name (mock-gen/generate-first-name))
           :kutsumanimi first-name
           :sukunimi (mock-gen/generate-last-name)
           :yhteystiedotRyhma
           '({:id 0
              :readOnly true
              :ryhmaAlkuperaTieto "testiservice"
              :ryhmaKuvaus "testiryhmä"
              :yhteystieto
              [{:yhteystietoArvo "kayttaja@domain.local"
                :yhteystietoTyyppi "YHTEYSTIETO_SAHKOPOSTI"}]})})))

    (GET "/koodisto-service/rest/codeelement/*/*" []
      (json-response-file
        "dev-routes/rest_codeelement_ravintolakokinatjarjestys__4_2.json"))

    (GET "/eperusteet-service/api/perusteet" request
      (json-response-file
        "dev-routes/eperusteet_api_perusteet.json"))

    (GET "/eperusteet-service/api/tutkinnonosat/52824/viitteet" []
      (json-response-file
        "dev-routes/eperusteet-service_api_tutkinnonosat_52824_viitteet.json"))

    (GET "/eperusteet-service/api/perusteet/diaari" []
      (json-response-file
        "dev-routes/eperusteet-service_api_perusteet_diaari.json"))

    (GET "/eperusteet-service/api/perusteet/3397335/suoritustavat/reformi/rakenne" []
      (json-response-file
        "dev-routes/eperusteet-service_api_perusteet_3397335_suoritustavat_reformi_rakenne.json"))

    (GET "/eperusteet-service/api/tutkinnonosat" request
      (if (= (get-in request [:query-params "koodiUri"]) "tutkinnonosat_101056")
        (json-response-file
          "dev-routes/eperusteet-service_api_tutkinnonosat_not_found.json")
        (json-response-file
          "dev-routes/eperusteet-service_api_tutkinnonosat.json")))

    (GET "/koski/api/oppija/*" []
      (json-response-file
        "dev-routes/koski_api_oppija_1.2.246.562.24.44651722625.json"))

    (GET "/koski/api/opiskeluoikeus/1.2.246.562.15.76811932037" []
      (json-response-file
        "dev-routes/koski_api_opiskeluoikeus_1.2.246.562.15.76811932037.json"))

    (GET "/lokalisointi/cxf/rest/v1/localisation" []
      (json-response-file
        "dev-routes/lokalisointi_cxf_rest_v1_localisation.json"))

    (GET "/koski/api/opiskeluoikeus/:oid" request
      (let [opiskeluoikeus-oid (get-in request [:params :oid])
            oppilaitos-oid
            (if (.startsWith opiskeluoikeus-oid "1.2.246.562.15.76811932")
              "1.2.246.562.10.12424158689"
              (mock-gen/generate-oppilaitos-oid))]
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
           :suoritukset []
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
        "koski_api_sure_oids_1.2.246.562.24.44651722625.json"))

    (GET "/kayttooikeus-service/kayttooikeus/kayttaja" request
      (if (= (get-in request [:query-params "username"]) "ehoksvirkailija")
        (json-response-file
          "dev-routes/kayttooikeus-service_kayttooikeus_kayttaja_virkailija.json")
        (json-response-file
          "dev-routes/kayttooikeus-service_kayttooikeus_kayttaja.json")))

    (POST "/organisaatio-service/rest/organisaatio/v4/findbyoids" request
      (json-response-file
        "dev-routes/organisaatio-service_rest_organisaatio_v4_findbyoids.json"))

    (GET "/organisaatio-service/rest/organisaatio/v4/:oid" request
      (json-response
        {:oid (get-in request [:params :oid])
         :parentOidPath "|1.2.246.562.10.00000000001|"}))))
