(ns oph.ehoks.middleware
  (:require [ring.util.http-response :refer [unauthorized header]]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.kayttooikeus :as kayttooikeus]
            [oph.ehoks.user :as user]))

(defn- authenticated?
  "Check whether request is authenticated"
  [request]
  (some? (get-in request [:session :user])))

(defn wrap-authorize
  "Require user with session"
  [handler]
  (fn
    ([request respond raise]
      (if (authenticated? request)
        (handler request respond raise)
        (respond (unauthorized))))
    ([request]
      (if (authenticated? request)
        (handler request)
        (unauthorized)))))

(defn- cache-control-no-cache-response
  "Add cache control no cache headers to response"
  [response]
  (-> response
      (header "Expires" 0)
      (header "Cache-Control" "no-cache, max-age=0")))

(defn wrap-cache-control-no-cache
  "Add cache control no cache headers in given handler"
  [handler]
  (fn
    ([request respond raise]
      (handler request
               (fn [response]
                 (respond (cache-control-no-cache-response response)))
               raise))
    ([request]
      (cache-control-no-cache-response (handler request)))))

(defn validate-headers
  "Require headers with caller id and service ticket"
  [request]
  (cond
    (nil? (get-in request [:headers "caller-id"]))
    {:error "Caller-Id header is missing"}
    (nil? (get-in request [:headers "ticket"]))
    {:error "Ticket is missing"}))

; TODO: Split and reuse code
(defn wrap-user-details
  "Wrap with user details (service ticket user)"
  [handler]
  (fn
    ([request respond raise]
      (if-let [result (validate-headers request)]
        (respond (unauthorized result))
        (if-let [ticket-user (kayttooikeus/get-ticket-user
                               (get-in request [:headers "ticket"]))]
          (handler
            (assoc
              request
              :service-ticket-user
              (merge ticket-user (user/get-auth-info ticket-user)))
            respond
            raise)
          (respond
            (unauthorized
              {:error
               "User not found for given ticket. Ticket may be expired."})))))
    ([request]
      (if-let [result (validate-headers request)]
        (unauthorized result)
        (if-let [ticket-user (kayttooikeus/get-ticket-user
                               (get-in request [:headers "ticket"]))]
          (handler (assoc
                     request
                     :service-ticket-user
                     (merge ticket-user (user/get-auth-info ticket-user))))

          (unauthorized
            {:error
             "User not found for given ticket. Ticket may be expired."}))))))

(defn add-hoks
  "Add HOKS to request"
  [request]
  (let [hoks-id (Integer/parseInt (get-in request [:route-params :hoks-id]))
        hoks (db-hoks/select-hoks-by-id hoks-id)]
    (assoc request :hoks hoks)))

(defn wrap-hoks
  "Wrap request with hoks"
  [handler]
  (fn
    ([request respond raise]
      (handler (add-hoks request) respond raise))
    ([request]
      (handler (add-hoks request)))))
