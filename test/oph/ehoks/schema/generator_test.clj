(ns oph.ehoks.schema.generator-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.schema.generator :as g]
            [schema.core :as s]
            [oph.ehoks.utils :refer [eq]]))

(def example
  {:name "Hello"
   :description "Some description"
   :schema {:id {:methods {:any :required
                           :post :excluded}
                 :description "Hello ID"
                 :types {:any s/Int}}
            :first {:methods {:any :optional
                              :post :required}
                    :description "Example value"
                    :types {:any s/Str}}}})

(deftest generate-simple-schema
  (testing "Generating simple schema"
    (eq {(s/optional-key :first) s/Str
            :id s/Int}
           (g/generate example :get))))
