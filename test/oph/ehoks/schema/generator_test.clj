(ns oph.ehoks.schema.generator-test
  (:require [clojure.test :refer [deftest testing]]
            [oph.ehoks.schema.generator :as g]
            [schema.core :as s]
            [ring.swagger.json-schema :as rsjs]
            [oph.ehoks.utils :refer [eq]]))

(def example
  ^{:type ::g/schema-template}
  {:id {:methods {:any :required
                  :post :excluded}
        :description "Hello ID"
        :types {:any s/Int}}
   :first {:methods {:any :optional
                     :post :required}
           :description "Example value"
           :types {:any s/Str}}
   :second {:methods {:any :optional
                      :get :required}
            :description "Some other value"
            :types {:any s/Str
                    :get Long}}
   :third {:methods {:any :excluded
                     :get :required}
           :description "Only for GET"
           :types {:any s/Str}}})

(deftest generate-simple-schema
  (testing "Generating simple GET schema"
    (eq {(s/optional-key :first) (rsjs/describe s/Str "Example value")
         :id (rsjs/describe s/Int "Hello ID")
         :second (rsjs/describe Long "Some other value")
         :third (rsjs/describe s/Str "Only for GET")}
        (g/generate example :get)))
  (testing "Generating simple POST schema"
    (eq {:first (rsjs/describe s/Str "Example value")
         (s/optional-key :second) (rsjs/describe s/Str "Some other value")}
        (g/generate example :post))))
