(ns oph.ehoks.schema-tools
  (:require [ring.swagger.json-schema :as rsjs]
            [schema-tools.core :as st]))

(defn describe
  "Describe schema and its keys. Adds description to all keys. Requires
   Key Value Description triples."
  [description & kvds]
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
  "Modifies schema. Removes removed, sets optionals, replaces values and adds
   added. Returns new schema."
  ([schema
    description
    {removed :removed optionals :optionals replaced-in :replaced-in
     added :added,
     :or {removed [] optionals [] replaced-in {} added {}}, :as options}]
    (assert
      (empty? (dissoc options :removed :optionals :replaced-in :added))
      (format
        "Only keys :removed, :optionals, :replaced, and :added is allowed.
         Got %s"
        (keys options)))
    (st/merge
      (describe description)
      (as-> schema x
        (apply st/dissoc x removed)
        (reduce (fn [c [ks v]] (st/assoc-in c ks v)) x replaced-in)
        (st/optional-keys x optionals))
      added))
  ([schema description] (modify schema description {})))
