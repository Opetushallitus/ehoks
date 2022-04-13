(ns oph.ehoks.hoks.opiskeluvalmiuksia-tukevat
  (:require [oph.ehoks.db.postgresql.opiskeluvalmiuksia-tukevat :as db]))

(defn get-opiskeluvalmiuksia-tukeva-opinto
  "Hakee opiskeluvalmiuksia tukevat opinnot tietokannasta ID:n perusteella."
  [oto-id]
  (db/select-opiskeluvalmiuksia-tukevat-opinnot-by-id oto-id))

(defn get-opiskeluvalmiuksia-tukevat-opinnot
  "Hakee opiskeluvalmiuksia tukevat opinnot tietokannasta HOKSin ID:n
  perusteella."
  [hoks-id]
  (mapv
    #(dissoc % :id)
    (db/select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id hoks-id)))

(defn save-opiskeluvalmiuksia-tukeva-opinto!
  "Tallentaa opiskeluvalmiuksia tukevan opinnon tietokantaan."
  [hoks-id new-oto-values]
  (db/insert-opiskeluvalmiuksia-tukeva-opinto!
    (assoc new-oto-values :hoks-id hoks-id)))

(defn save-opiskeluvalmiuksia-tukevat-opinnot!
  "Tallentaa opiskeluvalmiuksia tukevat opinnot tietokantaan."
  ([hoks-id new-oto-values]
    (db/insert-opiskeluvalmiuksia-tukevat-opinnot!
      (mapv #(assoc % :hoks-id hoks-id) new-oto-values)))
  ([hoks-id new-oto-values conn]
    (db/insert-opiskeluvalmiuksia-tukevat-opinnot!
      (mapv #(assoc % :hoks-id hoks-id) new-oto-values)
      conn)))
