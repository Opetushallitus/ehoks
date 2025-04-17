(ns oph.ehoks.db.db-operations.user-settings
  (:require [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]
            [clojure.java.jdbc :as jdbc]))

(defn select-user-settings-by-user-oid
  "Hakee käyttäjäasetukset käyttäjän OID:n perusteella."
  [user-oid]
  (db-ops/query
    [queries/select-user-settings-by-user-oid user-oid]
    {:row-fn db-ops/from-sql}))

(defn insert-or-update-user-settings!
  "Tallentaa tai päivittää käyttäjäasetukset tietokantaan."
  [user-oid data]
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
