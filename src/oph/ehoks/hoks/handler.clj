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

    (c-api/POST "/:id/todennetut-osaamiset/" []
      :summary "Lisää HOKSiin todennettu osaaminen"
      :body [_ hoks-schema/TodennettuOsaaminen]
      :return (response schema/POSTResponse)
      (rest-ok {:uri ""}))

    (c-api/POST "/:id/todentamattomat-osaamiset/" []
      :summary "Lisää HOKSiin todentamaton osaaminen"
      :body [_ hoks-schema/TodentamatonOsaaminen]
      :return (response schema/POSTResponse)
      (rest-ok {:uri ""}))

    (c-api/POST "/:id/tukevat-opinnot/" []
      :summary "Lisää HOKSiin opiskeluvalmiuksia tukeva opinto"
      :body [_ hoks-schema/TukevaOpinto]
      :return (response schema/POSTResponse)
      (rest-ok {:uri ""}))

    (c-api/POST "/:id/puuttuvat-osaamiset/" []
      :summary "Lisää HOKSiin puuttuvan osaamisen hankkiminen"
      :body [_ hoks-schema/PuuttuvaOsaaminen]
      :return (response schema/POSTResponse)
      (rest-ok {:uri ""}))))
