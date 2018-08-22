(ns oph.ehoks.config
  (:require [environ.core :refer [env]]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def ^:private defaults-file "config/defaults.edn")

(defn- load-config [file]
  (with-open [reader (io/reader file)]
    (edn/read (java.io.PushbackReader. reader))))

(def ^:private default-config (load-config defaults-file))

(def config (merge default-config
                   (select-keys env (keys default-config))))
