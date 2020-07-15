(ns oph.ehoks.oppija.settings-handler-test
  (:require [clojure.test :as t]
            [oph.ehoks.oppija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils]))

(t/use-fixtures :once utils/migrate-database)
(t/use-fixtures :each utils/empty-database-after-test)

(def base-url "/ehoks-oppija-backend/api/v1/oppija/session/settings")

(t/deftest user-settings
  (t/testing "GET current session user settings"
    (let [app (common-api/create-app handler/app-routes nil)
          auth-response (utils/authenticate app)
          session-cookie (first (get-in auth-response [:headers "Set-Cookie"]))
          post-response (app (-> (mock/request
                                   :put
                                   base-url)
                                 (mock/header :cookie session-cookie)
                                 (mock/json-body {})))
          get-response (app (-> (mock/request
                                  :get
                                  base-url)
                                (mock/header :cookie session-cookie)))
          body (utils/parse-body (:body get-response))]
      (t/is (= (:status post-response) 201))
      (t/is (= (:status get-response) 200))
      (t/is (= (:data body) {})))))
