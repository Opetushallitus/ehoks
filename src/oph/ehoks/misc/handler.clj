(ns oph.ehoks.misc.handler
  (:require [compojure.api.sweet :as c-api]
            [oph.ehoks.misc.schema :as schema]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.oph-url :as u]
            [schema.core :as s]))

(def routes
  "Eräitä handler-reittejä"
  (c-api/context "/misc" []
    :header-params [caller-id :- s/Str]
    :tags ["misc"]

    (c-api/GET "/environment" [:as request]
      :summary "Palauttaa ympäristön tiedot ja asetukset"
      :return (rest/response schema/Environment)
      (rest/ok
        (merge
          {:virkailija-login-url
           (format
             "%s?service=%s"
             (u/get-url "cas.login")
             (u/get-url "ehoks.virkailija-login-return"))}
          {:cas-oppija-login-url-fi
           (format
             "%s&service=%s%s"
             (u/get-url "cas-oppija.login" "fi" "false")
             (:frontend-url-fi config)
             (u/get-url "ehoks.oppija-login-return-path"))}
          {:cas-oppija-login-url-sv
           (format
             "%s&service=%s%s"
             (u/get-url "cas-oppija.login" "sv" "false")
             (:frontend-url-sv config)
             (u/get-url "ehoks.oppija-login-return-path"))}
          {:cas-oppija-logout-url-fi
           (format
             "%s?service=%s/%s?lang=fi"
             (u/get-url "cas-oppija.logout")
             (:frontend-url-fi config)
             (:frontend-url-path config))}
          {:cas-oppija-logout-url-sv
           (format
             "%s?service=%s/%s?lang=sv"
             (u/get-url "cas-oppija.logout")
             (:frontend-url-sv config)
             (:frontend-url-path config))}
          {:raamit-url (u/get-url "virkailija-raamit-url")}
          (select-keys config [:eperusteet-peruste-url]))))))
