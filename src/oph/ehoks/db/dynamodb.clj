(ns oph.ehoks.db.dynamodb
  (:require [taoensso.faraday :as far]
            [environ.core :refer [env]]
            [oph.ehoks.config :refer [config]])
  (:import (com.amazonaws.auth BasicAWSCredentials AWSStaticCredentialsProvider)
           (com.amazonaws.client.builder AwsClientBuilder$EndpointConfiguration)
           (com.amazonaws.services.dynamodbv2 AmazonDynamoDBClientBuilder)))

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
(def amis-table (delay (keyword (:heratepalvelu-amis-table config))))
(def jakso-table (delay (keyword (:heratepalvelu-jakso-table config))))
(def nippu-table (delay (keyword (:heratepalvelu-nippu-table config))))
