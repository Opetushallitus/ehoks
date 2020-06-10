(ns oph.ehoks.mocked-routes.mock-routes
  (:require [compojure.core :refer [GET POST defroutes routes]]
            [ring.util.http-response :as response]
            [oph.ehoks.config :refer [config]]
            [clojure.java.io :as io]
            [cheshire.core :as cheshire]
            [clj-time.core :as time]
            [oph.ehoks.mocked-routes.mock-koodisto-routes :as koodisto-mocks]
            [oph.ehoks.mocked-routes.mock-eperusteet-routes
             :as eperusteet-mocks]
            [oph.ehoks.mocked-routes.mock-koski-routes :as koski-mocks]
            [oph.ehoks.mocked-routes.mock-auth-routes :as auth-mocks]
            [oph.ehoks.mocked-routes.mock-organisaatio-routes
             :as organisaatio-mocks]
            [oph.ehoks.mocked-routes.mock-oppijanumerorekisteri-routes
             :as oppijanumeroreskisteri-mocks]))

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
    (GET "/lokalisointi/cxf/rest/v1/localisation" []
      (json-response-file
        "dev-routes/lokalisointi_cxf_rest_v1_localisation.json"))

    (GET "/kayttooikeus-service/kayttooikeus/kayttaja" request
      (if (= (get-in request [:query-params "username"]) "ehoksvirkailija")
        (json-response-file
          "dev-routes/kayttooikeus-service_kayttooikeus_kayttaja_virkailija.json")
        (json-response-file
          "dev-routes/kayttooikeus-service_kayttooikeus_kayttaja.json")))

    (GET "/api/vastauslinkki/v1/status/:linkId" request
      (json-response
        {:vastattu false
         :voimassa_loppupvm (time/plus (time/now) (time/hours 2))}))

    auth-mocks/routes
    oppijanumeroreskisteri-mocks/routes
    koodisto-mocks/routes
    eperusteet-mocks/routes
    koski-mocks/routes
    organisaatio-mocks/routes))
