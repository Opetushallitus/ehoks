(ns oph.ehoks.oppija.oppija-external-test
  (:require [clojure.test :as t]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.oppija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [oph.ehoks.session-store :refer [test-session-store]]))

(def base-url "/ehoks-oppija-backend/api/v1/oppija/external")

(defn- mock-authenticated [request]
  (let [store (atom {})
        app (common-api/create-app
              handler/app-routes (test-session-store store))]
    (utils/with-authenticated-oid
      store
      "1.2.246.562.24.12312312312"
      app
      request)))

(t/deftest koodisto-response-handling-test
  (t/testing "Querying non-existing koodisto values returns not found"
    (client/set-get!
      (fn [^String url _]
        (cond
          (.endsWith
            url "/rest/codeelement/latest/ammatillisenoppiaineet_VVTL")
          {:status 500
           :body "error.codeelement.not.found"})))
    (let [response (mock-authenticated
                     (mock/request
                       :get
                       (format "%s/%s/%s"
                               base-url
                               "koodisto"
                               "ammatillisenoppiaineet_VVTL")))
          body (utils/parse-body (:body response))]
      (t/is (= 200 (:status response)))
      (t/is (= "error.codeelement.not.found" (:data body))))))
