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

(defn- get-opiskeluoikeudet!
  [oppija-oids]
  (if (> (count oppija-oids) max-oppijat-query-size)
    (let [current-oppija-oids (take max-oppijat-query-size oppija-oids)
          remaining-oppija-oids (drop max-oppijat-query-size oppija-oids)]
      (concat (get-oppijoiden-opiskeluoikeudet! current-oppija-oids)
              (get-opiskeluoikeudet! remaining-oppija-oids)))
    (get-oppijoiden-opiskeluoikeudet! oppija-oids)))

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

(defn read-csv
  [filename]
  (csv-data->maps
    (with-open [reader (io/reader filename)]
      (doall
        (csv/read-csv reader)))))

(defn write-csv
  [filename table]
  (with-open [writer (io/writer filename)]
    (csv/write-csv writer table)))

(defn export! []
  "Vaatii karvi-in.csv tiedoston projektin juureen. Muoto:
   oppija-oid,opiskeluoikeus-oid,urasuunnitelma-koodi-uri
   123,234,koodi_1

   Input-tiedoston voi muodostaa esim kantakyselyllä:
   select oppija_oid, opiskeluoikeus_oid, urasuunnitelma_koodi_uri
   from hoksit where deleted_at is null order by 1

   psql -h localhost -p 5432 -d ehoks -U readonly -f karvi-q.sql --csv -F \",\" -o karvi-in.csv

   Tuottaa samanmuotoisen tiedoston karvi-out.csv"
  (let [hoksit-in (read-csv "karvi-in.csv")
        oppija-oids (set (map :oppija-oid hoksit-in))
        _ (println "oppija-oideja kaikkiaan:" (count oppija-oids))
        all-opiskeluoikeudet (get-opiskeluoikeudet!
                               oppija-oids)
        opiskeluoikeudet (filter matching-opiskeluoikeus all-opiskeluoikeudet)
        opiskeluoikeus-oidit (set (map :oid opiskeluoikeudet))
        hoksit-out (filter #(contains? opiskeluoikeus-oidit
                             (:opiskeluoikeus-oid %))
                           hoksit-in)
        headers ["oppija-oid" "opiskeluoikeus-oid" "urasuunnitelma-koodi-uri"]]
    (write-csv
      "karvi-out.csv"
      (concat [headers]
              (map (fn [hoks] [(:oppija-oid hoks)
                               (:opiskeluoikeus-oid hoks)
                               (:urasuunnitelma-koodi-uri hoks)])
                   hoksit-out)))))
