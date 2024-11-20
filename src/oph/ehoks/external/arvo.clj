(ns oph.ehoks.external.arvo
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.connection :as c]
            [oph.ehoks.opiskeluoikeus.suoritus :as suoritus]
            [oph.ehoks.utils :as utils])
  (:import (clojure.lang ExceptionInfo)))

(defn call!
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
  (call! :get (str "/vastauslinkki/v1/status/" tunnus) {}))

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
  (if-not (contains? (set (:arvo-responsibilities config)) :create-kyselytunnus)
    (log/warn "create-kyselytunnus!: configured to not do anything")
    (call! :post "/vastauslinkki/v1"
           {:form-params kyselylinkki-params :content-type :json})))

(defn delete-kyselytunnus
  "Poistaa kyselytunnuksen Arvosta."
  [tunnus]
  (call! :delete (str "/vastauslinkki/v1/" tunnus) {}))

(defn build-jaksotunnus-request-body
  "Luo dataobjektin TEP-jaksotunnuksen luomisrequestille."
  [{:keys [opiskeluoikeus suoritus koulutustoimija toimipiste niputuspvm]
    :as ctx}
   herate request-id]
  {:koulutustoimija_oid       koulutustoimija
   :tyonantaja                (:tyopaikan-y-tunnus herate)
   :tyopaikka                 (:tyopaikan-nimi herate)
   :tyopaikka_normalisoitu    (utils/normalize-string (:tyopaikan-nimi herate))
   :tutkintotunnus            (get-in
                                suoritus
                                [:koulutusmoduuli
                                 :tunniste
                                 :koodiarvo])
   :tutkinnon_osa             (when (:tutkinnon-osa-koodi-uri herate)
                                (last
                                  (string/split
                                    (:tutkinnon-osa-koodi-uri herate)
                                    #"_")))
   :paikallinen_tutkinnon_osa (:tutkinnonosa-nimi herate)
   :tutkintonimike            (map
                                :koodiarvo
                                (:tutkintonimike suoritus))
   :osaamisala                (suoritus/get-osaamisalat
                                suoritus (:oid opiskeluoikeus)
                                (:heratepvm herate))
   :tyopaikkajakson_alkupvm   (str (:alkupvm herate))
   :tyopaikkajakson_loppupvm  (str (:loppupvm herate))
   :rahoituskausi_pvm         (str (:loppupvm herate))
   :osa_aikaisuus             (:osa-aikaisuustieto herate)
   :sopimustyyppi             (last
                                (string/split
                                  (:osaamisen-hankkimistapa-koodi-uri herate)
                                  #"_"))
   :oppisopimuksen_perusta    (when (:oppisopimuksen-perusta-koodi-uri herate)
                                (last
                                  (string/split
                                    (:oppisopimuksen-perusta-koodi-uri herate)
                                    #"_")))
   :vastaamisajan_alkupvm     (str niputuspvm)
   :oppilaitos_oid            (:oid (:oppilaitos opiskeluoikeus))
   :toimipiste_oid            toimipiste
   :request_id                request-id})

(defn create-jaksotunnus
  [data]
  (if-not (contains? (set (:arvo-responsibilities config)) :create-jaksotunnus)
    (log/warn "create-jaksotunnus!: configured to not do anything")
    (call! :post "/tyoelamapalaute/v1/vastaajatunnus"
           {:form-params data :content-type :json})))

(defn delete-jaksotunnus
  [tunnus]
  (call! :delete (str "/tyoelamapalaute/v1/vastaajatunnus/" tunnus) {}))
