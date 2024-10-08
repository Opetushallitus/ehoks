(ns oph.ehoks.mocked-routes.mock-organisaatio-routes
  (:require [ring.util.http-response :as response]
            [cheshire.core :as cheshire]
            [oph.ehoks.mocked-routes.mock-gen :as mock-gen]
            [compojure.core :refer [GET POST routes]]))

(def mock-routes
  (routes
    (POST "/organisaatio-service/rest/organisaatio/v4/findbyoids" request
          (mock-gen/json-response-file
            "dev-routes/organisaatio-service_rest_organisaatio_v4_findbyoids.json"))

    (GET "/organisaatio-service/rest/organisaatio/v4/1.2.246.562.15.404" []
      (assoc-in
        (response/not-found
          (cheshire/generate-string
            {:errorMessage "organisaatio.exception.organisaatio.not.found"
             :errorKey ""}))
        [:headers "Content-Type"] "application/json"))

    (GET "/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.5921222" []
      (mock-gen/json-response
        {:oid "1.2.246.562.10.5921222"
         :nimi {:fi "Testaus-organisaatio"}
         :parentOidPath "|1.2.246.562.10.00000000001|"}))

    (GET "/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.54425555" []
      (mock-gen/json-response
        {:oid "1.2.246.562.10.54425555"
         :nimi {:fi "Joku koulutuksen järjestäjä-organisaatio"}
         :parentOidPath "|1.2.246.562.10.00000000001|"}))

    (GET "/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.54424444" []
      (mock-gen/json-response
        {:oid "1.2.246.562.10.54424444"
         :nimi {:fi "Aiemman arvioijan organisaatio"}
         :parentOidPath "|1.2.246.562.10.00000000001|"}))

    (GET "/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.54423333" []
      (mock-gen/json-response
        {:oid "1.2.246.562.10.54423333"
         :nimi {:fi "Osa-alueen järjestäjä-organisaatio"}
         :parentOidPath "|1.2.246.562.10.00000000001|"}))

    (GET "/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.21122461771"
      []
      (mock-gen/json-response-file
        (str "dev-routes/organisaatio-service_rest_organisaatio_v4_"
             "1.2.246.562.10.21122461771.json")))

    (GET "/organisaatio-service/rest/organisaatio/v4/:oid" [oid]
      (mock-gen/json-response
        {:oid oid
         :nimi {:fi "Esimerkki-organisaatio"}
         :tyypit ["organisaatiotyyppi_02" "organisaatiotyyppi_03"]  ; toimipiste
         :parentOidPath "|1.2.246.562.10.00000000001|"}))))
