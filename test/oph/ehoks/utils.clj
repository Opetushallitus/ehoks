(ns oph.ehoks.utils
  (:require [cheshire.core :as cheshire]
            [ring.mock.request :as mock]
            [oph.ehoks.config :refer [config]]))

(defn get-auth-cookie [app]
  (-> (mock/request :get "/ehoks-backend/api/v1/session/opintopolku/")
      (mock/header "FirstName" "Teuvo Testi")
      (mock/header "cn" "Teuvo")
      (mock/header "givenname" "Teuvo")
      (mock/header "hetu" "190384-9245")
      (mock/header "sn" "Testaaja")
      (app)
      (get-in [:headers "Set-Cookie"])
      (first)))

(defn with-authentication [app request]
  (let [cookie (get-auth-cookie app)]
    (app (mock/header request :cookie cookie))))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))
