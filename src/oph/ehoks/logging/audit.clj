(ns oph.ehoks.logging.audit
  (:require [clj-http.client :refer [client-error? server-error?]]
            [clj-time.local :as l]
            [clojure.data.json :as json]
            [clojure.set :refer [union]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [medley.core :refer [assoc-some filter-vals map-keys]]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.logging.access :refer [get-session]])
  (:import (java.time ZoneOffset)
           (java.util Date)))

(def enabled? (:audit? config))
(def heartbeat-enabled? (atom true))
(def logseq (atom 0))
(def ten-minutes-in-milliseconds (* 10 60 1000))

(declare hashmap-changes)
(declare vec-changes)
(declare atom-changes)

; Exclude certain changes based on the last key of the `path`.
(def exclusions #{:module-id})

(defn- changes
  "Takes an `old-value` and a `new-value` and recursively constructs a sequence
  containing information about the changes between the two values. Each entry in
  the sequence contain a \"path\", \"oldValue\", and/or \"newValue\", depending
  on the type of change (e.g., create, update, delete).

  `path` will be a sequence of keys (same as `ks` in `get-in` or `update-in`)
  indicating the location of the value starting from the root of the tree. This
  sequence of keys is accumulated during recursion.

  The function utilizes two helper functions, `map-changes` and `vec-changes`,
  which calculate the changes for nested maps and vectors, respectively."
  ([old-value new-value]
    (changes old-value new-value []))
  ([old-value new-value path]
    (cond
      (and (map? old-value) (map? new-value))
      (hashmap-changes old-value new-value path)
      (and (vector? old-value) (vector? new-value))
      (vec-changes old-value new-value path)
      :else (atom-changes old-value new-value path))))

(defn atom-changes
  [old-value new-value path]
  (if (or (= old-value new-value) (contains? exclusions (peek path)))
    []
    [(cond->
      {"path" (string/join "." path)}
       (some? new-value) (assoc "newValue" new-value)
       (some? old-value) (assoc "oldValue" old-value))]))

(defn- hashmap-changes
  "Constructs a sequence of changes between maps `old-value` and `new-value`.
  This is a helper function for [[changes]]. See its docstring for the meaning
  of each of the arguments."
  [old-value new-value path]
  (mapcat #(changes (get old-value %) (get new-value %) (conj path %))
          (union (set (keys old-value)) (set (keys new-value)))))

(defn- vec-changes
  "Constructs a sequence of changes between vectors `old-vec` and `new-vec`.
  This is a helper function for [[changes]]. See its docstring for the meaning
  of each of the arguments."
  [old-vec new-vec path]
  (mapcat #(changes (get old-vec %) (get new-vec %) (conj path %))
          (range (max (count old-vec) (count new-vec)))))

(defn- make-json-serializable
  "Make a Clojure `obj` JSON serializable. Especially, stringify
  Clojure-specific data types such as keywords, symbols, etc.."
  [obj]
  (cond
    (or (number? obj) (string? obj) (boolean? obj) (nil? obj)) obj
    (map? obj) (zipmap (keys obj) (map make-json-serializable (vals obj)))
    (coll? obj) (map make-json-serializable obj)
    (instance? Date obj) (-> (.toInstant ^Date obj)
                             (.atZone (ZoneOffset/UTC))
                             (str))
    :else (str obj)))

(defn- common-data
  "Gathers some common data to be put in audit log messages. Derefs `logseq`
  which is typically incremented before this function is called."
  []
  {"version"          1
   "logSeq"           (swap! logseq inc)
   "applicationType" "backend"
   "bootTime"        (make-json-serializable (l/local-now))
   "hostname"         (System/getProperty "HOSTNAME" "")
   "serviceName"     (or (:name env) "both")})

(defn- keyword->camel-case-str
  [k]
  (let [words            (string/split (name k) #"-")
        rest-capitalized (map string/capitalize (rest words))]
    (string/join (cons (first words) rest-capitalized))))

(defn- target-info
  "Collects relevant target info (HOKS being read / modified plus
  oppija and opiskeluois OIDs) from `request` for auditing purposes."
  [request response]
  (let [from-request
        (assoc-some
          nil
          :hoks-id            (or (get-in request [:hoks :id])
                                  (get-in request [:route-params :hoks-id]))
          :oppija-oid         (or (get-in request [:hoks :oppija-oid])
                                  (get-in request [:params :oppija-oid])
                                  (get-in request [:route-params :oppija-oid]))
          :opiskeluoikeus-oid (or (get-in request [:hoks :opiskeluoikeus-oid])
                                  (get-in request [:route-params
                                                   :opiskeluoikeus-oid])))]
    (->> (::target response)
         (filter-vals some?)
         not-empty
         (merge from-request)
         (map-keys keyword->camel-case-str))))

(defn- get-user-oid
  "Get OID of user from request."
  [request]
  (when-let [user (or (:service-ticket-user request)
                      (get-in request [:session :virkailija-user])
                      (:virkailija-user request)
                      (get-in request [:session :user]))]
    (or (:oidHenkilo user)
        (:oid user))))

(defn- get-client-ip
  "Get IP address of client from request for auditing purposes."
  [request]
  (if-let [ips (get-in request [:headers "x-forwarded-for"])]
    (-> ips (string/split #",") first)
    (:remote-addr request)))

(defn- user-info
  "Collects relevant user info from `request` for auditing purposes."
  [request]
  {"oid"       (get-user-oid request)
   "ipAddress" (get-client-ip request)
   "session"   (or (get-session request)
                   (get-in request [:headers "ticket"])
                   "no session")
   "userAgent" (or (get-in request [:headers "user-agent"])
                   "no user agent")})

(defn log!
  "A simple wrapper around `clojure.tools.logging/log`. Logs `message` with
  \"audit\" namespace and log level `:info`."
  [message]
  (log/log "audit" :info nil message))

(def method->crud
  "Map HTTP request method keys to CRUD operation names."
  {:get    "read"
   :post   "create"
   :patch  "update"
   :put    "overwrite"
   :delete "delete"})

(defn- handle-audit-logging!
  "Collects info about user, target, operation, and changes from `request` and
  `response` and puts it into a map which is finally serialized into JSON and
  finally logged to \"audit\" namespace."
  [request response]
  (when enabled?
    (let [request-method (:request-method request)
          status (if (or (some? (:error response))
                         (server-error? response)
                         (client-error? response))
                   "failed"
                   "succeeded")
          operation (or (::operation response)
                        (method->crud request-method))
          new-data (get-in response [::changes :new])
          old-data (get-in response [::changes :old])]
      (log! (-> (common-data)
                (assoc "type"      "log"
                       "user"      (user-info request)
                       "target"    (target-info request response)
                       "status"    status
                       "operation" operation
                       "changes"   (changes old-data new-data))
                make-json-serializable
                json/write-str)))))

(defn start-heartbeat!
  []
  (-> (common-data)
      (assoc "type"    "alive"
             "message" "started")
      json/write-str
      log!)
  (future
    (while @heartbeat-enabled?
      (do (Thread/sleep ^Long ten-minutes-in-milliseconds)
          (-> (common-data)
              (assoc "type"    "alive"
                     "message" "alive")
              json/write-str
              log!)))))

(defn wrap-logger
  "Create wrapper function to add audit logging to handler"
  [handler]
  (fn
    ([request respond raise]
      (try
        (handler request
                 (fn [response]
                   (handle-audit-logging! request response)
                   (respond (dissoc response ::changes ::target)))
                 (fn [exc]
                   (handle-audit-logging! request {:error exc})
                   (raise exc)))
        (catch Exception exc
          (handle-audit-logging! request {:error exc})
          (throw exc))))
    ([request]
      (try
        (let [response (handler request)]
          (handle-audit-logging! request response)
          (dissoc response ::changes ::target))
        (catch Exception exc
          (handle-audit-logging! request {:error exc})
          (throw exc))))))

(defn with-logging
  "Wraps compojure-api exception handler with a function that will do
  audit logging before executing the wrapped handler."
  [handler]
  (fn [^Exception e data request]
    (handle-audit-logging! request (or (:response data) {:error true}))
    (handler e data request)))

(defn hoks-target-data
  "Extract target data (`hoks-id`, `oppija-oid`, `opiskeluoikeus-oid`) from HOKS
  or if `hoks` is an `int`, fetch HOKS from DB and then extract the data."
  [hoks]
  (let [hoks (if (int? hoks) (db-hoks/select-hoks-by-id hoks) hoks)]
    {:hoks-id            (:id hoks)
     :oppija-oid         (:oppija-oid hoks)
     :opiskeluoikeus-oid (:opiskeluoikeus-oid hoks)}))
