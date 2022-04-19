(ns oph.ehoks.user-settings
  (:require [oph.ehoks.db.db-operations.user-settings :as db]))

(defn save-settings!
  "Save user settings to database"
  [user-oid data]
  (db/insert-or-update-user-settings! user-oid data)
  data)

(defn get-settings
  "Get user settings from database"
  [user-oid]
  (:data (first (db/select-user-settings-by-user-oid user-oid))))

(defn delete-settings!
  "Delete settings from database for user"
  [user-oid]
  (db/delete-user-settings! user-oid))
