(ns oph.ehoks.external.handler
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :as response]
            [schema.core :as s]
            [oph.ehoks.external.schema :as schema]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.external.eperusteet :as eperusteet]
            [clojure.core.async :as a]))

(def routes
  (c-api/context "/external" []

    (c-api/GET "/eperusteet/" [:as request]
      :summary "Hakee perusteiden tietoja ePerusteet-palvelusta"
      :query-params [nimi :- String]
      :return (rest/response [schema/Peruste])
      (a/go
        (let [data (eperusteet/search-perusteet-info nimi)
              values (eperusteet/map-perusteet data)]
          (rest/rest-ok values))))))
