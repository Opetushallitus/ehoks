(ns oph.ehoks.logging.audit
  (:require [clojure.tools.logging :as log]
            [oph.ehoks.logging.access :refer [current-fin-time-str
                                              get-session]]
            [oph.ehoks.config :refer [config]]
            [clj-http.client :refer [client-error? server-error?]])
  (:import (fi.vm.sade.auditlog Audit
                                ApplicationType
                                Logger
                                User
                                Operation
                                Target$Builder
                                Changes$Builder)
           (java.net InetAddress)
           (org.ietf.jgss Oid)))

(def  ^:private logger
  (proxy [Logger] [] (log [str]
                       (log/log "audit" :info nil str))))

(def ^:private audit
  (Audit. logger (:name config) (ApplicationType/BACKEND)))

(defn- create-operation [op]
  (proxy [Operation] [] (name [] op)))

(def operation-failed (create-operation "failure"))
(def operation-read (create-operation "read"))
(def operation-new (create-operation "create"))
(def operation-modify (create-operation "update"))
(def operation-delete (create-operation "delete"))

(defn- get-user-oid [request]
  (when-let [user (or (:service-ticket-user request)
                      (get-in request [:session :virkailija-user])
                      (:virkailija-user request))]
    (:oidHenkilo user)))

(defn- get-client-ip [request]
  (if-let [ips (get-in request [:headers "x-forwarded-for"])]
    (-> ips (clojure.string/split #",") first)
    (:remote-addr request)))

(defn- get-user [request]
  (User.
    (when-let [oid (get-user-oid request)]
      (Oid. oid))
    (if-let [ip (get-client-ip request)]
      (InetAddress/getByName ip)
      (InetAddress/getLocalHost))
    (or (get-session request) "no session")
    (or (:user-agent (:headers request)) "no user agent")))

(defn- build-changes [response]
  (.build (doto (Changes$Builder.)
            (#(doseq [[path val] (get-in response
                                         [:audit-data
                                          :added])]
                (.added % path (str val))))
            (#(doseq [[path val] (get-in response
                                         [:audit-data
                                          :removed])]
                (.removed % path (str val))))
            (#(doseq [[path [n o]] (get-in response
                                           [:audit-data
                                            :updated])]
                (.updated % (str path) (str o) (str n)))))))

(defn- build-target [request]
  (let [tb (Target$Builder.)]
    (doseq [[field value]
            {:hoks-id (get-in request
                              [:route-params :hoks-id])
             :oppija-oid (or (get-in request
                                     [:params :oppija-oid])
                             (get-in request
                                     [:route-params :oppija-oid]))
             :opiskeluoikeus-oid (get-in request
                                         [:route-params :opiskeluoikeus-oid])}
            :when (some? value)]
      (.setField tb (name field) value))
    (.build tb)))

(defn- do-log [request response]
  (let [user (get-user request)
        method (:method request)
        changes (build-changes response)
        target (build-target request)
        operation (if (or (server-error? response) (client-error? response))
                    operation-failed
                    (case method
                      :post operation-new
                      :patch operation-modify
                      :delete operation-delete
                      operation-read))]
    (.log audit
          user
          operation
          target
          changes)))

(defn- create-response-handler [request respond]
  (fn [response]
    (do-log request response)
    (respond response)))

(defn wrap-audit-logger [handler]
  (fn
    ([request respond raise]
      (handler request (create-response-handler request respond) raise))
    ([request]
      (let [response (handler request)]
        (do-log request response)
        response))))
