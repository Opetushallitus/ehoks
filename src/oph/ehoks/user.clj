(ns oph.ehoks.user
  (:require [clojure.string :as str]
            [oph.ehoks.external.organisaatio :as o]))

(defn resolve-privilege [privilege]
  (when (= (:palvelu privilege) "EHOKS")
    (case (:oikeus privilege)
      "CRUD" #{:read :write :delete :update}
      "READ" #{:read}
      #{})))

(defn get-service-privileges [service-privileges]
  (reduce
    (fn [c n]
      (into c (resolve-privilege n)))
    #{}
    service-privileges))

(defn resolve-role [service-role]
  (when (= (:palvelu service-role) "EHOKS")
    (case (:oikeus service-role)
      "OPHPAAKAYTTAJA" :oph-super-user
      nil)))

(defn get-service-roles [service-privileges]
  (reduce
    (fn [c n]
      (if-let [r (resolve-role n)]
        (conj c r)
        c))
    #{}
    service-privileges))

(defn resolve-privileges [organisation]
  {:oid (:organisaatioOid organisation)
   :privileges (get-service-privileges (:kayttooikeudet organisation))
   :roles (get-service-roles (:kayttooikeudet organisation))})

(defn get-auth-info [ticket-user]
  (when (some? ticket-user)
    {:organisation-privileges
     (map resolve-privileges (:organisaatiot ticket-user))}))

(defn oph-super-user? [ticket-user]
  (some
    #(when (get (:roles %) :oph-super-user) true)
    (:organisation-privileges ticket-user)))

(defn check-parent-oids [user-org target-org]
  (or (= user-org target-org)
      (some
        #(= user-org %)
        (str/split
          (get (o/get-organisaatio target-org)
               :parentOidPath "")
          #"\|"))))

(defn get-organisation-privileges [ticket-user organisation-oid]
  (some
    #(when (check-parent-oids (:oid %) organisation-oid) (:privileges %))
    (:organisation-privileges ticket-user)))
