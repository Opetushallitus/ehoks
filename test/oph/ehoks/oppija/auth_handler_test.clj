(ns oph.ehoks.oppija.auth-handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.session-store :refer [test-session-store]]
            [oph.ehoks.oppija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :refer [parse-body authenticate]]
            [oph.ehoks.external.http-client :as client]))

(def base-url "/ehoks-oppija-backend/api/v1/oppija/session")

(deftest session-without-authentication
  (testing "GET current session without authentication"
    (let [app (common-api/create-app handler/app-routes nil)
          response (app (mock/request
                          :get
                          base-url))]
      (is (= (:status response) 401))
      (is (empty? (:body response))))))

(deftest session-authenticate
  (testing "POST authenticate"
    (let [response (authenticate
                     (common-api/create-app handler/app-routes nil))]
      (is (= (:status response) 303)))))

(defn ticket-validation-mock-response [ticket]
  {:status 200
   :body
   (str "<cas:serviceResponse xmlns:cas=\"http://www.yale.edu/tp/cas\">"
        "<cas:authenticationSuccess>"
        "<cas:user>suomi.fi#070770-905D</cas:user>"
        "<cas:attributes>"
        "<cas:isFromNewLogin>true</cas:isFromNewLogin>"
        "<cas:mail>antero.asiakas@suomi.fi</cas:mail>"
        "<cas:authenticationDate>2020-08-18T11:35:38.453760Z[UTC]"
        "</cas:authenticationDate>"
        "<cas:clientName>suomi.fi</cas:clientName>"
        "<cas:displayName>Antero Asiakas</cas:displayName>"
        "<cas:givenName>Antero</cas:givenName>"
        "<cas:VakinainenKotimainenLahiosoiteS>Sepänkatu 111 A 50"
        "</cas:VakinainenKotimainenLahiosoiteS>"
        "<cas:VakinainenKotimainenLahiosoitePostitoimipaikkaS>KUOPIO"
        "</cas:VakinainenKotimainenLahiosoitePostitoimipaikkaS>"
        "<cas:cn>Asiakas Antero OP</cas:cn>"
        "<cas:notBefore>2020-08-18T11:35:35.788Z</cas:notBefore>"
        "<cas:personOid>1.2.246.562.24.44651722625</cas:personOid>"
        "<cas:personName>Asiakas Antero OP</cas:personName>"
        "<cas:firstName>Antero OP</cas:firstName>"
        "<cas:VakinainenKotimainenLahiosoitePostinumero>70100"
        "</cas:VakinainenKotimainenLahiosoitePostinumero>"
        "<cas:KotikuntaKuntanumero>297</cas:KotikuntaKuntanumero>"
        "<cas:KotikuntaKuntaS>Kuopio</cas:KotikuntaKuntaS>"
        "<cas:notOnOrAfter>2020-08-18T11:40:35.788Z</cas:notOnOrAfter>"
        "<cas:longTermAuthenticationRequestTokenUsed>false"
        "</cas:longTermAuthenticationRequestTokenUsed>"
        "<cas:sn>Asiakas</cas:sn>"
        "<cas:nationalIdentificationNumber>070770-905D"
        "</cas:nationalIdentificationNumber>"
        "</cas:attributes>"
        "</cas:authenticationSuccess>"
        "</cas:serviceResponse>")})

(defn mock-oppijanumerorekisteri-response [oid]
  {:status 200,
   :body {:oidHenkilo "1.2.246.562.24.44651722625"
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
             [{:yhteystietoTyyppi "YHTEYSTIETO_SAHKOPOSTI"
               :yhteystietoArvo "testikayttaja@testi.fi"}]})}})

