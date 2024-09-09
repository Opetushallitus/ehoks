(ns oph.ehoks.common.utils
  (:require [medley.core :refer [map-keys]]))

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
  "Associate the value associated with sk with the new key or sequence of nested
  keys tks in h, and then dissociate sk."
  [h sk tks]
  (if (some? (get h sk))
    (dissoc (assoc-in h tks (get h sk)) sk)
    h))

(defn replace-from
  "Functions similarly to replace-in, but can accept a sequence of nested keys
  as the source and expects a keyword as the destination."
  [h sks tk]
  (cond
    (get-in h sks)
    (if (= (count (get-in h (drop-last sks))) 1)
      (apply
        dissoc
        (assoc h tk (get-in h sks))
        (drop-last sks))
      (update-in
        (assoc h tk (get-in h sks))
        (drop-last sks)
        dissoc
        (last sks)))
    (empty? (get-in h (drop-last sks)))
    (apply dissoc h (drop-last sks))
    :else h))

(defn replace-with-in
  "Handles replacing one (possibly nested) key with another in a map."
  [m kss kst]
  (if (coll? kss)
    (replace-from m kss kst)
    (replace-in m kss kst)))

(defn remove-nils
  "Return same map, but without keys pointing to nil values"
  [m]
  (into {} (filter (fn [[k v]] (some? v)) m)))
