(ns oph.ehoks.restful
  (:require [ring.util.http-response :refer [ok]]))

(defn response [body & meta-data]
  (let [meta-map (apply hash-map meta-data)]
    {:meta (or meta-map {})
     :data body}))

(defn rest-ok [body & meta-data]
  (ok (apply response body meta-data)))
