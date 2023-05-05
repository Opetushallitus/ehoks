(ns oph.ehoks.ehoks-app
  (:require [oph.ehoks.common.api :as common-api]
            [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [compojure.route :as compojure-route]
            [ring.util.http-response :as response]
            [oph.ehoks.db.session-store :as session-store]
            [oph.ehoks.oppija.handler :as oppija-handler]
            [oph.ehoks.virkailija.handler :as virkailija-handler]
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

(def both-app
  "App with both oppija and virkailija routes initialized."
  (app-union
    oppija-handler/app-routes
    virkailija-handler/app-routes
    (compojure-route/not-found
      (-> (str "{\"reason\": \"Use APIs under /ehoks-virkailija-backend "
               "or /ehoks-oppija-backend\"}")
          (response/not-found)
          (response/content-type "application/json")))))

(defn create-app
  "Create ehoks web app of given name. Name will decide if system has oppija
  (ehoks), virkailija (ehoks-virkailija) or both routes."
  [app-name]
  (common-api/create-app
    (case app-name
      "ehoks-virkailija" virkailija-handler/app-routes
      "ehoks-oppija" oppija-handler/app-routes
      both-app)
    (session-store/db-store)))

(defn get-app-name
  "Get the app name."
  []
  (lower-case (:name env (or (System/getProperty "name") "both"))))

(def app
  "Global app variable."
  (create-app (get-app-name)))
