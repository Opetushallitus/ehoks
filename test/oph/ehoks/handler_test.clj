(ns oph.ehoks.handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.handler :refer [app]]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :refer [with-authentication]]))

(deftest not-found
  (testing "GET route which does not exists"
    (let [response (with-authentication
                     app
                     (mock/request
                       :get "/ehoks/api/v1/non-existing-resource/"))]
      (is (= (:status response) 404)))))

(deftest unatuhenticated
  (testing "GET unauthenticated route"
    (is (thrown-with-msg?
          clojure.lang.ExceptionInfo #"HTTP 401"
          (app (mock/request :get "/some-non-existing/route"))))))
