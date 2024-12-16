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
  [{:keys [opiskeluoikeus existing-palaute suoritus koulutustoimija toimipiste
           niputuspvm]
    :as ctx}
   request-id]
  {:koulutustoimija_oid       koulutustoimija
   :tyonantaja                (:tyopaikan-y-tunnus existing-palaute)
   :tyopaikka                 (:tyopaikan-nimi existing-palaute)
   :tyopaikka_normalisoitu    (utils/normalize-string
                                (:tyopaikan-nimi existing-palaute))
   :tutkintotunnus            (get-in
                                suoritus
                                [:koulutusmoduuli
                                 :tunniste
                                 :koodiarvo])
   :tutkinnon_osa             (when (:tutkinnon-osa-koodi-uri existing-palaute)
                                (last
                                  (string/split
                                    (:tutkinnon-osa-koodi-uri existing-palaute)
                                    #"_")))
   :paikallinen_tutkinnon_osa (:tutkinnonosa-nimi existing-palaute)
   :tutkintonimike            (map
                                :koodiarvo
                                (:tutkintonimike suoritus))
   :osaamisala                (suoritus/get-osaamisalat
                                suoritus (:oid opiskeluoikeus)
                                (:heratepvm existing-palaute))
   :tyopaikkajakson_alkupvm   (str (:alkupvm existing-palaute))
   :tyopaikkajakson_loppupvm  (str (:loppupvm existing-palaute))
   :rahoituskausi_pvm         (str (:loppupvm existing-palaute))
   :osa_aikaisuus             (:osa-aikaisuustieto existing-palaute)
   :sopimustyyppi             (last
                                (string/split
                                  (:osaamisen-hankkimistapa-koodi-uri
                                    existing-palaute)
                                  #"_"))
   :oppisopimuksen_perusta    (when (:oppisopimuksen-perusta-koodi-uri
                                      existing-palaute)
                                (last
                                  (string/split
                                    (:oppisopimuksen-perusta-koodi-uri
                                      existing-palaute)
                                    #"_")))
   :vastaamisajan_alkupvm     (str niputuspvm)
   :oppilaitos_oid            (:oid (:oppilaitos opiskeluoikeus))
   :toimipiste_oid            toimipiste
   :request_id                request-id})

(defn create-jaksotunnus!
  [request]
  (let [configured-to-call-arvo? (contains?
                                   (set (:arvo-responsibilities config))
                                   :create-jaksotunnus)
        response (when configured-to-call-arvo?
                   (call! :post "/tyoelamapalaute/v1/vastaajatunnus"
                          {:form-params request :content-type :json}))]
    (if (:tunnus response)
      response
      (throw (ex-info
               (if configured-to-call-arvo?
                 (format (str "No jaksotunnus got from Arvo with request %s. "
                              "Response from Arvo was %s.")
                         request response)
                 "create-jaksotunnus!: configured to not do anything.")
               {:type                     ::no-jaksotunnus-created
                :configured-to-call-arvo? configured-to-call-arvo?})))))

(defn delete-jaksotunnus
  [tunnus]
  {:pre [(some? tunnus)]}
  (call! :delete (str "/tyoelamapalaute/v1/vastaajatunnus/" tunnus) {}))
