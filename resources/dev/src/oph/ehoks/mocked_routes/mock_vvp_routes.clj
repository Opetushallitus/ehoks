(ns oph.ehoks.mocked-routes.mock-vvp-routes
  (:require [oph.ehoks.mocked-routes.mock-gen :as mock-gen]
            [ring.util.http-response :as response]
            [clojure.tools.logging :as log]
            [schema.core :as s]
            [compojure.core :refer [GET POST context defroutes routes]]))

(defroutes mock-routes
  (context "/lahetys" []
    (GET "/login/j_spring_cas_security_check" []
      (response/set-cookie
        (response/found "/") "JSESSIONID" "vvp-session" {:path "/lahetys"}))

    (POST "/v1/viestit" {:keys [cookies body]}
      (if (= (get-in cookies ["JSESSIONID" :value]) "vvp-session")
        (do (log/info "SENDING MESSAGE:" (slurp body))
            (mock-gen/json-response
              {:viestiTunniste "019cb395-5840-70fa-96c9-918eec8a6f42"
               :lahetysTunniste "019cb395-5840-70fa-96c9-918eec8a6f42"}))
        (response/unauthorized "")))

    (GET "/v1/lahetykset/:lahetys-id/vastaanottajat" request
      (if (= (get-in request [:cookies "JSESSIONID" :value]) "vvp-session")
        (mock-gen/json-response
          {:vastaanottajat
           [{:tunniste (get-in request [:path-params :lahetys-id])
             :sahkoposti "panu.kalliokoski@sange.fi"
             :viestiTunniste (get-in request [:path-params :lahetys-id])
             :tila "LAHETETTY"}]})
        (response/unauthorized "")))))
