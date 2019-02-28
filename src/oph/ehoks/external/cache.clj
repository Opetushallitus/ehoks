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

(defn expired? [response]
  (and (some? (:timestamp response))
       (t/before?
         (:timestamp response)
         (t/minus (t/now) (t/minutes (:ext-cache-lifetime-minutes config))))))

(defn expire-response! [url]
  (swap! cache dissoc url)
  nil)

(defn clean-cache! []
  (let [non-expired
        (reduce
          (fn [n [k v]]
            (if (expired? v)
              n
              (assoc n k v)))
          {}
          @cache)]
    (reset! cache non-expired)))

(defn get-cached [url]
  (when-let [response (get @cache url)]
    (when-not (expired? response)
      (log/debug "Using cached version for " url)
      response)))

(defn add-cached-response! [url response]
  (swap! cache assoc url
         (assoc response
                :timestamp (t/now)
                :ehoks-cached true
                :cached :HIT)))

(defn encode-url [url path params]
  (let [base (format "%s/%s" url path)]
    (if (empty? params)
      base
      (format "%s?%s" base (codec/form-encode params)))))

(defn with-cache!
  [{service :service path :path options :options :as data}]
  (or (get-cached (encode-url service path (:query-params options)))
      (let [response (if (:authenticate? data)
                       (cas/with-service-ticket data)
                       (c/with-api-headers data))]
        (add-cached-response!
          (encode-url service path (:query-params options)) response)
        (assoc response :cached :MISS))))
