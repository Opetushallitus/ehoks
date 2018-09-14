(ns oph.ehoks.external.handler-test
    (:require [cheshire.core :as cheshire]
              [clojure.test :refer [deftest testing is]]
              [oph.ehoks.handler :refer [app]]
              [ring.mock.request :as mock]
              [oph.ehoks.utils :refer [parse-body]]
              [stub-http.core :refer :all]))
  
(deftest localization
(testing "GET localizations without parameters"
    (let [response (app (mock/request :get "/ehoks/api/v1/localization"))
        body (parse-body (:body response))
        data (first (:data body))
        category (:category data)]
    (is (= (:status response) 200))
    (is (= category "ehoks"))))

(testing "GET localizations with oti category"
    (let [response (app 
    (mock/request :get "/ehoks/api/v1/localization?category=oti"))
        body (parse-body (:body response))
        data (first (:data body))
        category (:category data)]
    (is (= (:status response) 200))
    (is (= category "oti")))))

(def mock-data [
    {
      "category"  "ehoks",
      "createdBy"  "1.2.246.562.24.31103582397",
      "key"  "testiavain",
      "force"  false,
      "locale"  "fi",
      "value"  "joo",
      "created"  1536311376900,
      "modified"  1536311376900,
      "accessed"  1536311376900,
      "accesscount"  0,
      "id"  21399,
      "modifiedBy"  "1.2.246.562.24.31103582397"
    },
    {
      "category"  "ehoks",
      "createdBy"  "1.2.246.562.24.43953048723",
      "key"  "toinentesti",
      "force"  false,
      "locale"  "fi",
      "value"  "Upeeta",
      "created"  1536836410575,
      "modified"  1536836410575,
      "accessed"  1536836410575,
      "accesscount"  0,
      "id"  21401,
      "modifiedBy"  "1.2.246.562.24.43953048723"
    },
    {
      "category"  "ehoks",
      "createdBy"  "1.2.246.562.24.31103582397",
      "key"  "testiavain",
      "force"  false,
      "locale"  "sv",
      "value"  "bra",
      "created"  1536311415278,
      "modified"  1536311415278,
      "accessed"  1536311415278,
      "accesscount"  0,
      "id"  21400,
      "modifiedBy"  "1.2.246.562.24.31103582397"
    },
    {
      "category"  "ehoks",
      "createdBy"  "1.2.246.562.24.43953048723",
      "key"  "toinentesti",
      "force"  false,
      "locale"  "sv",
      "value"  "Utmärkt",
      "created"  1536836478213,
      "modified"  1536836478213,
      "accessed"  1536836478213,
      "accesscount"  0,
      "id"  21402,
      "modifiedBy"  "1.2.246.562.24.43953048723"
    }
  ])

(deftest mock-localization
    (with-routes!
        {"/localization" {:status 200 :content-type "application/json"
                            :body mock-data}}
    (testing "GET localizations with parameter"                
        (let [response (app (mock/request :get  
            (str uri  "/ehoks/api/v1/localization?category=ehoks")))
              body (parse-body (:body response))
              data (first (:data body))
              category (:category data)]
              (is (= category "ehoks"))))

    (testing "GET localizations without parameters"                
        (let [response (app (mock/request :get  
            (str uri  "/ehoks/api/v1/localization")))
                body (parse-body (:body response))
                data (first (:data body))
                category (:category data)]
                (is (= category "ehoks"))))))