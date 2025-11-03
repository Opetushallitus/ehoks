(ns oph.ehoks.virkailija.cas-handler
  (:require [compojure.api.sweet :as c-api]
            [schema.core :as s]
            [ring.util.http-response :as response]
            [oph.ehoks.external.cas :as cas]
            [oph.ehoks.user :as user]
            [oph.ehoks.external.oph-url :as u]
            [clojure.tools.logging :as log]
            [clojure.data.xml :as xml]
            [oph.ehoks.db.db-operations.session :as db-session]
            [clojure.string :as cstr]))

(def routes
  "Virkailija CAS handler routes"
  (c-api/context "" []
    :tags ["cas"]

    (c-api/GET "/" []
      :summary "Virkailijan Opintopolku-kirjautumisen endpoint (CAS)"
      :query-params [ticket :- s/Str]
      (if-let [user-details (cas/service-ticket->user-details!
                              (u/get-url "ehoks.virkailija-login-return")
                              ticket)]
        (-> (response/see-other (u/get-url "ehoks-virkailija-frontend"))
            (assoc-in [:session :virkailija-user] user-details)
            (assoc-in [:session :ticket] ticket))
        (response/unauthorized {:error "Invalid ticket"})))

    (c-api/POST "/" []
      :summary "Virkailijan CAS SLO endpoint"
      :form-params [logoutRequest :- s/Str]
      (when-let [ticket (some #(when (= (:tag %) :SessionIndex)
                                 (first (:content %)))
                              (:content (xml/parse-str logoutRequest)))]
        (db-session/delete-sessions-by-ticket! (cstr/trim ticket)))
      (response/ok))))
