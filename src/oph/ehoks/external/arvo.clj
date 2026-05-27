(ns oph.ehoks.external.arvo
  (:require [clojure.tools.logging :as log]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.connection :as c]
            [oph.ehoks.utils :as utils])
  (:import (clojure.lang ExceptionInfo)))

(defn call!
  "Apufunktio, jota kaikki API-kutsut Arvoon käyttävät."
  ([method url-suffix]
    (call! method url-suffix {}))
  ([method url-suffix options]
    (-> options
        (assoc :basic-auth [(:arvo-username config) (:arvo-password config)]
               :as :json)
        (->> (hash-map :method method, :service (:arvo-url config),
                       :url (str (:arvo-url config) url-suffix),
                       :options))
        (c/with-api-headers)
        :body)))

(defn get-kyselytunnus-status!
  "Hakee kyselylinkin tilan Arvosta."
  [vastaajatunnus]
  (try (->> (str "/vastauslinkki/v1/status/" vastaajatunnus)
            (call! :get)
            (utils/to-dash-keys))
       (catch ExceptionInfo e
         (when-not (= 404 (:status (ex-data e)))
           (throw e)))))

(defn create-kyselytunnus!
  "Luo kyselylinkin Arvoon."
  [kyselylinkki-params]
  (if-not (contains? (set (:arvo-responsibilities config)) :create-kyselytunnus)
    (do (log/info "create-kyselytunnus!: configured to not call Arvo")
        {:tunnus (str "<dummy-" (java.util.UUID/randomUUID) ">")})
    (call! :post "/vastauslinkki/v1"
           {:form-params kyselylinkki-params :content-type :json})))

(defn update-kyselytunnus!
  "Päivittää Arvoon kyselylinkin muuttuneet tiedot."
  [vastaajatunnus tila new-alkupvm new-loppupvm]
  (try
    (utils/to-dash-keys
      (call! :patch (str "/vastauslinkki/v1/" vastaajatunnus)
             {:content-type :json
              :form-params {:metatiedot {:tila tila}
                            :voimassa_alkupvm new-alkupvm
                            :voimassa_loppupvm new-loppupvm}}))
    (catch ExceptionInfo e
      (when-not (= 404 (:status (ex-data e)))
        (throw e)))))

(defn delete-kyselytunnus
  "Poistaa kyselytunnuksen Arvosta."
  [tunnus]
  (call! :delete (str "/vastauslinkki/v1/" tunnus) {}))

(defn create-jaksotunnus!
  "Create new työelämäpalaute-vastaajatunnus in Arvo."
  [request]
  (if-not (contains? (set (:arvo-responsibilities config)) :create-jaksotunnus)
    (do (log/info "create-jaksotunnus!: configured to not call Arvo")
        {:tunnus (str "<dummy-" (java.util.UUID/randomUUID) ">")})
    (call! :post "/tyoelamapalaute/v1/vastaajatunnus"
           {:form-params request :content-type :json})))

(defn delete-jaksotunnus
  [tunnus]
  {:pre [(some? tunnus)]}
  (call! :delete (str "/tyoelamapalaute/v1/vastaajatunnus/" tunnus) {}))
