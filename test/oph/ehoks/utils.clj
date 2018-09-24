(ns oph.ehoks.utils
  (:require [cheshire.core :as cheshire]
            [ring.mock.request :as mock]
            [oph.ehoks.config :refer [config]]))

(defn get-auth-cookie [app]
  (-> (mock/request
        :post "/ehoks/api/v1/session/opintopolku/"
        {"FirstName" "Teuvo Taavetti"
         "cn" "Teuvo"
         "givenName" "Teuvo"
         "hetu" "190384-9245"
         "sn" "Testaaja"})
      (mock/header "referer" (:opintopolku-login-url config))
      (app)
      (get-in [:headers "Set-Cookie"])
      (first)))


(defn with-authentication [app request]
  (let [cookie (get-auth-cookie app)]
    (app (mock/header request :cookie cookie))))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))
