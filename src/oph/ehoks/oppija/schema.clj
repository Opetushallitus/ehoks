(ns oph.ehoks.oppija.schema
  (:require [schema.core :as s]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.schema-tools :refer [describe modify]]
            [oph.ehoks.common.schema :refer [Translated]])
  (:import (java.time LocalDate)
           (java.util UUID)))

(s/defschema
  OppijaHOKS
  (modify
    hoks-schema/HOKS
    "Oppijan HOKS"
    {:removed [:id]}))

(s/defschema
  Jakolinkki
  "Tutkinnon osan jakolinkki"
  {:oppija-nimi s/Str
   :oppija-oid s/Str
   :tutkinto-nimi Translated
   :osaamisala-nimi Translated
   :voimassaolo-alku LocalDate,
   :voimassaolo-loppu LocalDate
   :module-tyyppi s/Str
   :module s/Any
   :tutkinnonosa-tyyppi s/Str
   :tutkinnonosa s/Any})

(s/defschema
  JakolinkkiLuonti
  "Tutkinnon osan jakolinkin luonti"
  {:tutkinnonosa-module-uuid s/Str
   :tutkinnonosa-tyyppi s/Str
   :shared-module-uuid s/Str
   :shared-module-tyyppi s/Str
   :voimassaolo-alku LocalDate
   :voimassaolo-loppu LocalDate
   :hoks-eid s/Str})

(s/defschema
  JakolinkkiListaus
  "Yhden moduulin jakolinkkien tiedot"
  {:voimassaolo-alku  LocalDate,
   :voimassaolo-loppu LocalDate,
   :share-id UUID})
