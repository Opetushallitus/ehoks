(ns oph.ehoks.redis
  (:require [ring.middleware.session.store :refer [SessionStore]])
  (:require [taoensso.carmine :as car])
  (:import java.util.UUID))

(defn create-session-key []
  (str (UUID/randomUUID)))

(deftype RedisStore [conn-opts]
  SessionStore
  (read-session [_ session-key]
    (when session-key
      (car/wcar conn-opts (car/get session-key))))
  (write-session [_ session-key data]
    (let [session-key (or session-key (create-session-key))]
      (car/wcar conn-opts (car/set session-key data))
      session-key))
  (delete-session [_ session-key]
    (car/wcar conn-opts (car/del session-key))
    nil))

(defn redis-store [conn-opts]
  (RedisStore. conn-opts))
