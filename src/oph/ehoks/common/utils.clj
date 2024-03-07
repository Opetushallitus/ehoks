(ns oph.ehoks.common.utils)

(defn apply-when
  "Useful when used in a threading macro."
  [v pred f]
  (if (pred v) (f v) v))
