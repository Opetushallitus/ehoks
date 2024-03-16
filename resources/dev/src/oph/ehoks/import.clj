(ns oph.ehoks.import
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [oph.ehoks.hoks :as hoks]
            [clj-time.coerce :as coerce]
            [clj-time.format :as f])
  (:import java.time.LocalDate))

(def timestamps
  #{:luotu :hyvaksytty :ensikertainen-hyvaksyminen :paivitetty})

(def dates
  #{:alku :loppu :osaamisen-saavuttamisen-pvm :lahetetty-arvioitavaksi})

(defn parse-date [s]
  (coerce/to-date (f/parse s)))

(defn value-reader [k v]
  (cond (contains? timestamps k) (parse-date v)
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
