(ns oph.ehoks.external.arvo
  (:require [oph.ehoks.external.connection :as c]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.oph-url :as u]))

(defn get-kyselytunnus-status [tunnus]
  (c/with-api-headers {:method :get
                       :service (u/get-url "arvo-url")
                       :url (u/get-url "arvo.get-status" tunnus)
                       :options {:basic-auth
                                 [(:arvo-username config)
                                  (:arvo-password config)]
                                 :as :json}}))
