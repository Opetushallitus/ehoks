(ns oph.ehoks.virkailija.cas-handler-test
  (:require [clojure.test :as t]
            [oph.ehoks.session-store :refer [test-session-store]]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.virkailija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.utils :as utils :refer [parse-body with-db]]
            [ring.mock.request :as mock]
            [oph.ehoks.db.session-store :as store]))

(t/use-fixtures :once utils/migrate-database)

(def session-url "/ehoks-virkailija-backend/api/v1/virkailija/session")

(defn- create-app [session-store]
  (common-api/create-app handler/app-routes session-store))

(defn- ticket-response [^String url options]
  (if (.endsWith url "/kayttooikeus-service/kayttooikeus/kayttaja")
    {:status 200
     :body [{:oidHenkilo "1.2.246.562.24.11474338834"
             :username "ehoksvirkailija"
             :organisaatiot
             [{:organisaatioOid "1.2.246.562.10.12944436166"
               :kayttooikeudet [{:palvelu "EHOKS"
                                 :oikeus "CRUD"}]}]}]}
    {:status 200
     :body
     (str "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"
          "<cas:authenticationSuccess>"
          "<cas:user>ehoksvirkailija</cas:user>"
          "<cas:attributes>"
          "<cas:longTermAuthenticationRequestTokenUsed>false"
          "</cas:longTermAuthenticationRequestTokenUsed>"
          "<cas:isFromNewLogin>false</cas:isFromNewLogin>"
          "<cas:authenticationDate>"
          "2019-02-20T10:14:24.046+02:00"
          "</cas:authenticationDate>"
          "</cas:attributes>"
          "</cas:authenticationSuccess>"
          "</cas:serviceResponse>")}))

(defn- create-ticket-response [url _]
  {:status 201
   :headers {"location" "http://test.ticket/1234"}})

(defn- with-ticket-session-multi [app requests ticket]
  (client/with-mock-responses
    [ticket-response
     create-ticket-response]
    (let [auth-response
          (app
            (mock/request
              :get
              (format
                "%s/opintopolku?ticket=%s"
                session-url
                ticket)))
          responses
          (mapv
            (fn [request]
              (app
                (-> request
                    (mock/header
                      :cookie
                      (first (get-in auth-response [:headers "Set-Cookie"])))
                    (mock/header "Caller-Id" "test"))))
            requests)]
      (client/reset-functions!)
      responses)))

(t/deftest cas-ticket-session-test
  (t/testing "Creating session with service ticket (CAS endpoint)"
    (client/with-mock-responses
      [ticket-response
       create-ticket-response]
      (let [store (atom {})
            app (create-app (test-session-store store))
            response
            (app
              (mock/request
                :get
                (str
                  "/ehoks-virkailija-backend/cas-security-check"
                  "?ticket=ST-12345-abcdefghIJKLMNopqrst-uvwxyz1234567890ab")))]
        (t/is (= (:status response) 303))))))

(t/deftest cas-logout-session-test
  (t/testing "Removing session with service ticket (CAS endpoint)"
    (with-db
      (let [responses
            (with-ticket-session-multi
              (create-app (store/db-store))
              [(mock/request
                 :get
                 (str
                   "/ehoks-virkailija-backend/cas-security-check"
                   "?ticket=ST-12345-abcdefghIJKLMNopqrst-uvwxyz1234567890ab"))
               (mock/request :get session-url)
               (mock/request
                 :post
                 "/ehoks-virkailija-backend/cas-security-check"
                 {:logoutRequest "
                 <samlp:LogoutRequest
                   xmlns:samlp= \"urn:oasis:names:tc:SAML:2.0:protocol\"
                   xmlns:saml= \"urn:oasis:names:tc:SAML:2.0:assertion\"
                   ID= \"some-id\"
                   Version= \"2.0\"
                   IssueInstant= \"2019-09-12\" >
                   <samlp:SessionIndex>
                     ST-12345-abcdefghIJKLMNopqrst-uvwxyz1234567890ab
                   </samlp:SessionIndex>
                 </samlp:LogoutRequest>
                 "})
               (mock/request :get session-url)]
              "ST-12345-abcdefghIJKLMNopqrst-uvwxyz1234567890ab")]
        (t/is (= (:status (first responses)) 303)
              "Successful login should response with see other")
        (t/is (= (:status (second responses)) 200)
              "Valid session should response with ok")
        (t/is (= (:status (nth responses 2)) 200)
              "Successful single logout should response with ok")
        (t/is (= (:status (nth responses 3)) 401)
              "Invalid session should response with unauthorized")))))
