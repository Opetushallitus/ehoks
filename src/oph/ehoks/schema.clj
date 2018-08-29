(ns oph.ehoks.schema
  (:require [schema.core :as s]))

(s/defschema User
             "User"
             {:first-name s/Str
              :common-name s/Str
              :surname s/Str})
