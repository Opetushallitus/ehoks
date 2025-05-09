(ns oph.ehoks.utils
  (:require [medley.core :refer [dissoc-in map-keys]]
            [clojure.core.cache :as cache]
            [clojure.core.memoize :as memo]
            [clojure.tools.logging :as log]
            [clojure.string])
  (:import (java.util.concurrent Future TimeUnit TimeoutException)))

(defn apply-when
  "Apply function `f` to value `v` if predicate `(pred v)` returns `true`.
  Otherwise returns value `v` unchanged. Useful when used in a threading macro."
  [v pred f]
  (if (pred v) (f v) v))

(defn to-underscore-str
  [kw]
  (.replace (name kw) \- \_))

(defn to-underscore-keys
  "Convert dashes in keys to underscores."
  [m]
  (map-keys #(keyword (to-underscore-str %)) m))

(defn to-dash-keys
  "Convert underscores in keys to dashes."
  [m]
  (map-keys #(keyword (.replace (name %) \_ \-)) m))

(defn replace-in
  "Associate the value associated with `sks` with the new key or sequence
  keys `dks` in `m`, and then dissociate `sks`. Both `sks` and `dks` can be a
  single keyword or sequence of keywords."
  [m sks dks]
  (let [sks (if (coll? sks) sks [sks])
        dks (if (coll? dks) dks [dks])]
    (if-let [value (get-in m sks)]
      (dissoc-in (assoc-in m dks value) sks)
      m)))

;; adapted from https://stackoverflow.com/questions/
;; 6694530/executing-a-function-with-a-timeout
(defn with-timeout
  "Execute a function with a given timeout, in milliseconds."
  [timeout-millis f]
  (let [^Future worker (future (f))]
    (try
      (.get worker timeout-millis TimeUnit/MILLISECONDS)
      (catch TimeoutException toe
        (log/warn toe "with-timeout:" f "timed out after"
                  timeout-millis "milliseconds")
        (future-cancel worker)
        :timeout))))

(defn with-fifo-ttl-cache
  [f ttl-millis fifo-threshold seed]
  (let [cache (-> {}
                  (cache/fifo-cache-factory :threshold fifo-threshold)
                  (cache/ttl-cache-factory :ttl ttl-millis))]
    (memo/memoizer f cache seed)))

(defn remove-nils
  "Return same map, but without keys pointing to nil values"
  [m]
  (into {} (filter (fn [[k v]] (some? v)) m)))

(defn get-in-and-propagate-fields
  "Hakee tietorakenteesta tietyn (listamuotoisen) kentän ja lisää
  listan joka kohtaan tietyt kentät alkuperäisestä, eli propagoi
  tietyt kentät syvemmälle tietorakenteessa."
  [obj field-to-get fields-to-propagate]
  (map #(merge % (select-keys obj fields-to-propagate))
       (get-in obj field-to-get)))

(defn koodiuri->koodi [koodiuri]
  (some-> koodiuri (clojure.string/split #"_") (last)))
