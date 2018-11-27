(ns oph.ehoks.external.http-client
  (:refer-clojure :exclude [get])
  (:require [clj-http.client :as client]
            [oph.ehoks.config :refer [config]]))

(def ^:private client-functions
  (atom {:get client/get
         :post client/post}))

(defn get [url options]
  ((:get @client-functions) url options))

(defn set-get! [f]
  (when-not (:allow-mock-http? config)
    (throw (Exception. "Mocking HTTP is not allowed")))
  (swap! client-functions assoc :get f))

(defn post [url options]
  ((:post @client-functions) url options))

(defn set-post! [f]
  (when-not (:allow-mock-http? config)
    (throw (Exception. "Mocking HTTP is not allowed")))
  (swap! client-functions assoc :post f))
