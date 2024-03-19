(ns oph.ehoks.user
  (:require [clojure.string :as str]
            [oph.ehoks.external.organisaatio :as organisaatio]
            [oph.ehoks.oppijaindex :as oi]))

(defn resolve-privilege
  "Resolves OPH privilege to keyword sets"
  [privilege]
  (when (= (:palvelu privilege) "EHOKS")
    (case (:oikeus privilege)
      "CRUD" #{:read :write :delete :update}
      "OPHPAAKAYTTAJA" #{:read :write :delete :update}
      "READ" #{:read}
      "HOKS_DELETE" #{:hoks_delete}
      #{})))

(defn get-service-privileges
  "Resolves service OPH privileges"
  [service-privileges]
  (reduce
    (fn [c n]
      (into c (resolve-privilege n)))
    #{}
    service-privileges))

(defn resolve-role
  "Detects role (currently only OPHPAAKAYTTAJA) in ehoks service"
  [service-role]
  (when (= (:palvelu service-role) "EHOKS")
    (case (:oikeus service-role)
      "OPHPAAKAYTTAJA" :oph-super-user
      nil)))

(defn get-service-roles
  "Resolves user roles"
  [service-privileges]
  (reduce
    (fn [c n]
      (if-let [r (resolve-role n)]
        (conj c r)
        c))
    #{}
    service-privileges))

(defn resolve-privileges
  "Resolves organisation privileges"
  [organisation]
  {:oid                (:organisaatioOid organisation)
   :privileges         (get-service-privileges (:kayttooikeudet organisation))
   :roles              (get-service-roles (:kayttooikeudet organisation))
   :child-organisations (if (= (:organisaatioOid organisation)
                               "1.2.246.562.10.00000000001")
                          (oi/get-oppilaitos-oids-cached)
                          (oi/get-oppilaitos-oids-by-koulutustoimija-oid
                            (:organisaatioOid organisation)))})

(defn get-auth-info
  "Get ticket user authentication info"
  [ticket-user]
  (when (some? ticket-user)
    {:organisation-privileges
     (map resolve-privileges (:organisaatiot ticket-user))}))

(defn oph-super-user?
  "Is user OPH ehoks super user"
  [ticket-user]
  (some #(contains? (:roles %) :oph-super-user)
        (:organisation-privileges ticket-user)))

(defn organisation-privileges!
  "Returns `ticket-user` privileges in organisation with oid `organisation-oid`.
  If `user` has no privileges in the given organisation, returns `nil`."
  [ticket-user organisation-oid]
  (let [parent-organisation-oids (some-> organisation-oid
                                         organisaatio/get-organisation!
                                         :parentOidPath
                                         (str/split #"\|"))]
    (->> (:organisation-privileges ticket-user)
         (filter #(or (= (:oid %) organisation-oid)
                      (some #{(:oid %)} parent-organisation-oids)))
         (map :privileges) ; Returns sequence of *sets*
         (reduce into)
         not-empty)))

(defn has-privilege-to-hoks?
  "Returns `true` if user has a specified `privilege` to hoks."
  [hoks ticket-user privilege]
  (some-> (:opiskeluoikeus-oid hoks)
          oi/get-existing-opiskeluoikeus-by-oid! ; throws if not found
          :oppilaitos-oid
          (->> (organisation-privileges! ticket-user))
          (contains? privilege)))

(defn has-read-privileges-to-oppija?
  "Returns `true` if `ticket-user` has read privileges in ANY organisation
  specified in opiskeluoikeudet of oppija.

  NOTE: This function basically grants `ticket-user` READ access to ALL oppija
  information if used for checking user access! This also includes data in other
  oppilaitokset that `ticker-user` isn't member of!"
  [ticket-user oppija-oid]
  (->> (oi/get-oppija-opiskeluoikeudet oppija-oid)
       (map :oppilaitos-oid)
       (some #(contains? (organisation-privileges! ticket-user %) :read))))
