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
  OppijaSearchResult
  "Oppijan haun tulos"
  {:oid s/Str
   :nimi s/Str
   :opiskeluoikeus-oid s/Str
   (s/optional-key :tutkinto-nimi) {(s/optional-key :fi) s/Str
                                    (s/optional-key :en) s/Str
                                    (s/optional-key :sv) s/Str}
   (s/optional-key :osaamisala-nimi) {(s/optional-key :fi) s/Str
                                      (s/optional-key :en) s/Str
                                      (s/optional-key :sv) s/Str}})

(s/defschema
  Oppija
  "Oppijan perustiedot"
  {:oid s/Str
   :nimi s/Str})

(s/defschema
  Opiskeluoikeus
  {:oid s/Str
   :oppija-oid s/Str
   :oppilaitos-oid s/Str
   (s/optional-key :koulutustoimija-oid) (s/maybe s/Str)
   (s/optional-key :tutkinto-nimi) {(s/optional-key :fi) s/Str
                                    (s/optional-key :en) s/Str
                                    (s/optional-key :sv) s/Str}
   (s/optional-key :osaamisala-nimi) {(s/optional-key :fi) s/Str
                                      (s/optional-key :en) s/Str
                                      (s/optional-key :sv) s/Str}
   (s/optional-key :paattynyt) (s/maybe s/Inst)})

(s/defschema
  Tutkinto
  "Tutkinnon perustiedot ePerusteet järjestelmässä"
  {:laajuus s/Int
   :nimi s/Str})

(s/defschema
  KoodiMetadata
  "Koodisto-koodin metadata, joka haetaan Koodisto-palvelusta"
  {(s/optional-key :nimi) (s/maybe s/Str)
   (s/optional-key :lyhyt-nimi) (s/maybe s/Str)
   (s/optional-key :kuvaus) (s/maybe s/Str)
   :kieli s/Str})

(s/defschema
  KoodistoKoodi
  "Koodisto-koodi"
  {:koodi-arvo s/Str
   :koodi-uri s/Str
   :versio s/Int
   (s/optional-key :metadata) [KoodiMetadata]})
