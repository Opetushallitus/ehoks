(ns oph.ehoks.db.postgresql
  (:require [clojure.java.jdbc :as jdbc]
            [oph.ehoks.config :refer [config]]))

(def pg-uri
  {:connection-uri (:database-url config)})
