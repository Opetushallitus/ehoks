(ns oph.ehoks.handler
  (:require [compojure.api.sweet :refer [api context GET]]
            [ring.util.http-response :refer [not-found]]
            [oph.ehoks.common.schema :as common-schema]
            [oph.ehoks.education.handler :as education-handler]
            [oph.ehoks.work.handler :as work-handler]
            [oph.ehoks.student.handler :as student-handler]))

(def app
  (api
    {:swagger
     {:ui "/doc/"
      :spec "/swagger.json"
      :data {:info {:title "eHOKS backend"
                    :description "Backend for eHOKS"}
             :tags [{:name "api", :description ""}]}}}

    (context "/api/v1" []
      :tags ["api v1"]

      education-handler/routes
      work-handler/routes
      student-handler/routes)

    (context "*" []
      (GET "*" []
        (not-found {:reason "Route not found"})))))
