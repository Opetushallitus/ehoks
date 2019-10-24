(ns oph.ehoks.logging.audit
  (:require [clojure.tools.logging :as log]
            [oph.ehoks.logging.access :refer [current-fin-time-str
                                              get-session]]
            [environ.core :refer [env]]
            [oph.ehoks.config :refer [config]]
            [clj-http.client :refer [client-error? server-error?]])
  (:import (fi.vm.sade.auditlog Audit
                                ApplicationType
                                Logger
                                User
                                Operation
                                Target$Builder
                                Changes)
           (java.net InetAddress)
           (org.ietf.jgss Oid)))

(def  ^:private logger
  (proxy [Logger] [] (log [str]
                       (log/log "audit" :info nil str))))

(def ^:private audit
  (when (:audit? config)
    (Audit. logger (or (:name env) "both") (ApplicationType/BACKEND))))

(defn- create-operation [op]
  (proxy [Operation] [] (name [] op)))

(def operation-failed (create-operation "failure"))
(def operation-read (create-operation "read"))
(def operation-new (create-operation "create"))
(def operation-modify (create-operation "update"))
(def operation-overwrite (create-operation "overwrite"))
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
    (or (get-in request [:headers "user-agent"]) "no user agent")))

(defn- build-changes [response]
  (let [new (get-in response [:audit-data :new])
        old (get-in response [:audit-data :old])]
    (cond
      (and (nil? new) (nil? old)) Changes/EMPTY
      (nil? old) (Changes/addedDto new)
      (nil? new) (Changes/deleteDto old)
      :else (Changes/updatedDto new old))))

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
  (when audit
    (let [user (get-user request)
          method (:request-method request)
          target (build-target request)
          operation (if (or (server-error? response) (client-error? response))
                      operation-failed
                      (case method
                        :post operation-new
                        :patch operation-modify
                        :delete operation-delete
                        :put operation-overwrite
                        operation-read))
          changes (build-changes response)]
      (.log audit
            user
            operation
            target
            changes))))

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
