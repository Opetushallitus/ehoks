(ns oph.ehoks.db.queries
  (:require [clojure.java.io :as io]))

(defmacro defq [name filename]
  `(def ~name (slurp (io/file (io/resource ~filename))))) ;~(slurp (io/file (io/resource filename)))

(defq select-hoks-by-oppija-oid "hoks/select_by_oppija_oid.sql")
