(ns oph.ehoks.tyopaikan-toimija.handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [ring.util.http-response :as response]
            [oph.ehoks.middleware :refer [wrap-authorize]]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.common.schema :as common-schema]))

(def routes
  (c-api/context "/tyopaikan-toimija" []
    :tags ["tyopaikan-toimija"]

    (c-api/GET "/auth" []
      :summary "Palauttaa uudelleenohjauksen työpaikan toimijan tunnistamiseen"
      (response/see-other
        (:opintopolku-tt-auth-url config)))

    (c-api/context "/:tt-eid" [tt-eid]
      (route-middleware
        [wrap-authorize]
        (c-api/GET "/oppijat" []
          :summary "Palauttaa työpaikalla olevat oppijat"
          :return (rest/response [common-schema/Oppija])
          (rest/rest-ok []))

        (c-api/GET "/oppijat/:oid" []
          :summary "Palauttaa työpaikalla olevan oppijan tiedot"
          :return (rest/response [common-schema/Oppija])
          (rest/rest-ok {}))

        (c-api/GET "/oppijat/:oid/tutkinto" []
          :summary "Palauttaa työpaikalla olevan oppijan tutkinnon perustiedot"
          :return (rest/response [common-schema/Tutkinto])
          (rest/rest-ok {}))))))
