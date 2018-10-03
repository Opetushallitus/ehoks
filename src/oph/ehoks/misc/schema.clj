(ns oph.ehoks.misc.schema
  (:require [schema.core :as s]))

(s/defschema Environment
             "Ympäristön tiedot ja asetukset"
             {:opintopolku-login-url s/Str
              :eperusteet-peruste-url s/Str
              :opintopolku-logout-url s/Str})
