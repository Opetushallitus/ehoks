(ns oph.ehoks.external.koski
  (:require [clojure.core.cache :as cache]
            [clojure.core.memoize :as memo]
            [clojure.data.json :as json]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.connection :as c]
            [oph.ehoks.external.oph-url :as u]
            [ring.util.http-status :as status])
  (:import (clojure.lang ExceptionInfo)))

(defn filter-oppija
  "Poistaa ylimääräiset avaimet henkilö-alaobjektista."
  [values]
  (update values :henkilö select-keys
          [:oid :hetu :syntymäaika :etunimet :kutsumanimi :sukunimi]))

(defn- with-fifo-ttl-cache
  [f ttl-millis fifo-threshold seed]
  (let [cache (-> {}
                  (cache/fifo-cache-factory :threshold fifo-threshold)
                  (cache/ttl-cache-factory :ttl ttl-millis))]
    (memo/memoizer f cache seed)))

(def get-oppijat-opiskeluoikeudet
  "Palauttaa annettujen oppijoiden kaikki opiskeluoikeudet"
  (with-fifo-ttl-cache
    (fn [oppija-oids]
      (:body
        (c/with-api-headers
          {:method :post
           :service (u/get-url "koski-url")
           :url (u/get-url "koski.post-sure-oids")
           :options {:body (json/write-str oppija-oids)
                     :basic-auth [(:cas-username config) (:cas-password config)]
                     :content-type :json
                     :as :json}})))
    (or (:koski-oppija-cache-ttl-millis config) 5000)
    30
    {}))

(defn get-oppija-opiskeluoikeudet
  "Palauttaa oppijan opiskeluoikeudet"
  [oppija-oid]
  (some
    #(when (= (get-in % [:henkilö :oid]) oppija-oid)
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
  (with-fifo-ttl-cache
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
  "Get info about opiskeluoikeus with `oid` from Koski. Returns `nil` if
  opiskeluoikeus is not found from Koski. Throws an exception in case of
  excetional status codes."
  [oid]
  (try
    (get-opiskeluoikeus-info-raw oid)
    (catch ExceptionInfo e
      (let [koski-virhekoodi (virhekoodi e)]
        (when-not (and (= (:status (ex-data e)) status/not-found)
                       (= koski-virhekoodi
                          "notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia"))
          (throw (ex-info "Error while fetching opiskeluoikeus from Koski"
                          {:type              ::opiskeluoikeus-fetching-error
                           :opiskeluoikeus-oid oid
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
