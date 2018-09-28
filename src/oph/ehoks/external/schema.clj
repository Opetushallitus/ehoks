(ns oph.ehoks.external.schema
  (:require [schema.core :as s]))

(s/defschema Peruste
             "Peruste-tieto ePerusteet-palvelusta"
             {:id s/Int
              (s/optional-key :nimi) {(s/optional-key :fi) s/Str
                                      (s/optional-key :en) s/Str
                                      (s/optional-key :sv) s/Str}
              (s/optional-key :osaamisalat) [{:nimi {(s/optional-key :fi) s/Str
                                                     (s/optional-key :sv) s/Str}}]
              (s/optional-key :tutkintonimikkeet) [{:nimi s/Str}]})
