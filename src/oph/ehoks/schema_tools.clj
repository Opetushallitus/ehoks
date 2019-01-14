(ns oph.ehoks.schema-tools
  (:require [ring.swagger.json-schema :as rsjs]
            [schema.core :as s]
            [schema-tools.core :as st]
            [clojure.set :refer [rename-keys]]))

(defn describe [description & kvds]
  (assert (or (seq kvds) (zero? (mod (count kvds) 3)))
          (format "%s: Invalid key-value-descriotion triples: %s"
                  description kvds))
  (rsjs/field
    (reduce
      (fn [c [k v d]]
        (assoc c k (rsjs/describe v d)))
      {}
      (partition 3 kvds))
    {:description description}))

(defn modify
  ([schema
    description
    {removed :removed optionals :optionals replaced-in :replaced-in,
     :or {removed [] optionals [] replaced-in {}}, :as options}]
    (assert (empty? (dissoc options :removed :optionals :replaced-in))
            (format
              "Only keys :removed, :optionals, and :replaced is allowed. Got %s"
              (keys options)))
    (st/merge
      (describe description)
      (as-> schema x
        (apply st/dissoc x removed)
        (st/optional-keys x optionals)
        (reduce (fn [c [ks v]] (st/assoc-in c ks v)) x replaced-in))))
  ([schema description] (modify schema description {})))
