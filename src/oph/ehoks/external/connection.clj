(ns oph.ehoks.external.connection
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.http-client :as client]
            [clojure.string :as cstr])
  (:import [com.fasterxml.jackson.core JsonParseException]))

(def allowed-params
  "Allowed parameters"
  #{:tutkintonimikkeet :tutkinnonosat :osaamisalat :category})

(def oid-pattern
  "Pattern that all OIDs must match"
  #"(\d+\.){5}\d+")

(defn sanitaze-path
  "Remove oids from path"
  [path]
  (when (some? path)
    (cstr/replace
      path
      oid-pattern
      "*FILTERED*")))

(defn sanitaze-params
  "Remove non-allower params"
  [options]
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

(defn- get-client-fn
  "Get appropriate REST function for given keyword."
  [method]
  (cond
    (= method :delete) client/delete
    (= method :get)    client/get
    (= method :post)   client/post
    :else              (throw (ex-info "Unsupported method" {:method method}))))

(defn with-api-headers
  "Perform request with API headers (OPH Caller ID) and error handling with
   logging."
  [{method :method options :options url :url}]
  (try
    (let [client-method-fn (get-client-fn method)]
      (client-method-fn url
                        (-> options
                            (assoc-in [:headers "Caller-Id"]
                                      (:client-sub-system-code config))
                            (assoc-in [:headers "CSRF"]
                                      (:client-sub-system-code config))
                            (assoc-in [:cookies "CSRF"]
                                      {:value (:client-sub-system-code config)
                                       :path "/"})
                            (assoc :debug (:debug config false))
                            (assoc :cookie-policy :standard))))
    (catch Exception e
      (throw (ex-info (format "HTTP request error: %s" (.getMessage e))
                      (merge
                        (ex-data e)
                        {:log-data {:method method
                                    :url (sanitaze-path url)
                                    :query-params (sanitaze-params
                                                    (:query-params options))}})
                      e)))))
