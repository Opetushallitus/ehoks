(ns oph.ehoks.restful
  (:require [ring.util.http-response :refer [ok not-found]]))

(defn response
  "Create RESTful response"
  [body & meta-data]
  (let [meta-map (apply hash-map meta-data)]
    {:meta (or meta-map {})
     :data body}))

(defn rest-ok
  "Create RESTful OK (200) response"
  [body & meta-data]
  (ok (apply response body meta-data)))

(defmacro with-not-found-handling
  "Macro for handling automatically not found exception from external service.
  When occurring one macro returns http not found response."
  [& body]
  `(try
     (rest-ok (do ~@body))
     (catch Exception e#
       (if (= (:status (ex-data e#)) 404)
         (not-found)
         (throw e#)))))