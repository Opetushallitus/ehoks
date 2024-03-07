(ns oph.ehoks.virkailija.middleware
  (:require [ring.util.http-response :as response]
            [oph.ehoks.user :as user]
            [clojure.tools.logging :as log]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]))

(defn wrap-require-virkailija-user
  "Require 'VIRKAILIJA' user type"
  [handler]
  (fn
    ([request respond raise]
     (if (user/service? (user/get request ::user/virkailija))
       (respond (response/forbidden
                 {:error "User type 'PALVELU' is not allowed"}))
       (handler request respond raise)))
    ([request]
     (if (user/service? (user/get request ::user/virkailija))
       (response/forbidden {:error "User type 'PALVELU' is not allowed"})
       (handler request)))))

(defn wrap-oph-super-user
  "Require OPH PAAKAYTTAJA user"
  [handler]
  (fn
    ([request respond raise]
      (if (user/oph-super-user? (user/get request ::user/virkailija))
        (handler request respond raise)
        (respond (response/forbidden))))
    ([request]
      (if (user/oph-super-user? (user/get request ::user/virkailija))
        (handler request)
        (response/forbidden)))))

(defn- handle-virkailija-write-access [request]
  (let [hoks (db-hoks/select-hoks-by-id
               (Integer/parseInt (get-in request [:params :hoks-id])))
        virkailija-user (user/get request ::user/virkailija)]
    (when-not (user/has-privilege-to-hoks? virkailija-user :write hoks)
      (log/warnf
        "User %s privileges do not match opiskeluoikeus %s of oppija %s"
        (:oidHenkilo virkailija-user)
        (:opiskeluoikeus hoks)
        (get-in request [:params :oppija-oid]))
      {:error "User has insufficient privileges"})))

(defn wrap-virkailija-write-access
  "Require write access"
  [handler]
  (fn
    ([request respond raise]
      (if-let [result (handle-virkailija-write-access request)]
        (respond (response/forbidden result))
        (handler request respond raise)))
    ([request]
      (if-let [result (handle-virkailija-write-access request)]
        (response/forbidden result)
        (handler request)))))

(defn wrap-virkailija-oppija-access
  "Require access to oppija"
  [handler]
  (fn
    ([request respond raise]
     (let [virkailija-user (user/get request ::user/virkailija)
           oppija-oid      (get-in request [:params :oppija-oid])]
      (if (user/has-read-privileges-to-oppija?! virkailija-user oppija-oid)
        (handler request respond raise)
        (do
          (log/warnf "User %s privileges don't match oppija %s"
                     (:oidHenkilo virkailija-user) oppija-oid)
          (respond
            (response/forbidden
              {:error (str "User privileges does not match oppija "
                           "opiskeluoikeus organisation")}))))))
    ([request]
     (let [virkailija-user (user/get request ::user/virkailija)
           oppija-oid      (get-in request [:params :oppija-oid])]
      (if (user/has-read-privileges-to-oppija?! virkailija-user oppija-oid)
        (handler request)
        (do (log/warnf "User %s privileges don't match oppija %s"
                       (:oidHenkilo virkailija-user) oppija-oid)
            (response/forbidden
              {:error (str "User privileges does not match oppija "
                           "opiskeluoikeus organisation")})))))))
