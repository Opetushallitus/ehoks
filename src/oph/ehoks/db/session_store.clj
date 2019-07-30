(ns oph.ehoks.db.session-store
  (:require [ring.middleware.session.store :refer [SessionStore]]
            [oph.ehoks.db.postgresql :as db]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json])
  (:import java.util.UUID))

(defn- to-kw-set [v]
  (set (map keyword v)))

(defn- convert-privileges [c]
  (map
    (fn [p]
      (-> p
          (update :roles to-kw-set)
          (update :privileges to-kw-set)))
    c))

(defn- convert-virkailija-privileges [session]
  (if (get-in session [:virkailija-user :organisation-privileges])
    (update-in
      session
      [:virkailija-user :organisation-privileges]
      convert-privileges)
    session))

(deftype DBStore []
  SessionStore
  (read-session [_ session-key]
    (when session-key
      (when-let [s (db/select-sessions-by-session-key session-key)]
        (convert-virkailija-privileges
          (json/read-str (:data s) :key-fn keyword)))))
  (write-session [_ session-key data]
    (db/insert-or-update-session! session-key data))
  (delete-session [_ session-key]
    (db/delete-session! session-key)
    nil))

(defn db-store []
  (log/info "Database session store enabled")
  (DBStore.))
