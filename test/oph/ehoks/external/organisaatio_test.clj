(ns oph.ehoks.external.organisaatio-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.connection :as c]
            [oph.ehoks.external.organisaatio :as o]))

(def stubbed-with-api-headers (constantly {:status 200
                                           :body {:test "testing"}}))

(deftest fetch-for-organisaatiot-is-cached
  (testing "Query with multiple oids is cached"
    (with-redefs [c/with-api-headers
                  stubbed-with-api-headers]
      (let [oids ["100" "200" "300"]
            stored-to-cache-response
            (o/try-to-get-organisaatiot-from-cache oids)
            fetched-from-cache
            (o/try-to-get-organisaatiot-from-cache oids)]
        (clojure.pprint/pprint stored-to-cache-response)
        (clojure.pprint/pprint fetched-from-cache)
        (is (= (:cached stored-to-cache-response) :MISS))
        (is (= (:cached fetched-from-cache) :HIT))))))
