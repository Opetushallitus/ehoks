(ns oph.ehoks.logging.audit
  (:require [clj-http.client :refer [client-error? server-error?]]
            [clj-time.local :as l]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.hoks.hoks-parts.parts-test-data
             :refer [ahato-data multiple-ahato-values-patched]]
            [oph.ehoks.logging.access :refer [get-session]])
  (:import [com.fasterxml.jackson.databind ObjectMapper]
           [com.github.fge.jsonpatch.diff JsonDiff]
           [fi.vm.sade.auditlog
            ApplicationType
            Audit
            Changes
            Logger
            Operation
            Target$Builder
            User]
           (java.net InetAddress)
           (java.time ZoneOffset)
           (java.util Date)
           (org.ietf.jgss Oid)))

(defn- make-json-deserializable [obj]
  (cond
    (or (number? obj) (string? obj) (boolean? obj) (nil? obj)) obj
    (map? obj) (zipmap (keys obj) (map make-json-deserializable (vals obj)))
    (coll? obj) (map make-json-deserializable obj)
    (instance? Date obj) (-> (.toInstant ^Date obj)
                             (.atZone (ZoneOffset/UTC))
                             (str))
    :else (str obj)))

(def auditing-enabled? (:audit? config))
(def heartbeat-enabled? (atom true))
(def logseq (atom 0))

(def mapper (new ObjectMapper))
(def ten-minutes-in-milliseconds (* 3 1000)) ; (* 10 60 1000))

(def common-data
  {:version          1
   :logseq           logseq
   :application-type "backend"
   :boot-time        (make-json-deserializable (l/local-now))
   :hostname         (System/getProperty "HOSTNAME" "")
   :service-name     (or (:name env) "both")})

