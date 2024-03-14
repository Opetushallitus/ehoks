(ns oph.ehoks.user
  (:require [clojure.string :as str]
            [oph.ehoks.external.organisaatio :as o]
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
  (some
    #(when (get (:roles %) :oph-super-user) true)
    (:organisation-privileges ticket-user)))

(defn check-parent-oids
  "Check whether user belongs to target organisation or its parent"
  [user-org target-org]
  (or (= user-org target-org)
      (some
        #(= user-org %)
        (str/split
          (:parentOidPath (o/get-existing-organisation! target-org) "")
          #"\|"))))

(defn get-organisation-privileges
  "Get ticket user privileges in organisation"
  [ticket-user organisation-oid]
  (let [privileges
        (reduce into
                (map :privileges
                     (filter
                       #(check-parent-oids (:oid %) organisation-oid)
                       (:organisation-privileges ticket-user))))]
    (when (seq privileges)
      privileges)))
