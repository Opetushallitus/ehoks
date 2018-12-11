(ns oph.ehoks.utils
  (:require [cheshire.core :as cheshire]
            [ring.mock.request :as mock]
            [clojure.test :refer [is]]
            [clojure.data :as d]
            [clojure.pprint :as p]))

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

(defn eq [value expect]
  (when (not= value expect)
    (let [diff (d/diff value expect)]
      (when (seq (first diff))
        (prn "Not expected:")
        (p/pprint (first diff)))
      (when (seq (second diff))
        (prn "Missing:")
        (p/pprint (second diff)))))
  (is (= value expect)))
