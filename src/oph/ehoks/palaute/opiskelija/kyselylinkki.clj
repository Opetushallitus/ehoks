(ns oph.ehoks.palaute.opiskelija.kyselylinkki
  "A namespace for handling opiskelijapalaute kyselylinkit"
  (:require [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]))

(defn insert!
  "Lisää yhden kyselylinkin tietokantatauluun."
  [m]
  (db-ops/insert-one! :kyselylinkit (db-ops/to-sql m)))

(defn update!
  "Päivittää yhden kyselylinkin tietokantarivin."
  [m]
  (db-ops/update! :kyselylinkit
                  (db-ops/to-sql m)
                  ["kyselylinkki = ?" (:kyselylinkki m)]))

(defn delete!
  "Poistaa kyselylinkin tietokannasta."
  [kyselylinkki]
  (db-ops/delete! :kyselylinkit ["kyselylinkki = ?" kyselylinkki]))

(defn get-by-oppija-oid!
  "Hakee kyselylinkkejä tietokannasta oppijan OID:n perusteella."
  [oid]
  (db-hoks/select-kyselylinkit-by-oppija-oid oid))
