(ns oph.ehoks.mock-routes
  (:require [oph.ehoks.handler :refer [app]]
            [compojure.core :refer [GET POST defroutes routes]]
            [ring.util.http-response :as response]
            [ring.middleware.reload :refer [wrap-reload]]
            [oph.ehoks.config :refer [config]]
            [hiccup.core :refer [html]]
            [clojure.java.io :as io]
            [clojure.string :as c-str]
            [cheshire.core :as cheshire]
            [clj-http.client :as client]
            [ring.middleware.cookies :as cookies]
            [cheshire.core :as cheshire]))

(defn- json-response [value]
  (assoc-in
      (response/ok
        (cheshire/generate-string
          value))
      [:headers "Content-Type"] "application/json"))

(defroutes mock-routes
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

  (POST "/cas-dev/tickets" request
    (response/created
      (format
        "http://localhost:%d/cas-dev/tickets/TGT-1234-Example-cas.1234567890abc"
        (:port config))))

  (POST "/cas-dev/tickets/TGT-1234-Example-cas.1234567890abc" []
    (response/ok "ST-1234-aBcDeFgHiJkLmN123456-cas.1234567890ab"))

  (GET "/cas-dev/p3/serviceValidate" []
    (response/ok
      (str "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"
           "<cas:authenticationSuccess><cas:user>ehoks</cas:user>"
           "<cas:attributes>"
           "<cas:longTermAuthenticationRequestTokenUsed>false"
           "</cas:longTermAuthenticationRequestTokenUsed>"
           "<cas:isFromNewLogin>false</cas:isFromNewLogin>"
           "<cas:authenticationDate>2019-02-20T10:14:24.046+02:00"
           "</cas:authenticationDate></cas:attributes>"
           "</cas:authenticationSuccess></cas:serviceResponse>")))

  (GET "/oppijanumerorekisteri-service/henkilo" request
    (json-response
      {:results
       [{:oidHenkilo "1.2.246.562.24.44651722625"
         :hetu "1.2.246.562.24.44651722625"
         :etunimet "Aarto Maurits"
         :kutsumanimi "Aarto"
         :sukunimi "Väisänen-perftest"}]}))

  (GET "/oppijanumerorekisteri-service/henkilo/*" []
    (json-response
      {:oidHenkilo "1.2.246.562.24.44651722625"
       :hetu "1.2.246.562.24.44651722625"
       :etunimet "Aarto Maurits"
       :kutsumanimi "Aarto"
       :sukunimi "Väisänen-perftest"
       :yhteystiedotRyhma
       '({:id 0,
          :readOnly true,
          :ryhmaAlkuperaTieto "testiservice",
          :ryhmaKuvaus "testiryhmä",
          :yhteystieto
          [{:yhteystietoArvo "kayttaja@domain.local",
            :yhteystietoTyyppi "YHTEYSTIETO_SAHKOPOSTI"}]})}))

  (GET "/koodisto-service/rest/codeelement/*/*" []
    (-> (io/resource
          "dev-routes/rest_codeelement_ravintolakokinatjarjestys__4_2.json")
        slurp
        (cheshire/parse-string true)
        json-response))

  (GET "/koski/api/oppija/*" []
    (-> (io/resource
          "dev-routes/koski_api_oppija_1.2.246.562.24.44651722625.json")
        slurp
        (cheshire/parse-string true)
        json-response)))
