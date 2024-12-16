(ns oph.ehoks.db.dynamodb
  (:require [clojure.set :refer [rename-keys]]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.db :as db]
            [oph.ehoks.palaute :refer
             [get-for-heratepalvelu-by-hoks-id-and-kyselytyypit!]]
            [oph.ehoks.palaute.tapahtuma :as tapahtuma]
            [oph.ehoks.utils :as utils]
            [taoensso.faraday :as far])
  (:import (com.amazonaws.auth AWSStaticCredentialsProvider
                               BasicSessionCredentials)
           (com.amazonaws.client.builder AwsClientBuilder$EndpointConfiguration)
           (com.amazonaws.services.dynamodbv2 AmazonDynamoDBClientBuilder)
           (com.amazonaws.services.dynamodbv2.model AttributeValue)))

(extend-protocol far/ISerializable
  java.time.LocalDate (serialize [date]
                        ^AttributeValue (far/serialize (str date))))

(defn get-test-client
  "Local DynamoDB needs a region even when the endpoint has been
  overridden, because DynamoDB identifies tables by region and account
  (different regions may have tables with the same name).  Thus we
  construct a client that uses AWS_REGION for signing API requests even
  when the API endpoint is in localhost.

  The correct parameters for connecting to the DynamoDB in
  ../../../../Makefile are:
  - AWS_REGION=eu-west-1
  - AWS_ENDPOINT_URL=http://localhost:18000
  - AWS_ACCESS_KEY_ID=foo
  - AWS_SECRET_KEY=bar"
  []
  (let [^AmazonDynamoDBClientBuilder builder
        (doto (AmazonDynamoDBClientBuilder/standard)
          (.setCredentials
            (AWSStaticCredentialsProvider.
              (BasicSessionCredentials. "foo" "bar" "baz")))
          (.setEndpointConfiguration
            (AwsClientBuilder$EndpointConfiguration.
              (env :aws-endpoint-url) (env :aws-region))))]
    (.build builder)))

(defn get-client
  "Return a DynamoDB client, based on environment variables, usable for
  either local testing or real DynamoDB."
  []
  (if (and (env :aws-endpoint-url) (env :aws-region))
    (get-test-client)
    (.build (AmazonDynamoDBClientBuilder/standard))))

(def faraday-opts (delay {:client (get-client)}))

(def tables
  {:amis (delay (keyword (:heratepalvelu-amis-table config)))
   :jakso (delay (keyword (:heratepalvelu-jakso-table config)))
   :nippu (delay (keyword (:heratepalvelu-nippu-table config)))})

(def table-keys
  {:amis [:toimija_oppija :tyyppi_kausi]
   :jakso [:hankkimistapa_id]
   :nippu [:ohjaaja_ytunnus_kj_tutkinto :niputuspvm]})

(defn get-item!
  "A wrapper for `taoensso.faraday/get-item`."
  [table prim-kvs]
  (far/get-item @faraday-opts @(tables table) prim-kvs))

(defn sync-item!
  "Does a partial upsert on item in DDB: if the item doesn't exist,
  it is created with the given values.  If it does exist, then only the
  given fields are updated, with the rest left intact.  sync-item! is a
  wrapper for faraday/update-item."
  [table item]
  (let [table-name @(tables table)
        key-names (table-keys table)
        item-key (zipmap key-names
                         (map #(or (item %)
                                   (throw (ex-info "item key missing"
                                                   {:key-name % :item item})))
                              key-names))
        rest-item (apply dissoc item key-names)
        attr-names (zipmap (map (partial str "#") (range))
                           (map name (keys rest-item)))
        attr-values (zipmap (map (partial str ":") (range)) (vals rest-item))
        updates (map #(str %1 " = " %2)
                     (sort (keys attr-names))
                     (sort (keys attr-values)))
        update-expr (str "SET " (str/join ", " updates))]
    (far/update-item @faraday-opts table-name item-key
                     {:update-expr update-expr :expr-attr-names attr-names
                      :expr-attr-vals attr-values})))

(def map-keys-to-ddb
  (some-fn {:toimija-oppija :toimija_oppija, :tyyppi-kausi :tyyppi_kausi}
           identity))

