(ns oph.ehoks.db.user-settings
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [clojure.java.jdbc :as jdbc]
            [oph.ehoks.db.hoks :as h]))

(defn select-user-settings-by-user-oid [user-oid]
  (db-ops/query
    [queries/select-user-settings-by-user-oid user-oid]
    {:row-fn h/hoks-from-sql}))

(defn delete-user-settings! [user-oid]
  (db-ops/delete! :user_settings ["user_oid = ?" user-oid]))

(defn insert-or-update-user-settings! [user-oid data]
  (jdbc/with-db-transaction
    [conn (db-ops/get-db-connection)]
    (if (seq (jdbc/query
               conn
               [queries/select-user-settings-by-user-oid user-oid]))
      (jdbc/update!
        conn
        :user_settings
        {:data data :updated_at (java.util.Date.)}
        ["user_oid = ?" user-oid])
      (jdbc/insert! conn :user_settings {:user_oid user-oid :data data}))
    data))