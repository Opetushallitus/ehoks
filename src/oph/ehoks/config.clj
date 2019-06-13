(ns oph.ehoks.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [oph.ehoks.schema :as schema]
            [schema.core :as s]
            [environ.core :refer [env]]))

(def ^:private default-file "oph-configuration/default.edn")

(defn- load-config [file]
  (with-open [reader (io/reader file)]
    (edn/read (java.io.PushbackReader. reader))))

(defn load-combined-config [custom-file]
  (let [default-config (load-config default-file)
        custom-config (if (seq custom-file) (load-config custom-file) {})]
    (s/validate
      schema/Config
      (merge default-config custom-config))))

(def config (when-not *compile-files*
              (load-combined-config
                (or (System/getenv "CONFIG")
                    (System/getProperty "config")
                    (:config env)))))
