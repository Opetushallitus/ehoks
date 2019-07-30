(ns oph.ehoks.db.session-store
  (:require [ring.middleware.session.store :refer [SessionStore]]
            [oph.ehoks.db.postgresql :as db]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json])
  (:import java.util.UUID))

(deftype DBStore []
  SessionStore
  (read-session [_ session-key]
    (when session-key
      (when-let [s (db/select-sessions-by-session-key session-key)]
        (json/read-str
          (:data s)
          :key-fn keyword))))
  (write-session [_ session-key data]
    (db/insert-or-update-session! session-key data))
  (delete-session [_ session-key]
    (db/delete-session! session-key)
    nil))

(defn db-store []
  (log/info "Database session store enabled")
  (DBStore.))
