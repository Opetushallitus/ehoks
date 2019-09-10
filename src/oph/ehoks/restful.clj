(ns oph.ehoks.restful
  (:require [ring.util.http-response :refer [ok not-found]]))

(defn response [body & meta-data]
  (let [meta-map (apply hash-map meta-data)]
    {:meta (or meta-map {})
     :data body}))

(defn rest-ok [body & meta-data]
  (ok (apply response body meta-data)))

(defmacro with-not-found-handling [& body]
  `(try
     (rest-ok (do ~@body))
     (catch Exception e#
       (if (= (:status (ex-data e#)) 404)
         (not-found)
         (throw e#)))))