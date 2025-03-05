(ns oph.ehoks.db.dynamodb
  (:require [clojure.set :refer [rename-keys]]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.db :as db]
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

(defn jaksot [] (far/scan @faraday-opts @(tables :jakso) {}))
(defn niput  [] (far/scan @faraday-opts @(tables :nippu) {}))

(defn get-item!
  "A wrapper for `taoensso.faraday/get-item`."
  [table prim-kvs]
  (far/get-item @faraday-opts @(tables table) prim-kvs))

(defn delete-item!
  "A wrapper for `taoensso.faraday/delete-item`."
  [table prim-kvs]
  (far/delete-item @faraday-opts @(tables table) prim-kvs))

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

(defn sync-amis-herate!
  "Put information for single opiskelijapalaute to herätepalvelu DDB."
  [kyselyrecord]
  (if-not (contains? (set (:heratepalvelu-responsibities config))
                     :sync-amis-heratteet)
    (log/info "sync-amis-herate!: configured to not write to DDB.")
    (sync-item! :amis kyselyrecord)))
