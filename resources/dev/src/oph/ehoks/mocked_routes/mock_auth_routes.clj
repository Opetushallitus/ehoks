(ns oph.ehoks.mocked-routes.mock-auth-routes
  (:require [ring.util.http-response :as response]
            [compojure.core :refer [GET POST routes]]
            [oph.ehoks.config :refer [config]]
            [clj-http.client :as client]))

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
             (get-in request [:query-params "service"]))))))
