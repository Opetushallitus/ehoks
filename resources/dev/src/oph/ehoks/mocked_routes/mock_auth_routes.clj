(ns oph.ehoks.mocked-routes.mock-auth-routes
  (:require [ring.util.http-response :as response]
            [compojure.core :refer [GET POST routes]]
            [oph.ehoks.config :refer [config]]
            [clj-http.client :as client]))

(def cas-oppija-ticket "ST-6778-aBcDeFgHiJkLmN123456-cas.1234567890ac")

(def mock-routes
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
                      "<cas:oidHenkilo>1.2.246.562.24.11474338835</cas:oidHenkilo>"
                      "<cas:kayttajaTyyppi>VIRKAILIJA</cas:kayttajaTyyppi>"
                      "<cas:idpEntityId>usernamePassword</cas:idpEntityId>"
                      "<cas:roles>"
                      "ROLE_APP_EHOKS_OPHPAAKAYTTAJA_1.2.246.562.10.12944436166"
                      "</cas:roles>"
                      "<cas:roles>"
                      "ROLE_APP_EHOKS_CRUD_1.2.246.562.10.12424158689"
                      "</cas:roles>"
                      "<cas:roles>"
                      "ROLE_APP_EHOKS_CRUD_1.2.246.562.10.12556417327"
                      "</cas:roles>"
                      "<cas:roles>"
                      "ROLE_APP_EHOKS_CRUD_1.2.246.562.10.84189747029"
                      "</cas:roles>"
                      "<cas:roles>"
                      "ROLE_APP_EHOKS_READ_1.2.246.562.10.12424158690"
                      "</cas:roles>"
                      "<cas:roles>"
                      "ROLE_APP_EHOKS_HOKS_DELETE_1.2.246.562.10.12424158690"
                      "</cas:roles>"
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

    (GET "/cas-oppija/login" request
      (response/see-other
        (format
          "%s?ticket=%s"
          (get-in request [:query-params "service"]) cas-oppija-ticket)))

    (GET "/cas-oppija/logout" request
      (response/see-other (get-in request [:query-params "service"])))

    (GET "/cas-oppija/serviceValidate" request
      (if (= (get-in request [:query-params "ticket"]) cas-oppija-ticket)
        (response/ok
          (format
            (str "<cas:serviceResponse xmlns:cas=\"http://www.yale.edu/tp/cas\">"
                 "<cas:authenticationSuccess>"
                 "<cas:user>suomi.fi#070770-905D</cas:user>"
                 "<cas:attributes>"
                 "<cas:isFromNewLogin>true</cas:isFromNewLogin>"
                 "<cas:mail>antero.asiakas@suomi.fi</cas:mail>"
                 "<cas:authenticationDate>2020-08-18T11:35:38.453760Z[UTC]</cas:authenticationDate>"
                 "<cas:clientName>suomi.fi</cas:clientName>"
                 "<cas:displayName>Antero Asiakas</cas:displayName>"
                 "<cas:givenName>Antero</cas:givenName>"
                 "<cas:VakinainenKotimainenLahiosoiteS>Sepänkatu 111 A 50</cas:VakinainenKotimainenLahiosoiteS>"
                 "<cas:VakinainenKotimainenLahiosoitePostitoimipaikkaS>KUOPIO</cas:VakinainenKotimainenLahiosoitePostitoimipaikkaS>"
                 "<cas:cn>Asiakas Antero OP</cas:cn>"
                 "<cas:notBefore>2020-08-18T11:35:35.788Z</cas:notBefore>"
                 "<cas:personOid>%s</cas:personOid>"
                 "<cas:personName>Asiakas Antero OP</cas:personName>"
                 "<cas:firstName>Antero OP</cas:firstName>"
                 "<cas:VakinainenKotimainenLahiosoitePostinumero>70100</cas:VakinainenKotimainenLahiosoitePostinumero>"
                 "<cas:KotikuntaKuntanumero>297</cas:KotikuntaKuntanumero>"
                 "<cas:KotikuntaKuntaS>Kuopio</cas:KotikuntaKuntaS>"
                 "<cas:notOnOrAfter>2020-08-18T11:40:35.788Z</cas:notOnOrAfter>"
                 "<cas:longTermAuthenticationRequestTokenUsed>false</cas:longTermAuthenticationRequestTokenUsed>"
                 "<cas:sn>Asiakas</cas:sn>"
                 "<cas:nationalIdentificationNumber>070770-905D</cas:nationalIdentificationNumber>"
                 "</cas:attributes>"
                 "</cas:authenticationSuccess>"
                 "</cas:serviceResponse>")
            "1.2.246.562.24.44651722625"))
        (response/ok
          (str
            "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n"
            "<cas:authenticationFailure code=\"INVALID_TICKET\">"
            "Ticket &#39;%s&#39; not recognized"
            "</cas:authenticationFailure>\n"
            "</cas:serviceResponse>\n"))))))
