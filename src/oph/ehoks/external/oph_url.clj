(ns oph.ehoks.external.oph-url
  (:require [clojure.java.io :as io]
            [oph.ehoks.config :refer [config]]
            [clojure.string :as cstr]
            [environ.core :refer [env]]))

(def base-urls
  "Global base URLs object"
  {"opintopolku-host" (:opintopolku-host config)})

(def re-comment
  "Regular expression that matches comments"
  #"^\s*#.*")

(def re-line
  "Regular expression that matches configuration file lines"
  #"^.+=.+$")

(defn valid-line?
  "Is line valid OPH service url"
  [s]
  (and (re-matches re-line s)
       (nil? (re-matches re-comment s))))

(defn replace-vars
  "Replace vars in url"
  [s vars]
  (reduce
    (fn [c [k v]]
      (cstr/replace c (format "${%s}" k) v))
    s
    vars))

(defn parse-line
  "Parse OPH service url line"
  [s]
  (cstr/split (cstr/trim s) #"=" 2))

(defn parse-urls
  "Parse service urls"
  [lines base]
  (reduce
    (fn [c n]
      (if (valid-line? n)
        (let [[k v] (parse-line n)]
          (assoc c k (replace-vars v c)))
        c))
    base
    lines))

(defn read-lines
  "Read lines from file"
  [file]
  (with-open [r (io/reader file)]
    (doall (line-seq r))))

(defn load-urls
  "Load urls from file"
  [file]
  (-> file
      read-lines
      (parse-urls base-urls)))

(defn get-file
  "Get OPH service urls file"
  []
  (or (io/file (or (System/getenv "SERVICES_FILE")
                   (System/getProperty "services_file")
                   (:services-file env)))
      (io/resource "ehoks-oph.properties")))

(def oph-service-urls
  "Global object containing service URLs"
  (when-not *compile-files*
    (load-urls (get-file))))

(defn replace-arg
  "Replace argument"
  [url i v]
  (cstr/replace url (format "$%d" i) (str v)))

(defn replace-args
  "Replace predefined arguments with values."
  [url args]
  (loop [u url a args]
    (if (empty? a)
      u
      (recur (replace-arg u (count a) (last a)) (drop-last a)))))

(defn get-url
  "Get service url with populated path params"
  [k & args]
  (replace-args (get oph-service-urls k) args))
