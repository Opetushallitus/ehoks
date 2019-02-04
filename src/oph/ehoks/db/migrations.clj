(ns oph.ehoks.db.migrations
  (:require [oph.ehoks.db.postgresql :as p])
  (:import org.flywaydb.core.Flyway))

(def flyway
  (-> (Flyway/configure)
      (.dataSource (:connection-uri p/pg-uri) nil nil)
      (.load)))

(defn migrate []
  (.migrate flyway))
