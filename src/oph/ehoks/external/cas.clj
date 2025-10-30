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
  [^String service]
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
  ([data service]
    (add-cas-ticket data service false))
  ([data service force-refresh-tgt?]
    (when (or force-refresh-tgt?
              (nil? (:url @grant-ticket))
              (t/after? (t/now) (:expires @grant-ticket)))
      (refresh-grant-ticket!))
    (let [ticket (try
                   (get-service-ticket (:url @grant-ticket) service)
                   (catch Exception e
                     (if (= 404 (:status (ex-data e)))
                       ; Refresh Ticket Granting Ticket and retry,
                       ; when fetch Service Ticket call returns 404.
                       ; This potentially happens after CAS service restart.
                       (do
                         (refresh-grant-ticket!)
                         (try
                           (get-service-ticket (:url @grant-ticket) service)
                           (catch Exception ex
                             (throw (ex-info "Error getting Service Ticket"
                                             {:type ::unauthorized}
                                             ex)))))
                       (throw (ex-info "Error getting Service Ticket"
                                       {:type ::unauthorized}
                                       e)))))]
      (-> data
          (assoc-in [:headers "accept"] "*/*")
          (assoc-in [:headers "ticket"] ticket)
          (assoc-in [:query-params :ticket] ticket)))))

(defn with-service-ticket
  "Perform request with API headers and valid service ticket"
  [data]
  (try
    (c/with-api-headers
      (update data :options add-cas-ticket (:service data)))
    (catch Exception e
      (if (= (:status (ex-data e)) 401)
        ; Retry the same request with a new Ticket Granting Ticket (TGT)
        (c/with-api-headers
          (update data :options add-cas-ticket (:service data) true))
        (throw e)))))

(defn xml->map
  "Convert XML data to Clojure map"
  [x]
  (hash-map
    (:tag x)
    (map
      #(if (instance? clojure.data.xml.Element %)
         (xml->map %)
         %)
      (:content x))))

(defn find-value
  "Recursively fetch the given keypath in map (similar to get-in), but for
  lists, fetch the first element that has the key we're looking for next"
  [data keypath]
  (reduce (fn [m key] (or (get m key) (some #(get % key) m)))
          data keypath))

(defn- convert-response-data
  "Extracts user and/or error info from response data"
  [data]
  (let [m (xml->map data)
        response (find-value m [:serviceResponse :authenticationSuccess])]
    {:success? (some? response)
     :error (first (find-value m [:serviceResponse :authenticationFailure]))
     :user (first (find-value response [:user]))
     :username (first (find-value response [:user]))
     :kayttajaTyyppi (first (find-value response
                                        [:attributes :kayttajaTyyppi]))
     :oidHenkilo (first (find-value response [:attributes :oidHenkilo]))
     :roles (keep (comp first :roles) (find-value response [:attributes]))}))

(defn- using-valtuudet?
  "Check whether user is using valtuudet"
  [response]
  (boolean (or (find-value
                 response
                 [:serviceResponse :authenticationSuccess
                  :attributes :impersonatorNationalIdentificationNumber])
               (find-value
                 response
                 [:serviceResponse :authenticationSuccess
                  :attributes :impersonatorDisplayName]))))

(defn- convert-oppija-cas-response-data
  "Extracts user OID and valtuudet info, or error, from CAS response data"
  [xml-data]
  (let [response (xml->map xml-data)
        success (some?
                  (find-value response
                              [:serviceResponse :authenticationSuccess]))
        using-valtuudet (using-valtuudet? response)]
    (log/infof "Response: %s" response)
    {:success? success
     :error (when-not success
              (first
                (find-value
                  response
                  [:serviceResponse :authenticationFailure])))
     :user-oid (if-not using-valtuudet
                 (first
                   (find-value
                     response
                     [:serviceResponse :authenticationSuccess
                      :attributes :personOid]))
                 (first
                   (find-value
                     response
                     [:serviceResponse :authenticationSuccess
                      :attributes :impersonatorPersonOid])))
     :usingValtuudet using-valtuudet}))

(defn validate-ticket
  "Validate service ticket"
  [service ticket]
  (let [validate-endpoint (u/get-url "cas.validate-service")]
    (-> {:method :get
         :service validate-endpoint
         :url validate-endpoint
         :options {:query-params {:service (get-cas-url service)
                                  :ticket ticket}}}
        (c/with-api-headers)
        :body
        (xml/parse-str)
        (convert-response-data))))

(defn- call-cas-oppija-ticket-validation
  "Do CAS oppija ticket valiation"
  [ticket ^String domain]
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

(def role-name->privileges
  "Resolves OPH role name to set of eHOKS privileges"
  {"CRUD"           #{:read :write :update :delete}
   "OPHPAAKAYTTAJA" #{:read :write :update :delete}
   "READ"           #{:read}
   "HOKS_DELETE"    #{:hoks_delete}})

(def ehoks-role-re #"ROLE_APP_EHOKS_(\w+)_(1\.2\.246\.562\.10\.\d+)")

(defn roles->org-privileges
  "Convert CAS roles to the format used by eHOKS"
  [roles]
  (keep (fn [role]
          (when-let [[match role-name org-oid] (re-matches ehoks-role-re role)]
            {:oid org-oid
             :privileges (or (role-name->privileges role-name) #{})
             :roles (if (= role-name "OPHPAAKAYTTAJA") #{:oph-super-user} #{})}))
        roles))

(defn validation-data->user-details
  "Convert validate-ticket results to the format earlier returned by
  kayttooikeuspalvelu and get-auth-info"
  [validation-data]
  (assoc validation-data
         :organisation-privileges
         (roles->org-privileges (:roles validation-data))))

(defn service-ticket->user-details!
  "Get username of CAS ticket at given service"
  ([ticket] (service-ticket->user-details!
              (u/get-url "ehoks-virkailija-backend-url")
              ticket))
  ([service ticket]
    (let [validation-data (validate-ticket service ticket)]
      (if (:success? validation-data)
        (validation-data->user-details validation-data)
        (log/warnf "Service ticket validation failed: %s" validation-data)))))
