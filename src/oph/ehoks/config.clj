(ns oph.ehoks.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [oph.ehoks.schema :as schema]
            [schema.core :as s]))

(def ^:private default-file "config/default.edn")

(defn- load-config [file]
  (with-open [reader (io/reader file)]
    (edn/read (java.io.PushbackReader. reader))))

(def config
  (let [default-config (load-config default-file)
        custom-file (or (System/getenv "CONFIG")
                        (System/getProperty "config"))
        custom-config (if (seq custom-file) (load-config custom-file) {})]
    ;TODO log used files and current configuration (with credential masking)
    (s/validate
      schema/Config
      (merge default-config custom-config))))
