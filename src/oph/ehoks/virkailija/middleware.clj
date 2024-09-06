(ns oph.ehoks.virkailija.middleware
  (:require [ring.util.http-response :as response]
            [oph.ehoks.user :as user]
            [clojure.tools.logging :as log]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]))

(defn- virkailija-authenticated?
  "Is virkailija authenticated"
  [request]
  (some? (get-in request [:session :virkailija-user])))

(defn wrap-require-virkailija-user
  "Require 'VIRKAILIJA' user type"
  [handler]
  (fn
    ([request respond raise]
      (if (not= (get-in request [:session :virkailija-user :kayttajaTyyppi])
                "PALVELU")
        (handler request respond raise)
        (respond (response/forbidden
                   {:error "User type 'PALVELU' is not allowed"}))))
    ([request]
      (if (not= (get-in request [:session :virkailija-user :kayttajaTyyppi])
                "PALVELU")
        (handler request)
        (response/forbidden
          {:error "User type 'PALVELU' is not allowed"})))))

(defn wrap-virkailija-authorize
  "Require virkailija to be authenticated"
  [handler]
  (fn
    ([request respond raise]
      (if (virkailija-authenticated? request)
        (handler request respond raise)
        (respond (response/unauthorized))))
    ([request]
      (if (virkailija-authenticated? request)
        (handler request)
        (response/unauthorized)))))

(defn wrap-oph-super-user
  "Require OPH PAAKAYTTAJA user"
  [handler]
  (fn
    ([request respond raise]
      (if (user/oph-super-user? (get-in request [:session :virkailija-user]))
        (handler request respond raise)
        (respond (response/forbidden))))
    ([request]
      (if (user/oph-super-user? (get-in request [:session :virkailija-user]))
        (handler request)
        (response/forbidden)))))

(defn- handle-virkailija-write-access [request]
  (let [hoks (db-hoks/select-hoks-by-id
               (Integer/parseInt (get-in request [:params :hoks-id])))
        user (get-in request [:session :virkailija-user])]
    (when-not (user/has-privilege-to-hoks?! hoks user :write)
      (log/warnf
        "User %s privileges do not match opiskeluoikeus
                                %s of oppija %s"
        (get-in request [:session
                         :virkailija-user
                         :oidHenkilo])
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
      (if (user/has-read-privileges-to-oppija?
            (get-in request [:session :virkailija-user])
            (get-in request [:params :oppija-oid]))
        (handler request respond raise)
        (do
          (log/warnf "User %s privileges don't match oppija %s"
                     (get-in request [:session :virkailija-user :oidHenkilo])
                     (get-in request [:params :oppija-oid]))
          (respond
            (response/forbidden
              {:error (str "User privileges does not match oppija "
                           "opiskeluoikeus organisation")})))))
    ([request]
      (if (user/has-read-privileges-to-oppija?
            (get-in request [:session :virkailija-user])
            (get-in request [:params :oppija-oid]))
        (handler request)
        (do
          (log/warnf "User %s privileges don't match oppija %s"
                     (get-in request [:session :virkailija-user :oidHenkilo])
                     (get-in request [:params :oppija-oid]))
          (response/forbidden
            {:error (str "User privileges does not match oppija opiskeluoikeus "
                         "organisation")}))))))
