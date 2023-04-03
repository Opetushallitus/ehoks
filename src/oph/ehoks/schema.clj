(ns oph.ehoks.schema
  (:require [schema.core :as s]
            [oph.ehoks.schema-tools :refer [describe modify]]))

(s/defschema POSTResponse
             "RESTful POST response"
             {:uri s/Str
              (s/optional-key :notifications) [s/Str]})

(s/defschema KoodistoErrorMeta
             "Koodiston virhetilanteen metatiedot vastauksessa"
             {(s/optional-key :errors) [{:error-type s/Keyword
                                         :keys [s/Keyword]
                                         :path s/Str}]})

(s/defschema User
             "User"
             {(s/optional-key :oid) s/Str
              (s/optional-key :first-name) s/Str
              (s/optional-key :common-name) s/Str
              (s/optional-key :surname) s/Str
              (s/optional-key :usingValtuudet) s/Bool})

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
              :cas-username s/Str
              :cas-password s/Str
              :client-sub-system-code s/Str
              :session-max-age s/Int
              :service-timeout-ms s/Int
              :ext-cache-lifetime-minutes s/Int
              :opintopolku-host s/Str
              :heratepalvelu-delete-tunnus-queue s/Str
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
              :arvo-url s/Str
              :enforce-opiskeluoikeus-match? s/Bool
              :prevent-finished-opiskeluoikeus-updates? s/Bool
              :heratepalvelu-resend-queue s/Str
              :koski-opiskeluoikeus-cache-ttl-millis s/Int
              :koski-oppija-cache-ttl-millis s/Int})
