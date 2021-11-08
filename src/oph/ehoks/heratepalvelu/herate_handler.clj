(ns oph.ehoks.heratepalvelu.herate-handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [oph.ehoks.hoks.middleware :as m]
            [oph.ehoks.middleware :refer [wrap-user-details]]
            [oph.ehoks.logging.audit :refer [wrap-audit-logger]]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.heratepalvelu.heratepalvelu :as hp]
            [schema.core :as s]
            [ring.util.http-response :as response])
  (:import (java.time LocalDate)))

(def routes
  (c-api/context "/heratepalvelu" []
    :tags ["heratepalvelu"]
    :header-params [ticket :- s/Str
                    caller-id :- s/Str]

    (route-middleware
      [wrap-user-details m/wrap-require-service-user
       wrap-audit-logger m/wrap-require-oph-privileges]

      (c-api/GET "/tyoelamajaksot" []
        :summary "Päättyneet työelämäjaksot"
        :query-params [start :- LocalDate
                       end :- LocalDate
                       limit :- (s/maybe s/Int)]
        (let [l (or limit 10)
              periods (hp/process-finished-workplace-periods start end limit)]
          (restful/rest-ok (count periods))))

      (c-api/GET "paivitetyt-tyoelamajaksot" []
        :summary (str "Päivitetyt työelämäjaksot opiskeluoikeuden ja"
                      "työpaikkatietojen perusteella")
        :query-params [opiskeluoikeus :- s/Str
                       ohjaajan-nimi :- s/Str
                       tyopaikan-nimi :- s/Str
                       tyopaikan-y-tunnus :- s/Str]
        (restful/rest-ok (hp/get-paivitetyt-tyoelamajaksot opiskeluoikeus
                                                           ohjaajan-nimi
                                                           tyopaikan-nimi
                                                           tyopaikan-y-tunnus)))

      (c-api/PATCH "/osaamisenhankkimistavat/:id/kasitelty" []
        :path-params [id :- s/Int]
        (hp/set-tep-kasitelty id true)
        (response/no-content)))))
