(ns oph.ehoks.dev-tools
  (:require [oph.ehoks.db.session-store :as store]))

(defn get-current-session-key [request]
  (get-in request [:cookies "ring-session" :value]))

(defn set-session-privileges [session-key privileges]
  (let [session (store/get-session session-key)]
    (store/save-session!
      session-key
      (assoc-in session
                [:virkailija-user :organisation-privileges]
                privileges))))

(defn get-session-organisation-privileges [session-key]
  (get-in (store/get-session session-key)
          [:virkailija-user :organisation-privileges]))

(defn add-session-privilege [session-key organisation-oid privileges]
  (let [session (store/get-session session-key)]
    (store/save-session!
      session-key
      (update-in session
                 [:virkailija-user :organisation-privileges]
                 conj
                 {:child-organisations []
                  :oid organisation-oid
                  :privileges privileges}))))

(defn remove-session-organisation [session-key organisation-oid]
  (let [session (store/get-session session-key)]
    (store/save-session!
      session-key
      (assoc-in
        session
        [:virkailija-user :organisation-privileges]
        (remove
          (fn [o]
            (= (:oid o) organisation-oid))
          (get-in session [:virkailija-user :organisation-privileges]))))))