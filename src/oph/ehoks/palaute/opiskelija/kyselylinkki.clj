(ns oph.ehoks.palaute.opiskelija.kyselylinkki
  "A namespace for handling opiskelijapalaute kyselylinkit"
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]
            [oph.ehoks.external.arvo :as arvo]
            [oph.ehoks.utils.date :as date])
  (:import [java.time LocalDate]))

(defn insert!
  "Lisää yhden kyselylinkin tietokantatauluun."
  [kyselylinkki]
  (db-ops/insert-one! :kyselylinkit (db-ops/to-sql kyselylinkki)))

(defn update!
  "Päivittää yhden kyselylinkin tietokantarivin."
  [kyselylinkki]
  (db-ops/update! :kyselylinkit
                  (db-ops/to-sql kyselylinkki)
                  ["kyselylinkki = ?" (:kyselylinkki kyselylinkki)]))

(defn active?
  "Checks if the kysely (corresponding `kyselylinkki`) is still active, i.e.,
  there is still time to answer and kysely hasn't been answered yet."
  [kyselylinkki]
  (not (or (:vastattu kyselylinkki)
           (some->> (:voimassa-loppupvm kyselylinkki)
                    (date/is-after (date/now))))))

(defn update-status!
  "Fetch the latest status (mainly, `:vastattu` and `voimassa-loppupvm`) of
  `kyselylinkki` from Arvo and update it accordingly."
  [kyselylinkki]
  (let [linkki (:kyselylinkki kyselylinkki)]
    (if-let [status (arvo/get-kyselylinkki-status! linkki)]
      (let [updates {:vastattu          (:vastattu status)
                     :voimassa-loppupvm
                     (LocalDate/parse
                       (first (str/split (:voimassa-loppupvm status) #"T")))}]
        (update! (assoc updates :kyselylinkki linkki))
        (merge kyselylinkki updates))
      (do (log/error (str "Not updating anything, because kyselylinkki `" linkki
                          "` was not found from Arvo."))
          kyselylinkki))))

(defn get-by-oppija-oid!
  "Hakee kyselylinkkejä tietokannasta oppijan OID:n perusteella."
  [oid]
  (db-hoks/select-kyselylinkit-by-oppija-oid oid))
