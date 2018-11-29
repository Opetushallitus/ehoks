(ns oph.ehoks.hoks.handler
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :as response]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.restful :as rest]))

(def routes
  (c-api/context "/hoks" []
    :tags ["hoks"]

    (c-api/GET "/:id" [:as id]
      :summary "Palauttaa HOKSin"
      :return (rest/response hoks-schema/HOKS)
      (rest/rest-ok {}))

    (c-api/POST "/" []
      :summary "Luo uuden HOKSin"
      :body [_ hoks-schema/HOKSArvot]
      :return (rest/response schema/POSTResponse)
      (rest/rest-ok {:uri ""}))

    (c-api/PUT "/:id" []
      :summary "Päivittää olemassa olevaa HOKSia"
      :body [_ hoks-schema/HOKSArvot]
      (response/no-content))
    ))
