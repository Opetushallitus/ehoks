(ns oph.ehoks.oppijaindex
  (:require [clojure.string :as cs]))

(defonce oppijat (atom []))

(defn- lower-values [m]
  (reduce
    (fn [c [k v]]
      (assoc c k (cs/lower-case v)))
    {}
    m))

(defn- matches-all? [o params]
  (nil?
    (some
      (fn [[k v]]
        (when-not (cs/includes? (cs/lower-case (get o k)) v) k))
      params)))

; TODO support for either includes or strict match

(defn search
  ([search-params sort-key-fn comp-fn]
    (let [lowered-params (lower-values search-params)]
      (sort-by
        sort-key-fn
        comp-fn
        (if (empty? search-params)
          @oppijat
          (filter
            (fn [o]
              (matches-all? o lowered-params))
            @oppijat)))))
  ([search-params sort-key-fn]
    (search search-params sort-key-fn compare))
  ([search-params]
    (search search-params :nimi compare))
  ([]
    @oppijat))