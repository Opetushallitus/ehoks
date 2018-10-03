(ns oph.ehoks.external.utils
  (:require [clojure.core.async :as a]))

(defmacro with-timeout
  "Simple macro for creating asyncronous timeout-safe function calls.
   `body` will be wrapped inside go block.
  Returns channel which returns either result of `body` or `timeout` depending
  on if given time exceeds."
  [time-ms body timeout]
  `(a/go
     (let [c# (a/go ~body)
           [v# p#] (a/alts! [c# (a/timeout ~time-ms)])]
       (if (and (not= p# c#) (nil? v#))
         ~timeout
         v#))))
