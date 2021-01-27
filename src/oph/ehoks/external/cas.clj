(ns oph.ehoks.external.cas
  (:require [oph.ehoks.external.connection :as c]
            [oph.ehoks.config :refer [config]]
            [clojure.data.xml :as xml]
            [clj-time.core :as t]
            [oph.ehoks.external.oph-url :as u]
            [clojure.tools.logging :as log]))

(defonce grant-ticket
  ^:private
  (atom {:url nil
         :expires nil}))

(defn- get-cas-url
  "Get CAS url (service param) of url. Some uses spring and ehoks uses
   cas-security-check."
  [service]
  (cond
    (.contains service (u/get-url "ehoks.virkailija-login-return"))
    service
    (.contains service "ehoks-virkailija-backend")
    (format "%s/cas-security-check" service)
    :else
    (format "%s/j_spring_cas_security_check" service)))

(defn refresh-grant-ticket!
  "Get new ticket granting ticket"
  []
  (let [response (c/with-api-headers
                   {:method :post
                    :service (u/get-url "cas.service-ticket")
                    :url (u/get-url "cas.service-ticket")
                    :options {:form-params {:username (:cas-username config)
                                            :password (:cas-password config)}}})
        url (get-in response [:headers "location"])]
    (if (and (= (:status response) 201)
             (seq url))
      (reset! grant-ticket
              {:url url
               :expires (t/plus (t/now) (t/hours 2))})
      (throw (ex-info "Failed to refresh CAS Service Ticket"
                      {:response response
                       :log-data {:status (:status response)
                                  :body (:body response)
                                  :location url}})))))

(defn get-service-ticket
  "Get new service ticket"
  [url service]
  (:body (c/with-api-headers
           {:method :post
            :service url
            :url url
            :options
            {:form-params
             {:service (get-cas-url service)}}})))

(defn add-cas-ticket
  "Add service ticket to headers and params"
  [data service]
  (when (or (nil? (:url @grant-ticket))
            (t/after? (t/now) (:expires @grant-ticket)))
    (refresh-grant-ticket!))
  (let [ticket (get-service-ticket (:url @grant-ticket) service)]
    (-> data
        (assoc-in [:headers "accept"] "*/*")
        (assoc-in [:headers "ticket"] ticket)
        (assoc-in [:query-params :ticket] ticket))))

(defn with-service-ticket
  "Perform request with API headers and valid service ticket"
  [data]
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

(defn find-value
  "Find value in map"
  [m init-ks]
  (loop [c (get m (first init-ks)) ks (rest init-ks)]
    (if (empty? ks)
      c
      (let [k (first ks)]
        (recur
          (if (map? c)
            (get c k)
            (some #(get % k) c))
          (rest ks))))))

(defn- convert-response-data [data]
  (let [m (xml->map data)
        success (some?
                  (find-value m [:serviceResponse :authenticationSuccess]))]
    {:success? success
     :error (when-not success
              (first (find-value m [:serviceResponse :authenticationFailure])))
     :user (first
             (find-value m [:serviceResponse :authenticationSuccess :user]))}))

(defn- using-valtuudet? [response]
  (boolean (or (find-value
                 response
                 [:serviceResponse :authenticationSuccess
                  :attributes :impersonatorNationalIdentificationNumber])
               (find-value
                 response
                 [:serviceResponse :authenticationSuccess
                  :attributes :impersonatorDisplayName]))))

(defn- convert-oppija-cas-response-data [xml-data]
  (let [response (xml->map xml-data)
        success (some?
                  (find-value response
                              [:serviceResponse :authenticationSuccess]))
        attributes (find-value
                     response
                     [:serviceResponse :authenticationSuccess
                      :attributes])
        using-valtuudet (using-valtuudet? response)]
    (log/infof "Response: %s" response)
    {:success? success
     :error (when-not success
              (first
                (find-value
                  response
                  [:serviceResponse :authenticationFailure])))
     :user-oid (first
                 (find-value
                   response
                   [:serviceResponse :authenticationSuccess
                    :attributes :personOid]))
     :using-valtuudet using-valtuudet}))

(defn validate-ticket
  "Validate service ticket"
  [service ticket]
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

(defn- call-cas-oppija-ticket-validation [ticket domain]
  (c/with-api-headers
    {:method :get
     :service (u/get-url "cas-oppija.validate-service")
     :url (u/get-url "cas-oppija.validate-service")
     :options
     {:query-params
      {:service (format
                  "%s%s"
                  (if (.contains domain "studieinfo")
                    (:frontend-url-sv config)
                    (:frontend-url-fi config))
                  (u/get-url "ehoks.oppija-login-return-path"))
       :ticket ticket}}}))

(defn validate-oppija-ticket
  "Validate oppija cas service ticket"
  [ticket, domain]
  (let [response (call-cas-oppija-ticket-validation ticket domain)]
    (let [xml-data (xml/parse-str (:body response))]
      (convert-oppija-cas-response-data xml-data))))
