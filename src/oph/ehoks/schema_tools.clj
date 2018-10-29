(ns oph.ehoks.schema-tools
  (:require [ring.swagger.json-schema :as rsjs]))

(defn describe [description & kvds]
  (assert (zero? (mod (count kvds) 3))
          (format "%s: Invalid key-value-descriotion triples: %s"
                  description kvds))
  (rsjs/field
    (reduce
      (fn [c [k v d]]
        (assoc c k (rsjs/describe v d)))
      {}
      (partition 3 kvds))
    {:description description}))
