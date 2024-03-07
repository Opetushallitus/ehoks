(ns oph.ehoks.external.handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [ring.util.http-response :as response]
            [schema.core :as s]
            [oph.ehoks.middleware :refer [wrap-require-user-type-and-auth]]
            [oph.ehoks.external.schema :as schema]
            [oph.ehoks.external.utils :as utils]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.external.eperusteet :as eperusteet]
            [oph.ehoks.external.koodisto :as koodisto]
            [oph.ehoks.external.koski :as koski]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.user :as user]))

(def routes
  "External handlerin reitit"
  (c-api/context "/external" []
    :header-params [caller-id :- s/Str]
    :tags ["external"]

    (c-api/GET "/eperusteet/" [:as request]
      :summary "Hakee perusteiden tietoja ePerusteet-palvelusta"
      :query-params [nimi :- String]
      :return (rest/response [schema/Peruste])
      (utils/with-timeout
        (:service-timeout-ms config)
        (-> (eperusteet/search-perusteet-info nimi)
            eperusteet/map-perusteet
            rest/rest-ok)
        (response/internal-server-error {:error "Service timeout exceeded"})))

    (route-middleware
      [(wrap-require-user-type-and-auth ::user/oppija)]
      (c-api/GET "/koodistokoodi/:uri/:versio" []
        :summary "Hakee koodisto koodin tietoja Koodisto-palvelusta"
        :path-params [uri :- s/Str, versio :- s/Int]
        :return (rest/response schema/ExtendedKoodistoKoodi)
        (utils/with-timeout
          (:service-timeout-ms config)
          (-> (koodisto/get-koodi-versio uri versio)
              :body
              koodisto/filter-koodisto-values
              rest/rest-ok)
          (response/internal-server-error {:error "Service timeout exceeded"})))

      (c-api/GET "/koski/oppija" [:as request]
        :summary "Hakee oppijan tietoja Koski-palvelusta"
        :return (rest/response schema/KoskiOppija)
        (utils/with-timeout
          (:service-timeout-ms config)
          (-> (:oid (user/get request ::user/oppija))
              (koski/get-student-info)
              koski/filter-oppija
              rest/rest-ok)
          (response/internal-server-error
            {:error "Service timeout exceeded"}))))))
