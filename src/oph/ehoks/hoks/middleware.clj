(ns oph.ehoks.hoks.middleware
  (:require [clojure.tools.logging :as log]
            [medley.core :refer [find-first]]
            [oph.ehoks.oppijaindex :as oppijaindex]
            [oph.ehoks.user :as user]
            [ring.util.http-response :as response]))

(def method-privileges
  "Privileges afforded to each REST method"
  {:get    :read
   :post   :write
   :patch  :update
   :put    :update
   :delete :delete})

(defn authorized?
  "Is user authorized"
  [hoks ticket-user method]
  (let [oppilaitos-oid (:oppilaitos-oid (oppijaindex/get-opiskeluoikeus-by-oid!
                                          (:opiskeluoikeus-oid hoks)))]
    (if oppilaitos-oid
      (some?
        (get
          (user/organisation-privileges! ticket-user oppilaitos-oid)
          method))
      (response/bad-request!
        {:error "Opiskeluoikeus not found"}))))

(defn hoks-access?
  "Does user has access to hoks"
  [hoks ticket-user method]
  (and (some? (:opiskeluoikeus-oid hoks))
       (authorized? hoks ticket-user method)))

(defn check-hoks-access!
  "Check if ticket user has access privileges to hoks"
  [hoks request]
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

(defn user-has-access?
  "Check if user has access privileges to hoks"
  [request hoks]
    (hoks-access?
      hoks
      (:service-ticket-user request)
      (get method-privileges (:request-method request))))

(defn wrap-hoks-access
  "Wrap with hoks access"
  [handler]
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

(defn wrap-require-service-user
  "Require 'PALVELU' user"
  [handler]
  (fn
    ([request respond raise]
      (if (user/service? (:service-ticket-user request))
        (handler request respond raise)
        (respond (response/forbidden
                   {:error "User type 'PALVELU' is required"}))))
    ([request]
      (if (user/service? (:service-ticket-user request))
        (handler request)
        (response/forbidden
          {:error "User type 'PALVELU' is required"})))))

(defn oph-authorized?
  "Does user have OPH privileges?"
  [request]
  (let [method (method-privileges (:request-method request))]
    (->> (:service-ticket-user request)
         :organisation-privileges
         (find-first #(= (:oid %) "1.2.246.562.10.00000000001"))
         :privileges
         method ; keyword, e.g., :read
         some?)))

(defn wrap-require-oph-privileges
  "Require oph org"
  [handler]
  (fn
    ([request respond raise]
      (if (oph-authorized? request)
        (handler request respond raise)
        (response/unauthorized
          {:error (str "No access is allowed. Check Opintopolku "
                       "privileges and 'opiskeluoikeus'")})))
    ([request]
      (if (oph-authorized? request)
        (handler request)
        (response/unauthorized
          {:error (str "No access is allowed. Check Opintopolku "
                       "privileges and 'opiskeluoikeus'")})))))
