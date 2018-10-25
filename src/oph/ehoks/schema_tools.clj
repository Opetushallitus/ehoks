(ns oph.ehoks.schema-tools
  (:require [ring.swagger.json-schema :as rsjs]))

(defn describe [description & kvds]
  {:pre [(zero? (mod (count kvds) 3))]}
  (rsjs/field
    (reduce
      (fn [c [k v d]]
        (assoc c k (rsjs/describe v d)))
      {}
      (partition 3 kvds))
    {:description description}))
