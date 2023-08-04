(ns oph.ehoks.schema.generator
  (:require [schema.core :as s]
            [clojure.walk]
            [ring.swagger.json-schema :as rsjs]))

(def method-fallbacks
  {:post-virkailija :post,
   :put-virkailija :put,
   :patch-virkailija :patch})

(defn get-access
  "Get access type for method"
  [v method]
  (or (get-in v [:methods method])
      (get-in v [:methods (method-fallbacks method)])
      (get-in v [:methods :any])
      :required))

(defn get-type
  "Get value type for method"
  [v method]
  (or (get-in v [:types method])
      (get-in v [:types (method-fallbacks method)])
      (get-in v [:types :any])))

(s/defschema
  ModelValue
  "Model value schema"
  {:methods {(s/optional-key :any) s/Keyword
             (s/optional-key :get) s/Keyword
             (s/optional-key :post) s/Keyword
             (s/optional-key :put) s/Keyword
             (s/optional-key :patch) s/Keyword
             (s/optional-key :post-virkailija) s/Keyword
             (s/optional-key :put-virkailija) s/Keyword
             (s/optional-key :patch-virkailija) s/Keyword}
   :types {(s/optional-key :any) s/Any
           (s/optional-key :get) s/Any
           (s/optional-key :post) s/Any
           (s/optional-key :put) s/Any
           (s/optional-key :patch) s/Any
           (s/optional-key :post-virkailija) s/Any
           (s/optional-key :put-virkailija) s/Any
           (s/optional-key :patch-virkailija) s/Any}
   :description s/Str})

(defn schema-template->schema
  "Generate schema for given HTTP method from template m.  If m is not
  a template, just return it as is."
  [m method]
  (if-not (= ::schema-template (:type (meta m)))
    m
    (with-meta
      (reduce
        (fn [c [k v]]
          (s/validate ModelValue v)
          (let [value-type (get-type v method)
                access-type (get-access v method)]
            (assert
              (and (some? access-type) (some? value-type))
              (format
                "Value type definition is missing for field %s for %s in %s"
                k method (or (:name (meta m)) m)))
            (case access-type
              :excluded c
              :optional (assoc c (s/optional-key k)
                               (rsjs/describe value-type (:description v)))
              :required (assoc c k
                               (rsjs/describe value-type (:description v))))))
        {}
        m)
      {:name (str (:name (meta m)) "-" (name method))
       :doc (:doc (meta m))})))

(defn generate
  "Walk a schema for any templates within, and turn those templates into
  concrete schemata, creating a real Clojure/JSON schema."
  [schema-with-templates specialisation-choice]
  (clojure.walk/prewalk
    (fn [subschema] (schema-template->schema subschema specialisation-choice))
    schema-with-templates))
