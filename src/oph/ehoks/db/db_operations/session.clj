(ns oph.ehoks.db.db-operations.session
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [clojure.java.jdbc :as jdbc]))

(defn select-sessions-by-session-key [session-key]
  (first (db-ops/query [queries/select-sessions-by-session-key session-key])))

(defn- generate-session-key [conn]
  (loop [session-key nil]
    (if (or (nil? session-key)
            (seq (jdbc/query
                   conn
                   [queries/select-sessions-by-session-key session-key])))
      (recur (str (java.util.UUID/randomUUID)))
      session-key)))

(defn insert-or-update-session! [session-key data]
  (jdbc/with-db-transaction
    [conn (db-ops/get-db-connection)]
    (let [k (or session-key (generate-session-key conn))
          db-sessions (jdbc/query
                        conn
                        [queries/select-sessions-by-session-key k])]
      (if (empty? db-sessions)
        (jdbc/insert!
          conn
          :sessions
          {:session_key k :data data})
        (jdbc/update!
          conn
          :sessions
          {:data data
           :updated_at (java.util.Date.)}
          ["session_key = ?" k]))
      k)))

(defn delete-session! [session-key]
  (db-ops/delete! :sessions ["session_key = ?" session-key]))

(defn delete-sessions-by-ticket! [ticket]
  (db-ops/delete! :sessions ["data->>'ticket' = ?" ticket]))
