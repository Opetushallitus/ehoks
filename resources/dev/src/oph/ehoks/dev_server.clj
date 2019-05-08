(ns oph.ehoks.dev-server
  (:require [oph.ehoks.handler :as hoks-api-handler]
            [oph.ehoks.virkailija.handler :as virkailija-handler]
            [oph.ehoks.common.api :as common-api]
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
            [oph.ehoks.redis :refer [redis-store]]))

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

(defroutes dev-hoks-api-routes
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

(def hoks-api-app
  (common-api/create-app
    hoks-api-handler/app-routes
    (when (seq (:redis-url config))
      (redis-store {:pool {}
                    :spec {:uri (:redis-url config)}}))))

(def virkailija-app
  (common-api/create-app
    virkailija-handler/app-routes
    (when (seq (:redis-url config))
      (redis-store {:pool {}
                    :spec {:uri (:redis-url config)}}))))

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

(def dev-virkailija-app
  (wrap-dev-cors
    (routes
;      (wrap-params (wrap-cookies (wrap-reload #'mock/mock-routes)))

      (wrap-reload #'virkailija-app))))

(def dev-hoks-api-app
  (wrap-dev-cors
    (routes
      (wrap-params (wrap-cookies (wrap-reload #'mock/mock-routes)))
      (wrap-reload #'dev-hoks-api-routes)
      (wrap-reload #'hoks-api-app))))

(defn start-server
  ([config-file]
   (when (some? config-file)
     (System/setProperty "config" config-file)
     (require 'oph.ehoks.config :reload)
     (when (.endsWith (:opintopolku-host config) "opintopolku.fi")
       (println "Using prod urls")
       (System/setProperty
         "services_file" "resources/prod/services-oph.properties"))
     (require 'oph.ehoks.external.oph-url :reload))
   (log/info "Starting development server...")
   (log/info "Not safe for production or public environments.")
   {:virkailija (jetty/run-jetty dev-virkailija-app
                                 {:port (:virkailija-backend-port config)
                                  :join? false
                                  :async? true})
    :hoks-api (jetty/run-jetty dev-hoks-api-app
                               {:port (:port config)
                                :join? false
                                :async? true})})
  ([] (start-server nil)))

(defn -main []
  (start-server))
