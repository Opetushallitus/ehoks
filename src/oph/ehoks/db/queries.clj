(ns oph.ehoks.db.queries
  (:require [clojure.java.io :as io]))

(defmacro defq [name filename]
  `(def ~name (slurp (io/file (io/resource ~filename)))))

(defq select-hoks-by-oppija-oid "hoksit/select_by_oppija_oid.sql")
(defq select-hoks-by-id "hoksit/select_by_id.sql")
(defq select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id
  "olemassa-olevat-ammatilliset-tutkinnon-osat/select_by_hoks_id.sql")
(defq select-puuttuvat-paikalliset-tutkinnon-osat-by-hoks-id
  "puuttuvat-paikalliset-tutkinnon-osat/select_by_hoks_id.sql")
(defq select-olemassa-olevat-paikalliset-tutkinnon-osat-by-hoks-id
  "olemassa-olevat-paikalliset-tutkinnon-osat/select_by_hoks_id.sql")
(defq select-olemassa-olevat-yhteiset-tutkinnon-osat-by-hoks-id
  "olemassa-olevat-yhteiset-tutkinnon-osat/select_by_hoks_id.sql")
(defq select-hankitun-osaamisen-naytot-by-ppto-id
  "puuttuvat-paikalliset-tutkinnon-osat/select_hankitun_osaamisen_naytot.sql")
