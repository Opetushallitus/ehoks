(ns oph.ehoks.dev-server
  (:require [oph.ehoks.handler :refer [app]]
            [compojure.core :refer [GET defroutes routes]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [oph.ehoks.config :refer [config]]
            [hiccup.core :refer [html]]))

(def dev-login-form
  [:div
   [:form {:action (:opintopolku-return-url config) :method "POST"}
    [:input {:type "hidden" :name "FirstName" :value "Teuvo Taavetti"}]
    [:input {:type "hidden" :name "cn" :value "Teuvo"}]
    [:input {:type "hidden" :name "givenName" :value "Teuvo"}]
    [:input {:type "hidden" :name "hetu" :value "010203-XXXXX"}]
    [:input {:type "hidden" :name "sn" :value "Testaaja"}]
    [:button {:type "submit" :value "submit"} "Login"]]])

(defroutes dev-routes
  (GET "/auth-dev/opintopolku-login/" [] (html dev-login-form)))


(defn wrap-dev-cors [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
          (assoc-in [:headers "Access-Control-Allow-Origin"]
                    (format
                      "%s:%d" (:frontend-url config) (:frontend-port config)))
          (assoc-in [:headers "Access-Control-Allow-Credentials"] "true")
          (assoc-in [:headers "Access-Control-Allow-Methods"]
                    "GET, PUT, POST, DELETE, OPTIONS")))))

(def dev-app
  (wrap-dev-cors (routes (wrap-reload #'dev-routes) (wrap-reload #'app))))

(defn start-server []
  (jetty/run-jetty dev-app
     {:port (:port config) :join? false}))
