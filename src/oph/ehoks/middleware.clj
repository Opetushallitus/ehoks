(ns oph.ehoks.middleware
  (:require [ring.util.http-response :refer [unauthorized!]]))

(defn- matches-route? [request route]
  (and (re-seq (:uri route) (:uri request))
       (= (:request-method request) (:request-method route))))

(defn- route-in? [request routes]
  (some?
    (some #(when (matches-route? request %) %) routes)))

(defn wrap-public [handler public-routes]
  (fn [request]
    (if (or (seq (:session request))
            (route-in?
              (select-keys request [:uri :request-method]) public-routes))
      (handler request)
      (unauthorized!))))
