(ns oph.ehoks.external.koski
  (:require [oph.ehoks.config :refer [config]]
            [oph.ehoks.external.connection :as c]
            [ring.util.http-status :as status]
            [clojure.data.json :as json]
            [oph.ehoks.external.oph-url :as u]
            [clojure.core.memoize :as memo])
  (:import (clojure.lang ExceptionInfo)))

(defn filter-oppija
  "Poistaa ylimääräiset avaimet henkilö-alaobjektista."
  [values]
  (update values :henkilö select-keys
          [:oid :hetu :syntymäaika :etunimet :kutsumanimi :sukunimi]))

(defn get-oppijat-opiskeluoikeudet
  "Palauttaa annettujen oppijoiden kaikki opiskeluoikeudet"
  [oppija-oids]
  (:body
    (c/with-api-headers
      {:method :post
       :service (u/get-url "koski-url")
       :url (u/get-url "koski.post-sure-oids")
       :options {:body (json/write-str oppija-oids)
                 :basic-auth [(:cas-username config) (:cas-password config)]
                 :content-type :json
                 :as :json}})))

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

(defn get-opiskeluoikeus-info-raw
  "Get opiskeluoikeus info without error handling"
  [oid]
  (:body
    (c/with-api-headers
      {:method :get
       :service (u/get-url "koski-url")
       :url (u/get-url "koski.get-opiskeluoikeus" oid)
       :options {:basic-auth [(:cas-username config) (:cas-password config)]
                 :as :json}})))

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

(def get-opiskeluoikeus-type
  (memo/ttl
    (fn [opiskeluoikeus-oid]
      (get-in
        (get-opiskeluoikeus-info opiskeluoikeus-oid)
        [:tyyppi :koodiarvo]))
    {}
    :ttl/threshold 300))

(defn tuva-opiskeluoikeus?
  "Tarkistaa, onko opiskeluoikeus TUVA-opiskeluoikeus."
  [opiskeluoikeus-oid]
  (= "tuva" (get-opiskeluoikeus-type opiskeluoikeus-oid)))

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
