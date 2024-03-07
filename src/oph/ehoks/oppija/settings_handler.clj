(ns oph.ehoks.oppija.settings-handler
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :as response]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.user :as user]
            [oph.ehoks.user.settings :as user-settings]
            [schema.core :as s]))

(def routes
  "Oppija settings routes"
  (c-api/context "/settings" []
    :header-params [caller-id :- s/Str]
    (c-api/GET "/" request
      :summary "Istunnon käyttäjän asetukset"
      :return (rest/response schema/UserSettings)
      (rest/rest-ok
        (or (user-settings/get! (:oid (user/get request ::user/oppija)))
            {})))

    (c-api/PUT "/" request
      :summary "Istunnon käyttäjän asetuksien tallennus"
      :body [data schema/UserSettings]
      (user-settings/save! (:oid (user/get request ::user/oppija)) data)
      (response/created))))
