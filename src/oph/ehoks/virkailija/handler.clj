(ns oph.ehoks.virkailija.handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [ring.util.http-response :as response]
            [oph.ehoks.external.cache :as c]))

(defn- virkailija-authenticated? [request]
  (some? (get-in request [:session :virkailija-user]))
  true)

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
  (route-middleware
    [wrap-virkailija-authorize]
    (c-api/context "/virkailija" []
      (route-middleware
        [wrap-oph-super-user]
        (c-api/DELETE "/cache" []
          :summary "Välimuistin tyhjennys"
          (c/clear-cache!)
          (response/ok))))))
