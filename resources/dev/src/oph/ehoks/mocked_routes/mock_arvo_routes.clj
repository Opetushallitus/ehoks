(ns oph.ehoks.mocked-routes.mock-arvo-routes
  (:require [compojure.core :refer [GET POST PATCH defroutes]]
            [ring.util.http-response :as response]
            [oph.ehoks.config :refer [config]]
            [oph.ehoks.mocked-routes.mock-gen :as mock-gen])
  (:import (java.time Instant)))

(defroutes mock-routes
  (GET "/api/vastauslinkki/v1/status/:linkId" []
    (mock-gen/json-response
      {:vastattu false
       :voimassa_loppupvm (.plusSeconds (Instant/now) 7200)}))

  (PATCH "/api/vastauslinkki/v1/:linkId" request
    (-> (response/ok (slurp (:body request)))
        (response/content-type "application/json")))

  (POST "/api/vastauslinkki/v1" request
    (let [tunnus (subs (str (java.util.UUID/randomUUID)) 30)]
      (mock-gen/json-response
        {:tunnus tunnus
         :kysely_linkki (str "https://arvovastaus-dev.csc.fi/v/" tunnus)
         :voimassa_loppupvm (Instant/now)}))))
