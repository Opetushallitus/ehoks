(ns oph.ehoks.external.koski
  (:require [clojure.data.json :as json]
            [clojure.string :refer [starts-with?]]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.connection :as c]
            [oph.ehoks.external.oph-url :as u]
            [oph.ehoks.utils :as utils]
            [ring.util.http-status :as status])
  (:import (clojure.lang ExceptionInfo)))

(defn missing-opiskeluoikeus-error?
  "Tells from HTTP status and koski error whether this error is about
  missing opiskeluoikeus."
  [http-status koski-virhekoodi]
  (or
    (and (= http-status status/not-found)
         (#{"notFound"
            "notFound.opiskeluoikeuttaEiLöydy"
            "notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia"}
           koski-virhekoodi))
    (and (= http-status status/bad-request)
         (starts-with? koski-virhekoodi "badRequest.queryParam"))))

(defn background-system-absent?
  "Tells whether an exception is about a downtime / missing dependent service."
  [exdata]
  (let [excl (:exception-class exdata)
        stat (:status exdata)]
    (or
      ;; No route to host (e.g. http://192.168.0.23 in same network)
      (= excl java.net.NoRouteToHostException)
      ;; Connection refused (e.g. http://localhost:9509)
      ;; Connection timed out (e.g. http://10.79.80.90)
      (= excl java.net.ConnectException)
      ;; Unknown host name (e.g. http://foo-bar-baz)
      (= excl java.net.UnknownHostException)
      ;; Load balancer / proxy reports missing service or misconfiguration
      (= stat status/bad-gateway)
      ;; Load balancer / proxy reports downtime in the service
      (= stat status/service-unavailable)
      ;; Service did not respond to load balancer / proxy in time
      (= stat status/gateway-timeout))))

(defn filter-oppija
  "Poistaa ylimääräiset avaimet henkilö-alaobjektista."
  [values]
  (update values :henkilö select-keys
          [:oid :hetu :syntymäaika :etunimet :kutsumanimi :sukunimi]))

(def get-oppijat-opiskeluoikeudet
  "Palauttaa annettujen oppijoiden kaikki opiskeluoikeudet"
  (utils/with-fifo-ttl-cache
    (fn [oppija-oids]
      (try
        (:body
          (c/with-api-headers
            {:method :post
             :service (u/get-url "koski-url")
             :url (u/get-url "koski.post-sure-oids")
             :options {:body (json/write-str oppija-oids)
                       :basic-auth [(:cas-username config)
                                    (:cas-password config)]
                       :content-type :json
                       :as :json}}))
        (catch ExceptionInfo e
          (if (background-system-absent? (ex-data e))
            (throw (ex-info
                     (str "Error while contacting Koski: " (ex-message e))
                     (merge (ex-data e) {:type ::koski-connection-error})))
            (throw e)))))
    (or (:koski-oppija-cache-ttl-millis config) 5000)
    30
    {}))

(defn get-oppija-opiskeluoikeudet
  "Palauttaa oppijan opiskeluoikeudet"
  [oppija-oid]
  (some #(when (= (get-in % [:henkilö :oid]) oppija-oid)
           (:opiskeluoikeudet %))
        (get-oppijat-opiskeluoikeudet [oppija-oid])))

(defn get-student-info
  "Palauttaa opiskelijan henkilötiedot ja opiskeluoikeudet, raskas kysely.
   Suositeltavaa käyttää vain jos tarvitsee molemmat, muissa tilanteissa
   muita rajapintoja."
  [oid]
  (:body
    (c/with-api-headers
      {:method :get
       :service (u/get-url "koski-url")
       :url (u/get-url "koski.get-oppija" oid)
       :options {:basic-auth [(:cas-username config) (:cas-password config)]
                 :as :json}})))

(def get-opiskeluoikeus-info-raw
  "Get opiskeluoikeus info"
  (utils/with-fifo-ttl-cache
    (fn [oid]
      (:body
        (c/with-api-headers
          {:method :get
           :service (u/get-url "koski-url")
           :url (u/get-url "koski.get-opiskeluoikeus" oid)
           :options {:basic-auth [(:cas-username config) (:cas-password config)]
                     :as :json}})))
    (or (:koski-opiskeluoikeus-cache-ttl-millis config) 2000)
    30
    {}))

(defn virhekoodi
  "Takes an ExceptionInfo object and tries to parse Koski-specific error code
  (virhekoodi) from a string in `:body` that is expected to be in JSON format.
  Returns `nil` if the function fails to parse the code for any reason."
  [^ExceptionInfo e]
  (try
    (some-> (:body (ex-data e))
            (json/read-str :key-fn keyword)
            (get-in [0 :key]))
    (catch Exception _ nil)))

(defn get-opiskeluoikeus!
  "Get info about opiskeluoikeus with `oid` from Koski.
  Returns `nil` if opiskeluoikeus is not found from Koski.
  Throws an exception in other cases."
  [oid]
  (try
    (get-opiskeluoikeus-info-raw oid)
    (catch ExceptionInfo e
      (let [http-status      (:status (ex-data e))
            koski-virhekoodi (virhekoodi e)]
        (cond
          (missing-opiskeluoikeus-error? http-status koski-virhekoodi)
          nil
          (background-system-absent? (ex-data e))
          (throw (ex-info
                   (str "Error while contacting Koski: " (ex-message e))
                   (merge (ex-data e) {:type ::koski-connection-error})))
          :else
          (throw (ex-info (format
                            (str "Error while fetching opiskeluoikeus `%s` "
                                 "from Koski. Got response with HTTP status %d "
                                 "and Koski-virhekoodi `%s`.")
                            oid
                            http-status
                            koski-virhekoodi)
                          {:type              ::opiskeluoikeus-fetching-error
                           :opiskeluoikeus-oid oid
                           :http-status        http-status
                           :koski-virhekoodi   koski-virhekoodi}
                          e)))))))

(defn get-existing-opiskeluoikeus!
  "Like `get-opiskeluoikeus!` but expects that opiskeluoikeus with `oid` is
  found from Koski and thus, throws an exception if no opiskeluoikeus is found."
  [oid]
  (if-let [opiskeluoikeus (get-opiskeluoikeus! oid)]
    opiskeluoikeus
    (throw (ex-info (format "Opiskeluoikeus `%s` not found in Koski" oid)
                    {:type               ::opiskeluoikeus-not-found
                     :opiskeluoikeus-oid oid}))))

(defn fetch-opiskeluoikeudet-by-oppija-id
  "Fetches list of opiskeluoikeudet from Koski for oppija"
  [oppija-oid]
  (get-oppija-opiskeluoikeudet oppija-oid))
