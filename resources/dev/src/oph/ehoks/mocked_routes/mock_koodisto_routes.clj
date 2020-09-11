(ns oph.ehoks.mocked-routes.mock-koodisto-routes
  (:require [oph.ehoks.mocked-routes.mock-gen :as mock-gen]
            [compojure.core :refer [GET routes]]))

(def mock-routes
  (routes
    (GET "/koodisto-service/rest/codeelement/tutkinnonosat_100031" []
         (mock-gen/json-response-file
           "dev-routes/koodisto-service_rest_codeelement_tutkinnonosat__100031.json"))

    (GET "/koodisto-service/rest/codeelement/*/ammatillisenoppiaineet_vvtk" []
         (mock-gen/json-response
           {:metadata [{:nimi "Viestintä ja vuorovaikutus toisella kotimaisella kielellä",
                        :kieli "FI"}]}))

    (GET "/koodisto-service/rest/codeelement/*/ammatillisenoppiaineet_yttt" []
         (mock-gen/json-response
           {:metadata [{:nimi "Työelämässä toimiminen",
                        :kieli "FI"}]}))

    (GET "/koodisto-service/rest/codeelement/*/oppimisymparistot_0002" []
         (mock-gen/json-response
           {:metadata [{:nimi "Verkko- ja virtuaaliympäristö",
                        :kieli "FI"}]}))

    (GET "/koodisto-service/rest/codeelement/*/oppimisymparistot_0003" []
         (mock-gen/json-response
           {:metadata [{:nimi "Lukio",
                        :kieli "FI"}]}))

    (GET "/koodisto-service/rest/codeelement/*/*" []
         (mock-gen/json-response-file
           "dev-routes/rest_codeelement_ravintolakokinatjarjestys__4_2.json"))

    (GET "/koodisto-service/rest/json/urasuunnitelma/koodi" []
         (mock-gen/json-response-file
           "dev-routes/koodisto-service_rest_json_urasuunnitelma_koodi.json"))

    (GET "/koodisto-service/rest/json/*/koodi" []
         (mock-gen/json-response []))))
