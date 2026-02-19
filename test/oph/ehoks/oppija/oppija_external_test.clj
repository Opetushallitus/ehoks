(ns oph.ehoks.oppija.oppija-external-test
  (:require [clojure.test :as t]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.test-utils :as test-utils]
            [oph.ehoks.oppija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.external.koski :as koski]
            [ring.mock.request :as mock]
            [oph.ehoks.session-store :refer [test-session-store]])
  (:import (java.net ConnectException)))

(def base-url "/ehoks-oppija-backend/api/v1/oppija/external")
(def base-url-2 "/ehoks-oppija-backend/api/v1/external")

(defn- mock-authenticated [request]
  (let [store (atom {})
        app (common-api/create-app
              handler/app-routes (test-session-store store))]
    (test-utils/with-authenticated-oid
      store
      "1.2.246.562.24.12312312319"
      app
      request)))

(t/deftest koski-oppija-test
  (t/testing "koski/oppija endpoint normal response"
    (with-redefs [koski/get-student-info
                  (fn [_] {:foo 3 :henkilö {:etunimet "Kaarlo" :bar 0}})]
      (let [resp (mock-authenticated
                   (mock/request :get (str base-url-2 "/koski/oppija")))
            body (test-utils/parse-body (:body resp))]
        (t/is (= 200 (:status resp)))
        (t/is (= {:etunimet "Kaarlo"} (get-in body [:data :henkilö]))))))
  (t/testing "koski/oppija endpoint with connection errors"
    (with-redefs [client/get
                  (fn [_ __]
                    (throw (new ConnectException "Ei toimi")))]
      (let [resp (mock-authenticated
                   (mock/request :get (str base-url-2 "/koski/oppija")))
            body (test-utils/parse-body (:body resp))]
        (t/is (= 500 (:status resp)))
        (t/is (re-find #"failure in call to a linked system"
                       (:error body)))))))

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
          body (test-utils/parse-body (:body response))]
      (t/is (= 200 (:status response)))
      (t/is (= "error.codeelement.not.found" (:data body))))))
