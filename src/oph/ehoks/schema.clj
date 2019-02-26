(ns oph.ehoks.schema
  (:require [schema.core :as s]))

(s/defschema POSTResponse
             "RESTful POST response"
             {:uri s/Str})

(s/defschema KoodistoErrorMeta
             "Koodiston virhetilanteen metatiedot vastauksessa"
             {(s/optional-key :errors) [{:error-type s/Keyword
                                         :keys [s/Keyword]
                                         :uri s/Str
                                         :version s/Int}]})

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
              :frontend-url-path s/Str
              :tyopaikan-toimija-frontend-path s/Str
              :database-url s/Str
              :redis-url (s/maybe s/Str)
              :opintopolku-login-url s/Str
              :opintopolku-logout-url s/Str
              :opintopolku-return-url s/Str
              :opintopolku-tt-auth-url s/Str
              :eperusteet-url (s/maybe s/Str)
              :eperusteet-peruste-url (s/maybe s/Str)
              :lokalisointi-url (s/maybe s/Str)
              :oppijanumerorekisteri-url s/Str
              :cas-service-ticket-url s/Str
              :cas-username s/Str
              :cas-password s/Str
              :client-sub-system-code s/Str
              :session-max-age s/Int
              :service-timeout-ms s/Int
              :koodisto-url s/Str
              :koski-url s/Str
              :ext-cache-lifetime-minutes s/Int
              :kayttooikeus-service-url s/Str
              :backend-url s/Str
              :save-hoks-json? s/Bool})
