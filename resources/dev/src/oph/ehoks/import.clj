(ns oph.ehoks.import
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [oph.ehoks.hoks :as hoks])
  (:import (java.time LocalDate Instant)
           (java.sql Timestamp)
           (java.time.format DateTimeParseException)))

(def timestamps
  #{:luotu :hyvaksytty :ensikertainen-hyvaksyminen :paivitetty})

(def dates
  #{:alku :loppu :osaamisen-saavuttamisen-pvm :lahetetty-arvioitavaksi})

(defn parse-date-or-time [d]
  (try
    (Instant/parse d)
    (catch DateTimeParseException _
      (println "Warning: time " d " not parseable as instant, trying date")
      (LocalDate/parse d))))

(defn value-reader [k v]
  (cond (contains? timestamps k) (parse-date-or-time v)
        (contains? dates k) (LocalDate/parse v)
        :else v))

(defn read-file [path]
  (with-open [in (io/reader path)]
    (json/read in
               :key-fn keyword
               :value-fn value-reader)))

(defn import-file! [path]
  (let [data (read-file path)]
    (if (map? data)
      (hoks/save! data)
      (map hoks/save! data))))

(defn lein-import-file! [path]
  (printf "Importing file %s\n" path)
  (clojure.pprint/pprint (import-file! path)))
