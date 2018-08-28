(ns oph.ehoks.auth.opintopolku
  (:require [clojure.walk :refer [keywordize-keys]]
            [clojure.set :refer [rename-keys]])
  (:import [java.nio.charset StandardCharsets]))

(defn convert [value src dest]
  (-> value
      String.
      (.getBytes src)
      String.
      (.getBytes dest)
      String.))

(defn convert-to-utf-8 [value]
  (convert value StandardCharsets/ISO_8859_1 StandardCharsets/UTF_8))

(defn parse [headers]
  (-> headers
      (select-keys ["FirstName" "cn" "givenName" "hetu" "oid" "security" "sn"])
      keywordize-keys
      (rename-keys {:FirstName :first-name :givenName :given-name
                    :sn :surname :cn :common-name})
      (update :first-name convert-to-utf-8)
      (update :given-name convert-to-utf-8)
      (update :surname convert-to-utf-8)))
