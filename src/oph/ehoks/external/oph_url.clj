(ns oph.ehoks.external.oph-url
  (:require [clojure.java.io :as io]
            [oph.ehoks.config :refer [config]]
            [clojure.string :as cstr]
            [environ.core :refer [env]]))

(def base-urls
  {"opintopolku-host" (:opintopolku-host config)})

(def re-comment #"^\s*#.*")

(def re-line #"^.+=.+$")

(defn valid-line? [s]
  (and (re-matches re-line s)
       (nil? (re-matches re-comment s))))

(defn replace-vars [s vars]
  (reduce
    (fn [c [k v]]
      (cstr/replace c (format "${%s}" k) v))
    s
    vars))

(defn parse-line [s]
  (cstr/split (cstr/trim s) #"="))

(defn parse-urls [lines base]
  (reduce
    (fn [c n]
      (if (valid-line? n)
        (let [[k v] (parse-line n)]
          (assoc c k (replace-vars v c)))
        c))
    base
    lines))

(defn read-lines [file]
  (with-open [r (io/reader file)]
    (doall (line-seq r))))

(defn load-urls [file]
  (-> file
      read-lines
      (parse-urls base-urls)))

(defn get-file []
  (or (io/file (or (System/getenv "SERVICES_FILE")
                   (System/getProperty "services_file")
                   (:services-file env)))
      (io/resource "services-oph.properties")))

(def oph-service-urls
  (when-not *compile-files*
    (load-urls (get-file))))

(defn replace-arg [url i v]
  (cstr/replace url (format "$%d" i) (str v)))

(defn replace-args [url args]
  (loop [u url a args]
    (if (empty? a)
      u
      (recur (replace-arg u (count a) (last a)) (drop-last a)))))

(defn get-url [k & args]
  (replace-args (get oph-service-urls k) args))
