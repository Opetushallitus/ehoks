(ns oph.ehoks.auth.opintopolku
  (:require [clojure.walk :refer [keywordize-keys]]
            [clojure.set :refer [rename-keys]]
            [clojure.string :refer [lower-case]])
  (:import [java.nio.charset StandardCharsets]))

(def valid-headers
  ["firstname" "cn" "givenname" "hetu" "sn"])

(defn keys-to-lower [m]
  (reduce
    (fn [p [k v]]
      (assoc p (lower-case k) v))
    {}
    m))

(defn validate [headers]
  (let [header-values (select-keys (keys-to-lower headers) valid-headers)]
    (loop [valid-keys valid-headers]
      (when-let [k (peek valid-keys)]
        (if (nil? (get header-values k))
          (format "Header %s is missing" k)
          (recur (pop valid-keys)))))))

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
      keys-to-lower
      (select-keys valid-headers)
      keywordize-keys
      (rename-keys {:firstname :first-name :givenname :given-name
                    :sn :surname :cn :common-name})
      (update :first-name convert-to-utf-8)
      (update :given-name convert-to-utf-8)
      (update :surname convert-to-utf-8)))
