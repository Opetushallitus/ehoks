(ns oph.ehoks.common.schema
  (:require [schema.core :as s]))

(s/defschema Translated
             "Translated string"
             {:fi s/Str
              (s/optional-key :en) (s/maybe s/Str)
              (s/optional-key :sv) (s/maybe s/Str)})

(s/defschema Information
             "Basic service and process information"
             {:basic-information Translated
              :hoks-process Translated})

(s/defschema HealthcheckStatus
             "Service healthcheck status"
             {})

(s/defschema Lokalisointi
             "Localization Service"
             [{:category s/Str
               :createdBy  s/Str
               :key  s/Str
               :force s/Bool
               :locale  s/Str
               :value  s/Str
               :created s/Num
               :modified s/Any
               :accessed s/Any
               :accesscount s/Int
               :id s/Int
               :modifiedBy  s/Str}])

(s/defschema LocalizationHealtcheckStatus
             "Healthcheck status of Localization Service"
             {})

(s/defschema
  Oppija
  "Oppijan perustiedot"
  {:eid s/Int
   :oid s/Str
   :etunimi s/Str
   :sukunimi s/Str})

(s/defschema
  Tutkinto
  "Tutkinnon perustiedot ePerusteet järjestelmässä"
  {:laajuus s/Int
   :nimi s/Str})
