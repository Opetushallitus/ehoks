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
  {:uuid java.util.UUID
   :tyyppi s/Str
   :voimassaolo-alku LocalDate
   :voimassaolo-loppu LocalDate
   :koodisto-koodi s/Str})

(s/defschema
  JakolinkkiLuonti
  "Tutkinnon osan jakolinkin luonti"
  {:voimassaolo-alku LocalDate
   :voimassaolo-loppu LocalDate
   :tyyppi s/Str})