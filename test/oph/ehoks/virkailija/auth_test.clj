(ns oph.ehoks.virkailija.auth-test
  (:require [clojure.test :as t]
            [ring.mock.request :as mock]
            [oph.ehoks.session-store :refer [test-session-store]]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.virkailija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.utils :as utils :refer [parse-body with-db]]))

(t/use-fixtures :once utils/migrate-database)

(def session-url "/ehoks-virkailija-backend/api/v1/virkailija/session")

(defn create-app [session-store]
  (common-api/create-app handler/app-routes session-store))

(defn ticket-response [url options]
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

(defn with-ticket-session-multi [app requests ticket]
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

(defn with-ticket-session [app request ticket]
  (first (with-ticket-session-multi app [request] ticket)))

(t/deftest ticket-session-test
  (t/testing "Creating session with service ticket"
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
                  session-url
                  "/opintopolku"
                  "?ticket=ST-12345-abcdefghIJKLMNopqrst-uvwxyz1234567890ab")))]
        (t/is (= (:status response) 303))))))

(t/deftest invalid-ticket-session-test
  (t/testing "Creating session with invalid service ticket"
    (client/with-mock-responses
      [invalid-ticket-response
       create-ticket-response]
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
        (t/is (= (:status response) 401))))))

(t/deftest get-session-test
  (t/testing "Get virkailija session"
    (with-db
      (let [response (with-ticket-session
                       (create-app (test-session-store (atom {})))
                       (mock/request :get session-url)
                       "ST-12345-abcdefghIJKLMNopqrst-uvwxyz1234567890ab")]
        (t/is (= (:status response) 200))
        (t/is (= (parse-body (:body response))
                 {:meta {}
                  :data
                  {:oidHenkilo "1.2.246.562.24.11474338834"
                   :isSuperuser false
                   :organisation-privileges
                   [{:oid "1.2.246.562.10.12944436166"
                     :privileges
                     ["read" "update" "delete" "write"]
                     :roles []
                     :child-organisations []}]}}))))))

(t/deftest delete-session-test
  (t/testing "Delete virkailija session"
    (with-db
      (let [responses (with-ticket-session-multi
                        (create-app (test-session-store (atom {})))
                        [(mock/request :get session-url)
                         (mock/request :delete session-url)
                         (mock/request :get session-url)]
                        "ST-12345-abcdefghIJKLMNopqrst-uvwxyz1234567890ab")]
        (t/is (= (:status (first responses)) 200))
        (t/is (= (:status (second responses)) 200))
        (t/is (= (:status (nth responses 2)) 401))))))
