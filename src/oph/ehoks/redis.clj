(ns oph.ehoks.redis
  (:require [ring.middleware.session.store :refer [SessionStore]]
            [taoensso.carmine :as car]
            [oph.ehoks.logging :as log])
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
  (log/info "Redis session store enabled")
  (RedisStore. conn-opts))
