(ns oph.ehoks.db
  (:require [hugsql.adapter :refer [result-many result-one]]
            [hugsql.core :as hugsql]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.db.db-operations.db-helpers :as db-helpers]))

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

(defn result-one-snake->kebab
  [this result options]
  (db-helpers/to-dash-keys (result-one this result options)))

(defn result-many-snake->kebab
  [this result options]
  (map db-helpers/to-dash-keys (result-many this result options)))

(defmethod hugsql/hugsql-result-fn :1
  [_] 'oph.ehoks.db/result-one-snake->kebab)

(defmethod hugsql/hugsql-result-fn :*
  [_] 'oph.ehoks.db/result-many-snake->kebab)
