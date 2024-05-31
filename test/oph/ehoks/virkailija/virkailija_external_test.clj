(ns oph.ehoks.virkailija.virkailija-external-test
  (:require [clojure.test :as t]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.test-utils :as test-utils]
            [oph.ehoks.virkailija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [oph.ehoks.session-store :refer [test-session-store]]))

(def base-url "/ehoks-virkailija-backend/api/v1/virkailija/external")

(defn with-test-virkailija
  ([request]
    (client/with-mock-responses
      [(fn [^String url _]
         (cond
           (.endsWith
             url "/rest/codeelement/latest/ammatillisenoppiaineet_VVTL")
           {:status 500
            :body "error.codeelement.not.found"}))]
      (let [session "12345678-1234-1234-1234-1234567890ab"
            cookie (str "ring-session=" session)
            store (atom
                    {session
                     {:virkailija-user {:name "Test"
                                        :kayttajaTyyppi "VIRKAILIJA"
                                        :organisation-privileges
                                        [{:oid "1.2.246.562.10.12000000005"
                                          :privileges #{:read}}]}}})
            app (common-api/create-app
                  handler/app-routes (test-session-store store))]
        (app (-> request
                 (mock/header :cookie cookie)
                 (mock/header "Caller-Id" "test")))))))

(t/deftest koodisto-response-handling-test
  (t/testing "Querying non-existing koodisto values returns not found"
    (let [response (with-test-virkailija
                     (mock/request
                       :get
                       (format "%s/%s/%s"
                               base-url
                               "koodisto"
                               "ammatillisenoppiaineet_VVTL")))
          body (test-utils/parse-body (:body response))]
      (t/is (= 200 (:status response)))
      (t/is (= "error.codeelement.not.found" (:data body))))))
