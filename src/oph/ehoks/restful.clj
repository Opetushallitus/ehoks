(ns oph.ehoks.restful
  (:require [ring.util.http-response :refer [ok]]))

(defn response [body & meta]
  (let [meta-map (apply hash-map meta)]
    {:meta (get :meta meta-map {})
     :data body}))

(defn rest-ok [body & data]
  (ok (apply response body data)))
