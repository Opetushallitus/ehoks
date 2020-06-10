(ns oph.ehoks.mocked-routes.mock-koodisto-routes)

(def routes
  (GET "/koodisto-service/rest/codeelement/tutkinnonosat_100031" []
       (json-response-file
         "dev-routes/koodisto-service_rest_codeelement_tutkinnonosat__100031.json"))

  (GET "/koodisto-service/rest/codeelement/*/oppimisymparistot_0002" []
       (json-response
         {:metadata [{:nimi "Verkko- ja virtuaaliympäristö",
                      :kieli "FI"}]}))

  (GET "/koodisto-service/rest/codeelement/*/oppimisymparistot_0003" []
       (json-response
         {:metadata [{:nimi "Lukio",
                      :kieli "FI"}]}))

  (GET "/koodisto-service/rest/codeelement/*/*" []
       (json-response-file
         "dev-routes/rest_codeelement_ravintolakokinatjarjestys__4_2.json"))

  (GET "/koodisto-service/rest/json/urasuunnitelma/koodi" []
       (json-response-file
         "dev-routes/koodisto-service_rest_json_urasuunnitelma_koodi.json"))

  (GET "/koodisto-service/rest/json/*/koodi" []
       (json-response [])))
