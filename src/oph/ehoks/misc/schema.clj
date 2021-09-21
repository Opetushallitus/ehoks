(ns oph.ehoks.misc.schema
  (:require [schema.core :as s]))

(s/defschema Environment
             "Ympäristön tiedot ja asetukset"
             {:eperusteet-peruste-url s/Str
              :virkailija-login-url s/Str
              :cas-oppija-login-url-fi s/Str
              :cas-oppija-login-url-sv s/Str
              :cas-oppija-logout-url-fi s/Str
              :cas-oppija-logout-url-sv s/Str
              :raamit-url s/Str})
