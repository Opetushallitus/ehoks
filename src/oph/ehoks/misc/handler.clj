(ns oph.ehoks.misc.handler
  (:require [compojure.api.sweet :as c-api]
            [oph.ehoks.misc.schema :as schema]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.config :refer [config]]))

(def routes
  (c-api/context "/misc" []

    (c-api/GET "/environment" [:as request]
      :summary "Palauttaa ympäristön tiedot ja asetukset"
      :return (rest/response schema/Environment)
      (rest/rest-ok (select-keys config [:opintopolku-login-url
                                         :eperusteet-peruste-url
                                         :opintopolku-logout-url])))))
