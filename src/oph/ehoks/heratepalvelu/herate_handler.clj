(ns oph.ehoks.heratepalvelu.herate-handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [oph.ehoks.virkailija.middleware :as m]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.heratepalvelu.heratepalvelu :as hp])
  (:import (java.time LocalDate)))

(def routes
  (route-middleware
    [m/wrap-oph-super-user]

    (c-api/GET "/heratepalvelu/tyoelamajaksot" []
      :summary "Päättyneet työelämäjaksot"
      :query-params [start :- LocalDate
                     end :- LocalDate]
      (let [periods (hp/process-finished-workplace-periods start end)]
        (restful/rest-ok (count periods))))))
