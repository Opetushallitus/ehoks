(ns oph.ehoks.db.dynamodb
  (:require [taoensso.faraday :as far])
  (:import (com.amazonaws.auth BasicAWSCredentials AWSStaticCredentialsProvider)
           (com.amazonaws.client.builder AwsClientBuilder$EndpointConfiguration)
           (com.amazonaws.services.dynamodbv2 AmazonDynamoDBClientBuilder)))

(defn get-test-client []
  (let [^AmazonDynamoDBClientBuilder builder
        (doto (AmazonDynamoDBClientBuilder/standard)
          (.setEndpointConfiguration
            (AwsClientBuilder$EndpointConfiguration.
              "http://localhost:18000" "eu-west-1"))
          (.setCredentials
            (AWSStaticCredentialsProvider.
              (BasicAWSCredentials. "foo" "bar"))))]
    (.build builder)))

(def localtesting-opts
  {:client (get-test-client)})

(defn test-faraday [] (far/list-tables localtesting-opts))
