(ns oph.ehoks.oppija.settings-handler-test
  (:require [clojure.test :as t]
            [oph.ehoks.oppija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [oph.ehoks.test-utils :as test-utils]))

(t/use-fixtures :once test-utils/migrate-database)
(t/use-fixtures :each test-utils/empty-database-after-test)

(def base-url "/ehoks-oppija-backend/api/v1/oppija/session/settings")

(t/deftest user-settings
  (t/testing "GET current session user settings"
    (let [app (common-api/create-app handler/app-routes nil)
          auth-response (test-utils/authenticate app)
          session-cookie (first (get-in auth-response [:headers "Set-Cookie"]))
          post-response (app (-> (mock/request
                                   :put
                                   base-url)
                                 (mock/header :cookie session-cookie)
                                 (mock/header "Caller-Id" "test")
                                 (mock/json-body {})))
          get-response (app (-> (mock/request
                                  :get
                                  base-url)
                                (mock/header :cookie session-cookie)
                                (mock/header "Caller-Id" "test")))
          body (test-utils/parse-body (:body get-response))]
      (t/is (= (:status post-response) 201))
      (t/is (= (:status get-response) 200))
      (t/is (= (:data body) {})))))
