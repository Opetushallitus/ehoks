(ns oph.ehoks.external.connection
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.http-client :as client])
  (:import [com.fasterxml.jackson.core JsonParseException]))

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
                                    :url url
                                    :query-params (:query-params options)}})
                      e)))))
