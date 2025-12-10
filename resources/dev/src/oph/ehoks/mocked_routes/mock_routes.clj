(ns oph.ehoks.mocked-routes.mock-routes
  (:require [compojure.core :refer [GET POST defroutes routes]]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.mocked-routes.mock-gen :as mock-gen]
            [oph.ehoks.mocked-routes.mock-koodisto-routes :as koodisto-mocks]
            [oph.ehoks.mocked-routes.mock-arvo-routes :as arvo-mocks]
            [oph.ehoks.mocked-routes.mock-eperusteet-routes
             :as eperusteet-mocks]
            [oph.ehoks.mocked-routes.mock-koski-routes :as koski-mocks]
            [oph.ehoks.mocked-routes.mock-auth-routes :as auth-mocks]
            [oph.ehoks.mocked-routes.mock-organisaatio-routes
             :as organisaatio-mocks]
            [oph.ehoks.mocked-routes.mock-oppijanumerorekisteri-routes
             :as oppijanumerorekisteri-mocks]))

(defroutes mock-routes
    (GET "/lokalisointi/cxf/rest/v1/localisation" []
      (mock-gen/json-response-file
        "dev-routes/lokalisointi_cxf_rest_v1_localisation.json"))

    (GET "/kayttooikeus-service/kayttooikeus/kayttaja" request
      (if (= (get-in request [:query-params "username"]) "ehoksvirkailija")
        (mock-gen/json-response-file
          "dev-routes/kayttooikeus-service_kayttooikeus_kayttaja_virkailija.json")
        (mock-gen/json-response-file
          "dev-routes/kayttooikeus-service_kayttooikeus_kayttaja.json")))

    auth-mocks/mock-routes
    arvo-mocks/mock-routes
    oppijanumerorekisteri-mocks/mock-routes
    koodisto-mocks/mock-routes
    eperusteet-mocks/mock-routes
    koski-mocks/mock-routes
    organisaatio-mocks/mock-routes)
