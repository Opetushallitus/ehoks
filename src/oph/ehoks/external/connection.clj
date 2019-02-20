(ns oph.ehoks.external.connection
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.http-client :as client]
            [clojure.string :as cstr])
  (:import [com.fasterxml.jackson.core JsonParseException]))

(def allowed-params
  #{:tutkintonimikkeet :tutkinnonosat :osaamisalat :category})

(def oid-pattern
  #"(\d+\.){5}\d+")

(defn sanitaze-path [path]
  (when (some? path)
    (cstr/replace
      path
      oid-pattern
      "*FILTERED*")))

(defn sanitaze-params [options]
  (if (and (some? options) (some? (:query-params options)))
    (assoc
      options
      :query-params
      (reduce
        (fn [n [k v]]
          (if (contains? allowed-params k)
            (assoc n k v)
            (assoc n k "*FILTERED*")))
        {}
        (:query-params options)))
    options))

(defn- get-client-fn [method]
  (if (= method :post)
    client/post
    client/get))

(defn with-api-headers
  [{method :method service :service options :options path :path}]
  (try
    (let [client-method-fn (get-client-fn method)]
      (client-method-fn (if (some? path)
                          (format "%s/%s" service path)
                          service)
                        (-> options
                            (assoc-in [:headers "Caller-Id"]
                                      (:client-sub-system-code config))
                            (assoc :debug (:debug config false))
                            (assoc :cookie-policy :standard))))
    (catch Exception e
      (throw (ex-info "HTTP request error"
                      (merge
                        (ex-data e)
                        {:log-data {:method method
                                    :service service
                                    :path (sanitaze-path path)
                                    :query-params (sanitaze-params
                                                    (:query-params options))}})
                      e)))))
