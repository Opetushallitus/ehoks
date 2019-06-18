(ns oph.ehoks.logging.audit
  (:require [clojure.tools.logging :as log]
            [oph.ehoks.logging.access :refer [current-fin-time-str]]
            [clojure.data.json :as json]))

(defn- get-user-oid [request]
  (when-let [user (or (:service-ticket-user request)
                      (get-in request [:session :virkailija-user])
                      (:virkailija-user request))]
    (:oidHenkilo user)))

(defn- get-audit-data [request response]
  (merge
    {:hoks-id (get-in request [:route-params :hoks-id])
     :oppija-oid (or (get-in request [:params :oppija-oid])
                     (get-in request [:route-params :oppija-oid]))
     :opiskeluoikeus-oid (get-in request [:route-params :opiskeluoikeus-oid])}
    (:audit-data response)))

(defn- remove-nils [m]
  (reduce
    (fn [c [k v]] (if (some? v) (assoc c k v) c))
    {}
    m))

(defn- log-audit-map [m]
  (log/log "audit" :info nil (json/write-str m)))

(defn- log-audit [request response]
  (log-audit-map {:timestamp (current-fin-time-str)
                  :oid (get-user-oid request)
                  :method (:request-method request)
                  :data (remove-nils (get-audit-data request response))}))

(defn- create-response-handler [request respond]
  (fn [response]
    (log-audit request response)
    (respond response)))

(defn wrap-audit-logger [handler]
  (fn
    ([request respond raise]
      (handler request (create-response-handler request respond) raise))
    ([request]
      (let [response (handler request)]
        (log-audit request response)
        response))))
