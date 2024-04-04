(ns oph.ehoks.middleware
  (:require [medley.core :refer [assoc-some]]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.kayttooikeus :as kayttooikeus]
            [oph.ehoks.external.koski :as k]
            [oph.ehoks.user :as user]
            [ring.util.http-response :refer [header unauthorized]]))

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
  (assoc-some request
              :hoks
              (some-> request
                      (get-in [:route-params :hoks-id])
                      Integer/parseInt
                      db-hoks/select-hoks-by-id)))

(defn wrap-hoks
  "Wrap request with hoks"
  [handler]
  (fn
    ([request respond raise]
      (handler (add-hoks request) respond raise))
    ([request]
      (handler (add-hoks request)))))

(defn add-opiskeluoikeus
  "Add opiskeluoikeus to request.  Depends on the HOKS that is already
  in the request, either added from database, or in the body."
  [request]
  (let [hoks (or (not-empty (:hoks request)) (:body-params request))
        oo   (some-> (:opiskeluoikeus-oid hoks) k/get-existing-opiskeluoikeus!)]
    (cond-> request
      (seq oo) (assoc :opiskeluoikeus oo))))

(def ^:dynamic *current-opiskeluoikeus* :none)

(defn get-current-opiskeluoikeus
  "Returns the opiskeluoikeus associated with the current HTTP request.
  This information is made available by wrap-opiskeluoikeus."
  []
  (when (= :none *current-opiskeluoikeus*)
    (throw (ex-info "get-current-opiskeluoikeus outside wrap-opiskeluoikeus"
                    {:error-type :internal})))
  *current-opiskeluoikeus*)

(defn wrap-opiskeluoikeus
  "Make opiskeluoikeus available for request handler.  It is there
  both in the request map (under the :opiskeluoikeus key), and via
  get-current-opiskeluoikeus."
  [handler]
  (fn
    ([request respond raise]
      (let [new-request (add-opiskeluoikeus request)]
        (binding [*current-opiskeluoikeus* (:opiskeluoikeus new-request)]
          (handler new-request respond raise))))
    ([request]
      (let [new-request (add-opiskeluoikeus request)]
        (binding [*current-opiskeluoikeus* (:opiskeluoikeus new-request)]
          (handler new-request))))))
