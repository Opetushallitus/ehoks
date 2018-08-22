(ns oph.ehoks.config
  (:require [environ.core :refer [env]]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def ^:private env-file (get env :config "config/prod.edn"))

(def ^:private defaults-file "config/defaults.edn")

(defn- load-config [file]
  (with-open [reader (io/reader file)]
    (edn/read (java.io.PushbackReader. reader))))

(def config (merge (load-config defaults-file)
                   (load-config env-file)))
