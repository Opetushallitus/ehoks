(ns oph.ehoks.external.koski
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.connection :as c]
            [ring.util.http-status :as status]
            [clojure.data.json :as json]
            [oph.ehoks.external.oph-url :as u]
            [clojure.core.memoize :as memo]
            [clojure.core.cache :as cache])
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

(defn get-opiskeluoikeus-info
  "Get opiskeluoikeus info with error handling"
  [oid]
  (try
    (get-opiskeluoikeus-info-raw oid)
    (catch ExceptionInfo e
      (let [e-data (ex-data e)
            body (if (some? (:body e-data))
                   (json/read-str (:body e-data) :key-fn keyword)
                   {})]
        (when-not (and (= (:status e-data) status/not-found)
                       (= (get-in body [0 :key])
                          "notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia"))
          (throw e))))))

(defn get-opiskeluoikeus
  "Get opiskeluoikeus. If opiskeluoikeus cannot be fetched, throws an
  exception with cause."
  [oid]
  (try
    (get-opiskeluoikeus-info-raw oid)
    (catch ExceptionInfo e
      (throw
        (ex-info (format "Couldn't get opiskeluoikeus `%s` from Koski." oid)
                 {:type :could-not-get-opiskeluoikeus
                  :opiskeluoikeus-oid oid
                  :status (:status (ex-data e))
                  :virhekoodi (some-> (:body (ex-data e))
                                      (json/read-str :key-fn keyword)
                                      (get-in [0 :key]))}
                 e)))))

(defn get-opiskeluoikeus-oppilaitos-oid
  "Get oppilaitos of opiskeluoikeus"
  [opiskeluoikeus-oid]
  (get-in
    (get-opiskeluoikeus-info opiskeluoikeus-oid)
    [:oppilaitos :oid]))

(defn fetch-opiskeluoikeudet-by-oppija-id
  "Fetches list of opiskeluoikeudet from Koski for oppija"
  [oppija-oid]
  (get-oppija-opiskeluoikeudet oppija-oid))
