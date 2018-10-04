(ns oph.ehoks.external.handler
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :as response]
            [schema.core :as s]
            [oph.ehoks.external.schema :as schema]
            [oph.ehoks.external.utils :as utils]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.external.eperusteet :as eperusteet]
            [oph.ehoks.external.koodisto :as koodisto]
            [oph.ehoks.config :refer [config]]))

(def routes
  (c-api/context "/external" []

    (c-api/GET "/koodistokoodi/:uri/:versio" []
      :summary "Hakee koodisto koodin tietoja Kooidsto-palvelusta"
      :path-params [uri :- s/Str, versio :- s/Int]
      :return (rest/response schema/ExtendedKoodistoKoodi)
      (utils/with-timeout
        (:service-timeout-ms config)
        (-> (koodisto/get-koodi-versio uri versio)
            koodisto/filter-koodisto-values
            rest/rest-ok)
        (response/internal-server-error {:error "Service timeout exceeded"})))

    (c-api/GET "/eperusteet/" [:as request]
      :summary "Hakee perusteiden tietoja ePerusteet-palvelusta"
      :query-params [nimi :- String]
      :return (rest/response [schema/Peruste])
      (utils/with-timeout
        (:service-timeout-ms config)
        (-> (eperusteet/search-perusteet-info nimi)
            eperusteet/map-perusteet
            rest/rest-ok)
        (response/internal-server-error {:error "Service timeout exceeded"})))))
