(ns oph.ehoks.virkailija.middleware
  (:require [ring.util.http-response :as response]
            [oph.ehoks.user :as user]
            [oph.ehoks.oppijaindex :as op]
            [clojure.tools.logging :as log]))

(defn- virkailija-authenticated? [request]
  (some? (get-in request [:session :virkailija-user])))

(defn wrap-require-virkailija-user [handler]
  (fn
    ([request respond raise]
      (if (= (get-in request [:session :virkailija-user :kayttajaTyyppi])
             "VIRKAILIJA")
        (handler request respond raise)
        (respond (response/forbidden
                   {:error "User type 'VIRKAILIJA' is required"}))))
    ([request]
      (if (= (get-in request [:session :virkailija-user :kayttajaTyyppi])
             "VIRKAILIJA")
        (handler request)
        (response/forbidden
          {:error "User type 'VIRKAILIJA' is required"})))))

(defn wrap-virkailija-authorize [handler]
  (fn
    ([request respond raise]
      (if (virkailija-authenticated? request)
        (handler request respond raise)
        (respond (response/unauthorized))))
    ([request]
      (if (virkailija-authenticated? request)
        (handler request)
        (response/unauthorized)))))

(defn wrap-oph-super-user [handler]
  (fn
    ([request respond raise]
      (if (user/oph-super-user? (get-in request [:session :virkailija-user]))
        (handler request respond raise)
        (respond (response/forbidden))))
    ([request]
      (if (user/oph-super-user? (get-in request [:session :virkailija-user]))
        (handler request)
        (response/forbidden)))))

(defn virkailija-has-privilege? [ticket-user oppija-oid privilege]
  (some?
    (some
      (fn [opiskeluoikeus]
        (when
         (contains?
           (user/get-organisation-privileges
             ticket-user (:oppilaitos-oid opiskeluoikeus))
           privilege)
          opiskeluoikeus))
      (op/get-oppija-opiskeluoikeudet oppija-oid))))

(defn virkailija-has-privilege-in-opiskeluoikeus?
  [ticket-user opiskeluoikeus-oid privilege]
  (let [opiskeluoikeus (op/get-opiskeluoikeus-by-oid opiskeluoikeus-oid)]
    (and (some? opiskeluoikeus)
         (contains?
           (user/get-organisation-privileges
             ticket-user (:oppilaitos-oid opiskeluoikeus))
           privilege))))

(defn virkailija-has-access? [virkailija-user oppija-oid]
  (virkailija-has-privilege? virkailija-user oppija-oid :read))

(defn wrap-virkailija-oppija-access [handler]
  (fn
    ([request respond raise]
      (if (virkailija-has-access?
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
      (if (virkailija-has-access?
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
