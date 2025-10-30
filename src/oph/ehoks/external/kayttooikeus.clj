(ns oph.ehoks.external.kayttooikeus
  (:require [oph.ehoks.external.cas :as cas]
            [oph.ehoks.external.oph-url :as u]
            [oph.ehoks.oppijaindex :as oi]
            [clojure.tools.logging :as log]))

(defn- filter-non-ehoks-privileges
  "Filter out all non-EHOKS privileges"
  [org]
  (update org :kayttooikeudet
          (fn [privileges]
            (filter
              #(= (:palvelu %) "EHOKS")
              privileges))))

(defn- remove-orgs-without-privileges
  "Remove organisations whose list of privileges is empty"
  [user]
  (update user :organisaatiot
          (fn [orgs]
            (filter
              #(not-empty (:kayttooikeudet %))
              (map filter-non-ehoks-privileges orgs)))))

(defn username->user-details!
  "Get user details of given username"
  [^String username]
  (-> {:method :get
       :authenticate? true
       :service (u/get-url "kayttooikeus-service-url")
       :url (u/get-url "kayttooikeus-service.kayttaja")
       :options {:as :json, :query-params {"username" username}}}
      (cas/with-service-ticket)
      (get-in [:body 0])
      (remove-orgs-without-privileges)))

(def role-name->privileges
  "Resolves OPH role name to set of eHOKS privileges"
  {"CRUD"           #{:read :write :update :delete}
   "OPHPAAKAYTTAJA" #{:read :write :update :delete}
   "READ"           #{:read}
   "HOKS_DELETE"    #{:hoks_delete}})

(def ehoks-role-re #"ROLE_APP_EHOKS_(\w+)_(1\.2\.246\.562\.10\.\d+)")

(defn org->child-organisations
  "Fetch all child organisation of given organisation from oppijaindex"
  [org]
  (if (= org "1.2.246.562.10.00000000001")
    (oi/get-oppilaitos-oids-cached)
    (oi/get-oppilaitos-oids-by-koulutustoimija-oid org)))

(defn roles->org-privileges
  "Convert CAS roles to the format used by eHOKS"
  [roles]
  (keep (fn [role]
          (when-let [[match role-name org-oid] (re-matches ehoks-role-re role)]
            {:oid org-oid
             :privileges (or (role-name->privileges role-name) #{})
             :roles (if (= role-name "OPHPAAKAYTTAJA") #{:oph-super-user} #{})
             :child-organisations (org->child-organisations org-oid)}))
        roles))

(defn validation-data->user-details
  "Convert validate-ticket results to the format earlier returned by
  kayttooikeuspalvelu and get-auth-info"
  [validation-data]
  (assoc validation-data
         :organisation-privileges
         (roles->org-privileges (:roles validation-data))))

(defn service-ticket->user-details!
  "Get username of CAS ticket at given service"
  ([ticket] (service-ticket->user-details!
              (u/get-url "ehoks-virkailija-backend-url")
              ticket))
  ([service ticket]
    (let [validation-data (cas/validate-ticket service ticket)]
      (if (:success? validation-data)
        (validation-data->user-details validation-data)
        ;(let [user-details (username->user-details! (:user validation-data))]
        ;  (merge user-details (user/get-auth-info user-details)))
        (log/warnf "Service ticket validation failed: %s" validation-data)))))
