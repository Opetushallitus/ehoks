(ns oph.ehoks.oppija.middleware
  (:require [oph.ehoks.db.db-operations.hoks :as h]
            [oph.ehoks.user :as user]
            [ring.util.http-response :as response]
            [clojure.tools.logging :as log]))

; TODO split and reuse hoks access
(defn wrap-hoks-access
  "Wrap with oppija hoks access"
  [handler]
  (fn
    ([request respond raise]
      (if (empty? (get-in request [:route-params :eid]))
        (respond (response/bad-request {:error "EID is missing"}))
        (let [hoks       (h/select-hoks-by-eid
                           (get-in request [:route-params :eid]))
              user-oid (:oid (user/get request ::user/oppija))]
          (if (= user-oid (:oppija-oid hoks))
            (handler (assoc request :hoks hoks) respond raise)
            (do
              (log/warnf
                (str "Oppija OID %s does not match one in requested HOKS"
                     "(id %d eid %s).")
                user-oid
                (:id hoks)
                (:eid hoks))
              (respond (response/forbidden)))))))
    ([request]
      (if (empty? (get-in request [:route-params :eid]))
        (response/bad-request {:error "EID is missing"})
        (let [hoks (h/select-hoks-by-eid
                     (get-in request [:route-params :eid]))
              user-oid (:oid (user/get request ::user/oppija))]
          (if (= user-oid (:oppija-oid hoks))
            (handler (assoc request :hoks hoks))
            (do
              (log/warnf
                (str "Oppija OID %s does not match one in requested HOKS"
                     "(id %d, eid %s, oppija-oid %s).")
                user-oid
                (:id hoks)
                (:eid hoks)
                (:oppija-oid hoks))
              (response/forbidden))))))))
