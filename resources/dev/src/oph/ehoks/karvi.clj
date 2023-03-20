(ns oph.ehoks.karvi
  (:require [oph.ehoks.external.koski :as k]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:import (java.time LocalDate)
           (clojure.lang ExceptionInfo)))

(def ^:private earliest-opiskeluoikeus-alkamispäivä
  (LocalDate/parse "2018-01-01"))
(def ^:private max-oppijat-query-size 1000)
(def ^:private retry-wait-ms 10000)

(defn- with-retries
  [retries f]
  (try
    (f)
    (catch ExceptionInfo e
      (println "virhe", e)
      (if (< 0 retries)
        (do
          (println "yritetään uudelleen hetken kuluttua")
          (Thread/sleep retry-wait-ms)
          (with-retries (dec retries) f))
        (do
          (println
            "uudelleen yrittämiset eivät tuottanut tulosta, keskeytetään")
          (throw e))))))

(defn- get-oppijoiden-opiskeluoikeudet!
  [oppija-oids]
  (println "kutsutaan Koskea, oppija-oideja:" (count oppija-oids))
  (let [oppijat (with-retries 2
                              (fn []
                                (k/get-oppijat-opiskeluoikeudet oppija-oids)))]
    (flatten (map (fn [oppija]
                    (map (fn [opiskeluoikeus]
                           (select-keys opiskeluoikeus
                                        [:oid :alkamispäivä :tila]))
                         (:opiskeluoikeudet oppija)))
                  oppijat))))

(defn- matching-opiskeluoikeus
  [opiskeluoikeus]
  (let [opiskeluoikeusjaksot (sort-by :alku
                                      (get-in opiskeluoikeus
                                              [:tila :opiskeluoikeusjaksot]))]
    (and (:alkamispäivä opiskeluoikeus)
         (not (.isBefore (LocalDate/parse (:alkamispäivä opiskeluoikeus))
                         earliest-opiskeluoikeus-alkamispäivä))
         (= (get-in (last opiskeluoikeusjaksot) [:tila :koodiarvo])
            "valmistunut"))))

(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (first csv-data) ;; First row is the header
            (map keyword)
            repeat)
       (rest csv-data)))

(defn export! []
  "Vaatii karvi-in.csv tiedoston projektin juureen. Muoto:
   oppija-oid,opiskeluoikeus-oid,urasuunnitelma-koodi-uri
   123,234,koodi_1

   Input-tiedoston voi muodostaa esim kantakyselyllä:
   select oppija_oid as \"oppija-oid\",
          opiskeluoikeus_oid as \"opiskeluoikeus-oid\",
          urasuunnitelma_koodi_uri as \"urasuunnitelma-koodi-uri\"
     from hoksit where deleted_at is null order by 1;

   psql -h localhost -p 5432 -d ehoks -U readonly -f karvi-query.sql --csv -F \",\" -o karvi-in.csv

   Tuottaa samanmuotoisen tiedoston karvi-out.csv"
  (let [hoksit-in (csv-data->maps
                   (with-open [reader (io/reader "karvi-in.csv")]
                     (doall
                      (csv/read-csv reader))))
        all-oppija-oidit (set (map :oppija-oid hoksit-in))]
    (println "oppija-oideja kaikkiaan:" (count all-oppija-oidit))
    (with-open [writer (io/writer "karvi-out.csv")]
      (csv/write-csv writer [["oppija-oid"
                              "opiskeluoikeus-oid"
                              "urasuunnitelma-koodi-uri"]])
      (.flush writer)
      (doseq [oppija-oidit (partition max-oppijat-query-size all-oppija-oidit)]
        (let [opiskeluoikeudet (get-oppijoiden-opiskeluoikeudet! oppija-oidit)
              matching-opiskeluoikeudet (filter matching-opiskeluoikeus
                                                opiskeluoikeudet)
              opiskeluoikeus-oidit (set (map :oid matching-opiskeluoikeudet))
              hoksit-out-part (filter #(contains? opiskeluoikeus-oidit
                                   (:opiskeluoikeus-oid %))
                                 hoksit-in)]
          (csv/write-csv writer
                         (map (fn [hoks] [(:oppija-oid hoks)
                                          (:opiskeluoikeus-oid hoks)
                                          (:urasuunnitelma-koodi-uri hoks)])
                              hoksit-out-part))
          (.flush writer))))))
