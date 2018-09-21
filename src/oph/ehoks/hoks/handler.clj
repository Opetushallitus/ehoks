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
      :summary "Lisää HOKSiin olemassa oleva osaaminen"
      :body [_ hoks-schema/Osaaminen]
      :return (response schema/POSTResponse)
      (rest-ok {:uri ""}))

    (c-api/POST "/:id/koulutukset/" []
      :body [_ hoks-schema/Koulutus]
      :summary "Lisää HOKSiin koulutus"
      :return (response schema/POSTResponse)
      (rest-ok {:uri ""}))

    (c-api/POST "/:id/suunnitellut-osaamiset/" []
      :summary "Lisää HOKSiin suunniteltu osaaminen"
      :body [_ hoks-schema/SuunniteltuOsaaminen]
      :return (response schema/POSTResponse)
      (rest-ok {:uri ""}))))
