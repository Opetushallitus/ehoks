(ns oph.ehoks.ehoks-app
  (:require [cheshire.core :as cheshire]
            [compojure.api.routes :as routes]
            [oph.ehoks.common.api :as common-api]
            [ring.util.http-response :as response]
            [oph.ehoks.db.session-store :as session-store]
            [oph.ehoks.oppija.handler :as oppija-handler]
            [oph.ehoks.virkailija.handler :as virkailija-handler]
            [oph.ehoks.palaute.handler :as palaute-handler]
            [clojure.java.io]
            [clojure.string :refer [lower-case]]
            [environ.core :refer [env]]))

(defn real-response? [response]
  (and (map? response) (not= 404 (:status response))))

(defn request-with-new-body [request body]
  (assoc request :body (clojure.java.io/input-stream body)))

(defn app-union
  "Combines apps by trying the same request with all of them,
  until one of them returns something else than not-found.
  Basically the same as compojure.core/routes,
  but not handling a request can be signalled by a not-found response
  in addition to nil."
  [first-app & apps]
  (if (empty? apps)
    first-app
    (let [rest-app (apply app-union apps)]
      (fn
        ([request]
          (let [body-bytes (-> request :body (slurp) (.getBytes))
                response (first-app (request-with-new-body request body-bytes))]
            (if (real-response? response)
              response
              (rest-app (request-with-new-body request body-bytes)))))
        ([request respond raise]
          (let [body-bytes (-> request :body (slurp) (.getBytes))]
            (first-app (request-with-new-body request body-bytes)
                       (fn [response]
                         (if (real-response? response)
                           (respond response)
                           (rest-app (request-with-new-body request body-bytes)
                                     respond
                                     raise)))
                       (fn [exception]
                         (if (real-response? (:response (ex-data exception)))
                           (raise exception)
                           (rest-app (request-with-new-body request body-bytes)
                                     respond
                                     raise))))))))))

(def all-route-templates
  "Templates of all documented oppija, virkailija and palaute routes."
  (->> [oppija-handler/app-routes
        virkailija-handler/app-routes
        palaute-handler/app-routes]
       (mapcat routes/get-routes)
       (remove (fn [[_ _ info]] (:no-doc info)))
       (map first)))

(defn not-found-handler
  "Not-found response for requests that none of the apps handled."
  [request]
  (-> (common-api/route-not-found-response all-route-templates request)
      (update :body cheshire/generate-string)
      (response/content-type "application/json")))

(def all-app-routes
  "App with all oppija, virkailija and palaute routes initialized."
  (app-union
    oppija-handler/app-routes
    virkailija-handler/app-routes
    palaute-handler/app-routes
    (fn
      ([request] (not-found-handler request))
      ([request respond _] (respond (not-found-handler request))))))

(def all-app
  "Ready-to-call app with all-app-routes and session middleware.
  It is important that it is assigned to a var, since hot reloading
  depends on that var (i.e. oph.ehoks.ehoks-app/all-app) getting
  updated."
  (common-api/create-app all-app-routes (session-store/db-store)))

(def virkailija-app
  "Ready-to-call app with virkailija app-routes and session middleware."
  (common-api/create-app
    virkailija-handler/app-routes (session-store/db-store)))

(def oppija-app
  "Ready-to-call app with oppija app-routes and session middleware."
  (common-api/create-app oppija-handler/app-routes (session-store/db-store)))

(def palaute-app
  "Ready-to-call app with palaute app-routes and session middleware"
  (common-api/create-app palaute-handler/app-routes (session-store/db-store)))

(def app-by-name
  {"ehoks-virkailija" #'virkailija-app
   "ehoks-oppija" #'oppija-app
   "ehoks-palaute" #'palaute-app
   "both" #'all-app})

(defn create-app
  "Give an app that has the routes that belong to app-name.  The app given
  will be updated when this namespace is reloaded, since it's a var that
  delegates to the actual app function."
  [app-name]
  (get app-by-name app-name #'all-app))

(defn get-app-name
  "Get the app name."
  []
  (lower-case (:name env (or (System/getProperty "name") "both"))))

(def app
  "Global app variable."
  (create-app (get-app-name)))
