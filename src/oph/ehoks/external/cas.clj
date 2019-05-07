(ns oph.ehoks.external.cas
  (:require [oph.ehoks.external.connection :as c]
            [oph.ehoks.config :refer [config]]
            [clojure.data.xml :as xml]
            [clj-time.core :as t]
            [oph.ehoks.external.oph-url :as u]))

(defonce service-ticket
  ^:private
  (atom {:url nil
         :expires nil}))

(defn get-cas-url [service]
  (cond
    (.contains service (u/get-url "ehoks.virkailija-login-return"))
    service
    (.contains service "ehoks-backend")
    (format "%s/cas-security-check" service)
    :else
    (format "%s/j_spring_cas_security_check" service)))

(defn refresh-service-ticket! []
  (let [response (c/with-api-headers
                   {:method :post
                    :service (u/get-url "cas.service-ticket")
                    :url (u/get-url "cas.service-ticket")
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
            :url url
            :options
            {:form-params
             {:service (get-cas-url service)}}})))

(defn add-cas-ticket [data service]
  (when (or (nil? (:url @service-ticket))
            (t/after? (t/now) (:expires @service-ticket)))
    (refresh-service-ticket!))
  (let [ticket (get-service-ticket (:url @service-ticket) service)]
    (-> data
        (assoc-in [:headers "accept"] "*/*")
        (assoc-in [:headers "ticket"] ticket)
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
  (let [m (xml->map data)
        success (some?
                  (find-value m [:serviceResponse :authenticationSuccess]))]
    {:success? success
     :error (when-not success
              (first (find-value m [:serviceResponse :authenticationFailure])))
     :user (first
             (find-value m [:serviceResponse :authenticationSuccess :user]))}))

(defn validate-ticket [service ticket]
  (let [response (c/with-api-headers
                   {:method :get
                    :service (u/get-url "cas.validate-service")
                    :url (u/get-url "cas.validate-service")
                    :options
                    {:query-params
                     {:service (get-cas-url service)
                      :ticket ticket}}})]
    (let [xml-data (xml/parse-str (:body response))]
      (convert-response-data xml-data))))
