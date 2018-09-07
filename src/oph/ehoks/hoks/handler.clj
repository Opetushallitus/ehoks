(ns oph.ehoks.hoks.handler
  (:require [compojure.api.sweet :refer [context POST DELETE]]
            [ring.util.http-response :refer [accepted]]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.restful :refer [response rest-ok]]))

(def routes
  (context "/hoks" []

    (POST "/" []
      :summary "Creates new HOKS document"
      :body [_ hoks-schema/DocumentValues]
      :return (response schema/POSTResponse)
      (rest-ok {:uri ""}))

    (DELETE "/:id" [:as id]
      :summary "Delete HOKS document"
      (accepted))))
