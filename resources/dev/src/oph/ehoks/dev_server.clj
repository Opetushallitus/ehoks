(ns oph.ehoks.dev-server
  (:require [oph.ehoks.ehoks-app :as ehoks-app]
            [oph.ehoks.db.migrations :as m]
            [oph.ehoks.config :refer [config] :as c]
            [oph.ehoks.mocked-routes.mock-routes :as mock]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [compojure.core :refer [GET defroutes routes]]
            [ring.util.http-response :refer [ok not-found]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.string :as c-str]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [oph.ehoks.dev-tools :as dev-tools]))

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
      (log/debugf
        "Route %s searching for file resources/dev/dev-routes/%s"
        (:uri request)
        filename)
      (assoc-in
        (if (some? file)
          (ok (slurp file))
          (not-found "{}"))
        [:headers "Content-Type"] "application/json"))))

(defn set-cors [response]
  (-> response
      (assoc-in [:headers "Access-Control-Allow-Origin"]
                (:frontend-url-fi config))
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

;; FIXME: combine this with the app of (start) and see that everything works OK
(def dev-reload-app
  (wrap-dev-cors
    (routes
      (wrap-params (wrap-cookies (wrap-reload #'mock/mock-routes)))
      (wrap-reload #'dev-routes)
      (wrap-cookies (wrap-reload #'dev-tools/routes))
      (wrap-reload #'ehoks-app/app))))

(defn populate-oppijaindex []
  (future
    (log/info "Updating oppijaindex")
    (oppijaindex/update-oppijat-without-index!)
    (oppijaindex/update-opiskeluoikeudet-without-index!)
    (log/info "Updating oppijaindex finished")))

(defn start-app-server! [app app-name config-file]
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
  (log/infof "Starting %s development server..." app-name)
  (log/info "Not safe for production or public environments.")
  (populate-oppijaindex)
  (jetty/run-jetty app
                   {:port (:port config)
                    :join? false
                    :async? true}))

(defn start [app-name config-file]
  (log/infof "Starting %s with config %s" app-name config-file)
  (let [app (wrap-dev-cors
              (routes
                (wrap-params (wrap-cookies mock/mock-routes))
                dev-routes
                (ehoks-app/create-app app-name)))]
    (start-app-server! app app-name config-file)))

(defn -main
  ([app-name config-file] (start app-name config-file))
  ([app-name] (start app-name (c/get-config-file)))
  ([] (start (ehoks-app/get-app-name) (c/get-config-file))))
