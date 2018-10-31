(ns oph.ehoks.hoks.handler
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :refer [no-content]]
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

    (c-api/PUT "/:id" []
      :summary "Päivittää olemassa olevaa HOKSia"
      :body [_ hoks-schema/HOKSArvot]
      (no-content))))
