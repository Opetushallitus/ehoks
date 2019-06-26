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

(defn size [] (count @cache))

(defn expired? [response]
  (and (some? (:timestamp response))
       (t/before?
         (:timestamp response)
         (t/minus (t/now) (t/minutes (:ext-cache-lifetime-minutes config))))))

(defn expire-response! [url]
  (swap! cache dissoc url)
  nil)

(defn clean-cache! []
  (when-not @cleaning?
    (log/debug "Cleaning cache")
    (reset! cleaning? true)
    (let [non-expired
          (reduce
            (fn [n [k v]]
              (if (expired? v)
                n
                (assoc n k v)))
            {}
            @cache)]
      (reset! cache non-expired))
    (log/debug "Cleaning cache finished")
    (reset! cleaning? false)))

(defn get-cached [url]
  (when-let [response (get @cache url)]
    (when-not (expired? response)
      (log/debugf "Using cached version for %s" url)
      response)))

(defn add-cached-response! [url response]
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
  [{service :service url :url options :options :as data}]
  (or (get-cached (encode-url url (:query-params options)))
      (let [response (if (:authenticate? data)
                       (cas/with-service-ticket data)
                       (c/with-api-headers data))]
        (future (clean-cache!))
        (add-cached-response!
          (encode-url url (:query-params options)) response)
        (assoc response :cached :MISS))))

(defn clear-cache! []
  (reset! cache {}))
