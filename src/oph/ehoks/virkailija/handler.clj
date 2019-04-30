(ns oph.ehoks.virkailija.handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [schema.core :as s]
            [ring.util.http-response :as response]
            [oph.ehoks.external.cache :as c]
            [oph.ehoks.virkailija.auth :as auth]
            [oph.ehoks.user :as user]
            [oph.ehoks.db.postgresql :as db]
            [oph.ehoks.hoks.hoks :as h]
            [oph.ehoks.restful :as restful]))

(defn- virkailija-authenticated? [request]
  (some? (get-in request [:session :virkailija-user])))

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

(def routes
  (c-api/context "/virkailija" []
    :tags ["virkailija"]
    auth/routes

    (route-middleware
      [wrap-virkailija-authorize wrap-oph-super-user]

      (c-api/context "/hoksit" []

        (c-api/GET "/" []
          :summary "Kaikki hoksit (perustiedot)"
          (restful/rest-ok (db/select-hoksit)))

        (c-api/GET "/:hoks-id" []
          :path-params [hoks-id :- s/Int]
          :summary "Hoksin tiedot"
          (restful/rest-ok (h/get-hoks-by-id hoks-id))))

      (c-api/DELETE "/cache" []
        :summary "Välimuistin tyhjennys"
        (c/clear-cache!)
        (response/ok)))))
