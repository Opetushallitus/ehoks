(ns oph.ehoks.external.cas
  (:require [oph.ehoks.external.connection :as c]
            [oph.ehoks.config :refer [config]]
            [clojure.data.xml :as xml]
            [clj-time.core :as t]))

(defonce service-ticket
  ^:private
  (atom {:url nil
         :expires nil}))

(defn refresh-service-ticket! []
  (let [response (c/with-api-headers
                   {:method :post
                    :service
                    (str (:cas-service-ticket-url config) "/v1/tickets")
                    :options {:form-params {:username (:cas-username config)
                                            :password (:cas-password config)}}})
        url (get-in response [:headers "location"])]
    (if (and (= (:status response) 201)
             (seq url))
      (reset! service-ticket
              {:url url
               :expires (t/plus (t/now) (t/hours 2))})
      (throw (ex-info "Failed to refresh CAS Service Ticket"
                      {:response response
                       :log-data {:status (:status response)
                                  :body (:body response)}})))))

(defn get-service-ticket [url service]
  (:body (c/with-api-headers
           {:method :post
            :service url
            :options
            {:form-params
             {:service (str service "/j_spring_cas_security_check")}}})))

(defn add-cas-ticket [data service]
  (when (or (nil? (:url @service-ticket))
            (t/after? (t/now) (:expires @service-ticket)))
    (refresh-service-ticket!))
  (let [ticket (get-service-ticket (:url @service-ticket) service)]
    (-> data
        (assoc-in [:headers "accept"] "*/*")
        (assoc-in [:query-params :ticket] ticket))))

(defn with-service-ticket [data]
  (c/with-api-headers
    (update data :options add-cas-ticket (:service data))))

(defn xml->map [x]
  (hash-map
    (:tag x)
    (map
      #(if (= (type %) clojure.data.xml.Element)
         (xml->map %)
         %)
      (:content x))))

(defn find-value [m init-ks]
  (loop [c (get m (first init-ks)) ks (rest init-ks)]
    (if (empty? ks)
      c
      (let [k (first ks)]
        (recur
          (if (map? c)
            (get c k)
            (some #(get % k) c))
          (rest ks))))))

(defn convert-response-data [data]
  (let [m (xml->map data)]
    {:success? (some? (find-value m [:serviceResponse :authenticationSuccess]))
     :user (first
             (find-value m [:serviceResponse :authenticationSuccess :user]))}))

(defn validate-ticket [service ticket]
  (let [response (c/with-api-headers
                   {:method :get
                    :service (str (:cas-service-ticket-url config) "/p3")
                    :path "serviceValidate"
                    :options
                    {:query-params
                     {:service (str service "/j_spring_cas_security_check")
                      :ticket ticket}}})]
    (let [xml-data (xml/parse-str (:body response))]
      (convert-response-data xml-data))))
