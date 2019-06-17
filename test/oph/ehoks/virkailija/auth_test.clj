(ns oph.ehoks.virkailija.auth-test
  (:require [clojure.test :as t]
            [ring.mock.request :as mock]
            [oph.ehoks.session-store :refer [test-session-store]]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.virkailija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.utils :refer [parse-body]]))

(def session-url "/ehoks-virkailija-backend/api/v1/virkailija/session")

(defn create-app [session-store]
  (common-api/create-app handler/app-routes session-store))

(defn ticket-response [url options]
  (if (.endsWith url "/kayttooikeus-service/kayttooikeus/kayttaja")
    {:status 200
     :body [{:oidHenkilo "1.2.246.562.24.11474338833"
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

(defn invalid-ticket-response [url options]
  {:status 200
   :body
   (format
     (str
       "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n"
       "<cas:authenticationFailure code=\"INVALID_TICKET\">"
       "Ticket &#39;%s&#39; not recognized"
       "</cas:authenticationFailure>\n"
       "</cas:serviceResponse>\n")
     (get-in options [:query-params :ticket]))})

(defn create-ticket-response [url _]
  {:status 201
   :headers {"location" "http://test.ticket/1234"}})

(defn with-ticket-session [app request ticket]
  (client/set-get! ticket-response)
  (client/set-post! create-ticket-response)
  (let [auth-response
        (app
          (mock/request
            :get
            (format
              "%s/opintopolku?ticket=%s"
              session-url
              ticket)))
        response
        (app
          (mock/header
            request
            :cookie
            (first (get-in auth-response [:headers "Set-Cookie"]))))]
    (client/reset-functions!)
    response))

(t/deftest invalid-ticket-session-test
  (t/testing "Creating session with invalid service ticket"
    (client/set-get! invalid-ticket-response)
    (client/set-post! create-ticket-response)
    (let [store (atom {})
          app (create-app (test-session-store store))
          response
          (app
            (mock/request
              :get
              (str
                session-url
                "/opintopolku"
                "?ticket=ST-12345-abcdefghIJKLMNopqrst-uvwxyz1234567890ab")))]
      (t/is (= (:status response) 401)))))

(t/deftest get-session-test
  (t/testing "Get virkailija session"
    (let [response (with-ticket-session
                     (create-app (test-session-store (atom {})))
                     (mock/request :get session-url)
                     "ST-12345-abcdefghIJKLMNopqrst-uvwxyz1234567890ab")]
      (t/is (= (:status response) 200))
      (t/is (= (parse-body (:body response))
               {:meta {}
                :data
                {:oidHenkilo "1.2.246.562.24.11474338833"
                 :organisation-privileges
                 [{:oid "1.2.246.562.10.12944436166"
                   :privileges ["read" "update" "delete" "write"]
                   :roles []}]}})))))