(ns oph.ehoks.external.arvo
  (:require [oph.ehoks.external.connection :as c]
            [oph.ehoks.config :refer [config]]
            [clojure.string :as string])
  (:import (clojure.lang ExceptionInfo)))

(defn arvo-call!
  "Apufunktio, jota kaikki API-kutsut Arvoon käyttävät."
  [method url-suffix options]
  (-> options
      (assoc :basic-auth [(:arvo-username config) (:arvo-password config)]
             :as :json)
      (->> (hash-map :method method, :service (:arvo-url config),
                     :url (str (:arvo-url config) url-suffix),
                     :options))
      (c/with-api-headers)
      :body))

(defn get-kyselytunnus-status!
  "Hakee kyselytunnuksen tilan Arvosta."
  [tunnus]
  (arvo-call! :get (str "/status/" tunnus) {}))

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

(defn create-kyselytunnus!
  "Luo kyselylinkin Arvoon."
  [kyselylinkki-params]
  (arvo-call! :post "" {:form-params kyselylinkki-params :content-type :json}))

(defn delete-kyselytunnus
  "Poistaa kyselytunnuksen Arvosta."
  [tunnus]
  (arvo-call! :delete (str "/" tunnus) {}))
