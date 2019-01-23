(ns oph.ehoks.schema.generator
  (:require [schema.core :as s]))

(defn defgenschema [schema])

(defn get-access [v method]
  (or (get-in v [:methods method]) (get-in v [:methods :any]) :required))

(defn get-type [v method]
  (or (get-in v [:types method]) (get-in v [:types :any])))

(defn generate [m method]
  (reduce
    (fn [c [k v]]
      (let [value-type (get-type v method)
            access-type (get-access v method)]
        (assert
          (and (some? access-type) (some? value-type))
          (format
            "Value type definition is missing for %s with method of %s (%s, %s)"
            k method value-type access-type))
        (case access-type
          :excluded c
          :optional (assoc c (s/optional-key k) value-type)
          :required (assoc c k value-type))))
    {}
    m))
