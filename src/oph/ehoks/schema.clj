(ns oph.ehoks.schema
  (:require [schema.core :as s]
            [oph.ehoks.schema-tools :refer [describe modify]]))

(s/defschema POSTResponse
             "RESTful POST response"
             {:uri s/Str})

(s/defschema KoodistoErrorMeta
             "Koodiston virhetilanteen metatiedot vastauksessa"
             {(s/optional-key :errors) [{:error-type s/Keyword
                                         :keys [s/Keyword]
                                         :path s/Str}]})

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

(s/defschema
  UserSettings
  "User settings"
  s/Any)

(s/defschema Config
             "Application configuration file"
             {(s/optional-key :version) s/Str
              (s/optional-key :debug) s/Bool
              :port s/Int
              :frontend-url-fi s/Str
              :frontend-url-sv s/Str
              :frontend-url-path s/Str
              :eperusteet-peruste-url s/Str
              :tyopaikan-toimija-frontend-path s/Str
              :opintopolku-login-url-fi s/Str
              :opintopolku-login-url-sv s/Str
              :opintopolku-logout-url-fi s/Str
              :opintopolku-logout-url-sv s/Str
              :opintopolku-return-url s/Str
              :opintopolku-tt-auth-url s/Str
              :cas-username s/Str
              :cas-password s/Str
              :client-sub-system-code s/Str
              :session-max-age s/Int
              :service-timeout-ms s/Int
              :ext-cache-lifetime-minutes s/Int
              :opintopolku-host s/Str
              :heratepalvelu-queue s/Str
              :heratepalvelu-tyoelamapalaute-queue s/Str
              :send-herate-messages? s/Bool
              :audit? s/Bool
              :db-type s/Str
              :db-server s/Str
              :db-name s/Str
              :db-port s/Int
              :db-username s/Str
              :db-password s/Str
              :arvo-password s/Str
              :arvo-username s/Str
              :arvo-url s/Str})
