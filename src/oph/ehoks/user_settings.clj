(ns oph.ehoks.user-settings
  (:require [oph.ehoks.db.db-operations.user-settings :as db]))

(defn save-settings! [user-oid data]
  (db/insert-or-update-user-settings! user-oid data)
  data)

(defn get-settings [user-oid]
  (:data (first (db/select-user-settings-by-user-oid user-oid))))

(defn delete-settings! [user-oid]
  (db/delete-user-settings! user-oid))