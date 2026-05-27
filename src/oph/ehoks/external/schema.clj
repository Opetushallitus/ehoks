(ns oph.ehoks.external.schema
  (:require [schema.core :as s]))

(s/defschema Nimi
             "Nimitieto eri kielillä"
             {(s/optional-key :fi) s/Str
              (s/optional-key :sv) s/Str
              (s/optional-key :en) s/Str})

(s/defschema Peruste
             "Peruste-tieto ePerusteet-palvelusta"
             {:id s/Int
              (s/optional-key :nimi) Nimi
              (s/optional-key :osaamisalat) [{:nimi Nimi}]
              (s/optional-key :tutkintonimikkeet) [{:nimi Nimi}]})
