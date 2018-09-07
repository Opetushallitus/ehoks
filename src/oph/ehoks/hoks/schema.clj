(ns oph.ehoks.hoks.schema
  (:require [schema.core :as s]))

(s/defschema Document
  "HOKS document"
  {:id s/Int
   :version s/Int
   :created-by-oid s/Str
   :updated-by-oid s/Str
   :create-acceptor-oid s/Str
   :update-acceptor-oid s/Str
   :created-at s/Inst
   :accepted-at s/Inst
   :updated-at s/Inst
   :student-oid s/Str})

(s/defschema DocumentValues
  "HOKS document values for creating one"
  {:student-oid s/Str})
