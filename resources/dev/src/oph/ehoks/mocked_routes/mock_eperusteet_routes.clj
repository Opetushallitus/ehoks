(ns oph.ehoks.mocked-routes.mock-eperusteet-routes
  (:require [compojure.core :refer [GET routes]]
            [oph.ehoks.mocked-routes.mock-gen :as mock-gen]))

(def mock-routes
  (routes
    (GET "/eperusteet-service/api/perusteet" request
         (mock-gen/json-response-file
           "dev-routes/eperusteet_api_perusteet.json"))

    (GET "/eperusteet-amosaa-service/api/julkinen/koodi/:koodi" request
         (if (= (get-in request [:params :koodi])
                "paikallinen_tutkinnonosa_1.2.246.562.10.41253773158_1983")
           (mock-gen/json-response-file
             "dev-routes/eperusteet-amosaa-service_api_julkinen_koodi_paikallinen__tutkinnonosa__1.2.246.562.10.41253773158__1983.json")
           (mock-gen/json-response [])))

    (GET "/eperusteet-service/api/tutkinnonosat/:tutkinnon-osa-id/viitteet" request
      (let [tutkinnon-osa-id (get-in request [:param :tutkinnon-osa-id])]
        (if (= tutkinnon-osa-id "3003003")
          (mock-gen/json-response-file
            "dev-routes/eperusteet-service_api_tutkinnonosat_3003003_viitteet.json")
          (mock-gen/json-response-file
            "dev-routes/eperusteet-service_api_tutkinnonosat_52824_viitteet.json"))))

    (GET "/eperusteet-service/api/perusteenosat/52824/osaalueet" []
      (mock-gen/json-response-file
        "dev-routes/eperusteet-service_api_perusteenosat_52824_osaalueet.json"))

    (GET "/eperusteet-service/api/perusteet/diaari" []
         (mock-gen/json-response-file
           "dev-routes/eperusteet-service_api_perusteet_diaari.json"))

    (GET "/eperusteet-service/api/perusteet/3397335/suoritustavat/reformi/rakenne" []
         (mock-gen/json-response-file
           "dev-routes/eperusteet-service_api_perusteet_3397335_suoritustavat_reformi_rakenne.json"))

    (GET "/eperusteet-service/api/perusteet/1352660/suoritustavat/ops/rakenne" []
         (mock-gen/json-response-file
           "dev-routes/eperusteet-service_api_perusteet_1352660_suoritustavat_ops_rakenne.json"))

    (GET "/eperusteet-service/api/tutkinnonosat" request
      (let [koodi-uri (get-in request [:query-params "koodiUri"])]
        (case koodi-uri
          "tutkinnonosat_101056"
          (mock-gen/json-response-file
            "dev-routes/eperusteet-service_api_tutkinnonosat_not_found.json")
          "tutkinnonosat_400005"
          (mock-gen/json-response-file
            "dev-routes/eperusteet-service_api_tutkinnonosat_telma.json")
          (mock-gen/json-response-file
            "dev-routes/eperusteet-service_api_tutkinnonosat.json"))))))
