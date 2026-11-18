(ns oph.ehoks.common.api-test
  (:require [clojure.test :refer [deftest is testing]]
            [oph.ehoks.common.api :as common-api]))

(def templates
  ["/api/v1/hoks"
   "/api/v1/hoks/:hoks-id"
   "/api/v1/hoks/:hoks-id/osaamisen-hankkimistavat"
   "/api/v1/hoks/opiskeluoikeus/:oid"
   "/api/v1/external/koodisto/:koodi-uri"
   "/healthcheck"])

(defn- nearest [uri] (common-api/children-of-longest-prefix templates uri))

(deftest test-children-of-longest-prefix
  (testing "Typo in the last segment lists the children of the parent."
    (is (= ["/api/v1/external"
            "/api/v1/hoks"]
           (nearest "/api/v1/hoksi")
           (nearest "/api/v1/hoksi/"))))

  (testing "Typo in the middle lists the children of the last valid prefix."
    (is (= ["/api/v1"]
           (nearest "/api/v2/hoks"))))

  (testing "Path parameter matches any segment."
    (is (= ["/api/v1/hoks/:hoks-id"
            "/api/v1/hoks/:hoks-id/osaamisen-hankkimistavat"]
           (nearest "/api/v1/hoks/123/olematon"))))

  (testing "Existing path lists itself and its children."
    (is (= ["/api/v1/hoks"
            "/api/v1/hoks/:hoks-id"
            "/api/v1/hoks/opiskeluoikeus"]
           (nearest "/api/v1/hoks"))))

  (testing "Unknown first segment lists the top level."
    (is (= ["/api"
            "/healthcheck"]
           (nearest "/olematon")
           (nearest "/")))))

(deftest test-route-not-found-response
  (let [response (common-api/route-not-found-response
                   templates {:uri "/api/v1/hoksi"})]
    (is (= 404 (:status response)))
    (is (= {:reason "Route not found"
            :nearest-routes ["/api/v1/external" "/api/v1/hoks"]}
           (:body response)))))
