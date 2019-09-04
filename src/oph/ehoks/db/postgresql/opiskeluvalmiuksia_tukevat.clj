(ns oph.ehoks.db.postgresql.opiskeluvalmiuksia-tukevat
  (:require [oph.ehoks.db.db-operations.hoks :as h]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(defn select-opiskeluvalmiuksia-tukevat-opinnot-by-id [oto-id]
  "Opiskeluvalmiuksia tukevat opinnot"
  (->
    (db-ops/query [queries/select-opiskeluvalmiuksia-tukevat-opinnot-by-id
                   oto-id])
    first
    h/opiskeluvalmiuksia-tukevat-opinnot-from-sql))

(defn select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id [id]
  "Opiskeluvalmiuksia tukevat opinnot"
  (db-ops/query
    [queries/select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id id]
    {:row-fn h/opiskeluvalmiuksia-tukevat-opinnot-from-sql}))

(defn insert-opiskeluvalmiuksia-tukeva-opinto! [new-value]
  "Lisää opiskeluvalmiuksia tukeva opinto"
  (db-ops/insert-one!
    :opiskeluvalmiuksia_tukevat_opinnot
    (db-ops/to-sql new-value)))

(defn insert-opiskeluvalmiuksia-tukevat-opinnot! [c]
  "Lisää opiskeluvalmiuksia tukevat opinnot"
  (db-ops/insert-multi!
    :opiskeluvalmiuksia_tukevat_opinnot
    (mapv db-ops/to-sql c)))

(defn update-opiskeluvalmiuksia-tukevat-opinnot-by-id! [oto-id new-values]
  "Päivitä opiskeluvalmiuksia tukevat opinnot"
  (db-ops/update!
    :opiskeluvalmiuksia_tukevat_opinnot
    (db-ops/to-sql new-values)
    ["id = ? AND deleted_at IS NULL" oto-id]))
