(ns oph.ehoks.session-store
  (:require [ring.middleware.session.store :refer [SessionStore]])
  (:import java.util.UUID))

(defn create-session-key []
  (str (UUID/randomUUID)))

(deftype TestSessionStore [store-atom]
  SessionStore
  (read-session [_ session-key]
    (when session-key
      (get @store-atom session-key)))
  (write-session [_ session-key data]
    (let [session-key (or session-key (create-session-key))]
      (swap! store-atom assoc session-key data)
      session-key))
  (delete-session [_ session-key]
    (swap! store-atom dissoc session-key)
    nil))

(defn test-session-store [store-atom]
  (TestSessionStore. store-atom))
