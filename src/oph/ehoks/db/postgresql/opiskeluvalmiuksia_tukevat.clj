(ns oph.ehoks.db.postgresql.opiskeluvalmiuksia-tukevat
  (:require [oph.ehoks.db.db-operations.hoks :as h]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(defn select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id
  "Opiskeluvalmiuksia tukevat opinnot"
  [id]
  (db-ops/query
    [queries/select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id id]
    {:row-fn h/opiskeluvalmiuksia-tukevat-opinnot-from-sql}))

(defn insert-opiskeluvalmiuksia-tukevat-opinnot!
  "Lisää opiskeluvalmiuksia tukevat opinnot"
  ([c]
    (db-ops/insert-multi!
      :opiskeluvalmiuksia_tukevat_opinnot
      (mapv db-ops/to-sql c)))
  ([c conn]
    (db-ops/insert-multi!
      :opiskeluvalmiuksia_tukevat_opinnot
      (mapv db-ops/to-sql c)
      conn)))

(defn delete-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id
  "Poista opiskeluvalmiuksia tukevat opinnot"
  [hoks-id db-conn]
  (db-ops/soft-delete!
    :opiskeluvalmiuksia_tukevat_opinnot
    ["hoks_id = ? AND deleted_at IS NULL" hoks-id] db-conn))
