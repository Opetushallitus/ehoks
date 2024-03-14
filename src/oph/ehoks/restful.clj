(ns oph.ehoks.restful
  (:require [ring.util.http-response :as http-response]))

(defn response
  "Create RESTful response"
  [body & meta-data]
  (let [meta-map (apply hash-map meta-data)]
    {:meta (or meta-map {})
     :data body}))

(defn ok
  "Create RESTful OK (200) response"
  [body & meta-data]
  (http-response/ok (apply response body meta-data)))

(defmacro with-not-found-handling
  "Macro for handling automatically not found exception from external service.
  When occurring one macro returns http not found response."
  [& body]
  `(try
     (ok (do ~@body))
     (catch Exception e#
       (if (= (:status (ex-data e#)) 404)
         (http-response/not-found)
         (throw e#)))))
