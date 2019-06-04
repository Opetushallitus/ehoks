(ns oph.ehoks.cas-test
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.cas :as cas]))

(defn test-auth []
  (let [service (:backend-url config)
        ticket-data (cas/add-cas-ticket {} service)]
    (cas/validate-ticket service (get-in ticket-data [:query-params :ticket]))))
