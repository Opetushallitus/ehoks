(ns oph.ehoks.external.handler
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :as response]
            [schema.core :as s]
            [oph.ehoks.external.schema :as schema]
            [oph.ehoks.external.connection :as c]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.external.eperusteet :as eperusteet]
            [clojure.core.async :as a]
            [oph.ehoks.config :refer [config]]))

(def routes
  (c-api/context "/external" []

    (c-api/GET "/eperusteet/" [:as request]
      :summary "Hakee perusteiden tietoja ePerusteet-palvelusta"
      :query-params [nimi :- String]
      :return (rest/response [schema/Peruste])
      (c/with-timeout
        (:service-timeout-ms config)
        (-> (eperusteet/search-perusteet-info nimi)
            eperusteet/map-perusteet
            rest/rest-ok)
        (response/internal-server-error {:error "Service timeout exceeded"})))))
