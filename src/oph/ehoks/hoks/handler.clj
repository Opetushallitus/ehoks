(ns oph.ehoks.hoks.handler
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :refer [accepted]]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.restful :refer [response rest-ok]]))

(def routes
  (c-api/context "/hoks" []

    (c-api/GET "/:id" [:as id]
      :summary "Palauttaa HOKSin"
      :return (response hoks-schema/HOKS)
      (rest-ok {}))

    (c-api/POST "/" []
      :summary "Luo uuden HOKSin"
      :body [_ hoks-schema/HOKSArvot]
      :return (response schema/POSTResponse)
      (rest-ok {:uri ""}))

    (c-api/POST "/:id/osaamiset/" []
      :summary "Listaa HOKSiin liitetyt osaamiset"
      :body [_ hoks-schema/Osaaminen]
      :return (response schema/POSTResponse)
      (rest-ok {:uri ""}))

    (c-api/POST "/:id/koulutukset/" []
      :summary "Listaa HOKSiin liitetyt koulutukset"
      :body [_ hoks-schema/Koulutus]
      :return (response schema/POSTResponse)
      (rest-ok {:uri ""}))

    (c-api/POST "/:id/suunnitellut-osaamiset/" []
      :summary "Listaa HOKSiin suunnitellut osaamiset"
      :body [_ hoks-schema/SuunniteltuOsaaminen]
      :return (response schema/POSTResponse)
      (rest-ok {:uri ""}))))
