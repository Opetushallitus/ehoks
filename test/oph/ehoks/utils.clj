(ns oph.ehoks.utils
  (:require [cheshire.core :as cheshire]
            [ring.mock.request :as mock]
            [clojure.test :refer [is]]
            [clojure.data :as d]
            [clojure.pprint :as p]
            [oph.ehoks.external.http-client :as client]))

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

(defn with-authenticated-oid [store oid app request]
  (let [cookie (get-auth-cookie app)
        session (first (vals @store))]
    (swap! store assoc-in [(-> @store keys first) :user :oid] oid)
    (app (mock/header request :cookie cookie))))

(defn with-service-ticket [app request]
  (client/set-get!
    (fn [url options]
      (cond (.endsWith url "/serviceValidate")
        {:status 200
         :body
         (str "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"
              "<cas:authenticationSuccess><cas:user>ehoks</cas:user>"
              "<cas:attributes>"
              "<cas:longTermAuthenticationRequestTokenUsed>false"
              "</cas:longTermAuthenticationRequestTokenUsed>"
              "<cas:isFromNewLogin>false</cas:isFromNewLogin>"
              "<cas:authenticationDate>2019-02-20T10:14:24.046+02:00"
              "</cas:authenticationDate></cas:attributes>"
              "</cas:authenticationSuccess></cas:serviceResponse>")}
        (.endsWith url "/koski/api/opiskeluoikeus/1.3.444.555.66.77777777777")
        {:status 200
         :body {:oppilaitos {:oid "1.2.246.562.24.47861388607"}}}
        (.endsWith url "/koski/api/opiskeluoikeus/1.3.444.555.66.77777777778")
        {:status 200
         :body {:oppilaitos {:oid "1.2.246.562.24.47861388608"}}})))
  (let [result (app (-> request
                        (mock/header "Caller-Id" "test")
                        (mock/header "ticket" "ST-testitiketti")))]
    (client/reset-functions!)
    result))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(defn eq [value expect]
  (when (not= value expect)
    (let [diff (d/diff value expect)]
      (when (seq (first diff))
        (println "Not expected:")
        (p/pprint (first diff)))
      (when (seq (second diff))
        (println "Missing:")
        (p/pprint (second diff)))))
  (is (= value expect)))
