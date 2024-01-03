(ns oph.ehoks.db.session-store
  (:require [ring.middleware.session.store :refer [SessionStore]]
            [clojure.tools.logging :as log]
            [oph.ehoks.db.db-operations.session :as db-session]))

(defn- to-kw-set
  "Convert collection to keyword set"
  [v]
  (set (map keyword v)))

(defn- convert-privileges
  "Convert user privileges (string list to keyword set)"
  [c]
  (map
    (fn [p]
      (-> p
          (update :roles to-kw-set)
          (update :privileges to-kw-set)))
    c))

(defn- convert-virkailija-privileges
  "Convert virkailija privileges from database"
  [session]
  (if (get-in session [:virkailija-user :organisation-privileges])
    (update-in
      session
      [:virkailija-user :organisation-privileges]
      convert-privileges)
    session))

(defn get-session
  "Get session info"
  [session-key]
  (when session-key
    (when-let [s (db-session/select-sessions-by-session-key session-key)]
      (convert-virkailija-privileges (:data s)))))

(defn save-session!
  "Save session info"
  [session-key data]
  (db-session/insert-or-update-session! session-key data))

(deftype DBStore []
  SessionStore
  (read-session [_ session-key]
    (get-session session-key))
  (write-session [_ session-key data]
    (save-session! session-key data))
  (delete-session [_ session-key]
    (db-session/delete-session! session-key)
    nil))

(defn ^DBStore db-store
  "Creates simple DB store for session"
  []
  (log/info "Database session store enabled")
  (DBStore.))
