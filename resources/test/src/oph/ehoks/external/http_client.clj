(ns oph.ehoks.external.http-client
  (:refer-clojure :exclude [get])
  (:require [clj-http.client :as client]))

(def original-client-functions
  {:delete client/delete
   :get client/get
   :post client/post
   :patch client/patch})

(def ^:private client-functions
  (atom original-client-functions))

(defn get-client-functions [] @client-functions)
(defn restore-functions! [fns] (reset! client-functions fns))
(defn reset-functions! [] (restore-functions! original-client-functions))

(defn delete [url options]
  (or ((:delete @client-functions) url options) (client/delete url options)))

(defn set-delete! [f] (swap! client-functions assoc :delete f))

(defn get [url options]
  (or ((:get @client-functions) url options) (client/get url options)))

(defn set-get! [f] (swap! client-functions assoc :get f))

(defn post [url options]
  (or ((:post @client-functions) url options) (client/post url options)))

(defn set-post! [f] (swap! client-functions assoc :post f))

(defn patch [url options]
  (or ((:patch @client-functions) url options) (client/patch url options)))

(defn set-patch! [f] (swap! client-functions assoc :patch f))

(defn with-mock-responses*
  "set handlers of GET, POST (and PATCH) requests temporarily to given
  functions for the duration of callback"
  [get-response post-response patch-response callback]
  (let [old-handlers (get-client-functions)]
    (set-get! get-response)
    (set-post! post-response)
    (when patch-response (set-patch! patch-response))
    (let [result (callback)]
      (restore-functions! old-handlers)
      result)))

(defmacro with-mock-responses
  "set handlers of GET, POST (and PATCH) requests temporarily to given
  functions for the duration of body"
  [[get-response post-response patch-response] & body]
  `(with-mock-responses* ~get-response ~post-response ~patch-response
     (fn [] ~@body)))
