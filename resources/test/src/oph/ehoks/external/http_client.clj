(ns oph.ehoks.external.http-client
  (:refer-clojure :exclude [get])
  (:require [clj-http.client :as client]
            [oph.ehoks.config :refer [config]]))

(def ^:private client-functions
  (atom {:get client/get
         :post client/post}))

(defn reset-functions! []
  (reset! client-functions {:get client/get
                            :post client/post}))

(defn get [url options]
  ((:get @client-functions) url options))

(defn set-get! [f]
  (swap! client-functions assoc :get f))

(defn post [url options]
  ((:post @client-functions) url options))

(defn set-post! [f]
  (swap! client-functions assoc :post f))
