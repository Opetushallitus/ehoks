(ns oph.ehoks.user
  (:require [clojure.string :as string]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.hoks :as hoks]
            [oph.ehoks.oppijaindex :as oi]
            [clojure.tools.logging :as log]))

(defn oph-super-user?
  "Is user OPH ehoks super user"
  [ticket-user]
  (some #(contains? (:roles %) :oph-super-user)
        (:organisation-privileges ticket-user)))

(defn organisation-privileges
  "Returns `ticket-user` privileges in `organisation`. If `user` has no
  privileges in the given `organisation`, returns `nil`."
  [organisation ticket-user]
  (let [parent-organisation-oids (some-> organisation
                                         :parentOidPath
                                         (string/split #"\|"))]
    (->> (:organisation-privileges ticket-user)
         (filter #(or (= (:oid %) (:oid organisation))
                      (some #{(:oid %)} parent-organisation-oids)))
         (map :privileges) ; Returns sequence of *sets*
         (reduce into)
         not-empty)))

(defn has-privilege-to-hoks?!
  "Returns `true` if user has a specified `privilege` to hoks."
  [hoks ticket-user privilege]
  (some-> (hoks/get-oppilaitos! hoks)
          (organisation-privileges ticket-user)
          (contains? privilege)))

(defn has-read-privileges-to-oppija?
  "Returns `true` if `ticket-user` has read privileges in ANY organisation
  specified in opiskeluoikeudet of oppija.

  NOTE: This function basically grants `ticket-user` READ access to ALL oppija
  information if used for checking user access! This also includes data in other
  oppilaitokset that `ticker-user` isn't member of!"
  [ticket-user oppija-oid]
  (->> (oi/get-oppija-opiskeluoikeudet oppija-oid)
       (map #(organisaatio/get-organisaatio! (:oppilaitos-oid %)))
       (some #(contains? (organisation-privileges % ticket-user) :read))))
