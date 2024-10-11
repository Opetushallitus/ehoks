(ns oph.ehoks.mocked-routes.mock-arvo-routes
  (:require [compojure.core :refer [GET POST defroutes]]
            [oph.ehoks.config :refer [config]]
            [clj-time.core :as time]
            [oph.ehoks.mocked-routes.mock-gen :as mock-gen]))

(defroutes mock-routes
  (GET "/api/vastauslinkki/v1/status/:linkId" []
    (mock-gen/json-response
      {:vastattu false
       :voimassa_loppupvm (time/plus (time/now) (time/hours 2))}))

  (POST "/api/vastauslinkki/v1" request
    (let [tunnus (subs (str (java.util.UUID/randomUUID)) 30)]
      (mock-gen/json-response
        {:tunnus tunnus
         :kysely_linkki (str "https://arvovastaus-dev.csc.fi/v/" tunnus)
         :voimassa_loppupvm (str (time/now))}))))
