(ns oph.ehoks.schema
  (:require [schema.core :as s]))

(s/defschema User
             "User"
             {:first-name s/Str
              :common-name s/Str
              :surname s/Str})

(s/defschema UserInfo
  "Full user info"
  {:first-names s/Str
   :surname s/Str
   :common-name s/Str
   :oid s/Str
   (s/optional-key :contact-values-group) [{:id s/Int
                                            :contact [{:value s/Str
                                                       :type s/Str}]}]})
