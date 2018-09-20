(ns oph.ehoks.hoks.schema
  (:require [schema.core :as s]))

(s/defschema Education
             "HOKS education"
             {:id s/Int
              :type s/Str
              :code s/Str
              :document-id s/Int})

(s/defschema Competence
             "HOKS student competence"
             {:type s/Str
              :document-id s/Int
              :qualification-register-number s/Str
              :competence-area-code s/Str
              :completed s/Inst})

(s/defschema PlannedCompetence
             "HOKS student planned competence"
             {:type s/Str
              :document-id s/Int
              :qualification-register-number s/Str
              :competence-area-code s/Str
              :acquiring-type s/Str
              :description s/Str
              :start s/Inst
              :end s/Inst
              :organisation s/Str
              :core-tasks [s/Str]
              :is-guided s/Bool
              :has-special-support s/Bool})

(s/defschema DocumentValues
             "HOKS document values for creating one"
             {:student-oid s/Str
              :career-objective s/Str
              :study-objective s/Str
              :qualification-register-number s/Str
              :competence-area-code s/Str
              :study-right-start s/Inst
              :study-finish-estimate s/Inst})

(s/defschema Document
             "HOKS document"
             (merge
               DocumentValues
               {:id s/Int
                :version s/Int
                :created-by-oid s/Str
                :updated-by-oid s/Str
                :create-acceptor-oid s/Str
                :update-acceptor-oid s/Str
                :created-at s/Inst
                :accepted-at s/Inst
                :updated-at s/Inst
                :student-oid s/Str
                :competences [Competence]
                :educations [Education]
                :planned-competencies [PlannedCompetence]}))
