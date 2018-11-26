(ns oph.ehoks.external.http-client
  (:refer-clojure :exclude [get])
  (:require [clj-http.client :as client]
            [oph.ehoks.config :refer [config]]))

(def get client/get)

(defn set-get [f]
  (when (not (:allow-mock-http? config))
    (throw (Exception. "Mocking HTTP is not allowed")))
  (def get f))

(def post client/post)
