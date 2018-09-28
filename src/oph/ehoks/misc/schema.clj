(ns oph.ehoks.misc.schema
  (:require [schema.core :as s]))

(s/defschema Environment
             "Ympäristön tiedot ja asetukset"
             {:opintopolku-login-url s/Str})
