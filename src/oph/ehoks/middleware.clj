(ns oph.ehoks.middleware
  (:require [ring.util.http-response :refer [unauthorized header]]
            [oph.ehoks.external.cas :as cas]
            [oph.ehoks.config :refer [config]]))

(defn- matches-route? [request route]
  (and (re-seq (:uri route) (:uri request))
       (= (:request-method request) (:request-method route))))

(defn- route-in? [request routes]
  (some?
    (some #(when (matches-route? request %) %) routes)))

(defn- authenticated? [request]
  (some? (seq (:session request))))

(defn- public-route? [request public-routes]
  (route-in?
    (select-keys request [:uri :request-method]) public-routes))

(defn- access-granted? [request public-routes]
  (or (authenticated? request)
      (public-route? request public-routes)))

(defn wrap-public [handler public-routes]
  (fn
    ([request respond raise]
      (if (access-granted? request public-routes)
        (handler request respond raise)
        (respond (unauthorized))))
    ([request]
      (if (access-granted? request public-routes)
        (handler request)
        (unauthorized)))))

(defn- cache-control-no-cache-response [response]
  (-> response
      (header "Expires" 0)
      (header "Cache-Control" "no-cache, max-age=0")))

(defn wrap-cache-control-no-cache [handler]
  (fn
    ([request respond raise]
      (handler request
               (fn [response]
                 (respond (cache-control-no-cache-response response)))
               raise))
    ([request]
      (cache-control-no-cache-response (handler request)))))

(defn validate-service-ticket [request]
  (cond
    (nil? (get-in request [:headers "caller-id"]))
    {:error "Caller-Id header is missing"}
    (nil? (get-in request [:headers "ticket"]))
    {:error "Ticket is missing"}
    (not (-> (:backend-url config)
             (cas/validate-ticket (get-in request [:headers "ticket"]))
             :success?))
    {:error "Invalid service ticket"}))

(defn wrap-service-ticket [handler]
  (fn
    ([request respond raise]
      (if-let [result (validate-service-ticket request)]
        (respond (unauthorized result))
        (handler request respond raise)))
    ([request]
      (if-let [result (validate-service-ticket request)]
        (unauthorized result)
        (handler request)))))
