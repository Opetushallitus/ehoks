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
    {removed :removed optionals :optionals, :or
     {removed [] optionals []}}]
     (st/merge
       (describe description)
       (as-> schema x
         (apply st/dissoc x removed)
         (st/optional-keys x optionals))))
  ([schema description] (modify schema description {})))
