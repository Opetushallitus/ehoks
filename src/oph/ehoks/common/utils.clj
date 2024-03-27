(ns oph.ehoks.common.utils)

(defn apply-when
  "Apply function `f` to value `v` if predicate `(pred v)` returns `true`.
  Otherwise returns value `v` unchanged. Useful when used in a threading macro."
  [v pred f]
  (if (pred v) (f v) v))
