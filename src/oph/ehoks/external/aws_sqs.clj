(ns oph.ehoks.external.aws-sqs
  (:require [clojure.data.json :as json]
            [oph.ehoks.config :refer [config]]
            [environ.core :refer [env]]
            [clojure.tools.logging :as log])
  (:import (software.amazon.awssdk.services.sqs SqsClient)
           (software.amazon.awssdk.regions Region)
           (software.amazon.awssdk.services.sqs.model
             SendMessageRequest
             GetQueueUrlRequest
             QueueDoesNotExistException)))

(def ^:private sqs-client
  "Globaali SQS-client."
  (when (:send-herate-messages? config)
    (-> (SqsClient/builder)
        (.region (Region/EU_WEST_1))
        (.build))))

(defn- get-queue-url
  "Hakee SQS-queuen URL:n sen nimen perusteella."
  [queue-name]
  (when (some? sqs-client)
    (if (nil? (:env-stage env))
      (log/warn "Stage missing from env variables")
      (.queueUrl (.getQueueUrl
                   sqs-client
                   (-> (GetQueueUrlRequest/builder)
                       (.queueName
                         (str (:env-stage env) "-"
                              queue-name))
                       (.build)))))))

(defn- get-queue-url-with-error-handling
  "Hakee SQS-queuen URL:n sen nimen perusteella, ja käsittelee virheitä."
  [queue-name]
  (try
    (get-queue-url queue-name)
    (catch QueueDoesNotExistException _
      (log/error (str queue-name " does not exist")))))

(def ^:private herate-queue-url
  "Globaali heräte queue -URL."
  (get-queue-url (:heratepalvelu-queue config)))

(def ^:private delete-tunnus-queue-url
  "Globaali tunnuksen poisto queue -URL."
  (get-queue-url-with-error-handling
    (:heratepalvelu-delete-tunnus-queue config)))

(def ^:private tyoelamapalaute-queue-url
  "Globaali työelämäpalaute queue -URL."
  (get-queue-url-with-error-handling
    (:heratepalvelu-tyoelamapalaute-queue config)))

(def ^:private resend-queue-url
  "Globaali uudelleenlähetys queue -URL."
  (get-queue-url-with-error-handling
    (:heratepalvelu-resend-queue config)))

(defn build-hoks-hyvaksytty-msg
  "Luo HOKS hyväksytty -viestin."
  [hoks]
  {:pre [(:id hoks)]}
  {:ehoks-id (:id hoks)
   :kyselytyyppi "aloittaneet"
   :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
   :oppija-oid (:oppija-oid hoks)
   :sahkoposti (:sahkoposti hoks)
   :puhelinnumero (:puhelinnumero hoks)
   :alkupvm (str (:ensikertainen-hyvaksyminen hoks))})

(defn build-hoks-osaaminen-saavutettu-msg
  "Luo HOKS osaamisen saavutettu -viestin."
  [hoks kyselytyyppi]
  {:pre [(:id hoks) (:osaamisen-saavuttamisen-pvm hoks)]}
  {:ehoks-id (:id hoks)
   :kyselytyyppi kyselytyyppi
   :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)
   :oppija-oid (:oppija-oid hoks)
   :sahkoposti (:sahkoposti hoks)
   :puhelinnumero (:puhelinnumero hoks)
   :alkupvm (str (:osaamisen-saavuttamisen-pvm hoks))})

(defn build-tyoelamapalaute-msg
  "Luo työelämäpalauteviestin."
  [msg]
  {:tyyppi (:tyyppi msg)
   :alkupvm (str (:alkupvm msg))
   :loppupvm (str (:loppupvm msg))
   :hoks-id (:hoks_id msg)
   :opiskeluoikeus-oid (:opiskeluoikeus_oid msg)
   :oppija-oid (:oppija_oid msg)
   :hankkimistapa-id (:hankkimistapa_id msg)
   :hankkimistapa-tyyppi (:hankkimistapa_tyyppi msg)
   :tutkinnonosa-id (:tutkinnonosa_id msg)
   :tutkinnonosa-koodi (:tutkinnonosa_koodi msg)
   :tutkinnonosa-nimi (:tutkinnonosa_nimi msg)
   :tyopaikan-nimi (:tyopaikan_nimi msg)
   :tyopaikan-ytunnus (:tyopaikan_ytunnus msg)
   :tyopaikkaohjaaja-email (:tyopaikkaohjaaja_email msg)
   :tyopaikkaohjaaja-puhelinnumero (:tyopaikkaohjaaja_puhelinnumero msg)
   :tyopaikkaohjaaja-nimi (:tyopaikkaohjaaja_nimi msg)
   :oppisopimuksen-perusta (:oppisopimuksen_perusta msg)
   :osa-aikaisuus (:osa_aikaisuus msg)
   :keskeytymisajanjaksot
   (map
     #(let [k {:alku (str (:alku %))}]
        (if (:loppu %) (assoc k :loppu (str (:loppu %))) k))
     (:keskeytymisajanjaksot msg))})

(defn send-message
  "Lähettää viestin SQS-queueen."
  [msg queue-url]
  (let [resp (.sendMessage sqs-client (-> (SendMessageRequest/builder)
                                          (.queueUrl queue-url)
                                          (.messageBody (json/write-str msg))
                                          (.build)))]
    (when-not (some? (.messageId resp))
      (log/error "Failed to send message " msg)
      (throw (ex-info
               "Failed to send SQS message"
               {:error :sqs-error})))))

(defn send-amis-palaute-message
  "Lähettää AMIS-palauteviestin."
  [msg]
  (if (some? herate-queue-url)
    (do
      (log/infof (str "Sending AMIS-kysely for hoks id %s. "
                      "Type: %s. "
                      "Opiskeluoikeus: %s. "
                      "Oppija: %s. "
                      "Alkupvm: %s.")
                 (:ehoks-id msg)
                 (:kyselytyyppi msg)
                 (:opiskeluoikeus-oid msg)
                 (:oppija-oid msg)
                 (:alkupvm msg))
      (send-message msg herate-queue-url))
    (log/error "No AMIS-palaute queue!")))

(defn send-delete-tunnus-message
  "Lähettää tunnuksen poistoviestin."
  [kyselylinkki]
  (if (some? delete-tunnus-queue-url)
    (send-message {:kyselylinkki kyselylinkki} delete-tunnus-queue-url)
    (log/error "No AMIS delete tunnus queue!")))

(defn send-tyoelamapalaute-message
  "Lähettää työelämäpalauteviestin."
  [msg]
  (if (some? tyoelamapalaute-queue-url)
    (send-message msg tyoelamapalaute-queue-url)
    (log/error "No työelämäpalaute queue!")))

(defn send-palaute-resend-message
  "Lähettää palautteen uudelleenlähetysviestin."
  [msg]
  (if (some? resend-queue-url)
    (send-message msg resend-queue-url)
    (log/error "No resend queue!")))
