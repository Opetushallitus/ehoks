(ns oph.ehoks.schema
  (:require [schema.core :as s]))

(s/defschema POSTResponse
             "RESTful POST response"
             {:uri s/Str})

(s/defschema User
             "User"
             {(s/optional-key :oid) s/Str
              :first-name s/Str
              :common-name s/Str
              :surname s/Str})

(s/defschema UserInfo
             "Full user info"
             (merge
               User
               {(s/optional-key :contact-values-group)
                [{:id s/Int
                  :contact [{:value s/Str
                             :type s/Str}]}]}))

(s/defschema Config
             "Application configuration file"
             {(s/optional-key :version) s/Str
              (s/optional-key :debug) s/Bool
              :port s/Int
              :frontend-url s/Str
              :database-url s/Str
              :redis-url (s/maybe s/Str)
              :opintopolku-login-url s/Str
              :opintopolku-logout-url s/Str
              :opintopolku-return-url s/Str
              :eperusteet-url (s/maybe s/Str)
              :lokalisointi-url (s/maybe s/Str)
              (s/optional-key :oppijanumerorekisteri-url) (s/maybe s/Str)
              (s/optional-key :cas-service-ticket-url) (s/maybe s/Str)
              (s/optional-key :cas-username) (s/maybe s/Str)
              (s/optional-key :cas-password) (s/maybe s/Str)
              (s/optional-key :client-sub-system-code) (s/maybe s/Str)
              (s/optional-key :session-max-age) s/Int})
