(ns oph.ehoks.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def ^:private default-file "config/default.edn")

(defn- load-config [file]
  (with-open [reader (io/reader file)]
    (edn/read (java.io.PushbackReader. reader))))

(def config
  (let [path (or (System/getenv "CONFIG")
                 (System/getProperty "config")
                 default-file)]
    (println "Load config from" path)  ;; TODO: print via logging
    (load-config path)))
