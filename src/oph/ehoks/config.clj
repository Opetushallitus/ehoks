(ns oph.ehoks.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def ^:private default-file "config/default.edn")

(defn- load-config [file]
  (with-open [reader (io/reader file)]
    (edn/read (java.io.PushbackReader. reader))))

(def config (load-config (or (System/getenv "CONFIG") default-file)))
