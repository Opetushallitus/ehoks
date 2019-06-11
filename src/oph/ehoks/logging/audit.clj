(ns oph.ehoks.logging.audit
  (:require [oph.ehoks.logging.core :as log]
            [oph.ehoks.logging.access :refer [current-fin-time-str]]))

(defn- get-user-oid [request]
  (when-let [user (or (:service-ticket-user request)
                      (:virkailija-user request))]
    (:oidHenkilo user)))

(defn- get-audit-data [request]
  {:hoks-id (get-in request [:route-params :hoks-id])
   :oppija-oid (or (get-in request [:params :oppija-oid])
                   (get-in request [:route-params :oppija-oid]))
   :opiskeluoikeus-oid (get-in request [:route-params :opiskeluoikeus-oid])})

(defn- remove-nils [m]
  (reduce
    (fn [c [k v]] (if (some? v) (assoc c k v) c))
    {}
    m))

(defn- log-request [request]
  (log/audit {:timestamp (current-fin-time-str)
              :oid (get-user-oid request)
              :method (:request-method request)
              :data (remove-nils (get-audit-data request))}))

(defn wrap-audit-logger [handler]
  (fn
    ([request respond raise]
      (log-request request)
      (handler request respond raise))
    ([request]
      (log-request request)
      (handler request))))