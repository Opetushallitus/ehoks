(ns oph.ehoks.db.queries
  (:require [clojure.java.io :as io]))

(defmacro defq [name filename]
  `(def ~name (slurp (io/file (io/resource ~filename))))) ;~(slurp (io/file (io/resource filename)))

(defq select-hoks-by-oppija-oid "hoksit/select_by_oppija_oid.sql")
(defq select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id
  "olemassa-olevat-ammatilliset-tutkinnon-osat/select_by_hoks_id.sql")
