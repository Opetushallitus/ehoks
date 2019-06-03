(ns oph.ehoks.url-list-generator
  (:require [clojure.walk :as walk]))

(defn get-value [x]
  (if (and (delay? x) (realized? x))
    (deref x)
    x))

(defn leave-path [m]
  (let [x (get-value m)]
    (if (map? x)
      (select-keys x [:path :childs])
      x)))

(defn filter-paths [t]
  (walk/prewalk leave-path t))

(defn replace-values [s]
  (let [vc (re-seq #":(?>\w|\d|-)+" s)]
    (reduce
      (fn [c n]
        (.replace c n (str "$" (inc (.indexOf vc n)))))
      s
      vc)))

(defn construct-urls [m path]
  (let [current-path (str path (:path m))]
    (if (some? (:childs m))
      (reduce
        (fn [c n]
          (apply conj c (construct-urls n current-path)))
        #{}
        (:childs m))
      (set (list (replace-values current-path))))))

(defn generate-urls [t]
  (let [paths (filter-paths t)]
    (construct-urls paths "")))
