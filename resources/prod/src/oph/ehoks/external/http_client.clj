(ns oph.ehoks.external.http-client
  (:refer-clojure :exclude [get])
  (:require [clj-http.client :as client]))

(def delete client/delete)

(def get client/get)

(def post client/post)
