(ns oph.ehoks.config
  (:require [environ.core :refer [env]]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def ^:private file (get env :config "config/prod.edn"))

(def config (with-open [reader (io/reader file)]
              (edn/read (java.io.PushbackReader. reader))))
