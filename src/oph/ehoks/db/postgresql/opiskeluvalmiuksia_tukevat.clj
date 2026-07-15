(ns oph.ehoks.db.postgresql.opiskeluvalmiuksia-tukevat
  (:require [oph.ehoks.db.db-operations.hoks :as h]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(defn select-oppimisen-tuki-by-hoks-id
  "Hae oppimisen tuki"
  [hoks-id]
  (db-ops/query
    [queries/select-oppimisen-tuki-by-hoks-id hoks-id]
    {:row-fn h/remove-hoks-id}))

(defn select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id
  "Opiskeluvalmiuksia tukevat opinnot"
  [id]
  (db-ops/query
    [queries/select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id id]
    {:row-fn h/remove-hoks-id}))

(defn insert-oppimisen-tuki!
  "Vie tietokantaan oppimisen tuen rivit"
  [values conn]
  (db-ops/insert-multi!
    :oppimisen_tuki
    (mapv db-ops/to-sql values)
    conn))

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

(defn delete-oppimisen-tuki-by-hoks-id
  [hoks-id conn]
  (db-ops/soft-delete!
    :oppimisen_tuki
    ["hoks_id = ? AND deleted_at IS NULL" hoks-id] conn))

(defn delete-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id
  "Poista opiskeluvalmiuksia tukevat opinnot"
  [hoks-id db-conn]
  (db-ops/soft-delete!
    :opiskeluvalmiuksia_tukevat_opinnot
    ["hoks_id = ? AND deleted_at IS NULL" hoks-id] db-conn))
