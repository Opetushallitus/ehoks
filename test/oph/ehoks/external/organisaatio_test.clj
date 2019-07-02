(ns oph.ehoks.external.organisaatio-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.external.connection :as c]
            [oph.ehoks.external.organisaatio :as o]))

(defn mocked-with-api-headers [{url :url options :options}]
  {:status 200
   :url url
   :query-params (:query-params options)})

(deftest fetch-for-organisaatiot-is-cached
  (testing "Query with multiple oids is cached"
    (with-redefs [c/with-api-headers
                  mocked-with-api-headers]
      (let [oids ["100" "200" "300"]
            stored-to-cache-response
            (o/try-to-get-organisaatiot-from-cache oids)
            fetched-from-cache
            (o/try-to-get-organisaatiot-from-cache oids)]
        (is (= (:cached stored-to-cache-response) :MISS))
        (is (= (:cached fetched-from-cache) :HIT))))))

(deftest multiple-fetches-for-organisaatiot-are-cached
  (testing "Multiple query with multiple oids are cached"
    (with-redefs [c/with-api-headers
                  mocked-with-api-headers]
      (let [first-group-of-oids ["100" "200" "300"]
            second-group-of-oids ["111" "222" "333"]
            first-group-stored-to-cache-response
            (o/try-to-get-organisaatiot-from-cache first-group-of-oids)
            second-group-stored-to-cache-response
            (o/try-to-get-organisaatiot-from-cache second-group-of-oids)
            first-group-fetched-from-cache
            (o/try-to-get-organisaatiot-from-cache first-group-of-oids)
            second-group-fetched-from-cache
            (o/try-to-get-organisaatiot-from-cache second-group-of-oids)]
        (is (= (:cached first-group-stored-to-cache-response) :MISS))
        (is (= (:cached second-group-stored-to-cache-response) :MISS))
        (is (= (:cached first-group-fetched-from-cache) :HIT))
        (is (= (:cached second-group-fetched-from-cache) :HIT))
        (is (= (get-in first-group-stored-to-cache-response
                       [:query-params :oids])
               first-group-of-oids))
        (is (= (get-in second-group-stored-to-cache-response
                       [:query-params :oids])
               second-group-of-oids))
        (is (= (get-in first-group-fetched-from-cache
                       [:query-params :oids])
               first-group-of-oids))
        (is (= (get-in second-group-fetched-from-cache
                       [:query-params :oids])
               second-group-of-oids))))))
