(ns oph.ehoks.handler
  (:require [compojure.api.sweet :refer [api context GET POST]]
            [compojure.route :as compojure-route]
            [ring.util.http-response :refer [ok not-found]]
            [oph.ehoks.restful :refer [response]]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.info :as info]))

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
        "/education" []
        (GET "/info/" []
          :return (response [common-schema/Information])
          :summary "System information for education provider"
          (ok (response [(info/get-ehoks-info :education)]))))

      (context
        "/work" []
        (GET "/info/" []
          :return (response [common-schema/Information])
          :summary "System information for workplace provider"
          (ok (response [(info/get-ehoks-info :work)]))))

      (context
        "/student" []
        (GET "/info/" []
          :return (response [common-schema/Information])
          :summary "System information for student"
          (ok (response [(info/get-ehoks-info :student)])))))

    (context
      "*" []
      (GET
        "*" []
        (not-found {:reason "Route not found"})))))
