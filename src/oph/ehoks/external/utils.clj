(ns oph.ehoks.external.utils
  (:import (java.util.concurrent ExecutionException)))

(defn call-with-timeout
  "Call a function with timeout, so that if that function doesn't return
  before time-ms (in milliseconds), return ::timeout instead."
  [time-ms callback]
  (try
    (let [fut (future (callback))
          res (deref fut time-ms ::timeout)]
      (when (= res ::timeout) (future-cancel fut))
      res)
    (catch ExecutionException e
      (throw (ex-cause e)))))

(defmacro with-timeout
  "Return the result of `body` if it returns something within `time-ms`
  milliseconds.  Otherwise return the result of `timeout-body`."
  [time-ms body timeout-body]
  `(let [res# (call-with-timeout ~time-ms (fn [] ~body))]
     (if (= res# ::timeout) ~timeout-body res#)))
