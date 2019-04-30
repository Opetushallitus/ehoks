(ns oph.ehoks.misc.handler
  (:require [compojure.api.sweet :as c-api]
            [oph.ehoks.misc.schema :as schema]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.oph-url :as u]))

(def routes
  (c-api/context "/misc" []
    :tags ["misc"]

    (c-api/GET "/environment" [:as request]
      :summary "Palauttaa ympäristön tiedot ja asetukset"
      :return (rest/response schema/Environment)
      (rest/rest-ok
        (merge
          {:virkailija-login-url
           (format
             "%s?service=%s"
             (u/get-url "cas.login")
             (u/get-url "ehoks.virkailija-login-return")
             (:virkailija-login-return-url config))}
          (select-keys config [:opintopolku-login-url
                               :eperusteet-peruste-url
                               :opintopolku-logout-url]))))))
