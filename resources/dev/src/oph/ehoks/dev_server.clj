(ns oph.ehoks.dev-server
  (:require [oph.ehoks.handler :refer [app]]
            [compojure.core :refer [GET defroutes routes]]
            [ring.util.http-response :refer [ok not-found]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [oph.ehoks.config :refer [config]]
            [hiccup.core :refer [html]]
            [clojure.java.io :as io]
            [clojure.string :as c-str]))

(def dev-login-form
  [:div
   [:form {:action (:opintopolku-return-url config) :method "POST"}
    [:input {:type "hidden" :name "FirstName" :value "Teuvo Taavetti"}]
    [:input {:type "hidden" :name "cn" :value "Teuvo"}]
    [:input {:type "hidden" :name "givenName" :value "Teuvo"}]
    [:input {:type "hidden" :name "hetu" :value "010203-XXXXX"}]
    [:input {:type "hidden" :name "sn" :value "Testaaja"}]
    [:button {:type "submit" :value "submit"} "Login"]]])

(defn uri-to-filename [uri]
  (-> uri
      (c-str/replace #"/dev-routes/" "")
      (c-str/replace #"/" "_")
      (str ".json")))

(defn find-dev-route-file
  "Finds response file for dev route.
   For security reasons only resource files in dev-routes are allowed to be
   returned."
  [filename]
  (some
    #(when (= (.getName %) filename) %)
    (file-seq (io/file (io/resource "dev-routes")))))

(defroutes dev-routes
  (GET "/auth-dev/opintopolku-login/" [] (html dev-login-form))
  (GET "/dev-routes/*" request
    (let [filename (uri-to-filename (:uri request))
          file (find-dev-route-file filename)]
      (prn (format "Route %s searching for file resources/dev/dev-routes/%s"
                   (:uri request)
                   filename))
      (assoc-in
        (if (some? file)
          (ok (slurp file))
          (not-found "{}"))
        [:headers "Content-Type"] "application/json"))))

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
  (wrap-dev-cors
    (routes
      (wrap-reload #'dev-routes)
      (wrap-reload #'app))))

(defn start-server []
  (prn "Starting development server...")
  (prn "Not safe for production or public environments.")
  (jetty/run-jetty dev-app
     {:port (:port config) :join? false}))