(defn sync-amis-herate!
  "Update the herätepalvelu AMISheratetable to have the same content
  for given heräte as palaute-backend has in its own database.
  sync-amis-herate! only updates fields it 'owns': currently that
  means that the messaging tracking fields are left intact (because
  herätepalvelu will update those)."
  [{:keys [existing-palaute] :as ctx}]
  (if-not (contains? (set (:heratepalvelu-responsibities config))
                     :sync-amis-heratteet)
    (log/warn "sync-amis-herate!: configured to not do anything")
    (let [hoks-id      (:hoks-id existing-palaute)
          kyselytyyppi (:kyselytyyppi existing-palaute)
          palautteet (get-for-heratepalvelu-by-hoks-id-and-kyselytyypit!
                       db/spec {:hoks-id hoks-id :kyselytyypit [kyselytyyppi]})
          palaute (-> (not-empty palautteet)
                      (or (throw (ex-info "palaute not found"
                                          {:hoks-id      hoks-id
                                           :kyselytyyppi kyselytyyppi})))
                      (first))]
      (try (-> palaute
               (utils/remove-nils)
               (update-keys map-keys-to-ddb)
               (dissoc :internal-kyselytyyppi)
               (->> (sync-item! :amis)))
           (catch Exception e
             (log/error e "while processing palaute" palaute)
             (tapahtuma/build-and-insert!
               (assoc ctx :tapahtumatyyppi :heratepalvelu-sync)
               :synkronointi-epaonnistui
               {:errormsg (.getMessage e) :body (:body (ex-data e))})
             (throw e))))))

(def map-jakso-keys-to-ddb
  (some-fn {:hankkimistapa-id :hankkimistapa_id,
            :osaamisen-hankkimistapa-koodi-uri :hankkimistapa_tyyppi,
            :hoks-id :hoks_id,
            :jakso-alkupvm :jakso_alkupvm,
            :jakso-loppupvm :jakso_loppupvm,
            :ohjaaja-email :ohjaaja_email,
            :ohjaaja-nimi :ohjaaja_nimi,
            :ohjaaja-puhelinnumero :ohjaaja_puhelinnumero,
            :ohjaaja-ytunnus-kj-tutkinto :ohjaaja_ytunnus_kj_tutkinto,
            :opiskeluoikeus-oid :opiskeluoikeus_oid,
            :oppija-oid :oppija_oid,
            :oppisopimuksen-perusta-koodi-uri :oppisopimuksen_perusta,
            :osa-aikaisuustieto :osa_aikaisuus,
            :request-id :request_id,
            :toimipiste-oid :toimipiste_oid,
            :tutkinnonosa-id :tutkinnonosa_id,
            :tutkinnon-osa-koodi-uri :tutkinnonosa_koodi,
            :tutkinnonosa-nimi :tutkinnonosa_nimi,
            :tutkinnonosa-tyyppi :tutkinnonosa_tyyppi,
            :tyopaikan-nimi :tyopaikan_nimi,
            :tyopaikan-normalisoitu-nimi :tyopaikan_normalisoitu_nimi,
            :tyopaikan-y-tunnus :tyopaikan_ytunnus,
            :vastuullinen-tyopaikka-ohjaaja-nimi :ohjaaja_nimi,
            :vastuullinen-tyopaikka-ohjaaja-sahkoposti :ohjaaja_email,
            :vastuullinen-tyopaikka-ohjaaja-puhelinnumero
            :ohjaaja_puhelinnumero,
            :viimeinen-vastauspvm :viimeinen_vastauspvm}
           identity))

(defn sync-jakso-herate!
  "Update the herätepalvelu jaksotunnustable to have the same content
  for given heräte as palaute-backend has in its own database.
  sync-jakso-herate! only updates fields it 'owns': currently that
  means that the messaging tracking fields are left intact (because
  herätepalvelu will update those)."
  [tep-palaute]
  (if-not (contains? (set (:heratepalvelu-responsibities config))
                     :sync-jakso-heratteet)
    (log/warn "sync-jakso-herate!: configured to not do anything")
    (-> tep-palaute
        utils/to-underscore-keys
        ;; the only field that has dashes in its name is tpk-niputuspvm
        (rename-keys {:tpk_niputuspvm :tpk-niputuspvm})
        (->> (sync-item! :jakso)))))

(defn sync-tpo-nippu-herate!
  "Update the Herätepalvelu nipputable to have the same content for given heräte
  as palaute-backend has in its own database."
  [nippu]
  (if-not (contains? (set (:heratepalvelu-responsibities config))
                     :sync-jakso-heratteet)
    (log/warn "sync-tpo-nippu-herate!: configured to not do anything")
    (if-let [existing-nippu (get-item! :nippu
                                       (select-keys
                                         nippu [:ohjaaja_ytunnus_kj_tutkinto
                                                :niputuspvm]))]
      (log/info "Tietokannassa on jo nippu: " existing-nippu)
      (sync-item! :nippu nippu))))
