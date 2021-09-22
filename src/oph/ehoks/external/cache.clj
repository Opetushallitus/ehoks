(ns oph.ehoks.external.cache
  (:require [clj-time.core :as t]
            [ring.util.codec :as codec]
            [clojure.tools.logging :as log]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.connection :as c]
            [oph.ehoks.external.cas :as cas]))

(defonce cache
  ^:private
  (atom {}))

(defonce cleaning? (atom false))

(defn size
  "Size of cache (item count)"
  [] (count @cache))

(defn expired?
  "Checks if response is too old. Max life time is set in config."
  [response]
  (and (some? (:timestamp response))
       (t/before?
         (:timestamp response)
         (t/minus (t/now) (t/minutes (:ext-cache-lifetime-minutes config))))))

(defn expire-response!
  "Makes response of url expired"
  [url]
  (swap! cache dissoc url)
  nil)

(defn- filter-expired!
  "Filter expired responses"
  []
  (doseq [[url response] @cache]
    (when (expired? response)
      (swap! cache dissoc url))))

(defn clean-cache!
  "Removes expired responses"
  []
  (when-not @cleaning?
    (log/debug "Cleaning cache")
    (reset! cleaning? true)
    (filter-expired!)
    (log/debug "Cleaning cache finished")
    (reset! cleaning? false)))

(defn get-cached
  "Get response of url if one exists"
  [url]
  (when-let [response (time (get @cache url))]
    (when-not (time (expired? response))
      (log/debugf "Using cached version for %s" url)
      response)))

(defn add-cached-response!
  "Add response to cache"
  [url response]
  (swap! cache assoc url
         (assoc response
                :timestamp (t/now)
                :ehoks-cached true
                :cached :HIT)))

(defn encode-url [url params]
  (if (empty? params)
    url
    (format "%s?%s" url (codec/form-encode params))))

(defn with-cache!
  "Returns cached response if one exists. In other case performs request and
   stores response to cache."
  [{url :url options :options :as data}]
  (or (get-cached (time (encode-url url (:query-params options))))
      (let [response (if (:authenticate? data)
                       (cas/with-service-ticket data)
                       (c/with-api-headers data))]
        (future (clean-cache!))
        (add-cached-response!
          (encode-url url (:query-params options)) response)
        (assoc response :cached :MISS))))

(defn clear-cache!
  "Removes all items in cache"
  []
  (reset! cache {}))