(deftest successful-cas-authentication
  (testing "Successful oppija authentication"
    (with-redefs [oph.ehoks.external.cas/call-cas-oppija-ticket-validation
                  ticket-validation-mock-response
                  oph.ehoks.external.oppijanumerorekisteri/find-student-by-oid
                  mock-oppijanumerorekisteri-response]
      (let [session-store (atom {})
            app (common-api/create-app handler/app-routes
                                       (test-session-store session-store))
            login-url (format
                        "%s/opintopolku2/?ticket=%s"
                        base-url
                        "ST-6778-aBcDeFgHiJkLmN123456-cas.1234567890ac")
            response (app (mock/request
                            :get
                            login-url))]
        (is (= (:status response) 303))))))

(defn ticket-validation-fail-mock-response [ticket]
  {:status 200
   :body
   (str
     "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n"
     "<cas:authenticationFailure code=\"INVALID_TICKET\">"
     "Ticket &#39;%s&#39; not recognized"
     "</cas:authenticationFailure>\n"
     "</cas:serviceResponse>\n")})

(deftest failed-cas-authentication
  (testing "Cas fails ticket validation"
    (with-redefs [oph.ehoks.external.cas/call-cas-oppija-ticket-validation
                  ticket-validation-fail-mock-response
                  oph.ehoks.external.oppijanumerorekisteri/find-student-by-oid
                  mock-oppijanumerorekisteri-response]
      (let [session-store (atom {})
            app (common-api/create-app handler/app-routes
                                       (test-session-store session-store))
            login-url (format
                        "%s/opintopolku2/?ticket=%s"
                        base-url
                        "ST-6778-aBcDeFgHiJkLmN123456-cas.1234567890ac")
            response (app (mock/request
                            :get
                            login-url))]
        (is (= (:status response) 401))))))

(deftest prevent-malformed-authentication
  (testing "Prevents malformed authentication"
    (let [app (common-api/create-app handler/app-routes nil)
          response
          (app (-> (mock/request
                     :get (str base-url "/opintopolku/")
                     {"FirstName" "Teuvo Testi"
                      "cn" "Teuvo"
                      "hetu" "190384-9245"
                      "sn" "Testaaja"})
                   (mock/header "FirstName" "Teuvo Testi")
                   (mock/header "cn" "Teuvo")
                   (mock/header "givenname" "Teuvo")
                   (mock/header "sn" "Testaaja")))]
      (is (= (:status response) 400)))))

(deftest session-authenticated
  (testing "GET current authenticated session"
    (let [app (common-api/create-app handler/app-routes nil)
          auth-response (authenticate app)
          session-cookie (first (get-in auth-response [:headers "Set-Cookie"]))
          response (app (-> (mock/request
                              :get
                              base-url)
                            (mock/header :cookie session-cookie)
                            (mock/header "Caller-Id" "test")))
          body (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (= (:data body) [{:oid "1.2.246.562.24.44651722625"
                            :first-name "Teuvo Testi"
                            :common-name "Teuvo"
                            :surname "Testaaja"}])))))

(deftest session-delete-authenticated
  (testing "DELETE authenticated session"
    (let [app (common-api/create-app handler/app-routes nil)
          auth-response (authenticate app)
          session-cookie (first (get-in auth-response [:headers "Set-Cookie"]))
          authenticated-response
          (app (-> (mock/request
                     :get
                     base-url)
                   (mock/header :cookie session-cookie)
                   (mock/header "Caller-Id" "test")))
          authenticated-body (parse-body (:body authenticated-response))
          delete-response
          (app (-> (mock/request
                     :delete
                     base-url)
                   (mock/header :cookie session-cookie)
                   (mock/header "Caller-Id" "test")))
          response (app (-> (mock/request
                              :get
                              base-url)
                            (mock/header :cookie session-cookie)
                            (mock/header "Caller-Id" "test")))]
      (is (= (:status authenticated-response) 200))
      (is (= (:data authenticated-body)
             [{:oid "1.2.246.562.24.44651722625"
               :first-name "Teuvo Testi"
               :common-name "Teuvo"
               :surname "Testaaja"}]))
      (is (= (:status response) 401))
      (is (= (:status delete-response) 200))
      (is (= (:status delete-response) 200)))))
