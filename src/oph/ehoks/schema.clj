(ns oph.ehoks.schema
  (:require [schema.core :as s]))

(s/defschema POSTResponse
  "RESTful POST response"
  {:uri s/Str})

(s/defschema User
             "User"
             {:first-name s/Str
              :common-name s/Str
              :surname s/Str})
