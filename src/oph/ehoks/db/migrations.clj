(ns oph.ehoks.db.migrations
  (:require [oph.ehoks.config :refer [config]])
  (:import org.flywaydb.core.Flyway))

(def flyway
  (when-not *compile-files*
    (-> (Flyway/configure)
        (.dataSource (:database-url config) nil nil)
        (.load))))

(defn migrate! []
  (.migrate flyway))

(defn clean! []
  (.clean flyway))
