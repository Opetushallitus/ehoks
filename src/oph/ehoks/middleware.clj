(ns oph.ehoks.middleware
  (:require [ring.util.http-response :refer [unauthorized header]]))

(defn- matches-route? [request route]
  (and (re-seq (:uri route) (:uri request))
       (= (:request-method request) (:request-method route))))

(defn- route-in? [request routes]
  (some?
    (some #(when (matches-route? request %) %) routes)))

(defn- authorized-or-public-route-request? [request public-routes]
  (or (seq (:session request))
      (route-in?
        (select-keys request [:uri :request-method]) public-routes)))

(defn wrap-public [handler public-routes]
  (fn
    ([request respond raise]
      (if (authorized-or-public-route-request? request public-routes)
        (handler request respond raise)
        (respond (unauthorized))))
    ([request]
      (if (authorized-or-public-route-request? request public-routes)
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
