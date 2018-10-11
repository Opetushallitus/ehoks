(ns oph.ehoks.roles
  (:require [clojure.tools.logging :as log]
            [clojure.set :refer [intersection]]
            [ring.util.http-response :as response]
            [compojure.api.meta :as meta]))

(defn require-role! [required roles]
  (when-not (seq (intersection required roles))
    (log/warnf
      "User has insufficient privileges. Roles: %s Required: %s"
      roles required)
    (response/unauthorized!
      {:message "User has insufficient privileges"})))

(defmethod meta/restructure-param :roles [_ roles acc]
  (update-in
    acc [:lets]
    into ['_ `(require-role! ~roles (:roles ~'+compojure-api-request+))]))
