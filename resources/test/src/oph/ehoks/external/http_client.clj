(ns oph.ehoks.external.http-client
  (:refer-clojure :exclude [get])
  (:require [clj-http.client :as client]))

(def ^:private client-functions
  (atom {:delete client/delete
         :get client/get
         :post client/post}))

(defn get-client-functions [] @client-functions)

(defn reset-functions! []
  (reset! client-functions {:delete client/delete
                            :get client/get
                            :post client/post}))

(defn restore-functions! [fns]
  (reset! client-functions fns))

(defn delete [url options]
  (or ((:delete @client-functions) url options) (client/delete url options)))

(defn set-delete! [f]
  (swap! client-functions assoc :delete f))

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
     (let [fns# (get-client-functions)]
       (set-get! ~get-response)
       (set-post! ~post-response)
       (let [result# (do ~@body)]
         (restore-functions! fns#)
         result#))))
