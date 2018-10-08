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

    (c-api/POST "/:id/olemassa-olevat-osaamiset/" []
      :summary "Lisää HOKSiin olemassa oleva osaaminen"
      :body [_ hoks-schema/OlemassaOlevaOsaaminen]
      :return (response schema/POSTResponse)
      (rest-ok {:uri ""}))

    (c-api/POST "/:id/muut-todennetut-osaamiset/" []
      :summary "Lisää HOKSiin muu todennettu osaaminen"
      :body [_ hoks-schema/MuuTodennettuOsaaminen]
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
      (rest-ok {:uri ""}))

    (c-api/POST "/:id/tyopaikalla-tapahtuvat-osaamiset/" []
      :summary "Lisää HOKSiin työpaikalla tapahtuvan osaamisen tiedot"
      :body [_ hoks-schema/TyopaikallaTapahtuvaOsaaminen]
      :return (response schema/POSTResponse)
      (rest-ok {:uri ""}))

    (c-api/POST "/:id/osaamisen-osoittamiset/" []
      :summary "Lisää HOKSiin hankitun osaamisen osoittaminen/näyttö"
      :body [_ hoks-schema/HankitunOsaamisenNaytto]
      :return (response schema/POSTResponse)
      (rest-ok {:uri ""}))))
