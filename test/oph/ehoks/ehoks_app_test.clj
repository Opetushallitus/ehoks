(ns oph.ehoks.ehoks-app-test
  (:require [cheshire.core :as cheshire]
            [clojure.test :refer [deftest is testing]]
            [oph.ehoks.ehoks-app :as ehoks-app]
            [ring.mock.request :as mock]))

(defn- get-not-found [uri]
  (let [response (ehoks-app/all-app-routes
                   (mock/body (mock/request :get uri) ""))]
    (is (= 404 (:status response)))
    (cheshire/parse-string (:body response) true)))

(deftest not-found
  (testing "Unknown top level path suggests the backends."
    (let [body (get-not-found "/olematon")]
      (is (= "Route not found" (:reason body)))
      (is (= ["/ehoks-oppija-backend"
              "/ehoks-palaute-backend"
              "/ehoks-virkailija-backend"]
             (:nearest-routes body)))))
  (testing "Unknown path under a backend suggests that backend's routes."
    (let [body (get-not-found "/ehoks-palaute-backend/api/v1/olematon")]
      (is (= ["/ehoks-palaute-backend/api/v1/healthcheck"
              "/ehoks-palaute-backend/api/v1/opiskelijapalaute"
              "/ehoks-palaute-backend/api/v1/opiskelijapalaute:hoks-id"
              "/ehoks-palaute-backend/api/v1/tyoelamapalaute"]
             (:nearest-routes body))))))
