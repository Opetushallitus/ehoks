(ns oph.ehoks.dev-server
  (:require [oph.ehoks.ehoks-app :as ehoks-app]
            [oph.ehoks.db.migrations :as m]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.mock-routes :as mock]
            [oph.ehoks.db.postgresql :as p]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.mock-gen :as mock-gen]
            [compojure.core :refer [GET defroutes routes]]
            [ring.util.http-response :refer [ok not-found]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.string :as c-str]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]))

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

(defroutes dev-routes
  (GET "/dev-routes/*" [:as request]
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
      (wrap-reload #'ehoks-app/app))))

(defn rand-str [max-len]
  (c-str/capitalize
    (apply
      str
      (take
        (inc (rand-int (dec max-len)))
        (repeatedly #(char (+ (rand 26) 65)))))))

(defn populate-oppijaindex []
  (doseq [h (p/select-hoksit)]
    (swap!
      oppijaindex/oppijat
      conj
      {:nimi (format "%s %s"
                     (mock-gen/generate-last-name)
                     (mock-gen/generate-first-name))
       :oppilaitos-oid (mock-gen/generate-oppilaitos-oid)
       :oid (:oppija-oid h)
       :tutkinto (rand-str 20)
       :osaamisala (rand-str 20)})))

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
    (log/info "Running migrations")
    (m/migrate!)
    (log/info "Starting development server...")
    (log/info "Not safe for production or public environments.")
    (populate-oppijaindex)
    (jetty/run-jetty dev-app
                     {:port (:port config)
                      :join? false
                      :async? true}))
  ([] (start-server nil)))

(defn -main []
  (start-server))
