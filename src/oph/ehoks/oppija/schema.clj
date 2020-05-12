(ns oph.ehoks.oppija.schema
  (:require [schema.core :as s]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.schema-tools :refer [describe modify]])
  (:import (java.time LocalDate)))

(s/defschema
  OppijaHOKS
  (modify
    hoks-schema/HOKS
    "Oppijan HOKS"
    {:removed [:id]}))

(s/defschema
  Jakolinkki
  "Tutkinnon osan jakolinkki"
  {:share-id java.util.UUID
   :tutkinnonosa-module-uuid java.util.UUID
   :tutkinnonosa-tyyppi s/Str
   :shared-module-uuid java.util.UUID
   :shared-module-tyyppi s/Str
   :voimassaolo-alku LocalDate
   :voimassaolo-loppu LocalDate})

(s/defschema
  JakolinkkiLuonti
  "Tutkinnon osan jakolinkin luonti"
  {:tutkinnonosa-module-uuid s/Str
   :tutkinnonosa-tyyppi s/Str
   :shared-module-uuid s/Str
   :shared-module-tyyppi s/Str
   :voimassaolo-alku LocalDate
   :voimassaolo-loppu LocalDate})
