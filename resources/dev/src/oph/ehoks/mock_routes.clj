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
            [ring.middleware.cookies :as cookies]))

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
             :headers {"firstname" "Teuvo Taavetti"
                       "cn" "Teuvo"
                       "givenname" "Teuvo"
                       "hetu" "190384-9245"
                       "sn" "Testaaja"}})
          cookie (-> (get-in result [:cookies "ring-session"])
                     (update :expires str)
                     (dissoc :version :discard))]
      (assoc
        (response/ok)
        :cookies
        {"ring-session" cookie})))

  (POST "/cas-dev/tickets" request
    (response/created
      (format
        "http://localhost:%d/cas-dev/tickets/TGT-1234-Example-cas.1234567890abc"
        (:port config))))
  (POST "/cas-dev/tickets/TGT-1234-Example-cas.1234567890abc" []
    (response/ok "ST-1234-aBcDeFgHiJkLmN123456-cas.1234567890ab"))
  (GET "/oppijanumerorekisteri-service/henkilo" request
    (json-response
      {:results
       [{:oidHenkilo "1.2.246.562.24.78058065184"
         :hetu "190384-9245"
         :etunimet "Vapautettu"
         :kutsumanimi "Testi"
         :sukunimi "Maksullinen"}]}))
  (GET "/oppijanumerorekisteri-service/henkilo/*" []
    (json-response
      {:oidHenkilo "1.2.246.562.24.78058065184"
       :hetu "190384-9245"
       :etunimet "Teuvo Taavetti"
       :kutsumanimi "Teuvo"
       :sukunimi "Testaaja"
       :yhteystiedotRyhma
       '({:id 0,
          :readOnly true,
          :ryhmaAlkuperaTieto "testiservice",
          :ryhmaKuvaus "testiryhmä",
          :yhteystieto
          [{:yhteystietoArvo "kayttaja@domain.local",
            :yhteystietoTyyppi "YHTEYSTIETO_SAHKOPOSTI"}]})})))
