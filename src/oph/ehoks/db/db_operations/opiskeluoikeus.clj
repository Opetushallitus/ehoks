(ns oph.ehoks.db.db-operations.opiskeluoikeus
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(defn select-opiskeluoikeudet-without-tutkinto []
  (db-ops/query
    [queries/select-hoks-opiskeluoikeudet-without-tutkinto]))
