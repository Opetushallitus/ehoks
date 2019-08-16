(ns oph.ehoks.hoks.middleware
  (:require [clojure.tools.logging :as log]
            [ring.util.http-response :as response]
            [oph.ehoks.db.postgresql :as pdb]
            [oph.ehoks.user :as user]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.db.db-operations.hoks :as db-hoks]))

(def method-privileges {:get :read
                        :post :write
                        :patch :update
                        :put :update
                        :delete :delete})

(defn authorized? [hoks ticket-user method]
  (let [oppilaitos-oid (:oppilaitos-oid (oppijaindex/get-opiskeluoikeus-by-oid
                                          (:opiskeluoikeus-oid hoks)))]
    (if oppilaitos-oid
      (some?
        (get
          (user/get-organisation-privileges ticket-user oppilaitos-oid)
          method))
      (response/bad-request!
        {:error "Opiskeluoikeus not found"}))))

(defn hoks-access? [hoks ticket-user method]
  (and
    (some? (:opiskeluoikeus-oid hoks))
    (authorized? hoks ticket-user method)))

(defn check-hoks-access! [hoks request]
  (if (nil? hoks)
    (response/not-found!)
    (let [ticket-user (:service-ticket-user request)]
      (when-not
       (hoks-access?
         hoks
         ticket-user
         (get method-privileges (:request-method request)))
        (log/warnf "User %s has no access to hoks %d with opiskeluoikeus %s"
                   (:username ticket-user)
                   (:id hoks)
                   (:opiskeluoikeus-oid hoks))
        (response/unauthorized!
          {:error (str "No access is allowed. Check Opintopolku privileges and "
                       "'opiskeluoikeus'")})))))

(defn user-has-access? [request hoks]
  (let [ticket-user (:service-ticket-user request)]
    (hoks-access?
      hoks
      ticket-user
      (get method-privileges (:request-method request)))))

(defn wrap-hoks-access [handler]
  (fn
    ([request respond raise]
      (let [hoks (:hoks request)]
        (if (nil? hoks)
          (respond (response/not-found {:error "HOKS not found"}))
          (if (user-has-access? request hoks)
            (handler request respond raise)
            (do
              (log/warnf
                "User %s has no access to hoks %d with opiskeluoikeus %s"
                (get-in request [:service-ticket-user :username])
                (:id hoks)
                (:opiskeluoikeus-oid hoks))
              (respond
                (response/unauthorized
                  {:error (str "No access is allowed. Check Opintopolku "
                               "privileges and 'opiskeluoikeus'")})))))))
    ([request]
      (let [hoks (:hoks request)]
        (if (nil? hoks)
          (response/not-found {:error "HOKS not found"})
          (if (user-has-access? request hoks)
            (handler request)
            (do
              (log/warnf
                "User %s has no access to hoks %d with opiskeluoikeus %s"
                (get-in request [:service-ticket-user :username])
                (:id hoks)
                (:opiskeluoikeus-oid hoks))
              (response/unauthorized
                {:error (str "No access is allowed. Check Opintopolku "
                             "privileges and 'opiskeluoikeus'")}))))))))

(defn wrap-require-service-user [handler]
  (fn
    ([request respond raise]
      (if (= (:kayttajaTyyppi (:service-ticket-user request)) "PALVELU")
        (handler request respond raise)
        (respond (response/forbidden
                   {:error "User type 'PALVELU' is required"}))))
    ([request]
      (if (= (:kayttajaTyyppi (:service-ticket-user request)) "PALVELU")
        (handler request)
        (response/forbidden
          {:error "User type 'PALVELU' is required"})))))

(defn add-hoks [request]
  (let [hoks-id (Integer/parseInt (get-in request [:route-params :hoks-id]))
        hoks (db-hoks/select-hoks-by-id hoks-id)]
    (assoc request :hoks hoks)))

(defn wrap-hoks [handler]
  (fn
    ([request respond raise]
      (handler (add-hoks request) respond raise))
    ([request]
      (handler (add-hoks request)))))
