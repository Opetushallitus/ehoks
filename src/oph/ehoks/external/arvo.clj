(ns oph.ehoks.external.arvo
  (:require [oph.ehoks.external.connection :as c]
            [oph.ehoks.config :refer [config]]
            [clojure.string :as str]
            [clojure.tools.logging :as log]))

(defn get-kyselytunnus-status [tunnus]
  (log/info "STATUS URL: " (str (:arvo-url config) "/status/" tunnus))
  (:body (c/with-api-headers {:method :get
                              :service (:arvo-url config)
                              :url (str (:arvo-url config) "/status/" tunnus)
                              :options {:basic-auth
                                        [(:arvo-username config)
                                         (:arvo-password config)]
                                        :as :json}})))

(defn get-kyselylinkki-status [link]
  (let [status (get-kyselytunnus-status (last (str/split link #"/")))]
    (log/info "status: " status)
    status))
