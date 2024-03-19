(ns oph.ehoks.external.arvo
  (:require [oph.ehoks.external.connection :as c]
            [oph.ehoks.config :refer [config]]
            [clojure.string :as string])
  (:import (clojure.lang ExceptionInfo)))

(defn get-kyselytunnus-status!
  "Hakee kyselytunnuksen tilan Arvosta."
  [tunnus]
  (:body (c/with-api-headers {:method :get
                              :service (:arvo-url config)
                              :url (str (:arvo-url config) "/status/" tunnus)
                              :options {:basic-auth
                                        [(:arvo-username config)
                                         (:arvo-password config)]
                                        :as :json}})))

(defn get-kyselylinkki-status
  "Hakee kyselylinkin tilan Arvosta."
  [link]
  (get-kyselytunnus-status! (last (string/split link #"/"))))

(defn get-kyselylinkki-status-catch-404
  "Hakee kyselylinkin tilan Arvosta, ja käsittelee 404-virheitä."
  [link]
  (try
    (get-kyselylinkki-status link)
    (catch ExceptionInfo e
      (when-not (and (:status (ex-data e))
                     (= 404 (:status (ex-data e))))
        (throw e)))))

(defn delete-kyselytunnus
  "Poistaa kyselytunnuksen Arvosta."
  [tunnus]
  (c/with-api-headers {:method :delete
                       :service (:arvo-url config)
                       :url (str (:arvo-url config) "/" tunnus)
                       :options {:basic-auth [(:arvo-username config)
                                              (:arvo-password config)]
                                 :as :json}}))
