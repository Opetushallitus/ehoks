(ns oph.ehoks.utils
  (:require [medley.core :refer [dissoc-in map-keys]]))

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
