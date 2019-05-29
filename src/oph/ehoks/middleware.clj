(ns oph.ehoks.middleware
  (:require [ring.util.http-response :refer [unauthorized header]]
            [oph.ehoks.external.kayttooikeus :as kayttooikeus]
            [oph.ehoks.user :as user]))

(defn- authenticated? [request]
  (some? (get-in request [:session :user])))

(defn wrap-authorize [handler]
  (fn
    ([request respond raise]
      (if (authenticated? request)
        (handler request respond raise)
        (respond (unauthorized))))
    ([request]
      (if (authenticated? request)
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

(defn validate-headers [request]
  (cond
    (nil? (get-in request [:headers "caller-id"]))
    {:error "Caller-Id header is missing"}
    (nil? (get-in request [:headers "ticket"]))
    {:error "Ticket is missing"}))

(defn set-user-details [request]
  (let [ticket-user (kayttooikeus/get-ticket-user
                      (get-in request [:headers "ticket"]))]
    (assoc
      request
      :service-ticket-user
      (merge ticket-user (user/get-auth-info ticket-user)))))

(defn wrap-user-details [handler]
  (fn
    ([request respond raise]
      (if-let [result (validate-headers request)]
        (respond (unauthorized result))
        (handler (set-user-details request) respond raise)))
    ([request]
      (if-let [result (validate-headers request)]
        (unauthorized result)
        (handler (set-user-details request))))))
