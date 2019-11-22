(ns oph.ehoks.misc.schema
  (:require [schema.core :as s]))

(s/defschema Environment
             "Ympäristön tiedot ja asetukset"
             {:opintopolku-login-url-fi s/Str
              :opintopolku-login-url-sv s/Str
              :eperusteet-peruste-url s/Str
              :opintopolku-logout-url-fi s/Str
              :opintopolku-logout-url-sv s/Str
              :virkailija-login-url s/Str
              :raamit-url s/Str})
