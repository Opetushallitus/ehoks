(ns oph.ehoks.handler
  (:require [compojure.api.sweet :refer [api context GET POST]]
            [ring.util.http-response :refer [ok]]
            [oph.ehoks.restful :refer [response]]
            [oph.ehoks.common.schema :as common-schema]))

(def app
  (api
    {:swagger
     {:ui "/doc/"
      :spec "/swagger.json"
      :data {:info {:title "eHOKS backend"
                    :description "Backend for eHOKS"}
             :tags [{:name "api", :description ""}]}}}

    (context
      "/api/v1" []
      :tags ["api v1"]

      (context
        "/student" []
        (GET "/info/" []
          :return (response [common-schema/Information])
          :summary "System information for student"
          (ok
            (response
              [{:basic-information
                {:fi "Perustietoa eHOKS-palvelusta"}
                :hoks-process
                {:fi "Perustietoa HOKS-prosessista"}}])))))))