(defn dotify-path
  [^String path]
  (-> path (subs 1) (str/replace #"/" ".")))

(defn- json-patch-operation->map
  "Takes a Json Patch `operation` object and converts it into a map with keys
  :op, :path, and possibly :value and :from."
  [op old-value new-value]
  (let [path (->     (.get op "path") .asText)
        from (some-> (.get op "from") .asText)]
    (-> {:path (dotify-path path)}
        (merge (case (.asText (.get op "op"))
                 "add"     {:new-value (.at new-value path)}
                 "remove"  {:old-value (.at old-value path)}
                 "replace" {:old-value (.at old-value path)
                            :new-value (.at new-value path)}
                 "move"    {:from   (dotify-path from)
                            :value (.at new-value path)}
                 "copy"    {:from  (dotify-path from)
                            :value (.at new-value path)})))))

(defn- data-as-json
  [key response ]
  (some->> (get-in response [:audit-data key])
           make-json-deserializable
           (.valueToTree mapper)))

(defn get-changes
  "Takes an `old-value` and a `new-value`, calculates diff between these two
  and returns a sequence of maps describing individual changes."
  ([response]
   (let [old-as-json (data-as-json :old response)
         new-as-json (data-as-json :new response)]
     (cond
       (and old-as-json new-as-json)
       (->> (JsonDiff/asJson old-as-json new-as-json)
            .iterator
            iterator-seq ; Sequence of Json Patch operations (JSON objects)
            (map #(json-patch-operation->map % old-as-json new-as-json)))
       (nil? old-as-json) {:new-value new-as-json}
       (nil? new-as-json) {:old-value old-as-json}))))

(defn- target-info
  "Collects relevant target info (HOKS being read / modified plus
  oppija and opiskeluois OIDs) from `request` for auditing purposes."
  [request]
  {:hoks-id            (get-in request [:route-params :hoks-id])
   :oppija-oid         (or (get-in request [:params :oppija-oid])
                           (get-in request [:route-params :oppija-oid]))
   :opiskeluoikeus-oid (get-in request [:route-params :opiskeluoikeus-oid])})

(defn- get-user-oid
  "Get OID of user from request"
  [request]
  (when-let [user (or (:service-ticket-user request)
                      (get-in request [:session :virkailija-user])
                      (:virkailija-user request)
                      (get-in request [:session :user]))]
    (or (:oidHenkilo user)
        (:oid user))))

(defn- get-client-ip
  "Get IP address of client from request"
  [request]
  (if-let [ips (get-in request [:headers "x-forwarded-for"])]
    (-> ips (str/split #",") first)
    (:remote-addr request)))

(defn- user-info
  "Collects relevant user info from `request` for auditing purposes."
  [request]
  {:oid        (get-user-oid request)
   :ip-address (get-client-ip request)
   :session    (or (get-session request)
                   (get-in request [:headers "ticket"])
                   "no session")
   :user-agent (or (get-in request [:headers "user-agent"])
                   "no user agent")})

(defn log
  [message]
  (log/log "audit" :info nil message)
  (swap! logseq inc))

(defn handle-audit-logging
  [request response]
  (when auditing-enabled?
    (let [user      (user-info request)
          target    (target-info request)
          operation (if (or (some? (:error response))
                            (server-error? response)
                            (client-error? response))
                      "failure"
                      ({:get    "read"
                        :post   "create"
                        :patch  "update"
                        :put    "overwrite"
                        :delete "delete"}
                       (:request-method request)))]
      (log (as-> common-data d
                 (update d :logseq deref)
                 (assoc d :user      user
                          :target    target
                          :operation operation
                          :changes   (get-changes response))
                 (.valueToTree mapper d))))))

(let [request {:request-method :post}
      response {:status 200 :audit-data {:old ahato-data
                                         :new multiple-ahato-values-patched}}]
  (handle-audit-logging request response))

(when auditing-enabled?
  (future
    (while @heartbeat-enabled?
      (do (Thread/sleep ten-minutes-in-milliseconds)
          (as-> common-data d
                (update d :logseq deref)
                (assoc d :alive true)
                (.valueToTree mapper d)
                (log d))))))

(def  ^:private logger
  "Global (to this file) logger"
  (proxy [Logger] [] (log [str]
                       (log/log "audit" :info nil str))))

(def ^:private audit
  "Global (to this file) audit logger"
  (when (:audit? config)
    (Audit. logger (or (:name env) "both") (ApplicationType/BACKEND))))

(defn- create-operation
  "Create instance of class Operation"
  [op]
  (proxy [Operation] [] (name [] op)))

(def operation-failed
  "Global failed operation instance"
  (create-operation "failure"))

(def operation-read
  "Global read operations instance"
  (create-operation "read"))

(def operation-new
  "Global create operation instance"
  (create-operation "create"))

(def operation-modify
  "Global update operation instance"
  (create-operation "update"))

(def operation-overwrite
  "Global overwrite operation instance"
  (create-operation "overwrite"))

(def operation-delete
  "Global delete operation instance"
  (create-operation "delete"))

(defn- get-user
  "Create instance of user object for given request"
  [request]
  (User.
    (when-let [^String oid (get-user-oid request)]
      (Oid. oid))
    (if-let [ip (get-client-ip request)]
      (InetAddress/getByName ip)
      (InetAddress/getLocalHost))
    (or (get-session request)
        (get-in request [:headers "ticket"])
        "no session")
    (or (get-in request [:headers "user-agent"]) "no user agent")))

(defn- build-changes
  "Create object representing changes"
  [response]
  (let [new (make-json-deserializable (get-in response [:audit-data :new]))
        old (make-json-deserializable (get-in response [:audit-data :old]))]
    (cond
      (and (nil? new) (nil? old)) Changes/EMPTY
      (nil? old) (Changes/addedDto new)
      (nil? new) (Changes/deleteDto old)
      :else (Changes/updatedDto new old))))

(defn- build-target
  "Build audit log target"
  [request]
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

(defn- do-log
  "When audit is set, log user, operation, target, and changes"
  [request response]
  (when audit
    (let [user (get-user request)
          method (:request-method request)
          target (build-target request)
          operation (if (or (some? (:error response))
                            (server-error? response)
                            (client-error? response))
                      operation-failed
                      (case method
                        :post operation-new
                        :patch operation-modify
                        :delete operation-delete
                        :put operation-overwrite
                        operation-read))
          changes (build-changes response)]
      (.log ^Audit audit
            user
            operation
            target
            changes))))

(defn wrap-audit-logger
  "Create wrapper function to add audit logging to handler"
  [handler]
  (fn
    ([request respond raise]
      (try
        (handler request
                 (fn [response] (do-log request response) (respond response))
                 (fn [exc] (do-log request {:error exc}) (raise exc)))
        (catch Exception exc
          (do-log request {:error exc})
          (throw exc))))
    ([request]
      (try
        (let [response (handler request)]
          (do-log request response)
          response)
        (catch Exception exc
          (do-log request {:error exc})
          (throw exc))))))
