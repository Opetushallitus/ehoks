(ns oph.ehoks.db.migrations
  (:require [oph.ehoks.config :refer [config]])
  (:import org.flywaydb.core.Flyway))

(def ^Flyway flyway
  "Flyway instance"
  (when-not *compile-files*
    (-> (Flyway/configure)
        (.dataSource
          (format
            "jdbc:%s://%s:%d/%s?user=%s&password=%s"
            (:db-type config)
            (:db-server config)
            (:db-port config)
            (:db-name config)
            (:db-username config)
            (:db-password config))
          nil
          nil)
        (.load))))

(defn migrate!
  "Run migrations with flyway"
  []
  (.migrate ^Flyway flyway))

(defn clean!
  "Clean database"
  []
  (.clean ^Flyway flyway))
