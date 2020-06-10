(ns oph.ehoks.mocked-routes.mock-routes
  (:require [compojure.core :refer [GET POST defroutes routes]]
            [ring.util.http-response :as response]
            [oph.ehoks.config :refer [config]]
            [clojure.java.io :as io]
            [cheshire.core :as cheshire]
            [clj-http.client :as client]
            [oph.ehoks.mock-gen :as mock-gen]
            [clj-time.core :as time]
            [oph.ehoks.mocked-routes.mock-koodisto-routes :as koodisto-mocks]
            [oph.ehoks.mocked-routes.mock-eperusteet-routes :as eperusteet-mocks]
            [oph.ehoks.mocked-routes.mock-koski-routes :as koski-mocks]))

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
                (:frontend-url-fi config)
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

      (if (= (get-in request [:query-params "ticket"]) "invalid")
        (response/ok
          (str
            "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n"
            "<cas:authenticationFailure code=\"INVALID_TICKET\">"
            "Ticket &#39;%s&#39; not recognized"
            "</cas:authenticationFailure>\n"
            "</cas:serviceResponse>\n"))
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
              username)))))

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

    (GET "/lokalisointi/cxf/rest/v1/localisation" []
      (json-response-file
        "dev-routes/lokalisointi_cxf_rest_v1_localisation.json"))

    koodisto-mocks/routes
    eperusteet-mocks/routes
    koski-mocks/routes

    (GET "/kayttooikeus-service/kayttooikeus/kayttaja" request
      (if (= (get-in request [:query-params "username"]) "ehoksvirkailija")
        (json-response-file
          "dev-routes/kayttooikeus-service_kayttooikeus_kayttaja_virkailija.json")
        (json-response-file
          "dev-routes/kayttooikeus-service_kayttooikeus_kayttaja.json")))

    (POST "/organisaatio-service/rest/organisaatio/v4/findbyoids" request
      (json-response-file
        "dev-routes/organisaatio-service_rest_organisaatio_v4_findbyoids.json"))

    (GET "/organisaatio-service/rest/organisaatio/v4/1.2.246.562.15.404" []
      (assoc-in
        (response/not-found
          (cheshire/generate-string
            {:errorMessage "organisaatio.exception.organisaatio.not.found"
             :errorKey ""}))
        [:headers "Content-Type"] "application/json"))

    (GET "/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.5921222" []
      (json-response
        {:oid  "1.2.246.562.10.5921222"
         :nimi {:fi "Testaus-organisaatio"}
         :parentOidPath "|1.2.246.562.10.00000000001|"}))

    (GET "/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.54425555" []
      (json-response
        {:oid  "1.2.246.562.10.54425555"
         :nimi {:fi "Joku koulutuksen järjestäjä-organisaatio"}
         :parentOidPath "|1.2.246.562.10.00000000001|"}))

    (GET "/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.54424444" []
      (json-response
        {:oid  "1.2.246.562.10.54424444"
         :nimi {:fi "Aiemman arvioijan organisaatio"}
         :parentOidPath "|1.2.246.562.10.00000000001|"}))

    (GET "/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.54423333" []
      (json-response
        {:oid  "1.2.246.562.10.54423333"
         :nimi {:fi "Osa-alueen järjestäjä-organisaatio"}
         :parentOidPath "|1.2.246.562.10.00000000001|"}))

    (GET "/organisaatio-service/rest/organisaatio/v4/:oid" request
      (json-response
        {:oid (get-in request [:params :oid])
         :nimi {:fi "Esimerkki-organisaatio"}
         :parentOidPath "|1.2.246.562.10.00000000001|"}))

    (GET "/api/vastauslinkki/v1/status/:linkId" request
      (json-response
        {:vastattu false
         :voimassa_loppupvm (time/plus (time/now) (time/hours 2))}))))
