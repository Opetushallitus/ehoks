(ns oph.ehoks.db
  (:require [hugsql.adapter :refer [result-many result-one]]
            [hugsql.core :as hugsql]
            [medley.core :refer [map-vals remove-vals]]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.utils :as utils])
  (:import (org.postgresql.jdbc PgArray)))

(def spec
  "Database specification"
  {:classname "org.postgresql.Driver"
   :subprotocol (:db-type config)
   :subname  (format "//%s:%d/%s"
                     (:db-server config)
                     (:db-port config)
                     (:db-name config))
   :user     (:db-username config)
   :password (:db-password config)
   :stringtype "unspecified"}) ; HACK to support enums

(defn pgarray->vec
  "If the argument is a PgArray, convert it to vector."
  [obj]
  (if (instance? PgArray obj) (vec (.getArray ^PgArray obj)) obj))

(def convert-result
  "Fix the results of hugsql to more clojuresque format."
  (comp #(dissoc % :created-at :updated-at :deleted-at)
        utils/to-dash-keys
        (partial map-vals pgarray->vec)
        (partial remove-vals nil?)))

(defn result-one-snake->kebab
  [this result options]
  (convert-result (result-one this result options)))

(defn result-many-snake->kebab
  [this result options]
  (map convert-result (result-many this result options)))

(defmethod hugsql/hugsql-result-fn :1
  [_] 'oph.ehoks.db/result-one-snake->kebab)

(defmethod hugsql/hugsql-result-fn :*
  [_] 'oph.ehoks.db/result-many-snake->kebab)
