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
