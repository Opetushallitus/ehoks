(ns oph.ehoks.migration-tools
  (:require [clojure.string :as s]))

(defn generate-migration! [^String folder ^String title]
  (if (.exists (clojure.java.io/file folder))
    (let [filename (format "V1_%d__%s.sql"
                           (System/currentTimeMillis)
                           (s/replace title #" |-" "_"))
          file (clojure.java.io/file folder filename)]
      (spit file "")
      (printf "Migration '%s' created.\n" (.getPath file)))
    (printf "Folder '%s' does not exist.\n" folder)))

(defn lein-genmigration [& args]
  (if (= (count args) 2)
    (generate-migration! (first args) (second args))
    (do (println "Usage: lein genmigration [path] [title]")
        (println
          "For example: lein genmigration src/db/migration \"Hello world\""))))