(ns oph.ehoks.dev-server
  (:require [oph.ehoks.handler :refer [app]]
            [compojure.core :refer [GET defroutes routes]]
            [ring.util.http-response :refer [ok not-found]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [oph.ehoks.config :refer [config]]
            [hiccup.core :refer [html]]
            [clojure.java.io :as io]
            [clojure.string :as c-str]
            [oph.ehoks.mock-routes :as mock]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.tools.logging :as log]
            [cheshire.core :as cheshire]
            [oph.ehoks.db.memory :as db]))

(defn uri-to-filename [uri]
  (-> uri
      (c-str/replace #"/dev-routes/" "")
      (c-str/replace #"_" "__")
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

(defn import-data! [file a]
  (when-let [data (slurp file)]
    (reset! a (cheshire/parse-string data true))))

(defn import-initial-demo-data! []
  (import-data!
    (-> "demo-data/hoksit.json" io/resource io/file) db/hoks-store)
  (import-data!
    (-> "demo-data/oppijat.json" io/resource io/file) db/oppijat-store))

(defroutes dev-routes
  (GET "/dev-routes/*" request
    (let [filename (uri-to-filename (:uri request))
          file (find-dev-route-file filename)]
      (log/debug
        (format "Route %s searching for file resources/dev/dev-routes/%s"
                (:uri request)
                filename))
      (assoc-in
        (if (some? file)
          (ok (slurp file))
          (not-found "{}"))
        [:headers "Content-Type"] "application/json"))))

(defn set-cors [response]
  (-> response
      (assoc-in [:headers "Access-Control-Allow-Origin"]
                (:frontend-url config))
      (assoc-in [:headers "Access-Control-Allow-Credentials"] "true")
      (assoc-in [:headers "Access-Control-Allow-Methods"]
                "GET, PUT, POST, DELETE, OPTIONS")))

(defn wrap-dev-cors [handler]
  (fn
    ([request respond raise]
       (handler
         request
         (fn [response] (respond (set-cors response)))
         raise))
    ([request]
      (let [response (handler request)]
        (set-cors response)))))

(def dev-app
  (wrap-dev-cors
    (routes
      (wrap-params (wrap-cookies (wrap-reload #'mock/mock-routes)))
      (wrap-reload #'dev-routes)
      (wrap-reload #'app))))

(defn start-server
  ([config-file]
   (when (some? config-file)
     (System/setProperty "config" config-file)
     (require 'oph.ehoks.config :reload))
   (log/info "Loading demodata...")
   (import-initial-demo-data!)
   (log/info "...done.")
   (log/info "Starting development server...")
   (log/info "Not safe for production or public environments.")
   (jetty/run-jetty dev-app
                    {:port (:port config)
                     :join? false
                     :async? true}))
  ([] (start-server nil)))

(defn -main []
  (start-server))
