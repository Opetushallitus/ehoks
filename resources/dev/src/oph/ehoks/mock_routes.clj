(ns oph.ehoks.mock-routes
  (:require [oph.ehoks.handler :refer [app]]
            [compojure.core :refer [GET POST defroutes routes]]
            [ring.util.http-response :as response]
            [ring.middleware.reload :refer [wrap-reload]]
            [oph.ehoks.config :refer [config]]
            [hiccup.core :refer [html]]
            [clojure.java.io :as io]
            [clojure.string :as c-str]
            [cheshire.core :as cheshire]))

(def dev-login-form
  [:div
   [:form {:action (:opintopolku-return-url config) :method "POST"}
    [:label "FirstName"
     [:input {:type "text" :name "FirstName" :value "Teuvo Taavetti"}]]
    [:label "cn"
     [:input {:type "text" :name "cn" :value "Teuvo"}]]
    [:label "givenName"
     [:input {:type "text" :name "givenName" :value "Teuvo"}]]
    [:label "hetu"
     [:input {:type "text" :name "hetu" :value "190384-9245"}]]
    [:label "sn"
     [:input {:type "text" :name "sn" :value "Testaaja"}]]
    [:button {:type "submit" :value "submit"} "Login"]]])

(defn- json-response [value]
  (assoc-in
      (response/ok
        (cheshire/generate-string
          value))
      [:headers "Content-Type"] "application/json"))

(defroutes mock-routes
  (GET "/auth-dev/opintopolku-login/" [] (html dev-login-form))
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
      {:results
       [{:oidHenkilo "1.2.246.562.24.78058065184"
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
              :yhteystietoTyyppi "YHTEYSTIETO_SAHKOPOSTI"}]})}]})))
