(ns oph.ehoks.oppija.schema
  (:require [schema.core :as s]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.schema-tools :refer [describe modify]]))

(s/defschema
  OppijaHOKS
  (modify
    hoks-schema/HOKS
    "Oppijan HOKS"
    {:replaced-in {[:eid] s/Str}
     :removed [:id]}))
