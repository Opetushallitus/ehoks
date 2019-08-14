(ns oph.ehoks.external.http-client
  (:refer-clojure :exclude [get])
  (:require [clj-http.client :as client]))

(def ^:private client-functions
  (atom {:get client/get
         :post client/post}))

(defn reset-functions! []
  (reset! client-functions {:get client/get
                            :post client/post}))

(defn get [url options]
  (or ((:get @client-functions) url options) (client/get url options)))

(defn set-get! [f]
  (swap! client-functions assoc :get f))

(defn post [url options]
  (or ((:post @client-functions) url options) (client/post url options)))

(defn set-post! [f]
  (swap! client-functions assoc :post f))

(defmacro with-mock-responses [[get-response post-response] & body]
  `(do
     (set-get! ~get-response)
     (set-post! ~post-response)
     (do ~@body)
     (reset-functions!)))