(ns oph.ehoks.middleware-test
  (:require [clojure.test :refer [deftest testing is]]
            [compojure.api.core :refer [route-middleware]]
            [compojure.api.sweet :as c-api]
            [ring.util.http-response :refer [ok]]
            [ring.mock.request :as mock]
            [oph.ehoks.middleware :as middleware]
            [oph.ehoks.user :as user]
            [ring.middleware.session :as session]
            [clojure.core.async :as async]))

(defn- handle-async [app request]
  (let [c (async/chan)]
    (async/go
      (app
        request
        (fn [r]
          (async/go (async/>! c r)))
        (fn [e]
          (async/go (async/>! c {:exception e})))))
    (let [result (async/<!! c)]
      (when-let [e (:exception result)]
        (throw (:exception result)))
      result)))

(defn- get-cookie [app]
  (-> (mock/request :post "/sync/authenticate")
      (app)
      (get-in [:headers "Set-Cookie"])
      (first)))

(defn- with-authentication-async [app request]
  (let [cookie (get-cookie app)]
    (handle-async app (mock/header request :cookie cookie))))

(defn- with-authentication [app request]
  (let [cookie (get-cookie app)]
    (app (mock/header request :cookie cookie))))

(def async-routes
  (c-api/context "/async" []
    (route-middleware
      [(middleware/wrap-require-user-type-and-auth ::user/oppija)]
      (c-api/GET "/" [] (async/go (ok ""))))
    (c-api/GET "/public" [] (async/go (ok "")))))

(def sync-routes
  (c-api/context "/sync" []
    (route-middleware
      [(middleware/wrap-require-user-type-and-auth ::user/oppija)]
      (c-api/GET "/" [] (ok "")))
    (c-api/GET "/public" [] (ok ""))
    (c-api/POST "/authenticate" [] (assoc (ok "") :session {:user "User"}))))

(def test-app
  (-> (c-api/api async-routes sync-routes)
      (middleware/wrap-cache-control-no-cache)
      (session/wrap-session)))

(deftest private-sync
  (testing "Private sync route"
    (let [response (with-authentication test-app (mock/request :get "/sync"))]
      (is (= (:status response) 200)))))

(deftest public-sync
  (testing "Public sync route"
    (let [response (test-app (mock/request :get "/sync/public"))]
      (is (= (:status response) 200)))))

(deftest private-sync-unauthorized
  (testing "Private sync route without authorization"
    (let [response (test-app (mock/request :get "/sync/"))]
      (is (= (:status response) 401)))))

(deftest private-async
  (testing "Private async route"
    (let [response (with-authentication-async
                     test-app (mock/request :get "/async"))]
      (is (= (:status response) 200)))))

(deftest with-authorization-async
  (testing "Public async route"
    (let [response (handle-async test-app (mock/request :get "/async/public"))]
      (is (= (:status response) 200)))))

(deftest private-async-unauthorized
  (testing "Private async route without authorization"
    (let [response (handle-async test-app (mock/request :get "/async/"))]
      (is (= (:status response) 401)))))

(deftest cache-control-no-cache-async
  (testing "Cache control async route"
    (let [response (handle-async test-app (mock/request :get "/async/public"))]
      (is (= (get-in response [:headers "Cache-Control"])
             "no-cache, max-age=0"))
      (is (= (get-in response [:headers "Expires"])
             "0")))))

(deftest cache-control-no-cache-sync
  (testing "Cache control sync route"
    (let [response (test-app (mock/request :get "/sync/public"))]
      (is (= (get-in response [:headers "Cache-Control"])
             "no-cache, max-age=0"))
      (is (= (get-in response [:headers "Expires"])
             "0")))))
